package com.kodak.flip;

import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.quickbook.database.ThumbnailProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images.ImageColumns;

public class SelectedImage {
	public int id = -1;
	private Bitmap thumbnail;
	private EditParam editParam;
	private Rect srcRect;
	private RectF dstRect;
	private float rotateDegrees = -1;
	private Context context;
	public boolean canEdit;
	public static int ORIGINAL_SIZE = 1;
	public static int COMPRESS_SIZE = 4;
	public String displayName;
	public int time = 0;
	public PhotoInfo photo;
	
	
	private ThumbnailProvider mProvider;
	/** This ID is return by Server when an image file done uploaded. */
	public String sUploadImageID;
	final String[] columns = { ImageColumns.DATE_MODIFIED, BaseColumns._ID, ImageColumns.DISPLAY_NAME};
	
	public SelectedImage(Context context, PhotoInfo photo){
		this.photo = photo;
		canEdit = true;
	}
	
	public SelectedImage(Context context, int sourceId) {
		this.context = context;
		thumbnail = BitmapFactory.decodeResource(context.getResources(), sourceId);
		editParam = new EditParam();
		srcRect = new Rect(0, 0, thumbnail.getWidth(), thumbnail.getHeight());
		dstRect = new RectF();
		canEdit = false;
	}
	
	
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Bitmap getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Bitmap bitmap) {
		this.thumbnail = bitmap;
	}
	public EditParam getEditParam() {
		return editParam;
	}
	public void setEditParam(EditParam editParam) {
		this.editParam = editParam;
	}

	public Rect getSrcRect() {
		return srcRect;
	}

	public void setSrcRect(Rect srcRect) {
		this.srcRect = srcRect;
	}

	public RectF getDstRect() {
		return dstRect;
	}

	public void setDstRect(RectF dstRect) {
		this.dstRect = dstRect;
	}

	public float getRotateDegrees() {
		return rotateDegrees;
	}

	public void setRotateDegrees(float rotateDegrees) {
		this.rotateDegrees = rotateDegrees;
	}

	public void setBitmap(Bitmap bitmap) {
		//this.bitmap = new WeakReference<Bitmap>(bitmap);
	}
	
}
