/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default transfer control block that will by default return true for every
 * filter operation. This control block is used to communicate between a process
 * requesting a transfer, and the actual transfer process. When recursively
 * doing operations such as a put or a get, there is a need to handle restarts,
 * and this object contains a method that can be overridden to filter which
 * files need to be transferred.
 * 
 * This class also contains a shared value that can be used to set a cancel in a
 * recursive transfer operation.
 * 
 * This implementation will cancel a transfer if the current errors exceeds the
 * maximum error threshold.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultTransferControlBlock implements TransferControlBlock {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultTransferControlBlock.class);

	private final String restartAbsolutePath;
	private boolean cancelled = false;
	private boolean restartHit = false;
	private boolean paused = false;
	private int maximumErrorsBeforeCanceling = MAX_ERROR_DEFAULT;
	private int errorCount = 0;
	private int totalFilesToTransfer = 0;
	private int totalFilesTransferredSoFar = 0;

	/**
	 * Initializer that takes a restart path. This will be ignored if blank or
	 * null.
	 * 
	 * @param restartAbsolutePath
	 *            <code>String</code> with a restart path. This may be set to
	 *            blank or null if restarts are not desired.
	 * @param maxErrorsBeforeCancelling
	 *            <code>int</code> with the maximum errors to tolerate before
	 *            transfer is canceled. A value of -1 indicates that errors will
	 *            be ignored.
	 * @return instance of <code>DefaultTransferControlBlock</code>
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance(
			final String restartAbsolutePath,
			final int maxErrorsBeforeCancelling) throws JargonException {
		return new DefaultTransferControlBlock(restartAbsolutePath,
				maxErrorsBeforeCancelling);
	}

	/**
	 * Initializer that takes a restart path, and a max errors before cancel.
	 * The restart path will be ignored if blank or null.
	 * 
	 * @param restartAbsolutePath
	 *            <code>String</code> with a restart path. This may be set to
	 *            blank or null if restarts are not desired.
	 * @param maxErrors
	 * @return instance of <code>DefaultTransferControlBlock</code>
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance(
			final String restartAbsolutePath) throws JargonException {
		return new DefaultTransferControlBlock(restartAbsolutePath,
				MAX_ERROR_DEFAULT);
	}

	/**
	 * Initializer that will have no restart path.
	 * 
	 * @return
	 * @throws JargonException
	 */
	public final static TransferControlBlock instance() throws JargonException {
		return new DefaultTransferControlBlock(null, MAX_ERROR_DEFAULT);
	}

	private DefaultTransferControlBlock(final String restartAbsolutePath,
			final int maximumErrorsBeforeCancelling) throws JargonException {

		if (maximumErrorsBeforeCancelling < -1) {
			throw new JargonException(
					"maximumErrorsBeforeCancelling must be >= -1");
		}
		this.maximumErrorsBeforeCanceling = maximumErrorsBeforeCancelling;
		this.restartAbsolutePath = restartAbsolutePath;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#filter(java.lang.
	 * String)
	 */
	@Override
	public synchronized boolean filter(final String absolutePath)
			throws JargonException {

		/*
		 * this simple filter looks for a match on the restart value (last good
		 * file). When it hits this file, then any subsequent files are
		 * transmitted.
		 */

		log.info("filtering: {}", absolutePath);

		if (restartAbsolutePath == null || restartAbsolutePath.isEmpty()) {
			log.info("no filter");
			return true;
		}

		if (restartHit) {
			log.info("filter passes");
			return true;
		}

		if (absolutePath.equals(restartAbsolutePath)) {
			log.info("hit the restart path");
			restartHit = true;
			// will be true for the next file
			return false;
		}

		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		synchronized (this) {
			return cancelled;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setCancelled(boolean)
	 */
	@Override
	public void setCancelled(final boolean cancelled) {
		log.info("setting this transfer to a cancelled value of: {}", cancelled);
		synchronized (this) {
			this.cancelled = cancelled;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#isPaused()
	 */
	@Override
	public boolean isPaused() {
		synchronized (this) {
			return paused;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setPaused(boolean)
	 */
	@Override
	public void setPaused(final boolean paused) {
		synchronized (this) {
			this.paused = paused;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * getMaximumErrorsBeforeCanceling()
	 */
	@Override
	public int getMaximumErrorsBeforeCanceling() {
		synchronized (this) {
			return this.maximumErrorsBeforeCanceling;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * setMaximumErrorsBeforeCanceling(int)
	 */
	@Override
	public void setMaximumErrorsBeforeCanceling(
			final int maximumErrorsBeforeCanceling) throws JargonException {
		if (maximumErrorsBeforeCanceling < -1) {
			throw new JargonException(
					"maximumErrorsBeforeCancelling must be >= -1");
		}

		synchronized (this) {
			this.maximumErrorsBeforeCanceling = maximumErrorsBeforeCanceling;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferControlBlock#getErrorCount()
	 */
	@Override
	public int getErrorCount() {
		synchronized (this) {
			return errorCount;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#reportErrorInTransfer
	 * ()
	 */
	@Override
	public void reportErrorInTransfer() {
		synchronized (this) {
			errorCount++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * reportErrorAndSeeIfTransferNeedsToBeCancelled()
	 */
	@Override
	public boolean shouldTransferBeAbandonedDueToNumberOfErrors() {
		boolean cancelForErrors = false;
		synchronized (this) {
			if (maximumErrorsBeforeCanceling > 0) {
				cancelForErrors = (errorCount >= maximumErrorsBeforeCanceling);
			}
		}
		return cancelForErrors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#getTotalFilesToTransfer
	 * ()
	 */
	@Override
	public int getTotalFilesToTransfer() {
		synchronized (this) {
			if (shouldTransferBeAbandonedDueToNumberOfErrors()) {
				log.warn("cancelling transfer due to error threshold");
				setCancelled(true);
			}
			return totalFilesToTransfer;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferControlBlock#setTotalFilesToTransfer
	 * (int)
	 */
	@Override
	public void setTotalFilesToTransfer(final int totalFilesToTransfer) {
		synchronized (this) {
			this.totalFilesToTransfer = totalFilesToTransfer;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * getTotalFilesTransferredSoFar()
	 */
	@Override
	public int getTotalFilesTransferredSoFar() {
		synchronized (this) {
			return totalFilesTransferredSoFar;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.transfer.TransferControlBlock#
	 * incrementFilesTransferredSoFar()
	 */
	@Override
	public void incrementFilesTransferredSoFar() {
		synchronized (this) {
			totalFilesTransferredSoFar++;
		}
	}

}
