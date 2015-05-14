package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.provider.MediaStore;
import android.util.Log;

public class Toaster
{
	private int id;
	private Bitmap img;
	private int x;
	private int y;
	private int dx;
	private int dy;
	private Context mContext;
	BitmapFactory.Options options = new BitmapFactory.Options();

	public Toaster(Context context, int id)
	{
		mContext = context;
		this.id = id;
		x = (int) -((700 * Math.random()) % 800);
		y = 110;
		dx = 2;
		dy = 0;
		boolean failure = false;
		try
		{
			Bitmap tempImg = null;
			// If setup database failed, we should just try and get images out
			// of the uriEncodedPaths
			if (PrintHelper.allUriEncodedPaths != null && PrintHelper.allUriEncodedPaths.size() > 0)
			{
				try
				{
					tempImg = PrintHelper.loadThumbnailImage(PrintHelper.allUriEncodedPaths.get((int) (Math.random() * PrintHelper.allUriEncodedPaths.size())), MediaStore.Images.Thumbnails.MICRO_KIND, null, context);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			// if setup database failed and we have a "last viewed album list"
			else if (PrintHelper.uriEncodedPaths != null && PrintHelper.uriEncodedPaths.size() >= 0)
			{
				try
				{
					tempImg = PrintHelper.loadThumbnailImage(PrintHelper.uriEncodedPaths.get((int) (Math.random() * PrintHelper.allUriEncodedPaths.size())), MediaStore.Images.Thumbnails.MICRO_KIND, null, context);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			// if setup database failed and the user didn't look at any images,
			// just use default image to show progress...right now the app icon
			else
			{
			/*	try
				{
					tempImg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}*/
				if (img == null || failure)
				{
					img = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
				}
			}
			final float scale = mContext.getResources().getDisplayMetrics().density;
			int pixels = 0;
			pixels = (int) (PrintHelper.thumbnailSize * scale + 0.5f);
			img = Bitmap.createScaledBitmap(tempImg, pixels, pixels, true);
		}
		catch (Exception ex)
		{
			failure = true;
			ex.printStackTrace();
			Log.e("Toaster", "problem setting image");
		}
		if (img == null || failure)
		{
			img = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
		}
	}

	public Bitmap getImg()
	{
		return img;
	}

	public void setImg(Bitmap img)
	{
		this.img = img;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getDx()
	{
		return dx;
	}

	public void setDx(int dx)
	{
		this.dx = dx;
	}

	public int getDy()
	{
		return dy;
	}

	public void setDy(int dy)
	{
		this.dy = dy;
	}

	protected int getId()
	{
		return id;
	}

	protected void setId(int id)
	{
		this.id = id;
	}

	// the draw method which draws the corresponding frame
	public void draw(Canvas canvas)
	{
		canvas.drawBitmap(img, x, y, null);
	}

	public void move()
	{
		this.x += this.dx;
		this.y += this.dy;
	}

	public void newImage()
	{
		try
		{
			setImg(PrintHelper.loadThumbnailImage(PrintHelper.allUriEncodedPaths.get((int) (Math.random() * PrintHelper.allUriEncodedPaths.size())), MediaStore.Images.Thumbnails.MICRO_KIND, null, mContext));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log.e("Toaster", "problem setting image");
		}
	}
}
