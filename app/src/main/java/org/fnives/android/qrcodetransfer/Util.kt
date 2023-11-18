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
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
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
    //copy pixel data from the Bitmap into the 'intArray' array
    val intArray = IntArray(width * height)
    getPixels(intArray, 0, width, 0, 0, width, height)

    val source: LuminanceSource = RGBLuminanceSource(width, height, intArray)

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
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager;
    val clipData = ClipData.newPlainText("label", text);
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