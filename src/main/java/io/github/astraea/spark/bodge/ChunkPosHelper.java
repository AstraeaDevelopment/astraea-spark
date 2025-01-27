package io.github.astraea.spark.bodge;

public class ChunkPosHelper {
    private static final long MASK = (1L << 32) - 1;

    public static int unpackX(long packed) {
        return (int) (packed & MASK);
    }

    public static int unpackZ(long packed) {
        return (int) ((packed >> 32) & MASK);
    }
}
