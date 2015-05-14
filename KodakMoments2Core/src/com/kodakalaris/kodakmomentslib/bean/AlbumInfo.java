package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.List;


public class AlbumInfo implements Serializable{
	 /**
	  * 
	  */
	private static final long serialVersionUID = -2421436877340152378L;
	private String mAlbumId ; // for local it is the bucketId,for net is an album id
	 private String mAlbumName ; // for local it is the bucket display name,for net is an album name
    private int photoNum ;        //the album's size
	 private int selectedPhotoNum ; //the number of photo is selected 
	 private String coverId ;       //the id of cover photo 
	 private String coverUri;
	 private String coverPath ;
	 private String mAlbumPath ;    
	 private List<PhotoInfo> mPhotosInAlbum ;
	 private boolean isVirtualAlbum ;
	 
	 public AlbumInfo(){
		 super() ;
	 }
	 
	 public String getmAlbumId() {
		return mAlbumId;
	}

	public void setmAlbumId(String mAlbumId) {
		this.mAlbumId = mAlbumId;
	}

	public String getmAlbumName() {
		return mAlbumName;
	}

	public void setmAlbumName(String mAlbumName) {
		this.mAlbumName = mAlbumName;
	}

	public int getPhotoNum() {
		return photoNum;
	}

	public void setPhotoNum(int photoNum) {
		this.photoNum = photoNum;
	}

	public int getSelectedPhotoNum() {
		return selectedPhotoNum;
	}

	public void setSelectedPhotoNum(int selectedPhotoNum) {
		synchronized (this) {
			this.selectedPhotoNum = selectedPhotoNum;
		}
		
	}

	public String getCoverId() {
		return coverId;
	}

	public void setCoverId(String coverId) {
		this.coverId = coverId;
	}

	
	public void plusSelectPhoto(){
		synchronized (this) {
			selectedPhotoNum++ ;
		}
		
	}
	 
	public void miniusSelectPhoto(){
       synchronized (this) {
       	selectedPhotoNum-- ;
		}
		
	}

	public List<PhotoInfo> getmPhotosInAlbum() {
		return mPhotosInAlbum;
	}

	public void setmPhotosInAlbum(List<PhotoInfo> mPhotosInAlbum) {
		this.mPhotosInAlbum = mPhotosInAlbum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAlbumId == null) ? 0 : mAlbumId.hashCode());
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
		AlbumInfo other = (AlbumInfo) obj;
		if (mAlbumId == null) {
			if (other.mAlbumId != null)
				return false;
		} else if (!mAlbumId.equals(other.mAlbumId))
			return false;
		return true;
	}

	public boolean isVirtualAlbum() {
		return isVirtualAlbum;
	}

	public void setVirtualAlbum(boolean isVirtualAlbum) {
		this.isVirtualAlbum = isVirtualAlbum;
	}

	public String getmAlbumPath() {
		return mAlbumPath;
	}

	public void setmAlbumPath(String mAlbumPath) {
		this.mAlbumPath = mAlbumPath;
	}

	public String getCoverPath() {
		return coverPath;
	}

	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}

	public String getCoverUri() {
		return coverUri;
	}

	public void setCoverUri(String coverUri) {
		this.coverUri = coverUri;
	}
	 
	
	
	 

}
