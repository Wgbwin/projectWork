package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;

import android.text.TextUtils;

import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSizeType;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoUploadingState;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.upload.ImageResource;

;

public class PhotoInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2383722791571366874L;
	private String photoId ;
	private String photoPath = "" ;
	private String bucketId ;    //for local photo
	private String bucketName ;  //for local photo
	private String localUri = "";
	
	private String thumbnailUrl ; //for photo on net
	private String sourceUrl ;   //for photo on net

	private String photoEditPath = "";   
	
	private boolean isSelected ;
	private PhotoSource photoSource ;
    private ImageResource imageResource;
    private boolean isThumbnailUploaded;
	private String productId="" ;  //for print ,just ignore it use the default ""
	private String desId;         
	private FlowType flowType ;
	private int width ;
	private int height ;
	private int orientation;
	private PhotoSizeType photoSizeType;
	private PhotoUploadingState photoUploadingState = PhotoUploadingState.INITIAL;  //this property is just indicate to original photo ,not for thumbnails 
	private long uploadOriginalTime;	//the time stamp of uploading  
	
	public PhotoInfo(){
	
	}


	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getPhotoPath() {

		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		if(!TextUtils.isEmpty(photoPath)){
			if(this.photoSource.isFromFaceBook()){
				/*String key = ImageUtil.hashKeyForDisk(photoPath);
				String filePath = AppContext.getApplication().getExternalKodakTempPictureWeb().getAbsolutePath()
						+"/"+key+".tmp" ;
				this.photoPath = filePath ;*/
			}else {
				this.photoPath = photoPath;
			}
		}else {
			this.photoPath = photoPath ;
		}
		
		
	}
	
	public String getBucketId() {
		return bucketId;
	}

	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getPhotoEditPath() {
		return photoEditPath;
	}


	public void setPhotoEditPath(String photoEditPath) {
		this.photoEditPath = photoEditPath;
	}
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	
	public PhotoSource getPhotoSource() {
		return photoSource;
	}

	public void setPhotoSource(PhotoSource photoSource) {
		this.photoSource = photoSource;
	}

	public String getLocalUri() {
		return localUri;
	}

	public void setLocalUri(String localUri) {
		this.localUri = localUri;
	}

	
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public PhotoSizeType getPhotoSizeType() {
		return photoSizeType;
	}

	public void setPhotoSizeType(PhotoSizeType photoSizeType) {
		this.photoSizeType = photoSizeType;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public ImageResource getImageResource() {
		return imageResource;
	}

	public void setImageResource(ImageResource imageResource) {
		this.imageResource = imageResource;
	}


	public boolean isThumbnailUploaded() {
		return isThumbnailUploaded;
	}


	public void setThumbnailUploaded(boolean isThumbnailUploaded) {
		this.isThumbnailUploaded = isThumbnailUploaded;
	}


	public PhotoUploadingState getPhotoUploadingState() {
		return photoUploadingState;
	}


	public void setPhotoUploadingState(PhotoUploadingState photoUploadingState) {
		this.photoUploadingState = photoUploadingState;
	}

	public long getUploadOriginalTime() {
		return uploadOriginalTime;
	}


	public void setUploadOriginalTime(long uploadOriginalTime) {
		this.uploadOriginalTime = uploadOriginalTime;
	}
	
    public boolean isNeedSwapWidthAndHeightForCalculate(){ 
	   return orientation==90 || orientation ==270 ;
    }
    
    public String getDesId() {
		return desId;
	}


	public void setDesId(String desId) {
		this.desId = desId;
	}
	

	

	@Override
	public String toString() {
		return "PhotoInfo [photoId=" + photoId + ", photoPath=" + photoPath
				+ ", bucketId=" + bucketId + ", bucketName=" + bucketName
				+ ", localUri=" + localUri + ", thumbnailUrl=" + thumbnailUrl
				+ ", sourceUrl=" + sourceUrl + ", isSelected=" + isSelected
				+ ", photoSource=" + photoSource + ", imageResource="
				+ imageResource + ", isThumbnailUploaded="
				+ isThumbnailUploaded + ", productId=" + productId + ", desId="
				+ desId + ", flowType=" + flowType + ", width=" + width
				+ ", height=" + height + ", orientation=" + orientation
				+ ", photoSizeType=" + photoSizeType + ", photoUploadingState="
				+ photoUploadingState + ", uploadOriginalSuccessTime="
				+ uploadOriginalTime + "]";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bucketId == null) ? 0 : bucketId.hashCode());
		result = prime * result + ((desId == null) ? 0 : desId.hashCode());
		result = prime * result
				+ ((flowType == null) ? 0 : flowType.hashCode());
		result = prime * result + ((photoId == null) ? 0 : photoId.hashCode());
		result = prime * result
				+ ((productId == null) ? 0 : productId.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhotoInfo other = (PhotoInfo) obj;
		if (bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!bucketId.equals(other.bucketId))
			return false;
		if (desId == null) {
			if (other.desId != null)
				return false;
		} else if (!desId.equals(other.desId))
			return false;
		if (flowType != other.flowType)
			return false;
		if (photoId == null) {
			if (other.photoId != null)
				return false;
		} else if (!photoId.equals(other.photoId))
			return false;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		return true;
	}

    
	public boolean equalsNotConsiderDesId(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhotoInfo other = (PhotoInfo) obj;
		if (bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!bucketId.equals(other.bucketId))
			return false;
		if (flowType != other.flowType)
			return false;
		if (photoId == null) {
			if (other.photoId != null)
				return false;
		} else if (!photoId.equals(other.photoId))
			return false;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		return true;
	}

	
	public PhotoInfo builderPhotoWithRSSEntry(RssEntry entry){
		PhotoInfo newPhoto = new PhotoInfo();
		newPhoto.bucketId = this.bucketId;
		newPhoto.bucketName = this.bucketName;
		newPhoto.desId = entry.proDescription.id;
		newPhoto.flowType = this.flowType;
		newPhoto.height = this.height;
		newPhoto.imageResource = null;
		newPhoto.isSelected = this.isSelected;
		newPhoto.isThumbnailUploaded = false;
		newPhoto.localUri = this.localUri;
		newPhoto.orientation = this.orientation;
		newPhoto.photoId = this.photoId;
		newPhoto.photoPath = this.photoPath;
		newPhoto.photoEditPath="";
		newPhoto.photoSizeType = this.photoSizeType;
		newPhoto.photoSource = this.photoSource;
		newPhoto.photoUploadingState = PhotoUploadingState.INITIAL;
		newPhoto.productId = this.productId;
		newPhoto.sourceUrl = this.sourceUrl;
		newPhoto.thumbnailUrl = this.thumbnailUrl;
		newPhoto.uploadOriginalTime =0;
		newPhoto.width =this.width;
		
		return newPhoto;
		
	}


	


	
	
	
	

}
