package org.irods.jargon.usertagging;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.usertagging.domain.UserTagCloudView;


/**
 * Service interface for query and processing of a user tag cloud.  Note that individual signatures may query for the tag cloud based on the logged-in user,
 * or may allow specification of a user.  The caller should be sure to ask for the correct user data.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface UserTagCloudService {

	/**
	 * Get the tag cloud for the logged-in user for data objects within iRODS.    This will return a view of tags with 0 counts for collections, and counts
	 * for the data objects.
	 * @return {@link org.irods.jargon.usertagging.domain.UserTagCloudView} with the tag cloud for the domain.
	 * @throws JargonException
	 */
	public UserTagCloudView getTagCloudForDataObjects() throws JargonException;

	/**
	 * Get the tag cloud for the logged-in user for collections within iRODS.  This will return a view of tags with 0 counts for data objects, and counts for collections.
	 * @return {@link org.irods.jargon.usertagging.domain.UserTagCloudView} with the tag cloud for the domain.
	 * @throws JargonException
	 */
	public UserTagCloudView getTagCloudForCollections() throws JargonException;

	/**
	 * Get the complete tag cloud for the logged-in user for collections and data objects in iRODS.  This method is what should typically be called to
	 * create a tag cloud, though methods are available to limit the data to either data objects or collections.
	 * @return {@link org.irods.jargon.usertagging.domain.UserTagCloudView} with the tag cloud for the files and collections.
	 * @throws JargonException
	 */
	public UserTagCloudView getTagCloud() throws JargonException;

}