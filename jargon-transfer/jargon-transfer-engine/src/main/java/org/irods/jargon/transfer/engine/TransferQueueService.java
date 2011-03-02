package org.irods.jargon.transfer.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.TransferDAOManager;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.irods.jargon.transfer.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the transfer queue and display of status of transfers. This
 * thread-safe object is meant to be a singleton and manages processing of a
 * transfer queue on behalf of the <code>TransferManager</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferQueueService {

	private final Logger log = LoggerFactory
			.getLogger(TransferQueueService.class);

	private final TransferDAOManager transferDAOMgr = TransferDAOManager
			.getInstance();

	/**
	 * @throws JargonException
	 */
	public TransferQueueService() {
		super();
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public LocalIRODSTransfer dequeueTransfer() throws JargonException {
		log.debug("entering dequeueTransfer()");
		LocalIRODSTransfer transfer = null;
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferState(TransferState.ENQUEUED,
							TransferState.PROCESSING, TransferState.PAUSED);
			if (localIRODSTransferList != null
					& localIRODSTransferList.size() > 0) {
				transfer = localIRODSTransferList.get(0);
				log.debug("dequeue transfer:{}", transfer);

				transfer.setTransferStart(new Date());
				transfer.setTransferState(TransferState.PROCESSING);
				transfer.setTransferStatus(TransferStatus.OK);

				localIRODSTransferDAO.save(transfer);
			}
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("dequeued");
		return transfer;
	}

	/**
	 * 
	 * @param localSourceAbsolutePath
	 * @param targetIRODSAbsolutePath
	 * @param targetResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	public LocalIRODSTransfer enqueuePutTransfer(
			final String localSourceAbsolutePath,
			final String targetIRODSAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException {
		log.debug("entering enqueuePutTransfer()");

		if (localSourceAbsolutePath == null
				|| localSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"localSourceAbsolutePath is null or empty");
		}

		if (targetIRODSAbsolutePath == null
				|| targetIRODSAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetIRODSAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue put transfer from local source: {}",
				localSourceAbsolutePath);
		log.info("   target iRODS path: {}", targetIRODSAbsolutePath);
		log.info("   target resource:{}", targetResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(targetIRODSAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(localSourceAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(targetResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.PUT);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			log.info("saving...{}", enqueuedTransfer);
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * 
	 * @param irodsSourceAbsolutePath
	 * @param targetLocalAbsolutePath
	 * @param sourceResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	public LocalIRODSTransfer enqueueGetTransfer(
			final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String sourceResource,
			final IRODSAccount irodsAccount) throws JargonException {
		log.debug("entering enqueueGetTransfer()");

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetLocalAbsolutePath == null
				|| targetLocalAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetLocalAbsolutePath is null or empty");
		}

		if (sourceResource == null) {
			throw new JargonException(
					"sourceResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue get transfer from irods source: {}",
				irodsSourceAbsolutePath);
		log.info("   target local path: {}", targetLocalAbsolutePath);
		log.info("   target resource:{}", sourceResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsSourceAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath(targetLocalAbsolutePath);
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(sourceResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.GET);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			log.info("saving...{}", enqueuedTransfer);
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param transferManager
	 * @throws JargonException
	 */
	public void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final TransferManager transferManager) throws JargonException {
		markTransferAsErrorAndTerminate(localIRODSTransfer, null,
				transferManager);
	}

	/**
	 * 
	 * @param localIRODSTransfer
	 * @param errorException
	 * @param transferManager
	 * @throws JargonException
	 */
	public void markTransferAsErrorAndTerminate(
			final LocalIRODSTransfer localIRODSTransfer,
			final Exception errorException,
			final TransferManager transferManager) throws JargonException {

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer mergedTransfer = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());

			mergedTransfer.setTransferEnd(new Date());
			mergedTransfer.setTransferState(TransferState.COMPLETE);
			mergedTransfer.setTransferStatus(TransferStatus.ERROR);

			if (errorException != null) {
				log.warn("setting global exception to:{}", errorException);
				mergedTransfer.setGlobalException(errorException.getMessage());
			}

			log.info("saving as error{}", mergedTransfer);
			localIRODSTransferDAO.save(mergedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/**
	 * 
	 * @param countOfEntriesToShow
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getLastNInQueue(
			final int countOfEntriesToShow) throws JargonException {
		log.debug("entering getLastNInQueue(int countOfEntriesToShow)");
		if (countOfEntriesToShow <= 0) {
			throw new JargonException("must show at least 1 entry");
		}

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findAllSortedDesc(80);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getCurrentQueue() throws JargonException {
		log.debug("entering getCurrentQueue()");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferState(80, TransferState.ENQUEUED,
							TransferState.PROCESSING, TransferState.PAUSED);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getErrorQueue() throws JargonException {
		log.debug("entering getErrorQueue()");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferStatus(80, TransferStatus.ERROR);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getWarningQueue() throws JargonException {
		log.debug("entering getWarningQueue()");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			List<LocalIRODSTransfer> localIRODSTransferList = localIRODSTransferDAO
					.findByTransferStatus(80, TransferStatus.WARNING);
			return localIRODSTransferList;
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> showErrorTransfers() throws JargonException {
		return getErrorQueue();
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> showWarningTransfers()
			throws JargonException {
		return getWarningQueue();
	}

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransfer> getRecentQueue() throws JargonException {
		return getLastNInQueue(80);
	}

	/**
	 * 
	 * @throws JargonException
	 */
	public void purgeQueue() throws JargonException {
		log.debug("entering purgeQueue()");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			log.info("purging the queue of all items (except a processing item");
			localIRODSTransferDAO.purgeQueue();
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @throws JargonException
	 */
	public void purgeSuccessful() throws JargonException {
		log.info("purging the queue of all complete items");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			log.info("purging the queue of all items (except a processing item");
			localIRODSTransferDAO.purgeSuccessful();
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer localIRODSTransfer = localIRODSTransferDAO
					.findById(localIRODSTransferId);
			return new ArrayList<LocalIRODSTransferItem>(
					localIRODSTransfer.getLocalIRODSTransferItems());
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @param localIRODSTransferId
	 * @return
	 * @throws JargonException
	 */
	public List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		log.debug("entering getAllTransferItemsForTransfer(Long localIRODSTransferId)");
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer localIRODSTransfer = localIRODSTransferDAO
					.findById(localIRODSTransferId, true);
			return new ArrayList<LocalIRODSTransferItem>(
					localIRODSTransfer.getLocalIRODSTransferItems());
		} catch (TransferDAOException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}
		log.info("restarting a transfer:{}", localIRODSTransfer);
		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer txfrToUpdate = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			log.info("beginning tx to store status of this transfer ");
			log.info(">>>>restart last successful path:{}",
					txfrToUpdate.getLastSuccessfulPath());
			txfrToUpdate.setTransferStatus(TransferStatus.OK);
			txfrToUpdate.setTransferState(TransferState.ENQUEUED);
			txfrToUpdate.setGlobalException("");
			txfrToUpdate.setGlobalExceptionStackTrace("");
			localIRODSTransferDAO.save(txfrToUpdate);
			log.info("status reset and enqueued for restart");
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}
	}

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer");
		}

		log.info("restarting a transfer:{}", localIRODSTransfer);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer txfrToUpdate = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			Set<LocalIRODSTransferItem> items = txfrToUpdate
					.getLocalIRODSTransferItems();

			LocalIRODSTransferItemDAO localIRODSTransferItemDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferItemDAO();
			localIRODSTransferItemDAO.delete(items
					.toArray(new LocalIRODSTransferItem[items.size()]));

			txfrToUpdate.setTransferStatus(TransferStatus.OK);
			txfrToUpdate.setTransferState(TransferState.ENQUEUED);
			txfrToUpdate.setLastSuccessfulPath("");

			localIRODSTransferDAO.save(txfrToUpdate);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/**
	 * 
	 * @param irodsAbsolutePath
	 * @param targetResource
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	public LocalIRODSTransfer enqueueReplicateTransfer(
			final String irodsAbsolutePath, final String targetResource,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set as blank if not used");
		}

		if (irodsAccount == null) {
			throw new JargonException("null irodsAccount");
		}

		log.info("enqueue replicate transfer from iRODS: {}", irodsAbsolutePath);
		log.info("   target resource:{}", targetResource);

		LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
		enqueuedTransfer.setCreatedAt(new Date());
		enqueuedTransfer.setIrodsAbsolutePath(irodsAbsolutePath);
		enqueuedTransfer.setLocalAbsolutePath("");
		enqueuedTransfer.setTransferHost(irodsAccount.getHost());
		enqueuedTransfer.setTransferPort(irodsAccount.getPort());
		enqueuedTransfer.setTransferResource(targetResource);
		enqueuedTransfer.setTransferZone(irodsAccount.getZone());
		enqueuedTransfer.setTransferStart(new Date());
		enqueuedTransfer.setTransferType(TransferType.REPLICATE);
		enqueuedTransfer.setTransferUserName(irodsAccount.getUserName());
		enqueuedTransfer.setTransferPassword(HibernateUtil
				.obfuscate(irodsAccount.getPassword()));
		enqueuedTransfer.setTransferState(TransferState.ENQUEUED);
		enqueuedTransfer.setTransferStatus(TransferStatus.OK);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			localIRODSTransferDAO.save(enqueuedTransfer);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

		log.info("enqueued...");
		return enqueuedTransfer;

	}

	/**
	 * 
	 * @param localIRODSTransfer
	 * @throws JargonException
	 */
	public void setTransferAsCancelled(
			final LocalIRODSTransfer localIRODSTransfer) throws JargonException {

		if (localIRODSTransfer == null) {
			throw new JargonException("localIRODSTransfer is null");
		}

		log.info("cancelling a transfer:{}", localIRODSTransfer);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			LocalIRODSTransfer txfrToCancel = localIRODSTransferDAO
					.findById(localIRODSTransfer.getId());
			if (!txfrToCancel.getTransferState().equals(TransferState.COMPLETE)) {
				txfrToCancel.setTransferStatus(TransferStatus.OK);
				txfrToCancel.setTransferState(TransferState.CANCELLED);
				localIRODSTransferDAO.save(txfrToCancel);
				log.info("status set to cancelled");
			}

		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/**
	 * 
	 * @param retentionDays
	 * @throws JargonException
	 */
	public void purgeQueueBasedOnDate(final int retentionDays)
			throws JargonException {

		if (retentionDays < 0) {
			throw new JargonException("retentionDays must be 0 or greater");
		}

		log.info(
				"purging the queue of all completed or cancelled items more than {} days old",
				retentionDays);

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			localIRODSTransferDAO.purgeQueueByDate(retentionDays);
		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}

	}

	/**
	 * 
	 * @throws JargonException
	 */
	public void processQueueAtStartup() throws JargonException {
		log.info("in startup...");
		List<LocalIRODSTransfer> currentQueue = getCurrentQueue();

		if (currentQueue.isEmpty()) {
			log.info("queue is empty");
			return;
		}

		for (LocalIRODSTransfer localIrodsTransfer : currentQueue) {
			if (localIrodsTransfer.getTransferState().equals(
					TransferState.PROCESSING)) {
				log.info("resetting a processing transfer to enqueued:{}",
						localIrodsTransfer);
				resetTransferToEnqueued(localIrodsTransfer);
			}
		}

	}

	/**
	 * Reset a transfer to enqueued. Used on startup so transfers marked as
	 * processed are not treated as such during dequeue.
	 * 
	 * @param transferToReset
	 *            {@link org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer}
	 *            to be reset
	 * @throws JargonException
	 */
	private void resetTransferToEnqueued(
			final LocalIRODSTransfer transferToReset) throws JargonException {

		try {
			LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr
					.getTransferDAOBean().getLocalIRODSTransferDAO();
			transferToReset.setTransferStatus(TransferStatus.OK);
			transferToReset.setTransferState(TransferState.ENQUEUED);
			localIRODSTransferDAO.save(transferToReset);
			log.info("status set to enqueued");

		} catch (TransferDAOException e) {
			log.error("error in transaction", e);
			throw new JargonException(e);
		}
	}

}