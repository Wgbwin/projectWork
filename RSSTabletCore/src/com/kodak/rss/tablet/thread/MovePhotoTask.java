package com.kodak.rss.tablet.thread;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class MovePhotoTask extends AsyncTask<String, Void, List<PhotobookPage>> {
	private Context mContext;
	private InfoDialog waitingDialog;
	private PhotobookPage toPage;
	private ImageInfo imageInfo;
	private ImageResource imageThumbnailResource;
	private Layer addNeweLayer;
	private Photobook currentPhotoBook;
	private String delLayerId = null;
	private String mLayerId = null;

	public MovePhotoTask(Context context, PhotobookPage toPage, ImageInfo imageInfo, Layer layer) {
		this.mContext = context;
		this.toPage = toPage;
		this.imageInfo = imageInfo;
		this.addNeweLayer = layer;
		this.currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (PhotoBookProductUtil.isTitlePage(toPage)) {
			if (toPage.layers != null) {
				for (Layer layer : toPage.layers) {
					if (layer != null && Layer.TYPE_IMAGE.equalsIgnoreCase(layer.type)) {
						delLayerId = layer.contentId;
						break;
					}
				}
			}
		}

		if ((delLayerId != null && PhotoBookProductUtil.isTitlePage(toPage)) || PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {
			waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait).setProgressBar(true).create();
			waitingDialog.show();
		}
	}

	@Override
	protected List<PhotobookPage> doInBackground(String... params) {
		List<PhotobookPage> pages = new ArrayList<PhotobookPage>(2);
		if (!PhotoBookProductUtil.isTitlePage(toPage) && !PhotoBookProductUtil.isBackCoverPageBlank(toPage)) return pages;
		if (imageInfo != null) {
			imageThumbnailResource = imageInfo.imageThumbnailResource;
			while (imageThumbnailResource == null) {
				if (imageInfo.isHavedThumbnailUpload) {
					imageThumbnailResource = imageInfo.imageThumbnailResource;
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		if (imageThumbnailResource == null && addNeweLayer == null) return pages;
		PhotobookWebService pbService = new PhotobookWebService(mContext);
//		String mLayerId = null;
		if (imageThumbnailResource != null) {
			mLayerId = imageThumbnailResource.id;
		} else {
			mLayerId = addNeweLayer.contentId;
		}
		if (PhotoBookProductUtil.isTitlePage(toPage)) {
			String delLayerId = null;
			if (toPage.layers != null) {
				for (Layer layer : toPage.layers) {
					if (layer != null && Layer.TYPE_IMAGE.equalsIgnoreCase(layer.type)) {
						delLayerId = layer.contentId;
						break;
					}
				}
			}
			if (delLayerId != null && delLayerId.equals(mLayerId)) return pages;
			if (delLayerId != null && !"".equals(delLayerId)) {
				PhotobookPage page = null;
				try {
					page = pbService.removePhotoFromPageTask(currentPhotoBook.id, delLayerId);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				if (page != null) {
					pages.add(page);
				}
			}

			if (PhotoBookProductUtil.isInlayer(mLayerId)) {
				PhotobookPage newFromPage = null;
				try {
					newFromPage = pbService.removePhotoFromPageTask(currentPhotoBook.id, mLayerId);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				if (newFromPage != null) {
					int item = -1;
					for (int i = 0; i < pages.size(); i++) {
						PhotobookPage dealPage = pages.get(i);
						if (dealPage != null && dealPage.id.equals(newFromPage.id)) {
							item = i;
							break;
						}
					}
					if (item >= 0) {
						pages.set(item, newFromPage);
					} else {
						pages.add(newFromPage);
					}
				}
			}

			PhotobookPage newToPage = null;
			try {
				newToPage = pbService.addImageToPageTask(currentPhotoBook.id,toPage, mLayerId);
			} catch (RssWebServiceException e) {
				mLayerId = null;
				e.printStackTrace();
			}
			if (newToPage != null) {
				int item = -1;
				for (int i = 0; i < pages.size(); i++) {
					PhotobookPage dealPage = pages.get(i);
					if (dealPage != null && dealPage.id.equals(newToPage.id)) {
						item = i;
						break;
					}
				}
				if (item >= 0) {
					pages.set(item, newToPage);
				} else {
					pages.add(newToPage);
				}
			}
		}
		if (PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {
			if (PhotoBookProductUtil.isInlayer(mLayerId)) {
				PhotobookPage newFromPage = null;
				try {
					newFromPage = pbService.removePhotoFromPageTask(currentPhotoBook.id, mLayerId);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				if (newFromPage != null) {
					int item = -1;
					for (int i = 0; i < pages.size(); i++) {
						PhotobookPage dealPage = pages.get(i);
						if (dealPage != null && dealPage.id.equals(newFromPage.id)) {
							item = i;
							break;
						}
					}
					if (item >= 0) {
						pages.set(item, newFromPage);
					} else {
						pages.add(newFromPage);
					}
				}
			}
			PhotobookPage newToPage = null;
			try {
				newToPage = pbService.addImageToPageTask(currentPhotoBook.id, toPage, mLayerId);
			} catch (RssWebServiceException e) {
				mLayerId = null;
				e.printStackTrace();
			}
			if (newToPage != null) {
				int item = -1;
				for (int i = 0; i < pages.size(); i++) {
					PhotobookPage dealPage = pages.get(i);
					if (dealPage != null && dealPage.id.equals(newToPage.id)) {
						item = i;
						break;
					}
				}
				if (item >= 0) {
					pages.set(item, newToPage);
				} else {
					pages.add(newToPage);
				}
			}
		}
		return pages;
	}

	@Override
	protected void onPostExecute(List<PhotobookPage> result) {
		super.onPostExecute(result);
		if(mContext != null && !((Activity)mContext).isFinishing()){
			if (waitingDialog != null && waitingDialog.isShowing()) {
				waitingDialog.dismiss();
			}
		
			if (result != null && result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					PhotobookPage newPage = result.get(i);
					if (newPage != null) {
						PhotoBookProductUtil.updatePageInPhotobook(newPage,true);
					}
				}
				if (mContext instanceof PhotoBooksProductActivity) {
					((PhotoBooksProductActivity) mContext).notifyPhotoBookPagesChanged();
					((PhotoBooksProductActivity) mContext).removeGiveUpItem(mLayerId);
				}
			} else {
				String prompt = "";
				if (!PhotoBookProductUtil.isTitlePage(toPage) || !PhotoBookProductUtil.isBackCoverPageBlank(toPage) || !PhotoBookProductUtil.isInsideCoverPageBlank(toPage)) {
					if (PhotobookPage.TYPE_COVER.equalsIgnoreCase(toPage.pageType)) {
						prompt = mContext.getString(R.string.cannot_add_to_cover_prompt);
					}

					if (PhotobookPage.TYPE_INSIDE_COVER.equalsIgnoreCase(toPage.pageType)) {
						prompt = mContext.getString(R.string.cannot_add_prompt);
					}

					if (PhotobookPage.TYPE_BACK_COVER.equalsIgnoreCase(toPage.pageType)) {
						prompt = mContext.getString(R.string.cannot_add_to_back_prompt);
					}

					if (PhotobookPage.TYPE_TITLE.equalsIgnoreCase(toPage.pageType)) {
						prompt = mContext.getString(R.string.cannot_add_to_title_prompt);
					}
				}
				new InfoDialog.Builder(mContext).setMessage(prompt).setPositiveButton(R.string.d_ok, null).create().show();
			}
		}
	}

}
