package com.kodakalaris.video.storydoc_format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.util.Log;

public class VideoGenParamsZipper {
	private static String TAG = VideoGenParamsZipper.class.getSimpleName();
	private static final int BUFFER = 2048;
	public void zip(String projectPath, String zipFile) throws FileNotFoundException, IOException {
		ArrayList<String> files = new ArrayList<String>();
		listAllFiles(projectPath, files);
		new File(zipFile).getParentFile().mkdirs();
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFile);

			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[BUFFER];

			for (int i = 0; i < files.size(); i++) {
				Log.v(TAG, "Adding: " + files.get(i));
				FileInputStream fi = new FileInputStream(files.get(i));
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(VideoGenParamsWriter.getRelativePath(projectPath, files.get(i)));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				out.closeEntry();
				origin.close();
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void listAllFiles(String directoryName, ArrayList<String> files) {
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				listAllFiles(file.getAbsolutePath(), files);
			}
		}
	}

}
