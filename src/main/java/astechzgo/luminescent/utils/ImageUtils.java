package astechzgo.luminescent.utils;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class ImageUtils {
    private ImageUtils() {}

    public static void memCopy2d(ByteBuffer src, ByteBuffer dst, int width, int height, int start, int stride, int channels) {
        memCopy2d(src, dst, width * channels, height, start * channels, stride * channels);
    }

    public static void memCopy2d(ByteBuffer src, ByteBuffer dst, int rowBytes, int numRows, int start, int stride) {
        for (int row = 0; row < numRows; row++) {
            MemoryUtil.memCopy(MemoryUtil.memSlice(src, row * rowBytes, rowBytes), dst.position(start + row * stride));
        }
        dst.rewind();
    }
}
