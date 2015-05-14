package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WifiConnectionSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	protected String TAG = WifiConnectionSurfaceView.VIEW_LOG_TAG;
	private WifiConnectionSVThread _thread;
	String usage = "";
	Paint pnt;
	Bitmap kiosk = null;
	Bitmap phone = null;
	Bitmap background = null;
	long FPS = 30;
	Context mContext;

	public WifiConnectionSurfaceView(Context context)
	{
		super(context);
		mContext = context;
		getHolder().addCallback(this);
		_thread = new WifiConnectionSVThread(getHolder(), this);
		pnt = new Paint();
		pnt.setColor(Color.WHITE);
		pnt.setStrokeWidth(10);
		pnt.setStyle(Style.FILL_AND_STROKE);
		kiosk = BitmapFactory.decodeResource(getResources(), R.drawable.graphickiosk);
		phone = BitmapFactory.decodeResource(getResources(), R.drawable.graphicphone);
	}

	public WifiConnectionSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		getHolder().addCallback(this);
		_thread = new WifiConnectionSVThread(getHolder(), this);
	}


	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawColor(Color.rgb(38, 38, 38));
		// canvas.drawText(usage, 0, 0, pnt);
		try
		{
			for (Toaster toast : PrintHelper.toast)
			{
				toast.draw(canvas);
			}
			if (kiosk == null)
				kiosk = BitmapFactory.decodeResource(getResources(), R.drawable.graphicconnectkiosk);
			if (phone == null)
				phone = BitmapFactory.decodeResource(getResources(), R.drawable.graphictagpictures);
			try
			{
				canvas.drawBitmap(phone, 0, (canvas.getHeight() - phone.getHeight()) / 2, null);
				canvas.drawBitmap(kiosk, canvas.getWidth() - kiosk.getWidth(), (canvas.getHeight() - kiosk.getHeight()) / 2, null);
			}
			catch (Exception ex)
			{
			}
		}
		catch (Exception ex)
		{
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!_thread.isAlive())
		{
			_thread = new WifiConnectionSVThread(getHolder(), this);
		}
		_thread.setRunning(true);
		_thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		boolean retry = true;
		_thread.setRunning(false);
		while (retry)
		{
			try
			{
				_thread.join();
				retry = false;
			}
			catch (InterruptedException e)
			{
				// we will try it again and again...
			}
		}
	}

	public float touched_x = 0.0f;
	public float touched_y = 0.0f;
	public boolean touched = false;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		touched_x = event.getX();
		touched_y = event.getY();
		int action = event.getAction();
		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			touched = false;
			break;
		case MotionEvent.ACTION_MOVE:
			touched = false;
			break;
		case MotionEvent.ACTION_UP:
			touched = true;
			break;
		case MotionEvent.ACTION_CANCEL:
			touched = false;
			break;
		case MotionEvent.ACTION_OUTSIDE:
			touched = true;
			break;
		default:
		}
		return true; // processed
	}

	class WifiConnectionSVThread extends Thread
	{
		private SurfaceHolder _surfaceHolder;
		private WifiConnectionSurfaceView _panel;
		private boolean _run = false;

		public WifiConnectionSVThread(SurfaceHolder surfaceHolder, WifiConnectionSurfaceView panel)
		{
			_surfaceHolder = surfaceHolder;
			_panel = panel;
		}

		public void setRunning(boolean run)
		{
			_run = run;
		}

		@Override
		public void run()
		{
			long ticksPS = 1000 / FPS;
			long startTime;
			long sleepTime;
			// fps checker
			long contms = 0;
			long lasttimecheck = System.currentTimeMillis();
			// int fps = 0;
			Canvas c;
			while (_run)
			{
				long time = System.currentTimeMillis();
				if (contms > 1000)
				{
					// Log.v("FPS",String.valueOf(fps));
					contms = time - lasttimecheck;
					// fps = 1;
				}
				else
				{
					// fps++;
					contms += time - lasttimecheck;
				}
				lasttimecheck = time;
				c = null;
				startTime = time;
				try
				{
					c = _surfaceHolder.lockCanvas(null);
					synchronized (_surfaceHolder)
					{
						try
						{
						_panel.onDraw(c);
							for (Toaster s : PrintHelper.toast)
							{
								s.move();
								if ((s.getX()) >= c.getWidth())
								{
									s.setX((int) (-(700 * Math.random()) % 800));
								}
							}
						}
						catch (Exception ex)
						{
						}
					}
				}
				finally
				{
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null)
					{
						_surfaceHolder.unlockCanvasAndPost(c);
					}
					sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
					try
					{
						if (sleepTime > 10)
							sleep(sleepTime);
						else
						{
							// Log.w("LOWFPS",String.valueOf(contms));
							sleep(10);
						}
					}
					catch (Exception ex)
					{
						Log.e(TAG, ex.getMessage().toString());
					}
				}
			}
		}
	}
}
