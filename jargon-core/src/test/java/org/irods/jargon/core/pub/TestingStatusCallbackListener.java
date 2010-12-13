package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;

public class TestingStatusCallbackListener implements
		TransferStatusCallbackListener {
	
	
	private int successCallbackCount = 0;
	private int errorCallbackCount = 0;
	private String lastSourcePath = "";
	private String lastTargetPath = "";
	private String lastResource = "";

	@Override
	public void statusCallback(TransferStatus transferStatus)
			throws JargonException {
		
		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			errorCallbackCount++;
		} else {
			successCallbackCount++;
		}
		
		
		lastSourcePath = transferStatus.getSourceFileAbsolutePath();
		lastTargetPath = transferStatus.getTargetFileAbsolutePath();
		lastResource = transferStatus.getTargetResource();

	}

	public int getSuccessCallbackCount() {
		return successCallbackCount;
	}

	public void setSuccessCallbackCount(int successCallbackCount) {
		this.successCallbackCount = successCallbackCount;
	}

	public int getErrorCallbackCount() {
		return errorCallbackCount;
	}

	public void setErrorCallbackCount(int errorCallbackCount) {
		this.errorCallbackCount = errorCallbackCount;
	}

	public String getLastSourcePath() {
		return lastSourcePath;
	}

	public String getLastTargetPath() {
		return lastTargetPath;
	}

	public String getLastResource() {
		return lastResource;
	}

}
