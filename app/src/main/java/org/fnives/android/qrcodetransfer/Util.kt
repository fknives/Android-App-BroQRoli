package org.fnives.android.qrcodetransfer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.text.MessageFormat
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.util.Locale


fun BitMatrix.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (get(x, y)) Color.BLACK else Color.WHITE)
        }
    }

    return bitmap
}

fun Bitmap.toBinaryBitmap(): BinaryBitmap {
    val bitmap = if (this.config != Bitmap.Config.ARGB_8888) {
        this.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        this
    }
    //copy pixel data from the Bitmap into the 'intArray' array
    val intArray = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)

    return BinaryBitmap(HybridBinarizer(source))
}

fun Context.openAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", packageName, null)
    intent.addCategory(CATEGORY_DEFAULT)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
    intent.addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

    startActivity(intent)
}

fun Int.toOrdinal(): String {
    val formatter = MessageFormat("{0,ordinal}", Locale.US)
    return formatter.format(arrayOf(this))
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clipData)
}

fun String.isMaybeLink() = contains("://")

fun Context.openLink(link: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    } catch (ignored: Throwable) {

    }
}

private val Context.sharedDir: File
    get() {
        // must be the same as in filepaths.xml!
        val cachePath = File(cacheDir, "shared")
        cachePath.mkdirs()
        return cachePath
    }

private val Context.sharedFile: File
    get() {
        // must be the same as in filepaths.xml!
        return File(sharedDir, "qrcode.jpg")
    }

private fun Context.bitmapToSharedFile(bitmap: Bitmap): Boolean {
    try {
        sharedFile.createNewFile()
        sharedFile.outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return true
    } catch (e: Throwable) {
        e.printStackTrace()
        return false
    }
}

private fun Context.shareQRCodeImageFile() {
    // must be the same as in manifest.xml!
    val contentUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", sharedFile)
        ?: return

    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
    shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
    val chooserIntent = Intent.createChooser(shareIntent, ContextCompat.getString(this, R.string.share))
    ContextCompat.startActivity(this, chooserIntent, null)
}

fun Context.shareBitmap(bitmap: Bitmap) {
    bitmapToSharedFile(bitmap)
    shareQRCodeImageFile()
}