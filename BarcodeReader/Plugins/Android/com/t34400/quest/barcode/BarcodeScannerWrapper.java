package com.t34400.quest.barcode;

import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarcodeScannerWrapper {
    private static final String TAG = BarcodeScannerWrapper.class.getSimpleName();

    private final MultiFormatReader reader;

    public BarcodeScannerWrapper() {
        reader = new MultiFormatReader();
        List<BarcodeFormat> possibleFormats = Collections.singletonList(BarcodeFormat.QR_CODE);
        Map<DecodeHintType, ?> hints = new HashMap<DecodeHintType, List<BarcodeFormat>> () {{
            put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);
        }};
        reader.setHints(hints);
    }

    public Result analyze(LuminanceSource source) {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result;
        try {
            result = reader.decodeWithState(binaryBitmap);
        } catch (NotFoundException e) {
            Log.v(TAG, "Barcode not found.", e);
            return null;
        }

        return result;
    }
}