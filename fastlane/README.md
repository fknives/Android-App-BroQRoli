fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android deployProdToPlayStore

```sh
[bundle exec] fastlane android deployProdToPlayStore
```

Submit a new Production Build to Play Store

By Default it sets the version_code to last from PlayStore + 1.

>Optionally version code increase can be skipped via:

```sh

[bundle exec] fastlane deployProdToPlayStore skip_build_number_increase:true

```

### android zipReleaseNativeSymbols

```sh
[bundle exec] fastlane android zipReleaseNativeSymbols
```

Zip the native_debug_symbols

By default zips the release version

>You may change the file name and build_type_and_flavour via:

```sh

[bundle exec] fastlane zipReleaseNativeSymbols output_path:./new.zip build_type_and_flavour:stagingRelease

```

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
