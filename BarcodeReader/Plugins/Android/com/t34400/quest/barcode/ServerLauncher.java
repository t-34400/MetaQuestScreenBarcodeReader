package com.t34400.quest.barcode;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.ImageReader;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.genymobile.scrcpy.DisplayInfo;
import com.genymobile.scrcpy.wrappers.DisplayManager;
import com.genymobile.scrcpy.wrappers.SurfaceControl;
import com.t34400.quest.barcode.connection.BarcodeReaderServer;

import java.lang.reflect.Method;

@SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
public class ServerLauncher {
    private static final String TAG = ServerLauncher.class.getSimpleName();
    private static final int INPUT_IMAGE_HEIGHT = 640;
    private static final float NORMALIZED_CROP_LEFT = 0.05f;
    private static final float NORMALIZED_CROP_TOP = 0.25f;
    private static final float NORMALIZED_CROP_WIDTH = 0.4f;
    private static final float NORMALIZED_CROP_HEIGHT = 0.5f;

    private static final int DEFAULT_PORT = 8250;

    public static void main(String[] args) {
        int pid = android.os.Process.myPid();
        Log.d(TAG, "Current Process ID: " + pid);

        int port = parsePortArg(args);

        int returnCode = 0;
        try {
            DisplayInfo displayInfo = getDisplayInfo();
            Log.d(TAG,"DisplayInfo: " + displayInfo);

            Looper.prepareMainLooper();

            Size size = displayInfo.getSize();
            float aspect = (float) size.getWidth() / size.getHeight();

            int inputImageWidth = (int) (aspect * INPUT_IMAGE_HEIGHT);
            int cropLeft = (int) (NORMALIZED_CROP_LEFT * inputImageWidth);
            int cropTop = (int) (NORMALIZED_CROP_TOP * INPUT_IMAGE_HEIGHT);
            int cropWidth = (int) (NORMALIZED_CROP_WIDTH * inputImageWidth);
            int cropHeight = (int) (NORMALIZED_CROP_HEIGHT * INPUT_IMAGE_HEIGHT);

            try (ImageReader imageReader = ImageReader.newInstance(inputImageWidth, INPUT_IMAGE_HEIGHT, PixelFormat.RGBA_8888, 2)) {
                IBinder displayToken = createDisplay();
                setDisplaySurface(displayToken, imageReader.getSurface(), inputImageWidth, INPUT_IMAGE_HEIGHT, displayInfo);

                BarcodeReaderServer server = new BarcodeReaderServer(port, imageReader, cropLeft, cropTop, cropWidth, cropHeight);

                Log.d(TAG,"Run server. port=" + port);
                server.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnCode = 1;
        }

        System.exit(returnCode);
    }

    private static int parsePortArg(String[] args) {
        int portIndex = 0;
        int port = DEFAULT_PORT;

        if (args.length > portIndex) {
            String portArg = args[portIndex];
            try {
                port = Integer.parseInt(portArg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return port;
    }

    private static DisplayInfo getDisplayInfo() throws Exception {
        Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
        Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
        Object dmg = getInstanceMethod.invoke(null);
        DisplayManager displayManager = new DisplayManager(dmg);

        return displayManager.getDisplayInfo(0);
    }

    private static IBinder createDisplay() {
        boolean secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R
                && !"S".equals(Build.VERSION.CODENAME));
        return SurfaceControl.createDisplay("ScreenCopy", secure);
    }

    private static void setDisplaySurface(IBinder display, Surface surface, int width, int height, DisplayInfo displayInfo) {
        Size displaySize = displayInfo.getSize();
        Rect displayRect = new Rect(0, 0, displaySize.getWidth(), displaySize.getHeight());
        Rect surfaceRect = new Rect(0, 0, width, height);

        SurfaceControl.openTransaction();
        try {
            SurfaceControl.setDisplaySurface(display, surface);
            SurfaceControl.setDisplayProjection(display, displayInfo.getRotation(), displayRect, surfaceRect);
            SurfaceControl.setDisplayLayerStack(display, displayInfo.getLayerStack());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            SurfaceControl.closeTransaction();
        }
    }
}