package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.AppContext;
import com.AppManager;
import com.kodak.drag.DragController;
import com.kodak.drag.DragGridView;
import com.kodak.drag.DragLayer1;
import com.kodak.drag.FrameImageView;
import com.kodak.drag.IDragDropPosition;
import com.kodak.flip.PhotoBookPage;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.quickbook.database.ThumbnailProvider;
import com.kodak.utils.DownloadPicture;

public class ArrangeActivity extends Activity{

	private final int moveUploadInformationStart = 1;
	private final int moveUploadInformationEnd = 2;
	private final int moveUploadInformationError = 3;
	private final int deleteUploadInformationStart = 4;
	private final int deleteUploadInformationEnd = 5;
	private final int deleteUploadInformationError = 6;
	private final int changeTitlePageStart = 7;
	private final int changeTitlePageEnd = 8;
	private final int changeTitlePageError = 9;
	
	private Button order, startOver, ibTrash;
	private TextView header;
	public IDragDropPosition dragPosition;
	private DragGridView grid;
	private GridAdapter adapter;
	private ProgressBar progress;
	
	
	private ThumbnailProvider mProvider;
	private DownloadPicture download;
	
	public static final String ARRANGE_THUMBNAIL_SUFFIX = "_at"; // "_arrange_thumbnail"
	
	private String TAG = "ArrangeActivity";
	
	/**  true when dragging grid item and uploading changes */
	private boolean stopTask = false;

	private SharedPreferences prefs;
	
	/** false when uploading the changes of book  */
	private boolean clickable = true;
	
	
	private final int[] mCoordinatesTemp = new int[2];
	private int trashX, trashY;
	private String SIZE_4_6 = "s46"; // "size_4_6";
	
	private PrintMakerWebService service;
	
	public DragLayer1 dragLayer;
	private DragController mDragController;
	public static int COLUM_NUM = 3;
	public static final int GRID_COLUMN_HOZIRONTALSPACING = 8;
	private int gridItem_height, gridItem_width;
	String mPosition = "";
	PhotoInfo mLocalURL = null;
	String mNumber = "";
	/**  the position where the moving image from */
	int movePosition = -1, movePositionTemp = -1;
	/**  the position where the image is dragged */
	int mDragPosition = -1;
	int lastPostion;
	Bitmap bm = null;
	int columnwidth;
	
    boolean is_4_6_size = false;
    boolean is_firstPageWait = false;

    /**  the urls of selected images */
	private ArrayList<String> images/*, tempImages*/;
	private ArrayList<PhotoInfo> localUris;
	private ArrayList<Bitmap> bms;
	
	/** if uploading failed, use these to back to the original sequence */
	private int tempDeletePosition = -1,tempFromPageIndex = -1,tempToPageIndex = -1;
	private Bitmap tempBitmap = null;
	private String tempURL = null;
	private PhotoInfo tempLocalURL = null;
	int titlePosition;
	private int viewCount;

	Bitmap bitmap;
	String temp;
	PhotoInfo tempUri;
	private Photobook photobook;
	
	private Button slideMenuOpen;
	private Button slideMenuClose_btn;
	private TextView sideMenuHome_tex;
	private TextView sideMenuSetting_tex;
	private TextView sideMenuInfo_tex;
	private TextView sideMenuVersion_tex;
	
	private boolean localyticsDelete = false;
	private boolean localyticsMove = false;
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case moveUploadInformationStart:
				localyticsMove = true;
				//step-8
				Log.e(TAG, "step-8");
				Log.i("", "arrange move Upload Information Start");
				clickable = false;
				stopAsync();
				progress.setVisibility(View.VISIBLE);
				Bundle b = msg.getData();
				int iFromPageIndex = tempFromPageIndex = b.getInt(FROM_INDEX);
				int iToPageIndex = tempToPageIndex = b.getInt(TO_INDEX);
				if(iFromPageIndex < iToPageIndex && tempToPageIndex != viewCount-1){
					new Thread(new MoveThread(iFromPageIndex + 1, iToPageIndex + 1)).start(); //changed by song
				}else if (iFromPageIndex >= iToPageIndex) {
					new Thread(new MoveThread(iFromPageIndex + 1, iToPageIndex + 1)).start();
				}else if (tempToPageIndex == viewCount-1) {
					new Thread(new MoveThread(iFromPageIndex + 1, iToPageIndex)).start();
				}
				break;
			case moveUploadInformationEnd:
				is_firstPageWait = false;
				PhotoBookPage tempPage = photobook.photoBookPages.get(tempFromPageIndex + 1);
				if(tempFromPageIndex < tempToPageIndex && tempToPageIndex != viewCount-1){
					photobook.photoBookPages.remove(tempFromPageIndex + 1);
					photobook.photoBookPages.add(tempToPageIndex + 1, tempPage); //changed by song
				}else if (tempFromPageIndex > tempToPageIndex){
					photobook.photoBookPages.remove(tempFromPageIndex + 1);
					photobook.photoBookPages.add(tempToPageIndex + 1, tempPage);
				}else if (tempToPageIndex == viewCount-1){
					photobook.photoBookPages.remove(tempFromPageIndex + 1);
					photobook.photoBookPages.add(tempToPageIndex, tempPage);
				}
				tempFromPageIndex = tempToPageIndex = -1;
				progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;
				adapter.notifyDataSetChanged();
				break;
			case moveUploadInformationError:
				is_firstPageWait = false;
				stopAsync();
				if(tempFromPageIndex < tempToPageIndex && tempToPageIndex != viewCount-1){
					bitmap = bms.get(tempToPageIndex);
					temp = images.get(tempToPageIndex); //changed by song
					tempUri = localUris.get(tempToPageIndex);
					bms.remove(tempToPageIndex);
					images.remove(tempToPageIndex);
					localUris.remove(tempToPageIndex);
					bms.add(tempFromPageIndex, bitmap);
					images.add(tempFromPageIndex, temp);
					localUris.add(tempFromPageIndex, tempUri);
				}else if(tempFromPageIndex > tempToPageIndex){
					bitmap = bms.get(tempToPageIndex);
					temp = images.get(tempToPageIndex);
					tempUri = localUris.get(tempToPageIndex);
					bms.remove(tempToPageIndex);
					images.remove(tempToPageIndex);
					localUris.remove(tempToPageIndex);
					bms.add(tempFromPageIndex, bitmap);
					images.add(tempFromPageIndex, temp);
					localUris.add(tempFromPageIndex, tempUri);
				} else if(tempToPageIndex == viewCount-1){
					bitmap = bms.get(tempToPageIndex-1);
					temp = images.get(tempToPageIndex-1);
					tempUri = localUris.get(tempToPageIndex-1);
					bms.remove(tempToPageIndex-1);
					images.remove(tempToPageIndex-1);
					localUris.remove(tempToPageIndex-1);
					bms.add(tempFromPageIndex, bitmap);
					images.add(tempFromPageIndex, temp);
					localUris.add(tempFromPageIndex, tempUri);
				}
				
				adapter.notifyDataSetChanged();
				progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ArrangeActivity.this);
				builder.setTitle(R.string.upload_change_failed);
				builder.setPositiveButton(R.string.share_upload_retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						bitmap = bms.get(tempFromPageIndex);
						temp = images.get(tempFromPageIndex);
						tempUri = localUris.get(tempFromPageIndex);
						
						bms.remove(tempFromPageIndex);
						images.remove(tempFromPageIndex);
						localUris.remove(tempFromPageIndex);

						
						if(tempFromPageIndex < tempToPageIndex && tempToPageIndex != viewCount-1){
							bms.add(tempToPageIndex , bitmap);
							images.add(tempToPageIndex , temp);
							localUris.add(tempToPageIndex, tempUri); //changed by song
						}else if(tempFromPageIndex > tempToPageIndex){
							bms.add(tempToPageIndex , bitmap);
							images.add(tempToPageIndex , temp);
							localUris.add(tempToPageIndex, tempUri);
						}else if(tempToPageIndex == viewCount-1){
							bms.add(tempToPageIndex-1 , bitmap);
							images.add(tempToPageIndex-1 , temp);
							localUris.add(tempToPageIndex-1, tempUri);
						}
						/*images.add(tempToPageIndex , temp);
						localUris.add(tempToPageIndex, tempUri);*/
						changeSequence(tempFromPageIndex, tempToPageIndex, moveUploadInformationStart);
						stopTask = false;
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//tempFromPageIndex = tempToPageIndex = -1;
						stopTask = false;
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				builder.create().show();
				break;
			case deleteUploadInformationStart:
				Log.i("", "arrange delete Upload Information Start");
				Bundle bundle = msg.getData();
				String pageID = bundle.getString(PHOTO_BOOK_PAGE_ID);
				stopAsync();
				clickable = false;
				progress.setVisibility(View.VISIBLE);
				new Thread(new DeleteThread(pageID)).start();
				break;
			case deleteUploadInformationEnd:
				PhotoInfo photo = tempLocalURL;
				photobook.selectedImages.remove(photo);
				photobook.imageEditParams.remove(photo);
				progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;
				images.remove(tempDeletePosition);
				localUris.remove(tempDeletePosition);
				bms.remove(tempDeletePosition);
				adapter.notifyDataSetChanged();
				adapter.notifyDataSetChanged();
				break;
			case deleteUploadInformationError:
				progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;
				/*if (!PrintHelper.selectedImageUrls.contains(tempLocalURL)){
					PrintHelper.selectedImageUrls.add(tempLocalURL);
				}*/
				if (photobook.selectedImages.contains(tempLocalURL)){
					photobook.selectedImages.add(tempLocalURL);
				}
				InfoDialog.InfoDialogBuilder builder1 = new InfoDialog.InfoDialogBuilder(ArrangeActivity.this);
				builder1.setTitle(R.string.delete_failed);
				builder1.setPositiveButton(R.string.share_upload_retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deletePhoto(photobook.photoBookPages.get(tempDeletePosition + 1).sPhotoBookPageID
								, deleteUploadInformationStart);
						dialog.dismiss();
					}
				});
				builder1.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				builder1.create().show();
				break;
			case changeTitlePageStart:
				if (!Connection.isConnected(ArrangeActivity.this) && !Connection.is3gConnected(ArrangeActivity.this))	{
					showNoConnectionDialog();
					break;
				}
				//step-3
				Log.e(TAG, "step-3");
				Bundle bun = msg.getData();
				Log.i("", "arrange change Title Page Start");
				clickable = false;
				stopAsync();
				progress.setVisibility(View.VISIBLE);
				
				new Thread(new SetTitlePageThread(titlePosition)).start();
				
				bms.remove(movePositionTemp);
				images.remove(movePositionTemp);
				localUris.remove(movePositionTemp);
				bms.add(2, bm);
				images.add(2, mPosition);
				localUris.add(2, mLocalURL);
				
				is_firstPageWait = true;
				if(bms.get(2) != null)
				     bms.get(2).recycle();
				bms.set(2, null);
				adapter.notifyDataSetChanged();
				
				break;
			case changeTitlePageEnd:
				//step-6
				Log.e(TAG, "step-6");
				String url = photobook.photoBookPages.get(2).sPhotoBookPageID + ARRANGE_THUMBNAIL_SUFFIX;
				mProvider.deleteMini(photobook.photoBookPages.get(2).sPhotoBookPageID);
				if(!is_4_6_size)
					mProvider.deleteMini(photobook.photoBookPages.get(0).sPhotoBookPageID);
				/*progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;*/
				if(bms.get(1) != null)
				     bms.get(1).recycle();
				bms.set(1, null);
				//adapter.notifyDataSetChanged();
				if(bms.get(2) != null)
				     bms.get(2).recycle();
				bms.set(2, null);
				changeSequence(movePositionTemp, 2, moveUploadInformationStart);
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldisabled);
				stopTask = false;
				mPosition = "";
				mLocalURL = null;
				bm = null;
				
				break;
			case changeTitlePageError:
				progress.setVisibility(View.INVISIBLE);
				clickable = true;
				stopTask = false;
				
				bms.remove(2);
				images.remove(2);
				localUris.remove(2);
				
				bms.add(movePositionTemp, bm);
				images.add(movePositionTemp, mPosition);
				localUris.add(movePositionTemp, mLocalURL);
				
				showNoConnectionDialog();
				break;
			default: break;
			}
		}
		
	};
	
	private final String FROM_INDEX = "iFromPageIndex";
	private final String TO_INDEX = "iToPageIndex";
	private final String TITLE_POSITION = "titlePosition";
	private final String PHOTO_BOOK_PAGE_ID = "sPhotoBookPageID";
	private void changeSequence(int movePosition, int toPosition, int what){
		//step-7
		Log.e(TAG, "step-7");
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt(FROM_INDEX, movePosition);
		b.putInt(TO_INDEX, toPosition);
		msg.setData(b);
		msg.what = what;
		handler.sendMessage(msg);
	}
	
	private void deletePhoto(String id, int what){
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putString(PHOTO_BOOK_PAGE_ID, id);
		msg.setData(b);
		msg.what = what;
		handler.sendMessage(msg);
	}
	
	private void setQuickbookTitlePage(int position){
		//step-2
		Log.e(TAG, "step-2");
		if(photobook.photoBookPages.get(position).PhotoBookPageImages == null){
			Log.i("", "photo ID: null");
		}else
		Log.i("", position + ",position ID:" + photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoID
				+ ",prefs image ID:" + photobook.titleImageId);
		Message msg = handler.obtainMessage();
		titlePosition = position;
		msg.what = changeTitlePageStart;
		handler.sendMessage(msg);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(!clickable){
			return clickable;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent myIntent = new Intent(ArrangeActivity.this, QuickBookFlipperActivity.class);
		myIntent.putExtra("isFromArrange", true);
		myIntent.putExtra("lmoved", localyticsMove);
		myIntent.putExtra("ldelete", localyticsDelete);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bms!=null && bms.size() > 0){
			for(Bitmap bitmap : bms){
				if(bitmap!=null){
					bitmap.recycle();
					bitmap=null;
				}
			}
		}
		bms.clear();
		bms = null;
	}

	private String getRemoveWarningText() {
        PrintProduct photoBookProduct = null;
        if(PrintHelper.inQuickbook){
          for(PrintProduct product : PrintHelper.products){
              if(product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage) && product.getId().equals(photobook.proDescId)){
              photoBookProduct = product;
              break;
            }
          }
        }
        String bookName = (photoBookProduct == null) ? "" : photoBookProduct.getName();
        return String.format(getString(R.string.remove_photo), bookName);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		photobook = AppContext.getApplication().getPhotobook();
		setContentView(R.layout.quickbook_arrange_view);
		
		mProvider = ThumbnailProvider.obtainInstance(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		findViewById(R.id.settingsButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.infoButton).setVisibility(View.INVISIBLE);
		progress = (ProgressBar)findViewById(R.id.progressBarArrange);
		progress.setVisibility(View.INVISIBLE);
		startOver = (Button) findViewById(R.id.backButton);
		startOver.setVisibility(View.INVISIBLE);
		order = (Button) findViewById(R.id.nextButton);
		order.setText(getString(R.string.save));
		header = (TextView)findViewById(R.id.headerBarText);
		header.setText(R.string.tv_arrange);
		
		service = new PrintMakerWebService(this, "arrange");
		images = new ArrayList<String>();
		localUris = new ArrayList<PhotoInfo>();
		Log.i(TAG, "photo book pages size:" + photobook.photoBookPages.size());
		for(int i = 0; i < photobook.photoBookPages.size() - 2; i++){
			if(i == 0){
				images.add(null);
				localUris.add(null);
			}
			if(i == 0 || i == 1)
				continue;
			if(i == 2){
				images.add(photobook.photoBookPages.get(i).sPhotoBookPageURL);
				localUris.add(null);
			}else{
				//images.add(PrintHelper.photoBookPages.get(i).PhotoBookPageImages.get(0).photoURL);
				if(!photobook.photoBookPages.get(i).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)){
					images.add(photobook.photoBookPages.get(i).sPhotoBookPageURL);
					// find out the PhotoInfo
					PhotoInfo photo = null;
					for(PhotoInfo p : photobook.selectedImages){
						if(p.getPhotoSource().isFromPhone()){
							if(p.getLocalUri().equals(photobook.photoBookPages.get(i).PhotoBookPageImages.get(0).photoLocalURI)){
								photo = p;
								break;
							}
						} else {
							if(p.getPhotoPath().equals(photobook.photoBookPages.get(i).PhotoBookPageImages.get(0).photoPath)){
								photo = p;
								break;
							}
						}
					}
					localUris.add(photo);
				}
			}
		}
		
        adapter = new GridAdapter(this, images);
        //is_4_6_size = !prefs.getBoolean(PrintHelper.sIsDuplex, false);
        is_4_6_size = !photobook.isDuplex;
        
        grid = (DragGridView)findViewById(R.id.gridView1);
        
        initVar();
        Configuration cf= this.getResources().getConfiguration();
        int ori = cf.orientation ;
        if(ori == Configuration.ORIENTATION_LANDSCAPE){
    		COLUM_NUM = 6;
        }else if(ori == Configuration.ORIENTATION_PORTRAIT){
        	COLUM_NUM = 3;
        }
        grid.setNumColumns(COLUM_NUM);
		Display display = getWindowManager().getDefaultDisplay();
		int screenwidth = display.getWidth();
		columnwidth = screenwidth / COLUM_NUM;
		grid.setColumnWidth(columnwidth);
		if (! is_4_6_size) {
			gridItem_width = columnwidth;
			gridItem_height = columnwidth * 2 / 3/* - GRID_COLUMN_HOZIRONTALSPACING*/;
			
			grid.setHorizontalSpacing(0);
			grid.setVerticalSpacing(6);
		}
		else {
			gridItem_width = columnwidth - GRID_COLUMN_HOZIRONTALSPACING;
			gridItem_height = columnwidth * 2 / 3/* - GRID_COLUMN_HOZIRONTALSPACING*/;
			
			grid.setHorizontalSpacing(1);
			grid.setVerticalSpacing(GRID_COLUMN_HOZIRONTALSPACING + 6);
		}
        bms = new ArrayList<Bitmap>();
        addBitmaps();
        
        grid.setAdapter(adapter);
        grid.setSelection(4);
        dragPosition = new IDragDropPosition() {
			
			@Override
			public void dropOnGrid(int x, int y, int offsetY) {
				//step-1
				Log.e(TAG, "step-1");
				mDragPosition = -1;
				if(is_4_6_size){
					int position = grid.pointToPosition(x, y - columnwidth/4);
					Log.i("ArrangeActivity", "dropOnGrid---------position:" + position + 
							",movePosition:" + movePosition + ",offsetY:" + columnwidth/4 + " mPosition: " + mPosition);
					if(position < 2 && position >= 0){
						if(position == 1)
							setQuickbookTitlePage(movePosition + 1);
						movePosition = -1;
						ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldisabled);
						adapter.notifyDataSetChanged();
						return;
					}
					
					bm = bms.get(movePosition);
					
					if (position > -1 && mPosition != "" && movePosition != position) {
						bms.remove(movePosition);
						images.remove(movePosition);
						localUris.remove(movePosition);
						if(movePosition <  position && position != viewCount-1){
							bms.add(position, bm);
							images.add(position, mPosition);
							localUris.add(position, mLocalURL);//changed by song
						}else if(movePosition >  position){
							bms.add(position, bm);
							images.add(position, mPosition);
							localUris.add(position, mLocalURL);
						}else if(position == viewCount-1){
							bms.add(position-1, bm);
							images.add(position-1, mPosition);
							localUris.add(position-1, mLocalURL);
						}
						if(movePosition != position){
							changeSequence(movePosition, position, moveUploadInformationStart);
						}							
					}
				}else{
					int position = grid.pointToPosition(x, y - columnwidth/2);
					Log.i("ArrangeActivity", "dropOnGrid---------position:" + position + 
							",movePosition:" + movePosition + ",offsetY:" + columnwidth/2 + " mPosition: " + mPosition);
					if(position < 2 && position >= 0){
						if(position == 1)
							setQuickbookTitlePage(movePosition + 1);
						movePosition = -1;
						adapter.notifyDataSetChanged();
						return;
					}
					bm = bms.get(movePosition);

					if (position > -1 && mPosition != "" && movePosition != position) {
						bms.remove(movePosition);
						images.remove(movePosition);
						//localUris.remove(movePosition);
						if(movePosition < position && position != viewCount-1){
							bms.add(position, bm);
							images.add(position, mPosition);
							localUris.add(position, mLocalURL); //changed by song
						}else if(movePosition >  position){
							bms.add(position, bm);
							images.add(position, mPosition);
							localUris.add(position, mLocalURL);
						}else if(position == viewCount-1){
							bms.add(position-1, bm);
							images.add(position-1, mPosition);
							localUris.add(position-1, mLocalURL);
						}

						if(movePosition != position ){
							changeSequence(movePosition, position, moveUploadInformationStart);
						}
							
					}
				}
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldisabled);
				stopTask = false;
				adapter.notifyDataSetChanged();
				movePosition = -1;
				mPosition = "";
				mLocalURL = null;
				bm = null;
			}

			@Override
			public void scroll(int x, int y, int upScrollBounce, int downScrollBounce) {
				int dragPosition = grid.pointToPosition(x, y);
				Log.i("ArrangeActivity", "x = " + x + ",y = " + y + ",up = " + upScrollBounce + ",down = " + downScrollBounce + ",position = " + dragPosition);
				if(y < upScrollBounce && dragPosition != AdapterView.INVALID_POSITION){
					Log.e("ArrangeActivity", "upScroll set selection:" + dragPosition + ",colum num:" + COLUM_NUM);
					grid.setSelection(dragPosition - COLUM_NUM);
				}else if(y > downScrollBounce && dragPosition != AdapterView.INVALID_POSITION){
					Log.e("ArrangeActivity", "downScroll set selection:" + dragPosition + ",colum num:" + COLUM_NUM);
					grid.setSelection(dragPosition);
				}
				
			}

			@Override
			public void resetPostionIndicator(int x, int y) 
			{
				int position = (is_4_6_size) ? 
						grid.pointToPosition(x, y - columnwidth/4) :
							grid.pointToPosition(x, y - columnwidth/2);

				
				if(position < 2 && position > -1/*|| position > bms.size() - 3*/){
					position = -1;
				}
//				if(is_4_6_size && position > bms.size() - 4 && evenPages){
//					return;
//				}
//				Log.i("ArrangeActivity", "current position for dragging:" + position);
				if (position > -1 && lastPostion != position) {
					mDragPosition = position;
					lastPostion = position;
					adapter.notifyDataSetChanged();
				}
				else if (position == -1 && lastPostion != position){
					mDragPosition = -1;
					lastPostion = position;
					adapter.notifyDataSetChanged();
				}
			}
		};
        dragLayer = (DragLayer1) findViewById(R.id.layout_drag_demo);
        mDragController = new DragController(dragLayer, dragPosition);
//        mDragController.setAdapter(adapter);
        dragLayer.setController(mDragController);
		
		setListener();
		
		adapter.setSelectItem(-1);
		ibTrash.setEnabled(false);
		
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuClose_btn = (Button) findViewById(R.id.slideMenuClose_btn);
		sideMenuHome_tex = (TextView) findViewById(R.id.sideMenuHome_tex);
		sideMenuSetting_tex = (TextView) findViewById(R.id.sideMenuSetting_tex);
		sideMenuInfo_tex = (TextView) findViewById(R.id.sideMenuInfo_tex);
		sideMenuVersion_tex = (TextView) findViewById(R.id.sideMenuVersion_tex);
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuClose_btn = (Button) findViewById(R.id.slideMenuClose_btn);
		slideMenuOpen.setVisibility(View.VISIBLE);
		
		slideMenuOpen.setOnClickListener(openMenu());

		slideMenuClose_btn.setOnClickListener(closeMenu());

		sideMenuHome_tex.setOnClickListener(gotoHome());

		sideMenuSetting_tex.setOnClickListener(gotoSettings());

		sideMenuInfo_tex.setOnClickListener(gotoInfo());
	}

	private class DeleteThread implements Runnable{
		String sPhotoBookPageID;
		
		private DeleteThread(String sPhotoBookPageID){
			this.sPhotoBookPageID = sPhotoBookPageID;
		}
		
		@Override
		public void run() {
			Log.i("ArrangeActivity", "sPhotoBookPageID:" + sPhotoBookPageID);
			String result = service.pbDeletePhotoBookPage(ArrangeActivity.this, sPhotoBookPageID);
			if(result != null && !result.equals("")){
				for(int i = 0; i < photobook.photoBookPages.size(); i++){
					if(photobook.photoBookPages.get(i).sPhotoBookPageID.equals(sPhotoBookPageID)){
						photobook.photoBookPages.remove(i);
						String url = photobook.photoBookPages.get(i).sPhotoBookPageID + ARRANGE_THUMBNAIL_SUFFIX;
						//mProvider.deleteThumbnail(url);
						mProvider.deleteMini(photobook.photoBookPages.get(i).sPhotoBookPageID);
						break;
					}
				}
				service.parsePhotoBook(result);
				handler.sendEmptyMessage(deleteUploadInformationEnd);
			}else{
				handler.sendEmptyMessage(deleteUploadInformationError);
			}
		}
		
	}
	
	private class MoveThread implements Runnable {
		
		int iFromPageIndex, iToPageIndex;
		private MoveThread(int iFromPageIndex,int iToPageIndex ){
			this.iFromPageIndex = iFromPageIndex;
			this.iToPageIndex = iToPageIndex;
		}

		@Override
		public void run() {
			Log.i("ArrangeActivity", "iFromPageIndex:" + iFromPageIndex + ",iToPageIndex:" + iToPageIndex);
			String result = service.pbMovePhotoBookPage(ArrangeActivity.this, iFromPageIndex, iToPageIndex);
			if(result != null && !result.equals("")){
				handler.sendEmptyMessage(moveUploadInformationEnd);
			}else {
				handler.sendEmptyMessage(moveUploadInformationError);
			}
		}
	}
	
	private class SetTitlePageThread implements Runnable{

		private int position;
		
		private SetTitlePageThread(int position){
			this.position = position;
		}
		
		@Override
		public void run() {
			//step-4
			Log.e(TAG, "step-4");
			String oldTitleImageId = photobook.titleImageId;
			String oldTitleImageLocalURI= photobook.titleImageLocalUri;
			String oldTitleImagePath = photobook.titleImagePath;
			String movePageId = photobook.photoBookPages.get(position).sPhotoBookPageID;
			String moveImageId = photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoID;
			String moveImageLocalURI = photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoLocalURI;
			String moveImagePath = photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoPath;
			
			String sCloneImageID1 = service.CloneImage(oldTitleImageId);
			
			String result1 = service.pbRemovePageImage(ArrangeActivity.this, moveImageId);
			String result2 = service.pbInsertImageOnPhotoBookPage(ArrangeActivity.this, movePageId, sCloneImageID1);
			System.out.println("setQuickbookTitlePage pbRemovePageImage result2:" + result1);
			System.out.println("setQuickbookTitlePage pbInsertImageOnPhotoBookPage result3:" + result2);
			
			is_firstPageWait = false;
			
			String result3 = service.pbRemovePageImage(ArrangeActivity.this, photobook.titleImageId);
			System.out.println("setQuickbookTitlePage pbRemovePageImage result3:" + result3);
			if(result1.equals("") || result2.equals("") || result3.equals("")){
				handler.sendEmptyMessage(changeTitlePageError);
				return;
			}
			photobook.titleImageId = moveImageId;
			photobook.titleImageLocalUri = moveImageLocalURI;
			photobook.titleImagePath = moveImagePath;
			String result = service.pbSetPhotoBookTitlePage(ArrangeActivity.this);
			if(result != null && !result.equals("")){
				handler.sendEmptyMessage(changeTitlePageEnd);
				System.out.println("result:" + result);
				photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoID = sCloneImageID1;
				photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoLocalURI = oldTitleImageLocalURI;
				photobook.photoBookPages.get(position).PhotoBookPageImages.get(0).photoPath = oldTitleImagePath;
			}else{
				handler.sendEmptyMessage(changeTitlePageError);
			}
		}
		
	}
	
    private void initVar(){
    	ibTrash = (Button)findViewById(R.id.ibTrash);
    	ibTrash.setVisibility(View.VISIBLE);
    	ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldisabled);
    }
	
	private void setListener(){
		ibTrash.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final int position = adapter.getmSelectPosition();
				if(position > 1 && position < grid.getAdapter().getCount()-1){
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(ArrangeActivity.this);
					builder.setTitle(getRemoveWarningText());
					builder.setPreviewImage(bms.get(position));
					builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							tempURL = images.get(position);
							tempLocalURL = localUris.get(position);
							
							int needRemoveIndex = 0;
							boolean foundRemoveIndex = false;
							Log.e(TAG, "tempLocalURL: " + tempLocalURL);
							for(int i=0; i<PrintHelper.selectedImageUrls.size(); i++){
								if(tempLocalURL.equals(PrintHelper.selectedImageUrls.get(i))){
									needRemoveIndex = i;
									foundRemoveIndex = true;
									break;
								}
							}
							if(foundRemoveIndex){
								Log.e(TAG, "remove: " + PrintHelper.selectedImageUrls.get(needRemoveIndex));
								PrintHelper.selectedImageUrls.remove(needRemoveIndex);
							}
							
							tempBitmap = bms.get(position);
							tempDeletePosition = position;
							deletePhoto(photobook.photoBookPages.get(position + 1).sPhotoBookPageID
									, deleteUploadInformationStart);
							dialog.dismiss();
							localyticsDelete = true;
						}
					});
					builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					InfoDialog d = builder.create();
					((ImageView)(d.findViewById(R.id.previewIV))).setScaleType(ScaleType.FIT_CENTER);
					d.show();
					
				}
			}
		});
		
    	grid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position < 2 || position >= grid.getAdapter().getCount()-1){
					if(is_4_6_size){
						return false;
					}else{
						return false;
					}
				}
//				if(!is_4_6_size && evenPages && position > bms.size() - 4 ){
//					return false;
//				}
				stopAsync();
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldown);
		    	int[] loc = mCoordinatesTemp;
				ibTrash.getLocationOnScreen(loc);
				trashX = loc[0];
				trashY = loc[1];
				int vLeft = ibTrash.getLeft();
				int vRight = ibTrash.getRight();
				int vTop = ibTrash.getTop();
				int vBottom = ibTrash.getBottom();
				Log.i("", vLeft + "," + vRight + "," + vTop + "," + vBottom);
				mDragController.setGridHeight(grid.getHeight());
				mDragController.startDrag(view);
				
				movePosition = movePositionTemp = position;
				bm = bms.get(position);
//				bms.remove(position);
				
				mPosition = images.get(position);
				mLocalURL = localUris.get(position);
//				mNumber = numbers.get(position);
//				images.remove(position);
				adapter.setSelectItem(-1);
				adapter.notifyDataSetChanged();
				return false;
			}
		});
    	
    	grid.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position < 2 || position >= grid.getAdapter().getCount()-1)
					return;
//				if(!is_4_6_size && evenPages && position > bms.size() - 4 ){
//					return;
//				}
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldown);
				adapter.setSelectItem(position);
				ibTrash.setEnabled(true);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
    	
    	grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position < 2 || position >= grid.getAdapter().getCount()-1)
					return;
//				if(!is_4_6_size && evenPages && position > bms.size() - 4 ){
//					return;
//				}
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldown);
				adapter.setSelectItem(position);
				ibTrash.setEnabled(true);
				adapter.notifyDataSetChanged();
			}
		});

		order.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Saves Quick book and navigates to the Shopping cart
				
				// TODO Delete the unselected check marks, use the selectedImageUrls
				
				Intent myIntent = new Intent(ArrangeActivity.this, QuickBookFlipperActivity.class);
				myIntent.putExtra("isFromArrange", true);
				myIntent.putExtra("lmoved", localyticsMove);
				myIntent.putExtra("ldelete", localyticsDelete);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
				finish();
			}
		});

    }
	private Bitmap wait_image = null;
	private Bitmap backcover_image = null;
	private Bitmap getWaitImage(){
		if(wait_image == null){
			wait_image =  PrintHelper.readBitMap(this, R.drawable.imagewait96x96);
		}
		return wait_image;
	}
	
	private void addBitmaps(){
		for(int i = 0; i < photobook.photoBookPages.size() - 2; i++){
			if(i == 0){
				if(backcover_image == null)
					backcover_image = PrintHelper.readBitMap(this, R.drawable.backcover_duplex);
				bms.add(backcover_image);
			}
			if(i == 0 || i == 1){
				continue;
			}
			// TODO resize
			int scale = 4;
			Bitmap b = mProvider.getMini(photobook.photoBookPages.get(i).sPhotoBookPageID);
			if(b!=null){
				b = Bitmap.createScaledBitmap(b, b.getWidth()/scale, b.getHeight()/scale, true);
			}
			if(photobook.photoBookPages.get(i).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)){
				
			} else if(b != null){
				bms.add(b);
			}else
				bms.add(null);
		}
	}
	
    private Bitmap setAlpha(Bitmap sourceImg, int number, String imageURL) {
    	if(sourceImg == null){
    		return sourceImg;
    	}
    	int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
    	sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,sourceImg.getWidth(), sourceImg.getHeight());// 
    	number = number * 255 / 100;
    	for (int i = 0; i < argb.length; i++) {
    	   argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);// 
    	}
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);
    	
    	return sourceImg;
    }
    
    private void stopAsync(){
    	if(loader != null){
    		stopTask = true;
    		loader.cancel(true);
    		loader = null;
    	}
    }
    
    class GridAdapter extends ArrayAdapter<String>{

    	Context context;
    	LayoutInflater inflater;
    	int mSelectPosition;
    	public int getmSelectPosition() {
			return mSelectPosition;
		}

		public GridAdapter(Context context,  List<String> objects) {
            super(context, 0, objects);
    		this.context = context;
        }
    	
    	public void setSelectItem(int position){
    		this.mSelectPosition = position;
    	}
    	
		@Override
		public int getCount() {
			if(bms == null){
				return 0;
			}
			return bms.size()+1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FrameImageView image;
			viewCount = this.getCount();
			if(convertView == null){
				inflater = LayoutInflater.from(getContext());
				convertView = inflater.inflate(R.layout.quickbook_arrange_griditem, null);
			}
			
			View spaceView1 = convertView.findViewById(R.id.spaceView1);
			View spaceView2 = convertView.findViewById(R.id.spaceView2);
			if(is_4_6_size){
//				spaceView1.setVisibility(View.VISIBLE);
//				spaceView2.setVisibility(View.VISIBLE);
				spaceView1.setVisibility(View.GONE);
				spaceView2.setVisibility(View.GONE);
			}else{
				if(position%2 != 0){
					spaceView1.setVisibility(View.GONE);
					spaceView2.setVisibility(View.VISIBLE);
				}else{
					spaceView1.setVisibility(View.VISIBLE);
					spaceView2.setVisibility(View.GONE);
				}
			}
			
			image =(FrameImageView) convertView.findViewById(R.id.gridItem);
			image.setScaleType(ScaleType.CENTER_CROP);
			Bitmap bitmap = null; 
			boolean haveBitmap = true;
			if(position == getCount()-1){
				haveBitmap = false;
			} else {
				bitmap = bms.get(position);
			}
			
			if(bitmap == null && haveBitmap){
				bitmap = getWaitImage();
				if(loader == null && !stopTask){
					if(position == 2 && !is_firstPageWait){
						loader = new ThumbLoader();
						loader.execute(images.get(position), position + "");
					}else if(position != 2){
						loader = new ThumbLoader();
						loader.execute(images.get(position), position + "");
					}
				}
			}
			
			if(movePosition != -1 && movePosition == position && haveBitmap){
				String url;
				if(bms.get(position) == null){
					url = "bgtn_"; // before_getting_the_thumbnail_from_network_
				}else{
//					url = images.get(position);
					url = photobook.photoBookPages.get(position + 1).sPhotoBookPageID;
				}
				bitmap = setAlpha(bitmap, 40, url);
				lastPostion = movePosition;
				
			}
			image.setImageBitmap(bitmap);
			convertView.setLayoutParams(new GridView.LayoutParams(
					gridItem_width, gridItem_height));
			if(mSelectPosition >= this.getCount()-1){
				mSelectPosition = -1;
				ibTrash.setBackgroundResource(R.drawable.toolsdeletesmalldisabled);
			}
			
			if(mSelectPosition == position){
				image.setDrawFrame(true);
				image.invalidate();
			}else{
				image.setDrawFrame(false);
				image.invalidate();
			}
			if(mDragPosition == position && mDragPosition != movePosition){
				if(movePosition > position || position == viewCount-1){
					image.setDrawPositionFront(true);
				}else if(movePosition < position && position != viewCount-1){
					image.setDrawPositionBehind(true);
				}			
				image.invalidate();
			}else{
				image.setDrawPositionFront(false);
				image.setDrawPositionBehind(false);
				image.invalidate();
			}
			return convertView;
		}
    	
    }
    
    private ThumbLoader loader;
    class ThumbLoader extends AsyncTask<String, Void, byte[]>{

    	String position;
//    	boolean stopTask = false;
    	private ThumbLoader(){
    		if(download == null){
    			download = new DownloadPicture();
    		}
    	}
    	
		@Override
		protected byte[] doInBackground(String... params) {
			try {
				Log.e("", "loader start");
				//Bitmap bitmap = download.downloadImageFromURL(params[0]);
				byte[] data = download.downloadImageFromURL(params[0]);
				Log.e("ArrangeActivity", "photo url: " + params[0]);
				position = params[1];
				return data;
			} catch (IOException e) {
				e.printStackTrace();
				position = "-1";
				return null;
			}
		}

		@Override
		protected void onPostExecute(byte[] result) {
			super.onPostExecute(result);
			loader = null;
			if(!position.equals("-1") && !stopTask){
				Log.e("ArrangeActivity", "arrange load down:");
				if(bms != null){
					// TODO resize
					int scale = 4;
					/*if(is_4_6_size){
						scale = 2;
					} else {
						scale = 2;
					}*/
					//bms.set(Integer.parseInt(position), BitmapFactory.decodeByteArray(result, 0, result.length));
					//bms.set(Integer.parseInt(position), Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(result, 0, result.length), resizeScaleX, resizeScaleY, true));
					Bitmap b;
					if(result == null){
						b = getWaitImage();
					}else{
						b = BitmapFactory.decodeByteArray(result, 0, result.length);
						if(b == null)
							b = getWaitImage();
						else
							b = Bitmap.createScaledBitmap(b, b.getWidth()/scale, b.getHeight()/scale, true);
					}
					bms.set(Integer.parseInt(position), b);
				}
				try {
					String url = photobook.photoBookPages.get(Integer.parseInt(position) + 1).sPhotoBookPageID;// + ARRANGE_THUMBNAIL_SUFFIX;
					//mProvider.cacheThumbnail(url, result);
					mProvider.cacheMini(url, result);
				}
	        	catch (Exception e) {
	        		Log.w("ArrangeActivity", "Fail to cache the thumbnail to database -03-!");
	        	}
	        	if(adapter!=null){
	        		adapter.notifyDataSetChanged();
	        	}
			}
			Log.e("ArrangeActivity", "arrange load end!");
			stopTask = false;
		}
    	
    }
    
    private void showNoConnectionDialog(){
    	InfoDialog.InfoDialogBuilder builder2 = new InfoDialog.InfoDialogBuilder(ArrangeActivity.this);
		builder2.setTitle(R.string.nointernetconnection);
		builder2.setPositiveButton(R.string.share_upload_retry, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = handler.obtainMessage();
				msg.what = changeTitlePageStart;
				handler.sendMessage(msg);
				//adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
		builder2.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				adapter.notifyDataSetChanged();
				/*SharedPreferences.Editor editor = prefs.edit();
				editor.putString(PrintHelper.sPhotoBookTitleImageID, "");
				editor.putString(PrintHelper.sPhotoBookTitleImageLocalURI, "");
				editor.commit();*/
				dialog.dismiss();
			}
		});
		builder2.create().show();
    }

    private OnClickListener gotoHome() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(ArrangeActivity.this, MainMenu.class);
				AppManager.getAppManager().finishAllActivity();
				startActivity(intent);

			}
		};
		return listener;

	}

	private OnClickListener gotoSettings() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(ArrangeActivity.this, NewSettingActivity.class);	
				startActivity(myIntent);
			}
		};
		return listener;

	}

	private OnClickListener gotoInfo() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(ArrangeActivity.this, HelpActivity.class);
				startActivity(myIntent);

			}
		};
		return listener;

	}

	private OnClickListener openMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				// open
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}

	private OnClickListener closeMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}
}
