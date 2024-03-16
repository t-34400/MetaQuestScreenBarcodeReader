/*
 * This file is largely borrowed from [Genymobile/scrcpy](https://github.com/Genymobile/scrcpy), 
 * which includes code licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the Apache License, Version 2.0 at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 */
package com.genymobile.scrcpy;


import android.util.Size;

import java.util.Locale;

public final class DisplayInfo {
    // Added by @t-34400
    private static final String format = "Id=%d, Size=(%d, %d), Rotation=%d, LayerStack=%d, Flags=%d";

    private final int displayId;
    private final Size size;
    private final int rotation;
    private final int layerStack;
    private final int flags;

    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 0x00000001;

    public DisplayInfo(int displayId, Size size, int rotation, int layerStack, int flags) {
        this.displayId = displayId;
        this.size = size;
        this.rotation = rotation;
        this.layerStack = layerStack;
        this.flags = flags;
    }

    public int getDisplayId() {
        return displayId;
    }

    public Size getSize() {
        return size;
    }

    public int getRotation() {
        return rotation;
    }

    public int getLayerStack() {
        return layerStack;
    }

    public int getFlags() {
        return flags;
    }

    // Added by @t-34400
    @Override
    public String toString() {
        return String.format(Locale.US, format, displayId, size.getWidth(), size.getHeight(), rotation, layerStack, flags);
    }
}

