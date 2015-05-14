package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.List;

public class AlbumHolder implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6268622297763605173L;
	private List<AlbumInfo> albums ;

	public List<AlbumInfo> getAlbums() {
		return albums;
	}

	public void setAlbums(List<AlbumInfo> albums) {
		this.albums = albums;
	}
	
	

}
