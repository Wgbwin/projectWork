package com.kodakalaris.video.roifacedetect;

import java.util.Stack;

import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;

public class FaceDetectorThreadPool extends Thread {
	private static final String TAG = FaceDetectorThreadPool.class.getSimpleName();
	
	private Stack<FaceDetectorThread> mStack;
	
	private FaceDetectorThread mCurrentThread;
	
	public FaceDetectorThreadPool() {
		mStack = new Stack<FaceDetectorThread>();
	}
	
	@Override
	public void run() {
		while (!mStack.isEmpty()) {
			mCurrentThread = mStack.pop();
			if (mCurrentThread != null) {
				mCurrentThread.start();
				try {
					mCurrentThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addImg(int position, String filePath, Vignette vignette, FinishedFindingFacesEvent e) {
		FaceDetectorThread thread = new FaceDetectorThread(null);
		thread.setId(position);
		thread.setImagePath(filePath);
		thread.setFinishEvent(e);
		addThread(thread);
	}
	
	public void addThread(FaceDetectorThread thread) {
		synchronized (mStack) {
			if (mCurrentThread != null && !mCurrentThread.isInterrupted() && mCurrentThread.equals(thread) ) {
				mCurrentThread.interrupt();
			}
			
			boolean added = false;
			for (int i = 0; i < mStack.size(); i++) {
				FaceDetectorThread t = mStack.get(i);
				if (thread.equals(t)) {
					mStack.set(i, thread);
					added = true;
					break;
				}
			}
			
			if (!added) {
				mStack.push(thread);
			}
		}
	}
	
}
