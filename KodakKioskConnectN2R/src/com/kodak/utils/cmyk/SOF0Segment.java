package com.kodak.utils.cmyk;

import java.util.Arrays;

public class SOF0Segment {
	public int marker;
	public int samplePrecision;
	public int lines; // height
	public int samplesPerLine; // width
	public SOFComponent[] components;

	public SOF0Segment(int marker, int samplePrecision, int lines,int samplesPerLine, SOFComponent[] components) {
		this.marker = marker;
		this.samplePrecision = samplePrecision;
		this.lines = lines;
		this.samplesPerLine = samplesPerLine;
		this.components = components;
	}

	final int componentsInFrame() {
		return components.length;
	}

	@Override
	public String toString() {
		return String.format("SOF%d[%04x, precision: %d, lines: %d, samples/line: %d, components: %s]",
			marker & 0xff - 0xc0, marker, samplePrecision, lines,samplesPerLine, Arrays.toString(components));
	}
}