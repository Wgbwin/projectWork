package com.kodak.utils.cmyk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import com.kodak.utils.SortableHashMap;


public class JudgeCMYKUtil {

	public static InputStream data(byte[] data, int marker) {
		return data != null ? new ByteArrayInputStream(data, offset(data,marker), length(data, marker)) : null;
	}

	public static int length(byte[] data, int marker) {
		return data != null ? data.length - offset(data, marker) : 0;
	}

	public static String identifier(byte[] data, int marker) {
		String id = null;
		if (isAppSegmentMarker(marker)) {
			id = asNullTerminatedAsciiString(data, 0);
		}
		return id;
	}

	private static int offset(byte[] data, int marker) {
		String identifier = identifier(data, marker);
		return identifier == null ? 0 : identifier.length() + 1;
	}

	public static String asNullTerminatedAsciiString(byte[] data, int offset) {
		for (int i = 0; i < data.length - offset; i++) {
			if (data[offset + i] == 0 || i > 255) {
				return asAsciiString(data, offset, offset + i);
			}
		}
		return null;
	}

	public static String asAsciiString(byte[] data, int offset, final int length) {
		return new String(data, offset, length, Charset.forName("ascii"));
	}

	private static boolean isAppSegmentMarker(final int marker) {
		return marker >= 0xFFE0 && marker <= 0xFFEF;
	}

	private static int readData(RandomAccessFile stream) throws IOException {
		int marker = stream.read();
		while (marker != 0xff) {
			marker = stream.read();
		}
		marker = 0xff00 | stream.read();
		while (marker == 0xffff) {
			marker = 0xff00 | stream.read();
		}
		return marker;
	}

	private static void readApp14(int marker, int length, RandomAccessFile stream, SortableHashMap<Integer, byte[]> segments) throws IOException {
		int APP14 = 0xFFEE;
		if (marker == APP14) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(length - 2);
			int tag;
			int readNum = 0;
			while ((tag = stream.read()) > -1 && readNum < length - 2) {
				buffer.write(tag);
				++readNum;
			}
			byte[] data = buffer.toByteArray();
			segments.put(APP14, data);
		}
	}

	private static boolean readSOF0(int marker, int length, RandomAccessFile stream, SortableHashMap<Integer, byte[]> segments) throws IOException {
		int SOF0 = 0xFFC0;
		if (marker == SOF0) {						
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(length - 2);
			int tag;
			int readNum = 0;
			while ((tag = stream.read()) > -1 && readNum < length - 2) {
				buffer.write(tag);
				++readNum;
			}
			byte[] data = buffer.toByteArray();
			segments.put(SOF0, data);
			return true;
		}
		return false;
	}

	private static SortableHashMap<Integer, byte[]> readJPEGHeader(String filePath) {
		SortableHashMap<Integer, byte[]> segments = new SortableHashMap<Integer, byte[]>();
		RandomAccessFile stream = null;
		try {
			stream = new RandomAccessFile(filePath, "r");
			stream.seek(0);
			boolean isDone = false;
			int marker = readData(stream);
			int length = stream.readUnsignedShort();
			while (!isDone) {
				readApp14(marker, length, stream, segments);
				isDone = readSOF0(marker, length, stream, segments);
				marker = readData(stream);
				length = stream.readUnsignedShort();
			}
		} catch (IOException ioe) {
			return segments;
		} catch (Exception e) {
			return segments;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return segments;
	}

	private static boolean isCMYK(SortableHashMap<Integer, byte[]> segments) throws IOException {
		int APP14 = 0xFFEE;
		int SOF0 = 0xFFC0;
		if (segments == null) return false;
		if (segments.size() == 0) return false;
		byte[] appSeg = null;
		byte[] appSof = null;
		if (segments.containsKey(SOF0)) {
			appSof = segments.get(SOF0);
		}
		if (appSof == null) return false;
		DataInputStream dataStream = new DataInputStream(data(appSof, SOF0));
		SOF0Segment sof0Seg = null;
		try {
			int samplePrecision = dataStream.readUnsignedByte();
			int lines = dataStream.readUnsignedShort();
			int samplesPerLine = dataStream.readUnsignedShort();
			int componentsInFrame = dataStream.readUnsignedByte();
			if (componentsInFrame != 4) return false;

			SOFComponent[] components = new SOFComponent[componentsInFrame];
			for (int i = 0; i < componentsInFrame; i++) {
				int id = dataStream.readUnsignedByte();
				int sub = dataStream.readUnsignedByte();
				int qtSel = dataStream.readUnsignedByte();
				components[i] = new SOFComponent(id, ((sub & 0xF0) >> 4),(sub & 0xF), qtSel);
			}
			sof0Seg = new SOF0Segment(SOF0, samplePrecision, lines, samplesPerLine, components);
		} finally {
			dataStream.close();
		}		
		
		if (segments.containsKey(APP14)) {
			appSeg = segments.get(APP14);
		}
		AdobeDCTSegment adobeDCT = null;
		if (appSeg != null) {		
			DataInputStream stream = new DataInputStream(data(appSeg, APP14));
			try {
				adobeDCT = new AdobeDCTSegment(stream.readUnsignedByte(),
						stream.readUnsignedShort(), stream.readUnsignedShort(),
						stream.readUnsignedByte());
			} finally {
				stream.close();
			}
		}

		if (adobeDCT != null) {
			if (adobeDCT.getTransform() == AdobeDCTSegment.Unknown) {
				if (sof0Seg.components.length == 4) {
					return true;
				}
			}
		} else if (sof0Seg.components.length == 4) {
			if (sof0Seg.components[0].id == 'C' && sof0Seg.components[1].id == 'M' && sof0Seg.components[2].id == 'Y' && sof0Seg.components[3].id == 'K') {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isCMYK(String filePath){
		boolean isCMYK = false;
		try {
			isCMYK = isCMYK(readJPEGHeader(filePath));
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return isCMYK;
	}

}
