package com.kodakalaris.kodakmomentslib.bean;

import com.kodakalaris.kpp.IKPPCommandCallback;
import com.kodakalaris.kpp.KPPAsset;
import com.kodakalaris.kpp.PrintJobInfo;
import com.kodakalaris.kpp.PrinterInfo;

public class SimpleKPPCommandCallback implements IKPPCommandCallback{

	@Override
	public void onPrintJobCreated(String jobId) {
		
	}

	@Override
	public void onCreatePrintJobFailed(String errMsg) {
		
	}

	@Override
	public void onPrintJobCancelled(String jobId) {
		
	}

	@Override
	public void onCancelPrintJobFailed(String jobId, String errMsg) {
		
	}

	@Override
	public void onUploadAssetDone(KPPAsset asset) {
		
	}

	@Override
	public void onUploadProgress(KPPAsset asset, int progress) {
		
	}

	@Override
	public void onUploadAssetFailed(KPPAsset asset, String errMsg) {
		
	}

	@Override
	public void onFinishUploadDone(int queuedJobs) {
		
	}

	@Override
	public void onFinishUploadFailed(String errMsg) {
		
	}

	@Override
	public void onGetJobInfoFailed(String errMsg) {
		
	}

	@Override
	public void onGetJobInfoDone(PrintJobInfo jobInfo) {
		
	}

	@Override
	public void onGetPrinterInfoDone(PrinterInfo printer) {
		
	}

	@Override
	public void onGetPrinterInfoFailed(String errMsg) {
		
	}

}
