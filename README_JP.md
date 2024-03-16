# Meta Quest Screen Barcode Reader
Meta Quest向けの，スクリーンからバーコードを読み取るサーバーを立ち上げるADBシェルスクリプトを組み込むプラグインです．
このプラグインでは，スクリーンパススルーからバーコードをスキャンすることも可能です．

## Note:
- このスクリプトはADBのShellコマンドから実行されるため，ユーザーは開発者モードのUSBデバッグを有効にする必要があります．
- スクリーンのBarrel Distortionは補正していません．
- 現時点では，QRコードのみを検出する仕様になっています．
- [/BarcodeReader/Plugins/Android/com/genymobile](./BarcodeReader/Plugins/Android/com/genymobile/) ディレクトリには、Apacheライセンスのもとで配布されているサードパーティのコードが含まれています．詳細については、このディレクトリ内の [NOTICE](./BarcodeReader/Plugins/Android/com/genymobile/NOTICE) ファイルを参照してください．
- バーコードの検出には，[Zxing](https://github.com/zxing/zxing)ライブラリを使用しています．使用する場合，こちらのライセンスについても確認してください．

## Implementation in Unity projects
1. `BarcodeReader`ディレクトリをUnityの`Assets`内にコピーする．
2. `Custom Main Gradle Template`を有効にする．
    1. メニューバーの`Edit` > `Project Settings`を選ぶ．
    2. `Player`タブを選び，`Android Player Settings`を開く．
    3. `Publishing Settings`セクションから，`Custom Main Gradle Template`を有効にする．
3. 2で生成された`Plugins/Android/mainTemplate.gradle`に以下の依存性を追記する．
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
1. アプリのパッケージ名を確認する．
2. Meta QuestのUSBデバッグを有効にする．
3. PCにADBをインストールした後に以下のコマンドを実行する（Windows）．

    ```powershell
    $packagePath = adb shell pm path <YourAppPackageName> | ForEach-Object { $_ -replace "^package:" }
    adb shell CLASSPATH=$packagePath app_process /system/bin com.t34400.quest.barcode.ServerLauncher <ServerPort>
    ```
4. アプリ内から指定したポートのローカルTCPサーバーにアクセスし，4バイトbig-endian integerで0を書き込むとスキャンが開始され，1を書き込むとスキャンが停止する（すでに行われているスキャンは停止できない）．
5. スキャンが完了するたびに，以下のデータがサーバーから送信される．
    - スキャンに成功した場合，以下の順番でデータが送信される．
        - バーコードのRaw Valueのサイズ（4バイトbig-endian integer）
        - 入力画像を取得したときのUnix Time[ms] (8バイトbig-endian integer)
        - コーナーのx座標, y座標 x 4 (4バイトbig-endian floating point number x 8)
        - Raw Value (big-endian, UTF8)
    - スキャンに失敗した場合，4バイトbig-endian integerで-1が送信される．

受信スクリプトのサンプルは，[/BarcodeReader/Sample](./BarcodeReader/Sample/)ディレクトリを参照してください．

## License
[MIT License](LICENSE)

サードパーティのライセンスについては，[Note](#note)を参照してください．

##  Acknowledgments
このプロジェクトで使用している，[Genymobile/scrcpy](https://github.com/Genymobile/scrcpy)，[Zxing](https://github.com/zxing/zxing)の開発者の皆様に感謝いたします．
