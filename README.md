# Meta Quest Screen Barcode Reader
An embedded plugin featuring a ADB shell script for setting up a server to read barcodes from the screen of Meta Quest.
This plugin allows for the scanning of barcodes from the screen pass-through.

## Notes
- This script requires enabling USB debugging in developer mode, as it is executed via ADB shell commands.
- Barrel distortion on the screen is not corrected.
- At the moment, only QR codes are detected.
- The [/BarcodeReader/Plugins/Android/com/genymobile](./BarcodeReader/Plugins/Android/com/genymobile/) directory contains third-party code distributed under the Apache License. For details, please refer to the [NOTICE](./BarcodeReader/Plugins/Android/com/genymobile/NOTICE) file in this directory.
- Barcode detection is performed using the [Zxing](https://github.com/zxing/zxing) library. Please ensure compliance with its license terms if you use it.

## Implementation in Unity projects
1. Copy the `BarcodeReader` directory into the `Assets` folder of your Unity project.
2. Enable `Custom Main Gradle Template`.
   1. Choose `Edit` > `Project Settings` from the menu bar.
   2. Select the `Player` tab and open `Android Player Settings`.
   3. Enable `Custom Main Gradle Template` from the `Publishing Settings` section.
3. Add the following dependencies to the `Plugins/Android/mainTemplate.gradle` file generated in step 2:
   ```gradle
   **EXTERNAL_SOURCES**
   repositories {
       mavenCentral()
   }
   dependencies {
       implementation "com.google.zxing:core:3.5.3"
   }
   ```

## Usage
1. Obtain the package name of your app.
   - In Unity, you can obtain it through the following steps:
     - By default, it is `com.<YourCompanyName>.<YourProductName>`.
       - The Company Name and Product Name can be found at the top of the Player tab, accessed by selecting `Edit` > `Project Settings` from the menu bar and opening the Player tab in the window that appears.
     - If the `Override Default Package Name` option is enabled under `Identification` > `Override Default Package Name` in the `Other Settings` section of the Player tab, the package name will be the one specified below it.
2. Enable USB debugging on your Meta Quest.
3. After installing ADB on your PC, connect your Meta Quest via wired or wireless connection and execute the following command (Windows).
   ```powershell
   $packagePath = adb shell pm path <YourAppPackageName> | ForEach-Object { $_ -replace "^package:" }
   adb shell CLASSPATH=$packagePath app_process /system/bin com.t34400.quest.barcode.ServerLauncher <ServerPort>
   ```
4. Access the local TCP server on the specified port from within your app. Write 0 as a 4-byte big-endian integer to start scanning for barcodes, and write 1 to stop scanning (ongoing scans cannot be stopped).
5. Upon completion of each scan, the server will send the following data:
    - If the scan is successful, the data will be sent in the following order:
        - Size of the barcode's Raw Value (4-byte big-endian integer)
        - Unix Time [ms] when the input image was acquired (8-byte big-endian integer)
        - Coordinates of the corners: x-coordinate, y-coordinate(4-byte big-endian floating-point numbers)
        - Raw Value (big-endian, UTF8)
    - If the scan fails, -1 will be sent as a 4-byte big-endian integer.

For sample receiving scripts, please refer to the [/BarcodeReader/Sample](./BarcodeReader/Sample/) directory.

## License
[MIT License](LICENSE)

Please refer to the [Notes](#notes) section for third-party licenses.

##  Acknowledgments
We would like to express our gratitude to the developers of [Genymobile/scrcpy](https://github.com/Genymobile/scrcpy) and [Zxing](https://github.com/zxing/zxing) for their contributions to this project.
