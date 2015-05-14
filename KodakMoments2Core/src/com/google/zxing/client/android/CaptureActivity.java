/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.client.android;

import java.io.IOException;
import java.util.Collection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.util.Log;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public abstract class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback
{
	private static final String TAG = CaptureActivity.class.getSimpleName();
	protected CameraManager cameraManager;
	protected CaptureActivityHandler handler;
	private Result savedResultToShow;
	protected ViewfinderView viewfinderView;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	
	/**
	 * Put up our own UI for how to handle the decoded contents.
	 * @param rawResult
	 * @param resultHandler
	 * @param barcode
	 */
	protected abstract void handleDecodeWifi(Result rawResult, ResultHandler resultHandler, Bitmap barcode);
	
	/**
	 * Content view layout id. When activity is onCreate, setContentView will call this method.
	 * @return
	 */
	protected abstract int getContentViewLayoutId();
	/**
	 * viewFinderView id
	 * @return
	 */
	protected abstract int getViewFinderViewId();
	
	/**
	 * camera preview id
	 * @return
	 */
	protected abstract int getPreviewId();

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public CameraManager getCameraManager()
	{
		return cameraManager;
	}

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(getContentViewLayoutId());
		
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		// CameraManager must be initialized here, not in onCreate(). This
		// is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if
		// we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was
		// the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());
		handler = null;
		// lastResult = null;
		viewfinderView = (ViewfinderView) findViewById(getViewFinderViewId());
		viewfinderView.setCameraManager(cameraManager);
		SurfaceView surfaceView = (SurfaceView) findViewById(getPreviewId());
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		}
		else
		{
			// Install the callback and wait for surfaceCreated() to init
			// the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		inactivityTimer.onResume();
		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
		
	}

	@Override
	protected void onPause()
	{
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface)
		{
			SurfaceView surfaceView = (SurfaceView) findViewById(getPreviewId());
			if(surfaceView!=null){
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				surfaceHolder.removeCallback(this);
			}
		}
		super.onPause();
	}
	
	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result)
	{
		// Bitmap isn't used yet -- will be used soon
		if (handler == null)
		{
			savedResultToShow = result;
		}
		else
		{
			if (result != null)
			{
				savedResultToShow = result;
			}
			if (savedResultToShow != null)
			{
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (holder == null)
		{
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode)
	{
		inactivityTimer.onActivity();
		// lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		drawResultPoints(barcode, rawResult);
		switch (source)
		{
		case NONE:
			handleDecodeWifi(rawResult, resultHandler, barcode);
			break;
		}
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	protected void drawResultPoints(Bitmap barcode, Result rawResult)
	{
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0)
		{
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2)
			{
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			}
			else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13))
			{
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			}
			else
			{
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points)
				{
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b)
	{
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}
	protected static ParsedResult parseResult(Result rawResult)
	{
		return ResultParser.parseResult(rawResult);
	}
	
	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null)
			{
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		}
		catch (IOException ioe)
		{
			Log.w(TAG,ioe);
		}
		catch (RuntimeException e)
		{
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}

	public void restartPreviewAfterDelay(long delayMS)
	{
		if (handler != null)
		{
			viewfinderView.setVisibility(View.VISIBLE);
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();
	}
	
}
