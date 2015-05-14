package com.kodak.rss.tablet.util.load;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.AsyncTask;

public final class LoaderUtil {
		
	private static volatile Executor executor;
	private static final int DEFAULT_CORE_POOL_SIZE = 3;//5
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 6;//128
	private static final int DEFAULT_KEEP_ALIVE = 5;//1
	private static final Object LOCK = new Object();
	private static final BlockingQueue<Runnable> DEFAULT_WORK_QUEUE = new LinkedBlockingQueue<Runnable>(DEFAULT_MAXIMUM_POOL_SIZE);
	private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
		private final AtomicInteger counter = new AtomicInteger(0);

		public Thread newThread(Runnable runnable) {
			return new Thread(runnable, " :" + counter.incrementAndGet());
		}
	};

	public static boolean isNullOrEmpty(String s) {
		return (s == null) || (s.length() == 0);
	}

	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
		}
	}

	public static void disconnectQuietly(URLConnection connection) {
		if (connection instanceof HttpURLConnection) {
			((HttpURLConnection) connection).disconnect();
		}
	}

	public static void clearCaches(Context context) {
		ImageDownloader.clearCache(context);
	}
	
	public static void clearCaches(Context context,String folderPath) {
		ImageDownloader.clearCache(context,folderPath);
	}

	public static void deleteDirectory(File directoryOrFile) {
		if (!directoryOrFile.exists()) {
			return;
		}

		if (directoryOrFile.isDirectory()) {
			for (File child : directoryOrFile.listFiles()) {
				deleteDirectory(child);
			}
		}
		directoryOrFile.delete();
	}

	public static Executor getExecutor() {
		synchronized (LOCK) {
			if (LoaderUtil.executor == null) {
				Executor executor = getAsyncTaskExecutor();
				if (executor == null) {
					executor = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
							DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVE,
							TimeUnit.SECONDS, DEFAULT_WORK_QUEUE,
							DEFAULT_THREAD_FACTORY);
				}
				LoaderUtil.executor = executor;
			}
		}
		return LoaderUtil.executor;
	}
	
	private static Executor getAsyncTaskExecutor() {
		Field executorField = null;
		try {
			executorField = AsyncTask.class.getField("THREAD_POOL_EXECUTOR");
		} catch (NoSuchFieldException e) {
			return null;
		}

		Object executorObject = null;
		try {
			executorObject = executorField.get(null);
		} catch (IllegalAccessException e) {
			return null;
		}

		if (executorObject == null) {
			return null;
		}

		if (!(executorObject instanceof Executor)) {
			return null;
		}

		return (Executor) executorObject;
	}

}
