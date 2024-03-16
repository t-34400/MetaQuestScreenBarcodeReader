package com.t34400.quest.barcode.connection;

import android.graphics.PointF;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.t34400.quest.barcode.BarcodeScannerWrapper;
import com.t34400.quest.barcode.ImageUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class BarcodeReaderServer {
    private static final String TAG = BarcodeReaderServer.class.getSimpleName();

    private final int port;
    private final ImageReader imageReader;
    private final int cropLeft;
    private final int cropTop;
    private final int cropWidth;
    private final int cropHeight;

    private boolean isRunning = false;

    public BarcodeReaderServer(int port, ImageReader imageReader, int cropLeft, int cropTop, int cropWidth, int cropHeight) {
        this.port = port;
        this.imageReader = imageReader;
        this.cropLeft = cropLeft;
        this.cropTop = cropTop;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            isRunning = true;
            Log.d(TAG, "Run server. port=" + port);
            runServerLoop(serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Failed to start a server. port=" + port, e);
        }
    }

    private void runServerLoop(ServerSocket serverSocket) {
        while (isRunning) {
            try (Socket clientSocket = serverSocket.accept()) {
                Log.d(TAG, "Accept a client socket. remotePort=" + clientSocket.getPort());
                runScanLoop(clientSocket);
            } catch (IOException e) {
                Log.e(TAG, "Failed to accept a client socket.", e);
            }
        }
    }

    private void runScanLoop(Socket clientSocket) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
             OutputStream outputStream = clientSocket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)
        ) {
            BarcodeScannerWrapper scanner = new BarcodeScannerWrapper();
            boolean isScanning = false;

            while (true) {
                if (inputStreamReader.ready()) {
                    int c = inputStreamReader.read();
                    if (c == -1) {
                        break;
                    } else isScanning = c == 0;
                }
                if (!isScanning) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(50);
                        continue;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Waiting for start message from client interrupted.", e);
                        return;
                    }
                }

                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(50);
                        continue;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Retry acquiring latest image interrupted.", e);
                        return;
                    }
                }

                long unixTime = System.currentTimeMillis();
                int[] pixels = ImageUtil.convertRGBA8888toIntArray(image);
                int width = image.getWidth();
                int height = image.getHeight();
                image.close();

                LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                source = source.crop(cropLeft, cropTop, cropWidth, cropHeight);

                Result result = scanner.analyze(source);

                if (result == null) {
                    // Check if client's alive
                    dataOutputStream.writeInt(-1);
                    Log.v(TAG, "Failed to detect barcode.");
                } else {
                    PointF[] cornerPoints = convertResultPointsToPointFs(result.getResultPoints());
                    if (cornerPoints != null && cornerPoints.length >= 4) {
                        Log.v(TAG, String.format("Barcode detected. text=%s, corner0=%s, corner1=%s, corner2=%s, corner3=%s", result.getText(), cornerPoints[0], cornerPoints[1], cornerPoints[2], cornerPoints[3]));
                        BarcodeDataPacket packet = new BarcodeDataPacket(unixTime, cornerPoints, result.getText());
                        packet.writeToOutputStream(outputStream);
                    } else {
                        // Check if client's alive
                        dataOutputStream.writeInt(-1);
                        Log.v(TAG, "Failed to detect barcode corners.");
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Scan loop interrupted.", e);
        }
    }

    private static PointF[] convertResultPointsToPointFs(ResultPoint[] resultPoints) {
        return Arrays.stream(resultPoints)
                .map(resultPoint -> new PointF(resultPoint.getX(), resultPoint.getY()))
                .toArray(PointF[]::new);
    }
}