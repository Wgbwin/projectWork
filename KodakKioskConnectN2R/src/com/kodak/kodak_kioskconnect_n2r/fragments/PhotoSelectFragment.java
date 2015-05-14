package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppConstants.LoadImageType;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.example.android.displayingbitmaps.util.Utils;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.CartItem;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardProductActivity;
import com.kodak.kodak_kioskconnect_n2r.ImageCheckBoxView;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;

public class PhotoSelectFragment extends Fragment {
	private static String TAG = PhotoSelectFragment.class.getSimpleName();
	private AlbumInfo mAlbum;
	private boolean isUseResStringName;
	private PhotoSource photoSource;
	private FlowType flowType;
	private List<PhotoInfo> mPhotosInAlbum;
	private PhotosAdapter mPhotosAdapter;
	private GridView vGridViewPhotos;

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";

	ICommunicating mListener;

	// private static final int XSPEED_MIN = 200 ;
	//
	// private static final int XDISTANCE_MIN = 150 ;
	//
	// private static final int YDISTANCE_MIN = 50 ;
	//
	// private float xDown ;
	//
	// private float yDown ;
	//
	// private float xMove ;
	//
	// private float yMove ;
	//
	// private VelocityTracker mVelocityTracker ;

	public PhotoSelectFragment() {

	}

	public static PhotoSelectFragment newInstance(Bundle b) {
		PhotoSelectFragment f = new PhotoSelectFragment();

		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (ICommunicating) activity;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View v = inflater.inflate(R.layout.imagegridview, container, false);

		vGridViewPhotos = (GridView) v.findViewById(R.id.common_gridview);
		vGridViewPhotos.setAdapter(mPhotosAdapter);
		setEvents(v);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

	}

	private void initData() {
		// TODO Auto-generated method stub
		flowType = AppContext.getApplication().getFlowType();
		Bundle bundle = getArguments();
		mAlbum = (AlbumInfo) bundle.getSerializable("album");
		isUseResStringName = bundle.getBoolean("useResStringName", false);
		photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
		mPhotosInAlbum = mAlbum.getmPhotosInAlbum();

		if (((PhotoSelectMainFragmentActivity) getActivity()).forEdit && flowType.isCollageWorkFlow()) {

			Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
			if (currentCollage != null && mPhotosInAlbum != null && mPhotosInAlbum.size() > 0) {
				List<PhotoInfo> photoInCollageList = currentCollage.page.getPhotosInCollagePage();
				if(photoInCollageList!=null && photoInCollageList.size()>0){
					for (PhotoInfo photo : mPhotosInAlbum) {
						for (PhotoInfo photoInCollage : photoInCollageList) {
							if (photoInCollage.equals(photo)) {
								photo.setInsertedForCollagePage(photoInCollage.isInsertedForCollagePage());
								photo.setSelected(photoInCollage.isSelected());
								break;
							}
						}
					}
				}

			}

		}

		if (mPhotosInAlbum != null && mPhotosInAlbum.size() > 0 && AppContext.getApplication().getmTempSelectedPhotos() != null
				&& AppContext.getApplication().getmTempSelectedPhotos().size() > 0) {
			int selectedNum = 0;

			if (flowType.isWifiWorkFlow()) {
				for (PhotoInfo photo : mPhotosInAlbum) {

					boolean selected = false;
					for (PhotoInfo selectedPhoto : AppContext.getApplication().getmTempSelectedPhotos()) {
						if (photo.equals(selectedPhoto)) {
							selected = true;
							selectedNum++;
							break;
						}
					}
					photo.setSelected(selected);

				}

			} else {
				for (PhotoInfo photo : mPhotosInAlbum) {
					for (PhotoInfo selectedPhoto : AppContext.getApplication().getmTempSelectedPhotos()) {
						if (photo.equals(selectedPhoto)) {
							photo.setSelected(selectedPhoto.isSelected());
							selectedNum++;
						}

					}
				}

			}
			mAlbum.setSelectedPhotoNum(selectedNum);
		}

		mPhotosAdapter = new PhotosAdapter(getActivity(), mPhotosInAlbum);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

	}

	private void setEvents(View v) {

		vGridViewPhotos.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// Before Honeycomb pause image loading on scroll to help
					// with performance
					if (!Utils.hasHoneycomb()) {
						mImageFetcher.setPauseWork(true);
					}
				} else {
					mImageFetcher.setPauseWork(false);
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}

		});

		vGridViewPhotos.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@TargetApi(16)
			@Override
			public void onGlobalLayout() {

				if (mPhotosAdapter!=null && mPhotosAdapter.getNumColumns() == 0) {
					final int numColumns = (int) Math.floor(vGridViewPhotos.getWidth() / (mImageThumbSize + mImageThumbSpacing));
					if (numColumns > 0) {
						final int columnWidth = (vGridViewPhotos.getWidth() / numColumns) - mImageThumbSpacing;
						mPhotosAdapter.setNumColumns(numColumns);
						mPhotosAdapter.setItemHeight(columnWidth);

						if (Utils.hasJellyBean()) {
							// vGridViewAlbums.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							Class<?> c = vGridViewPhotos.getViewTreeObserver().getClass();
							try {
								Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class);
								method.invoke(vGridViewPhotos.getViewTreeObserver(), this);

							} catch (NoSuchMethodException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							vGridViewPhotos.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				}

			}

		});

		vGridViewPhotos.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < mPhotosAdapter.getNumColumns()) {
					return;
				}

				if (photoSource.isFromPhone()) {
					if (isUseResStringName) { // Camera
						((PhotoSelectMainFragmentActivity) getActivity()).setLocalyticsEventAttr(
								PhotoSelectMainFragmentActivity.KEY_PHOTOS_CAMERA_ROLL, PhotoSelectMainFragmentActivity.VALUE_YES);

					} else {
						((PhotoSelectMainFragmentActivity) getActivity()).setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_PHOTOS_EVENT,
								PhotoSelectMainFragmentActivity.VALUE_YES);
					}
				}

				PhotoInfo photo = mPhotosInAlbum.get(position - mPhotosAdapter.getNumColumns());
				PhotosAdapter.ViewHolder holder = (PhotosAdapter.ViewHolder) view.getTag();
				ImageCheckBoxView imageView = holder.vImageViewPhoto;

				if (photo.isPhotoInLocal()) {
					String photoId = photo.getPhotoId();
					boolean isBitmapInErrorList = AppContext.getApplication().isBitmapInErrorList(photoId);
					if (isBitmapInErrorList && !imageView.getChecked()) {
						return;
					}

				}

				Class<com.kodak.kodak_kioskconnect_n2r.PictureUploadService2> pictureUploadService2 = com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class;
				Intent serviceIntent = new Intent(getActivity(), pictureUploadService2);
				ComponentName serviceComponentName = getActivity().startService(serviceIntent);

				if (flowType.isPrintWorkFlow()) {

					onItemClickPrintWorkFlow(photo, imageView);

				} else if (flowType.isGreetingCardWorkFlow()) {

					onItemClickGreetingCardWorkFlow(photo, imageView);

				} else if (flowType.isPhotoBookWorkFlow()) {

					onItemClickPhotoBookWorkFlow(photo, imageView);

				} else if (flowType.isCollageWorkFlow()) {
					onItemClickCollageWorkFlow(photo, imageView);
				}

				else if (flowType.isWifiWorkFlow()) {

					onItemClickWifiWorkFlow(photo, imageView);

				}

			}
		});

		/**
		 * 
		 v.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { // TODO
		 *           Auto-generated method stub createVelocityTracker(event) ;
		 *           Log.v(TAG, "sunny action BEGIN") ; switch
		 *           (event.getAction()) { case MotionEvent.ACTION_DOWN:
		 *           Log.v(TAG, "sunny action down") ; xDown = event.getRawX() ;
		 *           yDown = event.getRawY() ; break; case
		 *           MotionEvent.ACTION_MOVE : Log.v(TAG, "sunny action move") ;
		 *           xMove = event.getRawX() ; yMove = event.getRawY() ; int
		 *           distanceX= (int)(xMove - xDown) ; int distanceY =Math.abs(
		 *           (int)(yMove-yDown) ); int xSpeed = getScrollVelocity() ;
		 *           if(distanceX>XDISTANCE_MIN && distanceY < YDISTANCE_MIN &&
		 *           xSpeed>XSPEED_MIN ){ Log.v(TAG, "sunny,right") ;
		 * 
		 *           int currentTabIndex =
		 *           ((PhotoSelectMainFragmentActivity)getActivity
		 *           ()).getCurrentTabIndex() ;
		 * 
		 *           if(currentTabIndex!=0){
		 *           getActivity().getSupportFragmentManager().popBackStack() ;
		 *           }
		 * 
		 *           return true ;
		 * 
		 * 
		 *           }else {
		 * 
		 *           }
		 * 
		 * 
		 *           break ; case MotionEvent.ACTION_UP : Log.v(TAG,
		 *           "sunny action up") ; recycleVelocityTracker() ;
		 * 
		 *           break ; default: break; }
		 * 
		 * 
		 *           return false; } }) ;
		 * 
		 * 
		 **/

	}

	/**
	 * create VelocityTracker object
	 * 
	 * @param event
	 */
	// private void createVelocityTracker(MotionEvent event){
	// if(mVelocityTracker==null){
	// mVelocityTracker = VelocityTracker.obtain() ;
	// }
	//
	// mVelocityTracker.addMovement(event) ;
	// }
	//
	// private int getScrollVelocity(){
	// mVelocityTracker.computeCurrentVelocity(1000);
	// int velocity = (int) mVelocityTracker.getXVelocity() ;
	// return Math.abs(velocity) ;
	// }
	//
	// private void recycleVelocityTracker(){
	// mVelocityTracker.recycle() ;
	// mVelocityTracker = null ;
	// }

	public void selectAllEvent() {
		if (mPhotosInAlbum != null && mPhotosInAlbum.size() > 0) {

			if (photoSource.isFromPhone()) {
				if (isUseResStringName) { // Camera
					((PhotoSelectMainFragmentActivity) getActivity()).setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_PHOTOS_CAMERA_ROLL,
							PhotoSelectMainFragmentActivity.VALUE_YES);

				} else {
					((PhotoSelectMainFragmentActivity) getActivity()).setLocalyticsEventAttr(PhotoSelectMainFragmentActivity.KEY_PHOTOS_EVENT,
							PhotoSelectMainFragmentActivity.VALUE_YES);
				}
			}

			if (flowType.isPrintWorkFlow()) {
				Class<com.kodak.kodak_kioskconnect_n2r.PictureUploadService2> pictureUploadService2 = com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class;
				Intent serviceIntent = new Intent(getActivity(), pictureUploadService2);
				ComponentName serviceComponentName = getActivity().startService(serviceIntent);
			}

			for (PhotoInfo photo : mPhotosInAlbum) {

				if (photo.isPhotoInLocal()) {
					String photoId = photo.getPhotoId();
					boolean isBitmapInErrorList = AppContext.getApplication().isBitmapInErrorList(photoId);
					if ((isBitmapInErrorList || Utils.isFilter(photo.getPhotoPath())) && !photo.isSelected()) {

						continue;
					}

				}

				if (flowType.isPrintWorkFlow()) {
					PrintInfo printInfo = new PrintInfo(photo);
					addPhotoToTempAndUploadList(photo);
					photo.setSelected(true);
					boolean success = AppContext.getApplication().addPrintToPrintList(printInfo);
					if (success) {

						CartItem cartItem = new CartItem(getActivity());
						cartItem.photoInfo = photo;
						cartItem.quantity = printInfo.getQuantity();
						cartItem.roi = printInfo.getRoi();
						cartItem.price = printInfo.getPrice();
						cartItem.width = printInfo.getWidth();
						cartItem.height = printInfo.getHeight();
						cartItem.name = printInfo.getName();
						cartItem.shortName = printInfo.getShortName();
						cartItem.productDescriptionId = printInfo.getProductDescriptionId();
						cartItem.productType = AppConstants.PRINT_TYPE;
						PrintHelper.cartChildren.get(PrintHelper.defaultPrintSizeIndex).add(cartItem);
						if (!"".equals(cartItem.photoInfo.getLocalUri())) {
							if (PrintHelper.uploadShare2WmcQueue != null)
								PrintHelper.uploadShare2WmcQueue.add(cartItem.photoInfo.getLocalUri());
						}
					}
				} else if (flowType.isWifiWorkFlow()) {

					if (!photo.isSelected()) {
						addPhotoToTempList(photo);
						photo.setSelected(true);
					}

				}

			}

			mPhotosAdapter.notifyDataSetChanged();
			((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(AppContext.getApplication().getmTempSelectedPhotos().size() + " "
					+ getString(R.string.selected));
		}

	}

	public void invertSelectAllEvent() {

		if (mPhotosInAlbum != null && mPhotosInAlbum.size() > 0) {
			for (PhotoInfo photo : mPhotosInAlbum) {

				if (flowType.isPrintWorkFlow()) {

					PrintInfo printInfo = new PrintInfo(photo);
					removePhotoFromTempAndUploadList(photo);
					photo.setSelected(false);
					boolean success = AppContext.getApplication().removePrintFromPrintListByPhoto(photo);

					if (PrintHelper.uploadShare2WmcQueue != null) {

						PrintHelper.uploadShare2WmcQueue.remove(photo.getLocalUri());
					}

					if (success) {
						for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
							int count = 0;
							while (count < PrintHelper.cartChildren.get(i).size()) {
								CartItem tempItem = PrintHelper.cartChildren.get(i).get(count);

								if (tempItem.photoInfo.equals(photo)) {
									PrintHelper.cartChildren.get(i).remove(count);
								} else {
									count++;
								}
							}
						}
					}

				} else if (flowType.isWifiWorkFlow()) {
					if (photo.isSelected()) {
						removePhotoFromTempList(photo);
						photo.setSelected(false);
					}

				}

			}

			mPhotosAdapter.notifyDataSetChanged();
			if (!flowType.isGreetingCardWorkFlow()) {
				((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(AppContext.getApplication().getmTempSelectedPhotos().size()
						+ " " + getString(R.string.selected));
			} else {
				((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(getString(R.string.image_selection_title_for_card));
			}

		}

	}

	/**
	 * print work flow
	 * 
	 * @param photo
	 */
	private void onItemClickPrintWorkFlow(PhotoInfo photo, ImageCheckBoxView imageView) {

		PrintInfo printInfo = new PrintInfo(photo);

		if (photo.isSelected()) {
			// do unselect remove photo
			removePhotoFromTempAndUploadList(photo);
			boolean success = AppContext.getApplication().removePrintFromPrintListByPhoto(photo);

			if (PrintHelper.uploadShare2WmcQueue != null) {

				PrintHelper.uploadShare2WmcQueue.remove(photo.getLocalUri());
			}

			if (success) {
				for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
					int count = 0;
					while (count < PrintHelper.cartChildren.get(i).size()) {
						CartItem tempItem = PrintHelper.cartChildren.get(i).get(count);
						if (tempItem.photoInfo.equals(photo)) {
							PrintHelper.cartChildren.get(i).remove(count);
						} else {
							count++;
						}
					}
				}
			}

		} else {
			addPhotoToTempAndUploadList(photo);
			boolean success = AppContext.getApplication().addPrintToPrintList(printInfo);
			if (success) {
				CartItem cartItem = new CartItem(getActivity());
				cartItem.photoInfo = photo;
				cartItem.quantity = printInfo.getQuantity();
				cartItem.roi = printInfo.getRoi();
				cartItem.price = printInfo.getPrice();
				cartItem.width = printInfo.getWidth();
				cartItem.height = printInfo.getHeight();
				cartItem.name = printInfo.getName();
				cartItem.shortName = printInfo.getShortName();
				cartItem.productDescriptionId = printInfo.getProductDescriptionId();
				cartItem.productType = AppConstants.PRINT_TYPE;
				PrintHelper.cartChildren.get(PrintHelper.defaultPrintSizeIndex).add(cartItem);
				if (!"".equals(cartItem.photoInfo.getPhotoPath())) {
					if (PrintHelper.uploadShare2WmcQueue != null)
						PrintHelper.uploadShare2WmcQueue.add(cartItem.photoInfo.getPhotoId());
				}

			}

		}

		photo.setSelected(!photo.isSelected());
		// mPhotosAdapter.notifyDataSetChanged();
		imageView.setChecked(photo.isSelected());

		((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(AppContext.getApplication().getmTempSelectedPhotos().size() + " "
				+ getString(R.string.selected));

	}

	/**
	 * greeting card work flow
	 * 
	 * @param photo
	 */
	private void onItemClickGreetingCardWorkFlow(PhotoInfo photo, ImageCheckBoxView imageView) {
		// Greeting Card part start
		GreetingCardManager manager = GreetingCardManager.getGreetingCardManager(getActivity());
		manager.getEditLayer().setPhotoInfo(photo);
		if (manager.getAlbumMap4Click().containsKey(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex)) {
			// Log.i(TAG, "zzzzzz albummap contain the key holeindex = " +
			// (manager.getEditLayer().holeIndex +
			// PrintHelper.editedPageIndex));
			if (!manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex).equals(PrintHelper.albumid)
					&& manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex) != null) {
				if (!PrintHelper.albumSelected.isEmpty()) {
					String formerCount = PrintHelper.albumSelected.get(""
							+ manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex));
					// Log.i(TAG, "698 formerCount = " + formerCount);
					if (formerCount == null || formerCount.trim().equals("")) {
						formerCount = "1";
					}
					int former = Integer.parseInt(formerCount);
					former--;
					PrintHelper.albumSelected.put(""
							+ manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex), "" + former);
					for (int i = 0; i < PrintHelper.mAlbumButton.size(); i++) {
						if (manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex) == PrintHelper.mAlbumButton
								.get(i).getId()) {
						}
					}
				}
				manager.getAlbumMap4Click().put(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex, PrintHelper.albumid);
			}
		} else {
			manager.getAlbumMap4Click().put(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex, PrintHelper.albumid);
		}
		Intent intent = new Intent(getActivity(), GreetingCardProductActivity.class);
		intent.putExtra("selectedPhoto", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		getActivity().finish();
		AppContext.getApplication().getmTempSelectedPhotos().clear();
		AppContext.getApplication().addPhotoToTempSelectedList(photo);
	}

	/**
	 * photobook workflow
	 * 
	 * @param photo
	 */
	private void onItemClickPhotoBookWorkFlow(PhotoInfo photo, ImageCheckBoxView imageView) {
		Photobook photobook = AppContext.getApplication().getPhotobook();
		if (photo.isSelected()) {
			if (!photobook.isImageAlreadyInPhotobook(photo)) {
				photobook.selectedImages.remove(photo);
				photobook.imageEditParams.remove(photo);
				removePhotoFromTempAndUploadList(photo);
			} else {
				AppContext.getApplication().removePhotoFromTempSelectedList(photo);
			}
		} else {
			if (hasMaxNumberOfImages(photobook)) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(getActivity());
				builder.setTitle(getString(R.string.selectimages_max));
				builder.setMessage("");
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				return;
			} else {
				if (!photobook.isImageAlreadyInPhotobook(photo)) {
					photobook.selectedImages.add(photo);
					photobook.imageEditParams.put(photo, new SelectedImage(getActivity(), photo));
					addPhotoToTempAndUploadList(photo);
				}
			}
		}
		photo.setSelected(!photo.isSelected());
		imageView.setChecked(photo.isSelected());
		((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(AppContext.getApplication().getmTempSelectedPhotos().size() + " "
				+ getString(R.string.selected));
	}

	/**
	 * collage work flow
	 */
	private void onItemClickCollageWorkFlow(PhotoInfo photo, ImageCheckBoxView imageView) {
		Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
		if (currentCollage == null) {
			return;
		}

		if (photo.isSelected()) {

			boolean forAddFlag = ((PhotoSelectMainFragmentActivity) getActivity()).forEdit;
			if (forAddFlag) {
				if (currentCollage.isPhotoInCollage(photo)) {
					// can not be selected do nothing
					return;
				} else {
					removePhotoFromTempAndUploadList(photo);
					currentCollage.removePhotoFromCollage(photo);
				}

			} else {

				removePhotoFromTempAndUploadList(photo);
				currentCollage.removePhotoFromCollage(photo);

			}

		} else {

			if (CollageManager.getInstance().isPhotosNumberMaximum(currentCollage)) {
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(getActivity());

				builder.setMessage(getString(R.string.collage_maximum_tips,
						((PhotoSelectMainFragmentActivity) getActivity()).collageProduct.getName()));
				builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				return;
			} else {
				addPhotoToTempAndUploadList(photo);

			}

		}

		photo.setSelected(!photo.isSelected());
		imageView.setChecked(photo.isSelected());
		((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(CollageManager.getInstance().getCurrentTotalPhotoNumberInCollage(
				currentCollage)
				+ CollageManager.getInstance().getCurrentCollage().page.getTextLayerNumber()
				+ "/"
				+ CollageManager.getInstance().getCurrentCollage().page.maxNumberOfImages + " " + getString(R.string.maximum));

	}

	/**
	 * wifi work flow
	 * 
	 * @param photo
	 */
	private void onItemClickWifiWorkFlow(PhotoInfo photo, ImageCheckBoxView imageView) {
		if (photo.isSelected()) {

			removePhotoFromTempList(photo);
		} else {
			addPhotoToTempList(photo);
		}

		photo.setSelected(!photo.isSelected());
		// mPhotosAdapter.notifyDataSetChanged();
		imageView.setChecked(photo.isSelected());
		((PhotoSelectMainFragmentActivity) getActivity()).showSelectNumberText(AppContext.getApplication().getmTempSelectedPhotos().size() + " "
				+ getString(R.string.selected));
	}

	private boolean hasMaxNumberOfImages(Photobook photobook) {
		return photobook.minNumberOfImages != 0 && photobook.maxNumberOfImages != 0 && flowType.isPhotoBookWorkFlow()
				&& photobook.selectedImages.size() >= photobook.maxNumberOfImages;
	}

	/**
	 * add photo to temp selected list and upload list
	 * 
	 * @param photo
	 */
	private void addPhotoToTempAndUploadList(PhotoInfo photo) {
		boolean success = false;
		success = AppContext.getApplication().addPhotoToTempSelectedList(photo);
		AppContext.getApplication().addPhotosToUploadQueue(photo);
		if (success) {
			updateSelectPhotoNum(photo);
		}

	}

	/**
	 * remove photo from temp selected list and upload list
	 * 
	 * @param photo
	 */
	private void removePhotoFromTempAndUploadList(PhotoInfo photo) {
		boolean success = false;
		success = AppContext.getApplication().removePhotoFromTempSelectedList(photo);
		AppContext.getApplication().removePhotoFromUploadQueue(photo);
		if (success) {
			updateSelectPhotoNum(photo);
		}
	}

	/**
	 * add photo to temp selected list
	 * 
	 * @param photo
	 */
	private void addPhotoToTempList(PhotoInfo photo) {
		boolean success = false;
		success = AppContext.getApplication().addPhotoToTempSelectedList(photo);
		if (success) {
			updateSelectPhotoNum(photo);
		}

	}

	/**
	 * remove photo from temp selected list
	 * 
	 * @param photo
	 */
	private void removePhotoFromTempList(PhotoInfo photo) {
		boolean success = false;
		success = AppContext.getApplication().removePhotoFromTempSelectedList(photo);
		if (success) {
			updateSelectPhotoNum(photo);
		}
	}

	/**
	 * update the select photo num of this album
	 * 
	 * @param photo
	 */
	private void updateSelectPhotoNum(PhotoInfo photo) {
		if (!photo.isSelected()) {
			mAlbum.plusSelectPhoto();
		} else {
			mAlbum.miniusSelectPhoto();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAlbum != null) {
			String albumTitle = "";
			if (isUseResStringName) {
				albumTitle = getString(R.string.camera);
			} else {
				albumTitle = mAlbum.getmAlbumName();
			}
			mListener.setTitleText(albumTitle);
		}
		mImageFetcher.setExitTasksEarly(false);
		mPhotosAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
		mPhotosAdapter=null;
		vGridViewPhotos = null;
		
	}

	class PhotosAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private List<PhotoInfo> mDataList;
		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		private int mTopViewHeight = 0;

		public PhotosAdapter(Context context, List<PhotoInfo> dataList) {
			mDataList = dataList;
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mTopViewHeight = getResources().getDimensionPixelSize(R.dimen.top_view_height);
		}

		@Override
		public int getCount() {
			// If columns have yet to be determined, return no items
			if (getNumColumns() == 0) {
				return 0;
			}
			if (mDataList != null) {
				return mDataList.size() + mNumColumns;
			} else {
				return 0;
			}

		}

		@Override
		public PhotoInfo getItem(int position) {
			return position < mNumColumns ? null : mDataList.get(position - mNumColumns);
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height
				convertView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mTopViewHeight));
				return convertView;
			}
			// View v ;
			final ViewHolder holder;

			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				convertView = mInflater.inflate(R.layout.album_grid_item, parent, false);
				holder = new ViewHolder();
				holder.vImageViewPhoto = (ImageCheckBoxView) convertView.findViewById(R.id.image_album_cover);
				holder.vLayoutInfo = (LinearLayout) convertView.findViewById(R.id.linearlayout_info);
				holder.vLayoutInfo.setVisibility(View.GONE);
				holder.vImageViewPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.vImageViewPhoto.setLayoutParams(mImageViewLayoutParams);
				convertView.setTag(holder);
			} else { // Otherwise re-use the converted view
				holder = (ViewHolder) convertView.getTag();
			}

			PhotoInfo data = (PhotoInfo) getItem(position);
			if (holder.vImageViewPhoto.getLayoutParams().height != mItemHeight) {

				holder.vImageViewPhoto.setLayoutParams(mImageViewLayoutParams);

			}

			if (photoSource.isFromPhone()) {

				// mImageFetcher.loadImage(data.getPhotoId(),
				// holder.vImageViewPhoto, true);
				mImageFetcher.loadImage(data.getPhotoId(), data.getPhotoPath(), holder.vImageViewPhoto, LoadImageType.MEDIA_IMAGE);

			} else if (photoSource.isFromFaceBook()) {
				String url = data.getThumbnailUrl();
				// Log.v("sunny", "photo url :" + url);
				// mImageFetcher.loadImage(url, holder.vImageViewPhoto, false);
				mImageFetcher.loadImage(url, url, holder.vImageViewPhoto, LoadImageType.WEB_IMAGE);

			}

			holder.vImageViewPhoto.setChecked(data.isSelected());
			if (data.getFlowType().isCollageWorkFlow()) {
				holder.vImageViewPhoto.setmCheckedDisabled(data.isInsertedForCollagePage());
			}

			return convertView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}

		private class ViewHolder {
			private ImageCheckBoxView vImageViewPhoto;
			private LinearLayout vLayoutInfo;
		}
	}

}
