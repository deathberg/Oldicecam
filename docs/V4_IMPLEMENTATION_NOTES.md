# v4 implementation notes

This version replaces the previous placeholder UI with a practical control panel and diagnostics layer.

Important correction from recovered dex:

- TX11 signature is reconstructed as `writeString(path), writeInt(0), writeInt(loopFlag)`.
- TX14 signature is `writeInt(mode), writeString(value)`.
- TX24 signature is `writeInt(mode), writeFloat(x), writeFloat(y), writeFloat(scale), writeFloat(angle), writeInt(flags)`.

The app now logs every native/bootstrap/Binder step so failed paths can be diagnosed from a single exported log.
