package com.kodak.flip;

import com.kodak.kodak_kioskconnect_n2r.ROI;

public class PhotoDefinition {
	public int pageindex;
	
	/** uploaded image id */
	public String photoID;
	public String photoURL;
	public String photoSuperHighResolutionImageURL;
	public String photoLocalURI;
	public String photoPath;
	
	public ROI roi;
	public ROI croproi;
	public ROI screencoords;
	public int angle = 0;

	public boolean isImageResWarning;

	public String imageCaption;
}
