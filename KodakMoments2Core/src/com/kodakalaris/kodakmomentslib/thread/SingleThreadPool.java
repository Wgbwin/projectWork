package com.kodakalaris.kodakmomentslib.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SingleThreadPool {
	// Be careful , the runtimeexception in thread of ExecutorService will be
	// catched auto without log!
	private ExecutorService pool;
	private List<Future<Runnable>> tasks;

	public SingleThreadPool() {
		pool = Executors.newSingleThreadExecutor();
		tasks = new ArrayList<Future<Runnable>>();
	}

	public void addHighPriorityTask(Runnable runnable) {
		clearAllTasks();
		Future<Runnable> task = (Future<Runnable>) pool.submit(runnable);
		tasks.add(task);
	}

	public void clearAllTasks() {
		if (tasks.size() > 0) {
			for (Future<Runnable> future : tasks) {
				future.cancel(true);
			}
		}
	}

	public void shutdown() {
		clearAllTasks();
		pool.shutdown();
		tasks.clear();
	}
}
