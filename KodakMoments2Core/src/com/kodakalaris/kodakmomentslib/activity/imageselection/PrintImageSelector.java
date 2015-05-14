package com.kodakalaris.kodakmomentslib.activity.imageselection;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment.OnClickListener;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMImageView;

public class PrintImageSelector extends ImageSelector{
	
	public PrintImageSelector(Context context){
		super();
		mTempSelectedPhotos.addAll(getSelectedPhotos(context));
		
	}
	@Override
	public void selectPhoto(final Context context, final KMImageView imageView ,final PhotoInfo photo ) {
		if(photo.isSelected()){
			
			
			//if the photo has more than one print item ,invert should show a confirm dialog
			if(PrintManager.getInstance(context).isPhotoMultipleSelected(photo)){
				//show a dialog
				GeneralAlertDialogFragment isMultipleDialog = new GeneralAlertDialogFragment(context);
				isMultipleDialog.setMessage(R.string.ImageSelection_DeleteProducts);
				isMultipleDialog.setPositiveButton(R.string.Common_OK, new OnClickListener() {
					
					@Override
					public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
						removePhotoFromTempSelectedList(photo);
						PrintManager.getInstance(context).deletePrintItemsWithAppointedPhoto(photo);
						photo.setSelected(!photo.isSelected());
						removeImageViewChecked(photo);
						imageView.setmSelected(photo.isSelected());
						((MImageSelectionMainActivity)context).refreshImageTray();
					}
				});
				isMultipleDialog.setNegativeButton(R.string.Common_Cancel, null);
				isMultipleDialog.show(((BaseActivity)context).getSupportFragmentManager(), "deleteProducts");
				
			}else{
				removePhotoFromTempSelectedList(photo);
				if(PrintManager.getInstance(context).isPhotoAlreadyInPrintItems(photo)){
					PrintManager.getInstance(context).deletePrintItemsWithAppointedPhoto(photo);
				}
				photo.setSelected(!photo.isSelected());
				removeImageViewChecked(photo);
				imageView.setmSelected(photo.isSelected());
				((MImageSelectionMainActivity)context).refreshImageTray();
			}
			
			
			
		}else {
			addPhotoToTempSelectedList(photo);
			photo.setSelected(!photo.isSelected());
			addKMImageViewToList(imageView);
			imageView.setmSelected(photo.isSelected());
			((MImageSelectionMainActivity)context).refreshImageTray();
		}
	}
	
    @Override
	public List<PhotoInfo> getSelectedPhotos(Context context) {
    	List<PhotoInfo> selectedPhotos = new ArrayList<PhotoInfo>();
    	List<PhotoInfo> allPrintPhotos = PrintManager.getInstance(context).getmPrintPhotos();
    	
    	if(allPrintPhotos!=null && allPrintPhotos.size()>0){
    		for (PhotoInfo photoInfo : allPrintPhotos) {
    			
    			boolean isExist = false;
				for (PhotoInfo photo : selectedPhotos) {
					
					if(photo.equalsNotConsiderDesId(photoInfo)){
						isExist = true;
						break ;
					}
					
				}
    			
				if(!isExist){
					selectedPhotos.add(photoInfo);
				}
				
    			
			}
    	}
    	
		return selectedPhotos;
	}

}
