package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;

public class ReviewPhotoView extends ImageButton {
	private Paint paint = new Paint();
	private String productName = "";
	private PrintItem printItem;
	private Bitmap lowResIcon;
	private Context  context;
	
	public ReviewPhotoView(Context context,PrintItem printItem) {
		super(context, null, 0);
		this.context=context;
		this.printItem=printItem;
	}

	public ReviewPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		initPaint();
	}

	public ReviewPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initPaint();
	}
	
	private boolean checkIfLowResource() {
		if (printItem==null) return false;
		boolean showLowRes=false;
		double w = printItem.getRoi().w;
		double h = printItem.getRoi().h;
		if (TextUtils.isEmpty(printItem.getImage().getPhotoEditPath())) {
			String mPhotoEditPath = ImageUtil.getFilePath(context, printItem.getImage().getLocalUri());
			printItem.getImage().setPhotoEditPath((mPhotoEditPath));
		}
		int width = printItem.getEntry().proDescription.pageWidth;
		int height = printItem.getEntry().proDescription.pageHeight;
		int[] category = new int[2];
		if (w > h && width > height) {
			category[0] = width;
			category[1] = height;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		} else {
			category[0] = height;
			category[1] = width;
			showLowRes = ImageUtil.isLowResWarning(printItem.getImage(),
					category, w, h);
		}
		return showLowRes;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//canvas.drawText(productName, 10, this.getHeight() - 10, paint);
		if (checkIfLowResource()) {
			if (lowResIcon==null) {
				Bitmap src = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.icon_lowresolution);
				lowResIcon = Bitmap.createScaledBitmap(src, src.getWidth() *3/ 2,
						src.getHeight() * 3/ 2, true);
			}
			canvas.drawBitmap(lowResIcon,0, this.getHeight() -lowResIcon.getHeight(), null);
		}
	}

	public void setPrintItem(PrintItem printItem) {
		this.printItem=printItem;
		invalidate();
	}
	
	private void initPaint() {
		paint.setColor(Color.WHITE);
		paint.setTextSize(30);
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

}
