# Bluetooth Gamepad (Android source project)

An experimental Android Bluetooth HID gamepad app. It aims to make an Android phone appear to Windows as a standard Bluetooth gamepad, so no companion application is installed on the PC.

## Important compatibility note
Android exposes the Bluetooth HID Device profile from API 28, but some phone makers disable or restrict HID-device mode. This sample cannot guarantee compatibility with every phone or Windows PC. If the app reports that HID registration failed, the phone's Bluetooth stack likely does not support this role. It also needs Bluetooth permissions and Bluetooth enabled.

## Build
1. Install Android Studio on a development computer.
2. Open this folder as a project.
3. Let Android Studio sync Gradle.
4. Build > Build APK(s).
5. Install the APK on your Android phone.
6. Turn on Bluetooth and open the app.
7. On Windows, open Settings > Bluetooth & devices > Add device > Bluetooth and look for `Bluetooth Gamepad` (the phone must have successfully registered HID mode).
8. After pairing, test it with `joy.cpl` on Windows or a game that supports generic controllers.

## Controls
- Left stick: X/Y
- Right stick: Rx/Ry
- A/B/X/Y face buttons
- D-pad and shoulder-button placeholders

This is a starter project, not a signed prebuilt APK. It may need device-specific debugging. HID gamepad report mappings vary by game; some games may require remapping.


## Weird mode
The UI has been re-themed as VOIDPUPIL: acid-lime text, neon magenta/teal controls, occult-ish glyphs, and deliberately strange status messages. It is still a starter source project, not a prebuilt APK.
