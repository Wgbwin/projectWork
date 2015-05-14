package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.AppConstants;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.utils.ImageUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SingleImageEditActivity extends Activity
{
	private final String TAG = this.getClass().getSimpleName();
	private AppContext appContex;
	private List<List<ProductInfo>> childItemList;
	private List<String> groupItemList;
	Button done;
	TextView viewCropTV;
	TextView rotateTV;
	InputStream in = null;
	Bitmap img = null;
	Button printSizeButton;
	//Button rotateButton;
	Button info;
	Dialog myDialog;
	ImageCropSurfaceView surface;
	ImageSelectionDatabase mImageSelectionDatabase;
	//QntWidget qnty;
	MyCustomAdapter adapter;
	Boolean[] haveAskedKeepSize;
	String[] sizes;
	int effect = 0;
	int lastPosition = 0;
	ImageView lowResWarning;
	boolean shownLowRes = false;
	SharedPreferences prefs;
	Drawable edit;
	Drawable view;
	Drawable rotate;
	private ProgressBar pbWaiting;
	public class MyCustomAdapter extends ArrayAdapter<String>
	{
		public MyCustomAdapter(Context context, int textViewResourceId, String[] productSizes)
		{
			super(context, textViewResourceId, productSizes);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView, ViewGroup parent)
		{
			// return super.getView(position, convertView, parent);
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, parent, false);
			TextView label = (TextView) row.findViewById(R.id.productSizeTextView);
			if (label != null)
			{
				if (PrintHelper.products != null && PrintHelper.products.size() > position)
				{
					label.setText(PrintHelper.products.get(position).getShortName());
				}
				else
				{
					label.setText("");
				}
			}
			return row;
		}
	}

	public void addingitems()
	{
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mImageSelectionDatabase = new ImageSelectionDatabase(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(SingleImageEditActivity.this);
		mImageSelectionDatabase.open();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.singleimageeditfields);
		
		appContex = AppContext.getApplication();
		edit = getResources().getDrawable(R.drawable.toolseditsmallup);
		edit.setBounds(0,0,edit.getIntrinsicWidth(),edit.getIntrinsicHeight());
		pbWaiting = (ProgressBar) findViewById(R.id.pb_waiting);
		
		view = getResources().getDrawable(R.drawable.toolspictureup_big);
		view.setBounds(0,0,view.getIntrinsicWidth(),view.getIntrinsicHeight());
		
		done = (Button) findViewById(R.id.nextButton);
		info = (Button) findViewById(R.id.infoButton);
		done.setText(R.string.done);
		//rotateButton = (Button) findViewById(R.id.rotateButton);
		rotate = getResources().getDrawable(R.drawable.toolsrotateup_big);
		rotate.setBounds(0,0,rotate.getIntrinsicWidth(),rotate.getIntrinsicHeight());
		rotateTV = (TextView) findViewById(R.id.rotateTV);
		rotateTV.setTypeface(PrintHelper.tf);
		rotateTV.setCompoundDrawables(rotate, null, null, null);
		surface = (ImageCropSurfaceView) findViewById(R.id.surfaceView1);
		PhotoInfo photoInfo = PrintHelper.selectedImage.photoInfo;
		printSizeButton = (Button) findViewById(R.id.printSizeSpinner);
		viewCropTV = (TextView) findViewById(R.id.editTV);
		viewCropTV.setTypeface(PrintHelper.tf);
		childItemList = appContex.getChildItemList();
		groupItemList = appContex.getGroupItemList();
		int iSizeCount = 0;
		for(PrintProduct product : PrintHelper.products){
			if(product.getType().equals(PrintProduct.TYPE_PRINTS)){
				iSizeCount++;
			}
		}
		sizes = new String[iSizeCount];
		haveAskedKeepSize = new Boolean[iSizeCount];
		for (int i = 0; i < iSizeCount; i++)
		{
			sizes[i] = PrintHelper.products.get(i).getName();
		}

		info.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent;
				myIntent = new Intent(SingleImageEditActivity.this, HelpActivity.class);
				startActivity(myIntent);
			}
		});

		adapter = new MyCustomAdapter(SingleImageEditActivity.this, R.layout.row, sizes);
		printSizeButton.setText(PrintHelper.selectedImage.shortName);
		// for (int i = 0; i < PrintHelper.products.size(); i++)
		// {
		// if
		// (printSizeButton.getText().equals(PrintHelper.products.get(i).getName()))
		// {
		// PrintHelper.lastGroupPosition = i;
		// }
		// }
		printSizeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//PrintHelper.selectedImage.quantity = Integer.parseInt(qnty.qnty.getText().toString());
				final Dialog dialog = new Dialog(SingleImageEditActivity.this, R.style.DropDownDialog);
				dialog.setContentView(R.layout.custom_dialog);
				dialog.setCancelable(true);
				TextView titleTV = (TextView) dialog.findViewById(R.id.titleTV);
				titleTV.setText(getString(R.string.printSize));
				ListView ssidLV = (ListView) dialog.findViewById(R.id.ssidLV);
				ssidLV.setAdapter(adapter);
				ssidLV.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
					{
						String lastItemSize = PrintHelper.lastSelectedImage.shortName;
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(SingleImageEditActivity.this);
						builder.setTitle(getString(R.string.keepPrint1) + " " + lastItemSize + " " +getString(R.string.keepPrint2));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								PrintHelper.lastSelectedImage = PrintHelper.selectedImage;
								dialog.dismiss();
							}
						});
						builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								ProductInfo selectProduct = null;
								ProductInfo tempProduct = null;
								String selectUri = PrintHelper.lastSelectedImage.photoInfo.getLocalUri(); 
								String DescriptionId = PrintHelper.lastSelectedImage.descriptionId; 
								int selectChildPosition = -1;
								int selectGroupPosition = -1;
								for (int i = 0; i < groupItemList.size(); i++)
								{
									for (int j = 0; j < childItemList.get(i).size(); j++)
									{
										tempProduct = childItemList.get(i).get(j);
										if (tempProduct.photoInfo.getLocalUri().equals(selectUri) && tempProduct.descriptionId.equals(DescriptionId))
										{
											selectProduct = tempProduct;
											selectGroupPosition = i;
											selectChildPosition = j;
											break;
										}
									}
								}
								try
								{
									if (prefs.getBoolean("analytics", false))
									{
										try
										{
											PrintHelper.mTracker.trackEvent("Dialog", "Print_Removed", selectProduct.name, selectProduct.quantity);
											PrintHelper.mTracker.dispatch();
										}
										catch (Exception ex)
										{
											ex.printStackTrace();
										}
									}
									childItemList.get(selectGroupPosition).remove(selectChildPosition);
									if (childItemList.get(selectGroupPosition).size() == 0) {
										groupItemList.remove(selectGroupPosition);
										childItemList.remove(selectGroupPosition);
									}
									Log.d(TAG, "Remove Last Group Position: " + selectGroupPosition + "  Last Child Position: " + selectChildPosition);
									Log.d(TAG, "Remove Selected Group Position: " + PrintHelper.selectedImageGroup + "  Selected Child Position: " + PrintHelper.selectedImageChild);
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}
								PrintHelper.lastSelectedImage = PrintHelper.selectedImage;
								dialog.dismiss();
							}
						});
						String item = ((TextView) view.findViewById(R.id.productSizeTextView)).getText().toString();
						for (int k = 0; k < PrintHelper.products.size(); k++)
						{
							if (PrintHelper.products.get(k).getShortName().equals(item))
							{
								lastPosition = k;
							}
						}
						// If they did not select the same size do something
						if (!printSizeButton.getText().toString().equals(item))
						{
							boolean addItem = true;
							// if the list of the newly selected size is zero, just add the item
							if (!groupItemList.contains(PrintHelper.products.get(position).getName()))
							{
								addItem = true;
							}
							else
							// check to see if a size of this print is already there
							{/*
								for (int i = 0; i < childItemList.size(); i++){
									for (int j = 0; j < childItemList.get(i).size(); j++){
										String fileName = childItemList.get(i).get(j).photoInfo.getPhotoPath();
										if (fileName.equals(PrintHelper.selectedImage.photoInfo.getPhotoPath()))
										{
											surface.saveROI();
											Canvas c = null;
											try
											{
												c = surface.getHolder().lockCanvas();
												synchronized (surface.getHolder())
												{
													addItem = false;
													PrintHelper.selectedImage = childItemList.get(i).get(j);
													// PrintHelper.selectedImagePos = i;
													surface.img = null;												
													surface.newHeight = 0;
													surface.newWidth = 0;
													surface.height = 0;
													surface.width = 0;
													surface.rect = null;
													 
												}
											}
											catch (Exception ex)
											{
												Log.e(TAG, "Error locking and updating canvas in viewcrop click listener: " + ex.getMessage());
											}
											finally
											{
												if (c != null)
												{
													surface.getHolder().unlockCanvasAndPost(c);
												}
											}
											break;
										}
									}
								}
							*/}
							if (addItem)
							{
								if (prefs.getBoolean("analytics", false))
								{
									try
									{
										PrintHelper.mTracker.trackEvent("Edit", "*Add_Size", PrintHelper.products.get(position).getName(), 0);
										PrintHelper.mTracker.dispatch();
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}
								// PrintHelper.lastGroupPosition = PrintHelper.selectedImageGroup;
								// PrintHelper.lastChildPosition = PrintHelper.selectedImageChild;
								surface.saveROI();
								printSizeButton.setText(item);
								ProductInfo tempItem = new ProductInfo(SingleImageEditActivity.this);
								tempItem.productType = PrintHelper.selectedImage.productType;
								tempItem.photoInfo = PrintHelper.selectedImage.photoInfo;
								tempItem.price = Double.parseDouble(PrintHelper.products.get(position).getMinPrice());
								tempItem.name = PrintHelper.products.get(position).getName();
								tempItem.descriptionId = PrintHelper.products.get(position).getId();
								tempItem.shortName = PrintHelper.products.get(position).getShortName();
								tempItem.ProductId = PrintHelper.selectedImage.serverID;
								tempItem.minQuantity = PrintHelper.products.get(position).getQuantityIncrement()==0 ? 1: PrintHelper.products.get(position).getQuantityIncrement();
								if (PrintHelper.products.get(position).getHeight() < PrintHelper.products.get(position).getWidth())
								{
									tempItem.height = "" + PrintHelper.products.get(position).getHeight();
									tempItem.width = "" + PrintHelper.products.get(position).getWidth();
								}
								else
								{
									tempItem.width = "" + PrintHelper.products.get(position).getHeight();
									tempItem.height = "" + PrintHelper.products.get(position).getWidth();
								}
								tempItem.quantity = PrintHelper.selectedImage.minQuantity;
								double productWidth = Double.parseDouble(tempItem.width);
								double productHeight = Double.parseDouble(tempItem.height);
								double ratio = 1.0;
								if (productWidth > productHeight)
								{
									ratio = productWidth / productHeight;
								}
								else
								{
									ratio = productHeight / productWidth;
								}
								
								int outWidth, outHeight;
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inJustDecodeBounds = true;
								if(tempItem.photoInfo.getPhotoSource().isFromPhone()){
									PrintHelper.loadThumbnailImage(tempItem.photoInfo.getLocalUri(), MediaStore.Images.Thumbnails.MINI_KIND, options, SingleImageEditActivity.this);
									outWidth = options.outWidth;
									outHeight = options.outHeight;
									try {
										ExifInterface exif = new ExifInterface(tempItem.photoInfo.getPhotoPath());
										int attOri = exif.getAttributeInt("Orientation", 0);
										if(attOri == ExifInterface.ORIENTATION_ROTATE_90 || attOri == ExifInterface.ORIENTATION_ROTATE_270){
											int temp = outWidth;
											outWidth = outHeight;
											outHeight = temp;
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									outWidth = tempItem.photoInfo.getWidth();
									outHeight = tempItem.photoInfo.getHeight();
								}
								
								ROI roi = PrintHelper.CalculateDefaultRoi(1.0 * outWidth, 1.0 * outHeight, ratio);
								ROI tempRoi2 = new ROI();
								tempRoi2.h = roi.h / outHeight;
								tempRoi2.w = roi.w / outWidth;
								tempRoi2.y = roi.y / outHeight;
								tempRoi2.x = roi.x / outWidth;
								tempItem.roi = tempRoi2;
								// PrintHelper.items.add(PrintHelper.selectedImagePos
								// + 1, tempItem);
								List<ProductInfo> tempList= new ArrayList<ProductInfo>();
								boolean needAddGroup = true; //add new group
								boolean isAddedChild = false;//has add the child item
								for (String tempName : groupItemList){
									if (tempName.equals(tempItem.name)){
										needAddGroup = false;
										break;
									}
								}
								PrintHelper.selectedImage = tempItem;
								if (needAddGroup){
									tempList.add(tempItem);
									childItemList.add(childItemList.size()-1,tempList);
									groupItemList.add(groupItemList.size()-1,tempItem.name);
								}else {
									tempList= new ArrayList<ProductInfo>();
									for (int j = 0;j <childItemList.size();j++){
										if (!isAddedChild){
											tempList = childItemList.get(j);
											for (int k = 0;k < tempList.size(); k++){
												if (!childItemList.get(j).get(k).productType.equals(AppConstants.PRINT_TYPE)){
													continue;
												}
												if (tempList.get(k).descriptionId.equals(tempItem.descriptionId)){
													childItemList.get(j).add(tempItem);
													isAddedChild = true;
													//remove the children item which is repeated
									  				if (tempList.size() >1){
														for (int m =0; m < tempList.size()-1; m++){
															if (tempList.get(m).equals(tempItem)){
																childItemList.get(j).remove(m);
																continue;
															}
														}
													}	
													break;
												}
																				
											}
										}
										
									}
								}
								
								PrintHelper.selectedImageChild = 0;
								PrintHelper.selectedImageGroup = groupItemList.size()-1;
								Canvas c = null;
								try
								{
									c = surface.getHolder().lockCanvas();
									synchronized (surface.getHolder())
									{
										//qnty.qnty.setText("" + PrintHelper.selectedImage.quantity);
										// PrintHelper.selectedImagePos++;
										surface.img = null;
									}
								}
								catch (Exception ex)
								{
									Log.e(TAG, "Error locking and updating canvas in viewcrop click listener: " + ex.getMessage());
								}
								finally
								{
									if (c != null)
									{
										surface.getHolder().unlockCanvasAndPost(c);
									}
								}
							}
							else
							{
								if (prefs.getBoolean("analytics", false))
								{
									try
									{
										PrintHelper.mTracker.trackEvent("Edit", "*Change_Size", item, 0);
										PrintHelper.mTracker.dispatch();
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}
								printSizeButton.setText(item);
								//qnty.qnty.setText("" + PrintHelper.selectedImage.quantity);
							}
							if (!haveAskedKeepSize[lastPosition])
							{
								builder.create().show();
								haveAskedKeepSize[lastPosition] = true;
							}else {
								PrintHelper.lastSelectedImage = PrintHelper.selectedImage;
							}
						}
						
						// to fresh the low res warning icon
						surface.needRefresh = true;
						surface.invalidate();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		/*qnty = (QntWidget) findViewById(R.id.qntWidget1);
		try
		{
			qnty.qnty.setText("" + PrintHelper.selectedImage.quantity);
		}
		catch (Exception ex)
		{
			qnty.qnty.setText("1");
		}*/
		rotateTV.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Canvas c = null;
				try
				{
					c = surface.getHolder().lockCanvas();
					synchronized (surface.getHolder())
					{
						surface.rotate = !surface.rotate;
					}
				}
				catch (Exception ex)
				{
					Log.e(TAG, "Error locking and updating canvas in viewcrop click listener: " + ex.getMessage());
				}
				finally
				{
					if (c != null)
					{
						surface.needRefresh=true;
						surface.getHolder().unlockCanvasAndPost(c);
					}
					surface.saveROI();
				}
				if (prefs.getBoolean("analytics", false))
				{
					try
					{
						PrintHelper.mTracker.trackEvent("Edit", "*Rotate", "", 0);
						PrintHelper.mTracker.dispatch();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
		viewCropTV.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Canvas c = null;
				try
				{
					c = surface.getHolder().lockCanvas();
					synchronized (surface.getHolder())
					{
						surface.saveROI();
						if (surface.showCropBox)
						{
							viewCropTV.setText(R.string.crop);
							viewCropTV.setCompoundDrawables(edit, null, null, null);
							surface.showCropBox = false;
							rotateTV.setVisibility(View.GONE);
						}
						else
						{
							viewCropTV.setText(R.string.preview);
							viewCropTV.setCompoundDrawables(view, null, null, null);
							surface.showCropBox = true;
							rotateTV.setVisibility(View.VISIBLE);
						}
					}
				}
				catch (Exception ex)
				{
					Log.e(TAG, "Error locking and updating canvas in viewcrop click listener: " + ex.getMessage());
				}
				finally
				{
					if (c != null)
					{
						surface.getHolder().unlockCanvasAndPost(c);
					}
				}
				if (prefs.getBoolean("analytics", false))
				{
					try
					{
						PrintHelper.mTracker.trackEvent("Edit", "Zoom_Crop", "", 0);
						PrintHelper.mTracker.dispatch();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
		done.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (surface.showLowRes && !shownLowRes)
				{
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(SingleImageEditActivity.this);
					builder.setTitle(getString(R.string.lowResWarning));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							if (surface != null && surface.showCropBox)
							{
								Canvas c = null;
								try
								{
									try
									{
										c = surface.getHolder().lockCanvas();
										synchronized (surface.getHolder())
										{
											surface.saveROI();
											surface.InterruptThread();
										}
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}
								catch (Exception ex)
								{
								}
								finally
								{
									if (c != null)
									{
										surface.getHolder().unlockCanvasAndPost(c);
									}
								}
							}
							shownLowRes = true;
							List<ProductInfo> productInfoList = new ArrayList<ProductInfo>();
							for (List<ProductInfo> temp : childItemList) {
								for (ProductInfo pro : temp) {
									productInfoList.add(pro);
								}
							}
							
							appContex.setProductInfos(productInfoList);
							appContex.setChildItemList(childItemList);
							appContex.setGroupItemList(groupItemList);
							dialog.dismiss();
							//PrintHelper.selectedImage.quantity = Integer.parseInt(qnty.qnty.getText().toString());
							Intent intent = new Intent(SingleImageEditActivity.this, ShoppingCartActivity.class);
							startActivity(intent);
							finish();
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
				else
				{
					if (surface != null && surface.showCropBox)
					{
						Canvas c = null;
						try
						{
							try
							{
								c = surface.getHolder().lockCanvas();
								synchronized (surface.getHolder())
								{
									surface.saveROI();
									surface.InterruptThread();
								}
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}
						}
						catch (Exception ex)
						{
						}
						finally
						{
							if (c != null)
							{
								surface.getHolder().unlockCanvasAndPost(c);
							}
						}
					}
					List<ProductInfo> productInfoList = new ArrayList<ProductInfo>();
					for (List<ProductInfo> temp : childItemList) {
						for (ProductInfo pro : temp) {
							productInfoList.add(pro);
						}
					}
					
					appContex.setProductInfos(productInfoList);
					appContex.setChildItemList(childItemList);
					appContex.setGroupItemList(groupItemList);
					//PrintHelper.selectedImage.quantity = Integer.parseInt(qnty.qnty.getText().toString());
					Intent intent = new Intent(SingleImageEditActivity.this, ShoppingCartActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});
		/*
		 * Bitmap img = null; BitmapFactory.Options options = new Options();
		 * Bitmap bit = null; options.inJustDecodeBounds = true;
		 * BitmapFactory.decodeFile(PrintHelper.selectedImage, options); int
		 * origW = options.outWidth; int origH = options.outHeight;
		 * options.inJustDecodeBounds = true;
		 */
		// img = PrintHelper.loadThumbnailImage(PrintHelper.selectedImage,
		// MediaStore.Images.Thumbnails.MINI_KIND, options, this);
		//
		//
		// PrintHelper.CalculateDefaultRoi(1.0*options.outWidth,1.0*options.outHeight,6.0/4.0);
		//
		// int x = (int)PrintHelper.roiX;
		// int y = (int)PrintHelper.roiY;
		// int width = (int)PrintHelper.roiWidth;
		// int height = (int)PrintHelper.roiHeight;
		//
		// options.inJustDecodeBounds = false;
		//
		// img = PrintHelper.loadThumbnailImage(PrintHelper.selectedImage,
		// MediaStore.Images.Thumbnails.MINI_KIND, options, this);
		//
		if(photoInfo != null && !photoInfo.getPhotoSource().isFromPhone() && ImageUtil.getBitmapOfPhotoInfo(photoInfo, this)==null){
			new DownloadImage(this).execute(photoInfo);
		}
	}

	@Override
	public void onPause()
	{
		mImageSelectionDatabase.close();
		super.onPause();
	}

	@Override
	public void onResume()
	{
		if (prefs.getBoolean("analytics", false))
		{
			try
			{
				if (PrintHelper.wififlow)
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Wifi_At_Kiosk", 3);
				}
				else
				{
					PrintHelper.mTracker.setCustomVar(2, "Workflow", "Prints_To_Store", 3);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		shownLowRes = false;
		try
		{
			mImageSelectionDatabase.open();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		boolean showCrop = getIntent().getBooleanExtra("EditMode", false);
		Log.w(TAG, "EditMode: " + showCrop);
		if (showCrop)
		{
			viewCropTV.setText(R.string.preview);
			viewCropTV.setCompoundDrawables(view, null, null, null);
			surface.showCropBox = true;
			rotateTV.setVisibility(View.VISIBLE);
		}
		else
		{
			viewCropTV.setText(R.string.crop);
			viewCropTV.setCompoundDrawables(edit, null, null, null);
			surface.showCropBox = false;
			rotateTV.setVisibility(View.INVISIBLE);
		}
		if (PrintHelper.isLowResWarning(PrintHelper.selectedImage))
		{
			surface.showLowRes = true;
		}
		else
		{
			surface.showLowRes = false;
		}
		for (int i = 0; i < haveAskedKeepSize.length; i++)
		{
			haveAskedKeepSize[i] = false;
		}
		String item = printSizeButton.getText().toString();
		for (int i = 0; i < PrintHelper.products.size(); i++)
		{
			if (PrintHelper.products.get(i).getShortName().equals(item))
			{
				lastPosition = i;
			}
		}
		super.onResume();
	}
	
	class DownloadImage extends AsyncTask<PhotoInfo, Void, Void>{
		
		private Context mContext;
		public DownloadImage(Context context){
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {
			printSizeButton.setEnabled(false);
			rotateTV.setEnabled(false);
			viewCropTV.setEnabled(false);
			if(pbWaiting != null){
				pbWaiting.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Void doInBackground(PhotoInfo... params) {
			ImageUtil.downloadUrlToStream(params[0], mContext);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			printSizeButton.setEnabled(true);
			rotateTV.setEnabled(true);
			viewCropTV.setEnabled(true);
			pbWaiting.setVisibility(View.GONE);
		}
		
	}
	@Override
	public void onBackPressed() {
		
	}
}
