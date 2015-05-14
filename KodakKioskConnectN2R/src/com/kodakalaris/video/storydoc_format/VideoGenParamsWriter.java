package com.kodakalaris.video.storydoc_format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import com.AppContext;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.activities.AddTitleActivity;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;

public class VideoGenParamsWriter {

	private static final String TAG = VideoGenParamsWriter.class.getSimpleName();
	public void persist(VideoGenParams params) throws FileNotFoundException, IOException {
		ArrayList<Pair<String, String>> types = new ArrayList<Pair<String, String>>();
		types.add(new Pair<String, String>("image/jpeg", "jpg"));
		types.add(new Pair<String, String>("image/png", "png"));
		types.add(new Pair<String, String>("audio/wav", "wav"));
		types.add(new Pair<String, String>("audio/mp3", "mp3"));
		types.add(new Pair<String, String>("audio/acc", "acc"));
		types.add(new Pair<String, String>("audio/mp4", "mp4"));
		types.add(new Pair<String, String>("application/vnd.openxmlformats-package.relationships+xml", "rels"));
		types.add(new Pair<String, String>("application/xml", "xml"));
		types.add(new Pair<String, String>("audio/3gpp", "3gp"));
		doPersistStoryToFileSystem(params, types);
		doPersistCoreToFileSystem(params);
		doPersistContentTypesToFileSystem(params, types);
		doPersistRelsToFileSystem(params);
	}
	private void doPersistContentTypesToFileSystem(VideoGenParams params, ArrayList<Pair<String, String>> types) throws IOException, FileNotFoundException {
		File f = new File(params.getProjectPath() + "[Content_Types].xml");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		XmlSerializer xml = Xml.newSerializer();
		xml.setOutput(fos, Xml.Encoding.UTF_8.name());
		xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		xml.startDocument("UTF-8", null);
		xml.startTag(null, "Types");
		xml.attribute(null, "xmlns", "http://schemas.openxmlformats.org/package/2006/content-types");
		for (Pair<String, String> type : types) {
			xml.startTag(null, "Default");
			xml.attribute(null, "ContentType", type.first);
			xml.attribute(null, "Extension", type.second);
			xml.endTag(null, "Default");
		}
		xml.endTag(null, "Types");
		xml.endDocument();
	}
	private void doPersistRelsToFileSystem(VideoGenParams params) throws IOException, FileNotFoundException {
		File p = new File(params.getProjectPath() + "_rels/");
		p.mkdirs();
		File f = new File(params.getProjectPath() + "_rels/.rels");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		XmlSerializer xml = Xml.newSerializer();
		xml.setOutput(fos, Xml.Encoding.UTF_8.name());
		xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		xml.startDocument("UTF-8", true);
		xml.startTag(null, "Relationships");
		xml.attribute(null, "xmlns", "http://schemas.openxmlformats.org/package/2006/relationships");
		xml.startTag(null, "Relationship");
		xml.attribute(null, "Id", "rId1");
		xml.attribute(null, "Type", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties");
		xml.attribute(null, "Target", "/core.xml");
		xml.endTag(null, "Relationship");
		xml.startTag(null, "Relationship");
		xml.attribute(null, "Id", "rId2");
		xml.attribute(null, "Type", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail");
		xml.attribute(null, "Target", "/thumbnail.jpg");
		xml.endTag(null, "Relationship");
		xml.endTag(null, "Relationships");
		xml.endDocument();
	}
	private void doPersistCoreToFileSystem(VideoGenParams params) throws IOException, FileNotFoundException {
		File f = new File(params.getProjectPath() + "core.xml");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		XmlSerializer xml = Xml.newSerializer();
		xml.setOutput(fos, Xml.Encoding.UTF_8.name());
		xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		xml.startDocument("UTF-8", true);
		xml.startTag(null, "cp:coreProperties");
		xml.attribute(null, "xmlns:cp", "http://schemas.openxmlformats.org/package/2006/metadata/core-properties");
		xml.attribute(null, "xmlns:dc", "http://purl.org/dc/elements/1.1/");
		xml.attribute(null, "xmlns:dcterms", "http://purl.org/dc/terms/");
		xml.attribute(null, "xmlns:dcmitype", "http://purl.org/dc/dcmitype/");
		xml.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xml.attribute(null, "xsi:schemaLocation",
				"http://schemas.openxmlformats.org/package/2006/metadata/core-properties ../../OPC/ECMA-376,%20Third%20Edition,%20Part%202%20-%20Open%20Packaging%20Conventions/opc-coreProperties.xsd");
		if (params.mProjectTitle != null) {
			xml.startTag(null, "dc:title");
			xml.text(params.mProjectTitle);
			xml.endTag(null, "dc:title");
		}
		xml.startTag(null, "dc:subject");
		xml.endTag(null, "dc:subject");
		if (!(params.mCreatedByName.equals(""))) {
			xml.startTag(null, "dc:creator");
			xml.text(params.mCreatedByName);
			xml.endTag(null, "dc:creator");
		}
		xml.startTag(null, "dc:description");
		xml.endTag(null, "dc:description");
		if (!(params.mLastModifiedByName.equals(""))) {
			xml.startTag(null, "cp:lastModifiedBy");
			xml.text(params.mLastModifiedByName);
			xml.endTag(null, "cp:lastModifiedBy");
		}
		xml.startTag(null, "cp:revision");
		xml.text(params.mCoreRevision + "");
		xml.endTag(null, "cp:revision");
		xml.startTag(null, "dcterms:created");
		xml.attribute(null, "xsi:type", "dcterms:W3CDTF");
		xml.text("2014-06-04T21:29:00Z");
		xml.endTag(null, "dcterms:created");
		xml.startTag(null, "dcterms:modified");
		xml.attribute(null, "xsi:type", "dcterms:W3CDTF");
		xml.text("2014-06-04T21:29:00Z");
		xml.endTag(null, "dcterms:modified");
		xml.startTag(null, "cp:version");
		xml.text(params.mCoreVersion + "");
		xml.endTag(null, "cp:version");
		xml.endTag(null, "cp:coreProperties");
		xml.endDocument();
		fos.close();
	}
	private void doPersistStoryToFileSystem(VideoGenParams params, ArrayList<Pair<String, String>> types) throws IOException, FileNotFoundException {
		File f = new File(params.getProjectPath() + "story.xml");
		f.delete();
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		XmlSerializer xml = Xml.newSerializer();
		xml.setOutput(fos, Xml.Encoding.UTF_8.name());
		xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		xml.startDocument("UTF-8", null);
		xml.startTag(null, "StoryDoc");
		xml.attribute(null, "version", "1.0");
		xml.attribute(null, "xmlns", "http://www.kodak.com/schemas/2011/StoryDoc");
		xml.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xml.attribute(null, "xsi:schemaLocation", "http://www.kodak.com/schemas/2011/StoryDoc ../../../../../Assetselection/Specifications/StoryDoc/StoryDoc.xsd");
		xml.startTag(null, "Manifest");

		xml.attribute(null, "xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		xml.attribute(null, "xmlns:tiff", "http://ns.adobe.com/tiff/1.0/");
		xml.attribute(null, "xmlns:exif", "http://ns.adobe.com/exif/1.0/");
		xml.attribute(null, "xmlns:sem", "http://ns.kodak.com/sem/1.0/");
		xml.attribute(null, "xmlns:xmpDM", "http://ns.adobe.com/xmp/1.0/DynamicMedia/");
		for (Vignette v : params.mVignettes) {
			persistAudioAsset(params, xml, v, types);
		}
		for (Vignette v : params.mVignettes) {
			persistImageAsset(params, xml, v, types);
		}
		xml.endTag(null, "Manifest");
		xml.startTag(null, "Story");
		xml.startTag(null, "Metadata");
		xml.attribute(null, "xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		xml.attribute(null, "xmlns:dc", "http://purl.org/dc/elements/1.1/");
		xml.startTag(null, "rdf:RDF");
		xml.startTag(null, "rdf:Description");
		xml.attribute(null, "rdf:about", "urn:guid:" + params.mUUID.toString());
		xml.startTag(null, "dc:creator");
		xml.text("3 Pic Story Creator");
		xml.endTag(null, "dc:creator");
		xml.startTag(null, "dc:publisher");
		xml.text("Kodak Alaris Personalized Imaging");
		xml.endTag(null, "dc:publisher");
		xml.endTag(null, "rdf:Description");
		xml.endTag(null, "rdf:RDF");
		xml.endTag(null, "Metadata");
		xml.startTag(null, "StoryBoard");
		xml.startTag(null, "Segment");
		xml.attribute(null, "presentationType", "sequential");
		xml.attribute(null, "segmentType", "root");
		xml.startTag(null, "Title");
		if (params.mProjectTitle != null) {
			xml.startTag(null, "TextItem");
			xml.text(params.mProjectTitle);
			xml.endTag(null, "TextItem");
		}
		xml.startTag(null, "Segment");
		xml.startTag(null, "Title");
		//We must always write these together because their order matters
		//If we don't write the first one, than the second would become the first
		if (params.mProjectSubTitleTimeDate != null && params.mProjectSubTitleLocation != null) {
			xml.startTag(null, "TextItem");
			xml.text(params.mProjectSubTitleTimeDate);
			xml.endTag(null, "TextItem");
			xml.startTag(null, "TextItem");
			xml.text(params.mProjectSubTitleLocation);
			xml.endTag(null, "TextItem");
		}
		xml.endTag(null, "Title");
		xml.endTag(null, "Segment");
		xml.endTag(null, "Title");
		for (Vignette v : params.mVignettes) {
			persistSegment(xml, v);
		}
		xml.endTag(null, "Segment");
		xml.endTag(null, "StoryBoard");
		xml.endTag(null, "Story");
		xml.endTag(null, "StoryDoc");
		xml.endDocument();
		fos.close();

	}
	private void persistSegment(XmlSerializer xml, Vignette v) throws IllegalArgumentException, IllegalStateException, IOException {
		xml.startTag(null, "Segment");
		xml.attribute(null, "presentationType", "parallel");
		if (!(v.mAudioPath.equals(""))) {
			xml.startTag(null, "AssetRef");
			xml.attribute(null, "reference", "aud-" + v.mIndex);
			xml.endTag(null, "AssetRef");
		}
		if (!(v.mImagePath.equals(""))) {
			xml.startTag(null, "AssetRef");
			Log.e(TAG, "Writing AssetRef:" + v.mIndex);
			xml.attribute(null, "reference", "img-" + v.mIndex);
			xml.endTag(null, "AssetRef");
		}
		xml.endTag(null, "Segment");
		
	}
	private String getMimeTypeFromExtention(ArrayList<Pair<String, String>> types, String path) {
		String extention = BaseActivity.getFileExtension(path);
		for (Pair<String, String> type : types) {
			if (type.second.equals(extention)) {
				return type.first;
			}
		}
		Log.e(TAG, "Unknown mime type for:" + path);
		return "";// we'll probably crash...
	}
	private void persistAudioAsset(VideoGenParams params, XmlSerializer xml, Vignette v, ArrayList<Pair<String, String>> types) throws IllegalArgumentException, IllegalStateException, IOException {
		if (!v.mAudioPath.equals("")) {
			xml.startTag(null, "Asset");
			xml.attribute(null, "label", "aud-" + v.mIndex);
			String mimeType = getMimeTypeFromExtention(types, v.mAudioPath);
			Log.e(TAG, "Mime:" + mimeType);
			if (mimeType == null) {
				mimeType = "audio/acc";
			}
			if (mimeType.equals("video/3gpp")) {
				mimeType = "audio/3gpp";
				// Log.e(TAG, "MimeChanged:" + mimeType);
			}
			if (mimeType.equals("video/mp4")) {
				mimeType = "audio/mp4";
				// Log.e(TAG, "MimeChanged:" + mimeType);
			}
			xml.attribute(null, "MIMEType", mimeType == null ? "" : mimeType);
			xml.startTag(null, "OriginalURI");
			String relative = getRelativePath(params.getProjectPath(), v.mAudioPath);
			xml.text(relative);
			xml.endTag(null, "OriginalURI");
			xml.startTag(null, "Metadata");
			xml.startTag(null, "rdf:RDF");

			xml.startTag(null, "rdf:Description");
			xml.attribute(null, "rdf:about", "");
			xml.startTag(null, "xmpDM:duration");
			xml.text(AddTitleActivity.calculateLength(v.mAudioPath) / 1000.0f + "");

			xml.endTag(null, "xmpDM:duration");

			xml.endTag(null, "rdf:Description");
			xml.endTag(null, "rdf:RDF");
			xml.endTag(null, "Metadata");
			xml.endTag(null, "Asset");
		}

	}
	static String getRelativePath(String basePath, String leafPath) {
		return new File(basePath).toURI().relativize(new File(leafPath).toURI()).getPath();
	}
	private void persistImageAsset(VideoGenParams params, XmlSerializer xml, Vignette v, ArrayList<Pair<String, String>> types) throws IllegalArgumentException, IllegalStateException, IOException {
		if (!v.mImagePath.equals("")) {
			xml.startTag(null, "Asset");
			xml.attribute(null, "label", "img-" + v.mIndex);
			String mimeType = getMimeTypeFromExtention(types, v.mImagePath);
			// Log.e(TAG, "Mime:" + mimeType);
			xml.attribute(null, "MIMEType", mimeType == null ? "" : mimeType);
			xml.startTag(null, "OriginalURI");
			String relative = getRelativePath(params.getProjectPath(), v.mImagePath);
			xml.text(relative);
			xml.endTag(null, "OriginalURI");
			xml.startTag(null, "Metadata");
			xml.startTag(null, "rdf:RDF");

			xml.startTag(null, "rdf:Description");
			xml.attribute(null, "rdf:about", "");

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(v.mImagePath, o);
			xml.startTag(null, "tiff:ImageWidth");
			xml.text(o.outWidth + "");
			xml.endTag(null, "tiff:ImageWidth");

			xml.startTag(null, "tiff:ImageLength");
			xml.text(o.outHeight + "");
			xml.endTag(null, "tiff:ImageLength");

			xml.startTag(null, "tiff:Orientation");
			xml.text(MediaStoreUtils.getExifOrientation(v.mImagePath) + "");
			xml.endTag(null, "tiff:Orientation");

			xml.startTag(null, "exif:DateTimeOriginal");
			xml.text(MediaStoreUtils.getExifTimeDateStoryDocFormat(v.mImagePath));
			xml.endTag(null, "exif:DateTimeOriginal");

			xml.startTag(null, "sem:BaseROI");
			if (v.mStartBounds != null) {
				xml.text(v.mStartBounds.left + " " + v.mStartBounds.top + " " + v.mStartBounds.width() + " " + v.mStartBounds.height());
			} else {
				Log.w(TAG, "Start bounds null");
				xml.text("");
			}
			xml.endTag(null, "sem:BaseROI");

			xml.startTag(null, "sem:HighlightROI");
			if (v.mEndBounds != null) {
				xml.text(v.mEndBounds.left + " " + v.mEndBounds.top + " " + v.mEndBounds.width() + " " + v.mEndBounds.height());
			} else {
				xml.text("");
			}
			xml.endTag(null, "sem:HighlightROI");

			xml.endTag(null, "rdf:Description");
			xml.endTag(null, "rdf:RDF");
			xml.endTag(null, "Metadata");
			xml.endTag(null, "Asset");
			
			persistImageId(v.mImagePath, v.mImageId);
		}
	}
	
	public static void persistImageId(String imagePath, String imageId) {
		PreferenceManager.getDefaultSharedPreferences(AppContext.getApplication()).edit().putString(imagePath, imageId).commit();
	}
}
