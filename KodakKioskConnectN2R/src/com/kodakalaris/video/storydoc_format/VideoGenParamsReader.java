package com.kodakalaris.video.storydoc_format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;

public class VideoGenParamsReader {

	private static final String TAG = VideoGenParamsReader.class.getSimpleName();

	public ArrayList<VideoGenParams> read(String projectBasePath, Context context) throws FileNotFoundException, IOException, XmlPullParserException {
		File f = new File(projectBasePath);
		f.mkdirs();
		File[] projects = f.listFiles();
		VideoGenParams.sortProjects(projects);
		ArrayList<VideoGenParams> result = new ArrayList<VideoGenParams>(projects.length);
		for (int i = 0; i < projects.length; i++) {
			try {
				result.add(i, parseProject(projects[i], context));
			} catch (Exception e) {
				e.printStackTrace();
				result.add(i, null);
			}

		}
		return result;
	}

	public VideoGenParams read(String projectBasePath, String guid, Context context) throws XmlPullParserException, IOException {
		File f = new File(projectBasePath);
		File[] projects = f.listFiles();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].getName().equals(guid)) {
				return parseProject(projects[i], context);
			}
		}
		return null;
	}

	private VideoGenParams parseProject(File file, Context context) throws FileNotFoundException, IOException, XmlPullParserException {
		ArrayList<Vignette> vigs = null;
		String projectTitle = null;
		UUID uuid = null;
		String createdBy = "";
		String lastModifiedBy = "";
		int coreRevision = 0;
		double coreVersion = 0;
		String createdOn = "";
		String modifiedOn = "";
		String subTitleTimeDate = null;
		String subTitleLocation = null;
		String text;
		File coreFile = new File(file.getAbsoluteFile() + "/core.xml");
		File storyFile = new File(file.getAbsoluteFile() + "/story.xml");
		FileInputStream fisCore = new FileInputStream(coreFile);
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(fisCore, null);
		// parser.nextTag();
		// parser.require(XmlPullParser.START_TAG, null, "cd:coreProperties");
		while (parser.next() != XmlPullParser.END_TAG) {
			// og.i(TAG, "While outer:" + parser.getEventType());
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			text = parser.getName();
			if (text.equals("cp:coreProperties")) {
				while (parser.next() != XmlPullParser.END_TAG) {
					//
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						//
						if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							break;
						}
						continue;
					}
					text = parser.getName();
					if (text.equals("dc:title")) {
						projectTitle = readText(parser);
					} else if (text.equals("dc:creator")) {
						createdBy = readText(parser);
					} else if (text.equals("cp:lastModifiedBy")) {
						lastModifiedBy = readText(parser);
					} else if (text.equals("dcterms:created")) {
						createdOn = readText(parser);
					} else if (text.equals("dcterms:modified")) {
						modifiedOn = readText(parser);
					} else if (text.equals("cp:revision")) {
						coreRevision = Integer.parseInt(readText(parser));
					} else if (text.equals("cp:version")) {
						coreVersion = Double.parseDouble(readText(parser));
					} else {
						skip(parser, text + "~InnerTop");
					}
				}
			} else {
				skip(parser, text + "~OuterTop");
			}
		}
		// .e(TAG, "projectTitle:" + projectTitle);
		// Log.e(TAG, "createdBy:" + createdBy);
		// Log.e(TAG, "lastModifiedBy:" + lastModifiedBy);
		// Log.e(TAG, "createdOn:" + createdOn);
		// Log.e(TAG, "coreRevision:" + coreRevision);
		// Log.e(TAG, "coreVersion:" + coreVersion);
		fisCore.close();

		FileInputStream fisStory = new FileInputStream(storyFile);
		parser.setInput(fisStory, null);
		HashMap<String, VignetteElement> vignetteElements = new HashMap<String, VideoGenParamsReader.VignetteElement>();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			text = parser.getName();
			if (text.equals("StoryDoc")) {
				while (parser.next() != XmlPullParser.END_TAG) {
					text = parser.getName();
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							break;
						}
						continue;
					}
					if (text.equals("Manifest")) {
						while (parser.next() != XmlPullParser.END_TAG) {
							text = parser.getName();
							if (parser.getEventType() != XmlPullParser.START_TAG) {
								if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
									break;
								}
								continue;
							}
							if (text.equals("Asset")) {
								Pair<String, VignetteElement> vignetInfo = parseAsset(parser);
								vignetteElements.put(vignetInfo.first, vignetInfo.second);
							} else {
								skip(parser, text + "~Inner");
							}
						}
					} else if (text.equals("Story")) {
						GenerateVignettsResult info = generateVignetts(parser, vignetteElements, context);
						vigs = info.mVigs;
						uuid = info.mUUID;
						subTitleTimeDate = info.mSubTitleTimeDate;
						subTitleLocation = info.mSubTitleLocation;
					} else {
						skip(parser, text + "~Center");
					}
				}
			} else {
				skip(parser, text + "~Outer");
			}
		}
		// Log.e(TAG, vignetteElements.toString());
		fisStory.close();

		return new VideoGenParams(file.getParent() + "/", projectTitle, vigs, uuid, createdBy, lastModifiedBy, coreRevision, coreVersion, createdOn, modifiedOn, subTitleTimeDate,
				subTitleLocation);
	}

	private GenerateVignettsResult generateVignetts(XmlPullParser parser, HashMap<String, VignetteElement> vignetteElements, Context context) throws XmlPullParserException, IOException {
		ArrayList<VideoGenParams.Vignette> result = new ArrayList<VideoGenParams.Vignette>();
		UUID uuid = UUID.randomUUID();
		String subTitleDateTime = null;
		String subTitleLocation = null;
		String text;
		int curSegmentIndex = 0;
		while (parser.next() != XmlPullParser.END_TAG) {
			text = parser.getName();
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			if (text.equals("Metadata")) {
				while (parser.next() != XmlPullParser.END_TAG) {
					text = parser.getName();
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							break;
						}
						continue;
					}
					if (text.equals("rdf:RDF")) {
						while (parser.next() != XmlPullParser.END_TAG) {
							text = parser.getName();
							if (parser.getEventType() != XmlPullParser.START_TAG) {

								if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
									break;
								}
								continue;
							}
							if (text.equals("rdf:Description")) {
								String uuidText = parser.getAttributeValue(null, "rdf:about");
								uuidText = uuidText.split(":")[2];
								uuid = UUID.fromString(uuidText);
								skip(parser, text + "~But wait its UUID");
							} else {
								skip(parser, text + "~story5");
							}
						}
					} else {
						skip(parser, text + "~story4");
					}
				}
			} else if (text.equals("StoryBoard")) {
				while (parser.next() != XmlPullParser.END_TAG) {
					text = parser.getName();
					if (parser.getEventType() != XmlPullParser.START_TAG) {

						if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							break;
						}
						continue;
					}
					if (text.equals("Segment")) {
						while (parser.next() != XmlPullParser.END_TAG) {
							text = parser.getName();
							if (parser.getEventType() != XmlPullParser.START_TAG) {

								if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
									break;
								}
								continue;
							}
							if (text.equals("Title")) {
								while (parser.next() != XmlPullParser.END_TAG) {
									text = parser.getName();
									if (parser.getEventType() != XmlPullParser.START_TAG) {

										if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
											break;
										}
										continue;
									}
									if (text.equals("TextItem")) {
										skip(parser, text + " but its duplicate information");
									} else if (text.equals("Segment")) {
										while (parser.next() != XmlPullParser.END_TAG) {
											text = parser.getName();
											if (parser.getEventType() != XmlPullParser.START_TAG) {

												if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
													break;
												}
												continue;
											}
											if (text.equals("Title")) {
												boolean hasReadFirstSubTitle = false;
												while (parser.next() != XmlPullParser.END_TAG) {
													text = parser.getName();
													if (parser.getEventType() != XmlPullParser.START_TAG) {

														if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
															break;
														}
														continue;
													}
													if (text.equals("TextItem")) {
														if (!hasReadFirstSubTitle) {
															subTitleDateTime = readText(parser);
															hasReadFirstSubTitle = true;
														} else {
															subTitleLocation = readText(parser);
														}
													} else {
														skip(parser, text + "~story6");
													}
												}
											} else {
												skip(parser, text + "~story5");
											}
										}
									} else {
										skip(parser, text + "~story4");
									}
								}
							} else if (text.equals("Segment")) {
								// Warning, This assumes uuid is set to the
								// correct value before getting here
								// This means the Metadata tag must come before
								// the StoryBoard tag
								Vignette v = parseVignette(curSegmentIndex, parser, vignetteElements, context, uuid.toString());
								// Log.e(TAG, "Adding Vignette to list:" +
								// curSegmentIndex);
								result.add(curSegmentIndex, v);
								curSegmentIndex++;
							} else {
								skip(parser, text + "~story3");
							}
						}
					} else {
						skip(parser, text + "~story2");
					}
				}
			} else {
				skip(parser, text + "~story");
			}
		}
		// Log.e(TAG, "Returining:" + result.size() + " vignettes");
		return new GenerateVignettsResult(result, uuid, subTitleDateTime, subTitleLocation);
	}

	private class GenerateVignettsResult {
		public ArrayList<Vignette> mVigs;
		public UUID mUUID;
		public String mSubTitleTimeDate;
		public String mSubTitleLocation;

		public GenerateVignettsResult(ArrayList<Vignette> vigs, UUID uuid, String subTitleTimeDate, String subTitleLocation) {
			super();
			this.mVigs = vigs;
			this.mUUID = uuid;
			this.mSubTitleTimeDate = subTitleTimeDate;
			this.mSubTitleLocation = subTitleLocation;
		}
	}

	private Vignette parseVignette(int index, XmlPullParser parser, HashMap<String, VignetteElement> vignetteElements, Context context, String uuid) throws XmlPullParserException, IOException {
		String text;
		ArrayList<String> keys = new ArrayList<String>();
		while (parser.next() != XmlPullParser.END_TAG) {
			text = parser.getName();
			if (parser.getEventType() != XmlPullParser.START_TAG) {

				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			if (text.equals("AssetRef")) {
				String key = parser.getAttributeValue(null, "reference");
				skip(parser, "~not really ref");
				keys.add(key);
			}
		}
		String imageId = "";
		String imagePath = "";
		String audioPath = "";
		int length = 0;
		RectF baseRectF = null;
		RectF highlightRectF = null;
		for (int i = 0; i < keys.size(); i++) {
			VignetteElement ele = vignetteElements.get(keys.get(i));
			if (ele instanceof VignetteAudioElement) {
				VignetteAudioElement audioElement = (VignetteAudioElement) ele;
				length = audioElement.mDuration;
				audioPath = VideoGenParams.getProjectBasePath(context) + uuid + "/" + audioElement.mRelativePath;
			} else if (ele instanceof VignetteImageElement) {
				VignetteImageElement imageElement = (VignetteImageElement) ele;
				baseRectF = imageElement.mBaseRIO;
				highlightRectF = imageElement.mHighLightROI;
				imagePath = VideoGenParams.getProjectBasePath(context) + uuid + "/" + imageElement.mRelativePath;
				imageId = getImageId(context, imagePath);
			}
		}

		return new Vignette(index, imagePath, imageId, audioPath, length, baseRectF, highlightRectF);
	}

	private Pair<String, VignetteElement> parseAsset(XmlPullParser parser) throws XmlPullParserException, IOException {
		VignetteElement result = null;
		String relativePath = "";
		String label = parser.getAttributeValue(null, "label");
		String mimeType = parser.getAttributeValue(null, "MIMEType");
		String text;
		while (parser.next() != XmlPullParser.END_TAG) {
			text = parser.getName();
			if (parser.getEventType() != XmlPullParser.START_TAG) {

				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			if (text.equals("OriginalURI")) {
				relativePath = readText(parser);
			} else if (text.equals("Metadata")) {
				while (parser.next() != XmlPullParser.END_TAG) {
					text = parser.getName();
					if (parser.getEventType() != XmlPullParser.START_TAG) {

						if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
							break;
						}
						continue;
					}
					if (text.equals("rdf:RDF")) {
						while (parser.next() != XmlPullParser.END_TAG) {

							text = parser.getName();
							if (parser.getEventType() != XmlPullParser.START_TAG) {

								if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
									break;
								}
								continue;
							}
							if (text.equals("rdf:Description")) {
								if (mimeType.startsWith("audio/")) {
									result = parseAudioAsset(parser);
								} else if (mimeType.startsWith("image/")) {
									result = parseImageAsset(parser);
								} else {
									Log.e(TAG, "Unexpected MimeType:" + relativePath + ":" + mimeType + ":" + text);
									skip(parser, text + "~Unexpected Mime Type");
								}
							} else {
								Log.e(TAG, "Unexpected MimeType:" + relativePath + " :" + text);
								skip(parser, text + "~error");
							}
						}
					} else {
						skip(parser, text + "~Center");
					}
				}
			} else {
				skip(parser, text + "~Outer");
			}
		}
		// Log.e(TAG, "label:" + label);
		// Log.e(TAG, "relativePath:" + relativePath);
		// Log.e(TAG, "mimeType:" + mimeType);
		result.mRelativePath = relativePath;
		return new Pair<String, VignetteElement>(label, result);

	}

	private VignetteImageElement parseImageAsset(XmlPullParser parser) throws IOException, XmlPullParserException {
		int imageWidth = -1;
		int imageHeight = -1;
		int orientation = -1;
		String dateTimeOriginal = null;
		String baseROI = null;
		String highlightROI = null;
		String text;
		while (parser.next() != XmlPullParser.END_TAG) {

			text = parser.getName();
			if (parser.getEventType() != XmlPullParser.START_TAG) {

				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			if (text.equals("tiff:ImageWidth")) {
				imageWidth = Integer.parseInt(readText(parser));
			} else if (text.equals("tiff:ImageLength")) {
				imageHeight = Integer.parseInt(readText(parser));
			} else if (text.equals("tiff:Orientation")) {
				orientation = Integer.parseInt(readText(parser));
			} else if (text.equals("exif:DateTimeOriginal")) {
				dateTimeOriginal = readText(parser);
			} else if (text.equals("sem:BaseROI")) {
				baseROI = readText(parser);
			} else if (text.equals("sem:HighlightROI")) {
				highlightROI = readText(parser);
			} else {
				skip(parser, text + "~Inner");
			}
		}
		// Log.e(TAG, "imageWidth:" + imageWidth);
		// Log.e(TAG, "imageHeight:" + imageHeight);
		// Log.e(TAG, "orientation:" + orientation);
		// Log.e(TAG, "dateTimeOriginal:" + dateTimeOriginal);
		// Log.e(TAG, "baseROI:" + baseROI);
		// Log.e(TAG, "highlightROI:" + highlightROI);
		return new VignetteImageElement(imageWidth, imageHeight, orientation, dateTimeOriginal, baseROI, highlightROI);
	}

	private class VignetteElement {

		public String mRelativePath;
	}

	private class VignetteAudioElement extends VignetteElement {
		public int mDuration;

		public VignetteAudioElement(int mDuration) {
			this.mDuration = mDuration;
		}
	}

	private class VignetteImageElement extends VignetteElement {
		public int mImageWidth;
		public int mImageHeight;
		public int mOrientation;
		public String mDateTimeOrig;
		public RectF mBaseRIO = null;
		public RectF mHighLightROI = null;

		public VignetteImageElement(int mImageWidth, int mImageHeight, int mOrientation, String dateTimeOrig, String baseRIO, String highLightROI) {
			this.mImageWidth = mImageWidth;
			this.mImageHeight = mImageHeight;
			this.mOrientation = mOrientation;
			this.mDateTimeOrig = dateTimeOrig;
			try {
				if (baseRIO != null && !baseRIO.equals("")) {
					String[] basePieces = highLightROI.split(" ");
					float left = Float.parseFloat(basePieces[0]);
					float top = Float.parseFloat(basePieces[1]);
					float width = Float.parseFloat(basePieces[2]);
					float height = Float.parseFloat(basePieces[3]);
					this.mBaseRIO = new RectF(left, top, left + width, top + height);

					// Log.e(TAG, "Real base params read");
				}
				if (highLightROI != null && !highLightROI.equals("")) {
					String[] hilightPieces = highLightROI.split(" ");
					float left = Float.parseFloat(hilightPieces[0]);
					float top = Float.parseFloat(hilightPieces[1]);
					float width = Float.parseFloat(hilightPieces[2]);
					float height = Float.parseFloat(hilightPieces[3]);
					this.mHighLightROI = new RectF(left, top, left + width, top + height);
					// Log.e(TAG, "Real highlight params read");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

	}

	private VignetteAudioElement parseAudioAsset(XmlPullParser parser) throws IOException, XmlPullParserException {
		int duration = -1;
		String text;
		while (parser.next() != XmlPullParser.END_TAG) {
			text = parser.getName();
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
					break;
				}
				continue;
			}
			if (text.equals("xmpDM:duration")) {
				duration = (int) (Float.parseFloat(readText(parser)) * 1000);
			} else {
				skip(parser, text + "~Inner");
			}
		}
		return new VignetteAudioElement(duration);
	}

	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
		Log.d(TAG, "Skipping:" + name);
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
	
	private String getImageId(Context context, String relativePath) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(relativePath, "");
	}
}
