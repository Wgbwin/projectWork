package com.kodak.kodak_kioskconnect_n2r.collage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;

public class CollageManager {
	private List<Collage> collages = null;
	private volatile static CollageManager instance;

	private CollageManager() {

	}

	public static CollageManager getInstance() {
		if (instance == null) {
			synchronized (CollageManager.class) {
				if (instance == null) {
					instance = new CollageManager();
				}

			}

		}
		return instance;
	}

	public List<Collage> getCollages() {

		return AppContext.getApplication().getCollages();

	}

	public void addCollage(Collage collage) {
		collages = AppContext.getApplication().getCollages();

		for (Collage collageItem : collages) {
			collageItem.setCurrentCollage(false);
		}

		collage.setCurrentCollage(true);
		collages.add(collage);

	}

	public Collage getCurrentCollage() {
		Collage currentCollage = null;
		collages = getCollages();
		for (Collage collageItem : collages) {
			if (collageItem.isCurrentCollage()) {
				currentCollage = collageItem;
				break;
			}
		}

		return currentCollage;
	}

	public void updateCurrentCollage(Collage newCollage) {
		if (newCollage == null) {
			return;
		}
		collages = getCollages();
		newCollage.setCurrentCollage(true);
		Collage oldCollage = getCurrentCollage();
		if (oldCollage != null) {
			// TODO save old local data to the new collage object
			newCollage.updateCollagePageSelectedPhotos(oldCollage.page.getPhotosInCollagePage());
			newCollage.page.setTextLayerNumber(oldCollage.page.getTextLayerNumber()) ;
			collages.remove(oldCollage);
			collages.add(newCollage);
		}

	}

	public void setCurrentCollage(Collage collage) {

		if (collage == null) {
			return;
		}
		collages = getCollages();

		for (Collage collageItem : collages) {
			if (collageItem.id.equals(collage.id)) {
				collageItem.setCurrentCollage(true);
			} else {
				collageItem.setCurrentCollage(false);
			}

		}

	}

	public void setCurrentCollageById(String id) {
		collages = getCollages();
		for (Collage collageItem : collages) {
			if (collageItem.id.equals(id)) {
				collageItem.setCurrentCollage(true);
			} else {
				collageItem.setCurrentCollage(false);
			}

		}

	}

	public boolean removeCollageFromCollageList(Collage collage) {
		boolean success = false;
		collages = getCollages();
		if (collages != null && collages.size() > 0) {

			Iterator<Collage> itor = collages.iterator();
			while (itor.hasNext()) {
				Collage collageInList = itor.next();
				if (collage.equals(collageInList)) {
					itor.remove();
					success = true;
				}

			}
		}

		return success;
	}

	public void removeCollageById(String id) {
		collages = getCollages();
		if (collages != null && collages.size() > 0) {

			Iterator<Collage> itor = collages.iterator();
			while (itor.hasNext()) {
				Collage collageInList = itor.next();
				if (collageInList.id.equals(id)) {
					itor.remove();
				}

			}
		}

	}

	
	//============================
	//for collage page change and save below
	
	public void updataCurrentCollagePage(CollagePage page){
		Collage currentCollage = getCurrentCollage() ;
		CollagePage currentCollagePage = currentCollage.page ;
		page.setPhotosInCollagePage(currentCollagePage.getPhotosInCollagePage());
		page.setTextLayerNumber(currentCollagePage.getTextLayerNumber()) ;
		currentCollage.page = page ;
	}
	
	
	public boolean isPhotosNumberMaximum(Collage collage){
		if(collage.page!=null && collage.page.maxNumberOfImages!=0){
			int currentSelectTotal = 0 ;
			currentSelectTotal = getCurrentTotalPhotoNumberInCollage(collage);
			return currentSelectTotal >= (collage.page.maxNumberOfImages - collage.page.getTextLayerNumber()) ;
		}else {
			return false ;
		}
	}
	
	public int getCurrentTotalPhotoNumberInCollage(Collage collage){
		Set<PhotoInfo> photosSet = new HashSet<PhotoInfo>() ;
		photosSet.addAll(AppContext.getApplication().getmTempSelectedPhotos()) ;
		if(collage.page.getPhotosInCollagePage()!=null){
			photosSet.addAll(collage.page.getPhotosInCollagePage()) ;
		}
		
		
		return photosSet.size() ;
		
		
	}

}
