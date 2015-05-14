package com.kodak.utils.cmyk;

public class AdobeDCTSegment {

	public static final int Unknown = 0;
    public static final int YCC = 1;
    public static final int YCCK = 2;

    final int version;
    final int flags0;
    final int flags1;
    final int transform;

    public AdobeDCTSegment(int version, int flags0, int flags1, int transform) {
        this.version = version; // 100 or 101
        this.flags0 = flags0;
        this.flags1 = flags1;
        this.transform = transform;
    }

    public int getVersion() {
        return version;
    }

    public int getFlags0() {
        return flags0;
    }

    public int getFlags1() {
        return flags1;
    }

    public int getTransform() {
        return transform;
    }
    
    @Override
    public String toString() {
        return String.format(
                "AdobeDCT[ver: %d.%02d, flags: %s %s, transform: %d]",
                getVersion() / 100, getVersion() % 100, Integer.toBinaryString(getFlags0()), Integer.toBinaryString(getFlags1()), getTransform()
        );
    }
}
