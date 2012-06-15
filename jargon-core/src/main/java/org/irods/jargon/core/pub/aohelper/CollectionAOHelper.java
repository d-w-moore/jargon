/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper clas to support the {@link org.irods.jargon.core.pub.CollectionAO}.
 * This class is primarily for internal use, but does provide helpful methods
 * when creating extensions that work with iRODS collections.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class CollectionAOHelper extends AOHelper {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionAOHelper.class);

	/**
	 * Create a set of selects for a collection, used in general query
	 * 
	 * @return <code>String</code> with select statements for the domain object.
	 */
	public static String buildSelects() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_COLL_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_ZONE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_MAP_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INHERITANCE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_COMMENTS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INFO1.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_INFO2.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_TYPE.getName());
		return query.toString();
	}

	/**
	 * Build a set of selects for collection metadata. This method does not add
	 * the "select" statement or any trailing delimiter, and is handy when you
	 * want to tack a metadata selection onto the end of a query.
	 * 
	 * @return <code>String</code> with metadata select fragment.
	 */
	public static String buildMetadataSelects() {
		final StringBuilder query = new StringBuilder();
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS.getName());
		return query.toString();
	}

	/**
	 * Return a <code>Collection</code> domain object given a result row from a
	 * query
	 * 
	 * @param row
	 *            {@link org.irods.jargon.core.query.IRODSQueryResultRow}
	 *            containing the result of a query
	 * @return {@link org.irods.jargon.pub.domain.Collection} that represents
	 *         the data in the row.
	 * @throws JargonException
	 */
	public static Collection buildCollectionFromResultSetRow(
			final IRODSQueryResultRow row) throws JargonException {
		Collection collection = new Collection();
		collection.setCollectionId(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(0)));
		collection.setCollectionName(row.getColumn(1));
		collection.setCollectionParentName(row.getColumn(2));
		collection.setCollectionOwnerName(row.getColumn(3));
		collection.setCollectionOwnerZone(row.getColumn(4));
		collection.setCollectionMapId(row.getColumn(5));
		collection.setCollectionInheritance(row.getColumn(6));
		collection.setComments(row.getColumn(7));
		collection.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(8)));
		collection.setModifiedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(9)));
		collection.setInfo1(row.getColumn(10));
		collection.setInfo2(row.getColumn(11));
		collection.setInfo2(row.getColumn(11));
		// collection.setCollectionType(row.getColumn(12));
		collection.setLastResult(row.isLastResult());

		if (log.isInfoEnabled()) {
			log.info("collection built \n");
			log.info(collection.toString());
		}

		return collection;
	}

	/**
	 * Given a set of AVU Query parameters, build the appropriate condition to
	 * add to a query
	 * 
	 * @param queryCondition
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            describes a metadata query
	 * @param queryElement
	 *            <codeStringBuilder</code> with the given AVU query in iquest
	 *            query form.
	 */
	public static StringBuilder buildConditionPart(
			final AVUQueryElement queryElement) {
		StringBuilder queryCondition = new StringBuilder();
		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {
			queryCondition.append(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		return queryCondition;
	}

	public static List<Collection> buildListFromResultSet(
			final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		final List<Collection> collections = new ArrayList<Collection>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			collections.add(buildCollectionFromResultSetRow(row));
		}

		return collections;
	}

	/**
	 * for a result set row, create a
	 * <code>CollectionAndDataObjectListingEntry</code>
	 * 
	 * @param row
	 *            <code>IRODSQueryResultRow</code> with raw data.
	 * @param parentPath
	 *            <code>String</code> with the parent path under which the
	 *            collection or data object lives
	 * @return {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws JargonException
	 */
	public static CollectionAndDataObjectListingEntry buildCollectionListEntryFromResultSetRowForCollectionQuery(
			final IRODSQueryResultRow row) throws JargonException {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(row.getColumn(0));
		entry.setObjectType(ObjectType.COLLECTION);
		entry.setPathOrName(row.getColumn(1));
		entry.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(2)));
		entry.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(3)));
		entry.setId(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row
				.getColumn(4)));
		entry.setOwnerName(row.getColumn(5));
		entry.setOwnerZone(row.getColumn(6));

		entry.setCount(row.getRecordCount());
		entry.setLastResult(row.isLastResult());

		log.debug("listing entry built {}", entry.toString());

		return entry;
	}

	/**
	 * for a result set row from a query for data objects in a collection,
	 * create a <code>CollectionAndDataObjectListingEntry</code>
	 * 
	 * @param row
	 *            <code>IRODSQueryResultRow</code> with raw data.
	 * @return {@link org.irods.jargon.core.query.CollectionAndDataObjectListingEntry}
	 * @throws JargonException
	 */
	public static CollectionAndDataObjectListingEntry buildCollectionListEntryFromResultSetRowForDataObjectQuery(
			final IRODSQueryResultRow row) throws JargonException {
		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(row.getColumn(0));
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setPathOrName(row.getColumn(1));
		entry.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(2)));
		entry.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row
				.getColumn(3)));
		entry.setId(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row
				.getColumn(4)));
		entry.setDataSize(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(5)));
		entry.setOwnerName(row.getColumn(7));
		entry.setCount(row.getRecordCount());
		entry.setLastResult(row.isLastResult());

		log.debug("listing entry built {}", entry.toString());

		return entry;
	}

	/**
	 * Shortcut to build selects used in creating
	 * <code>CollectionAndDataObjectListingEntry</code> items for collections.
	 * Does not include the 'SELECT', just the field names.
	 * 
	 * @return
	 */
	public static String buildSelectsNeededForCollectionsInCollectionsAndDataObjectsListingEntry() {
		StringBuilder query = new StringBuilder();
		query.append(RodsGenQueryEnum.COL_COLL_PARENT_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_MODIFY_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_OWNER_ZONE.getName());
		return query.toString();
	}

	/**
	 * Build a GenQuery string that will get the inheritance flag for a given
	 * collection.
	 * 
	 * @param absolutePathToCollection
	 *            <code>String</code> with the absolute path to the collection.
	 * @return <code>String</code> with the inheritance bit (1 or blank).
	 */
	public static String buildInheritanceQueryForCollectionAbsolutePath(
			final String absolutePathToCollection) {

		if (absolutePathToCollection == null
				|| absolutePathToCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToCollection");
		}

		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_COLL_INHERITANCE.getName());
		query.append(" WHERE  ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(absolutePathToCollection));
		query.append("'");
		return query.toString();
	}

	/**
	 * @param userFilePermissions
	 * @param row
	 * @param userAO
	 * @throws JargonException
	 */
	public static void buildUserFilePermissionForCollection(
			final List<UserFilePermission> userFilePermissions,
			final IRODSQueryResultRow row, final UserAO userAO,
			final String irodsAbsolutePath) 
			throws JargonException {
		
		
		String collectionZone = MiscIRODSUtils
				.getZoneInPath(irodsAbsolutePath);
		/*
		 * There appears to be a gen query issue with getting user type in the
		 * permissions query, so, unfortunately, I need to do another query to
		 * get the user type
		 */
		UserFilePermission userFilePermission;
	
		/*
		 * Gracefully ignore a not found for the user name and zone, just set
		 * the type to unknown and return what I have.
		 */
		try {
			User user = userAO
					.findByIdInZone(row.getColumn(10), collectionZone);

			userFilePermission = new UserFilePermission(row.getColumn(7),
					row.getColumn(10),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(9))),
					user.getUserType(), row.getColumn(8));

		} catch (DataNotFoundException dnf) {
			log.warn(
					"user info not found for permission for user:{}, this permission will not be added",
					row.getColumn(7));
			userFilePermission = new UserFilePermission(row.getColumn(7),
					row.getColumn(10),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(9))),
					UserTypeEnum.RODS_UNKNOWN, row.getColumn(8));
		}
		userFilePermissions.add(userFilePermission); 
	}

	/**
	 * @param userFilePermissions
	 * @param row
	 * @throws JargonException
	 */
	public static void buildUserFilePermissionForDataObject(
			final List<UserFilePermission> userFilePermissions,
			final IRODSQueryResultRow row, final String irodsAbsolutePath,
			final String currentZone) throws JargonException {

		/*
		 * There appears to be a gen query issue with getting user type in the
		 * permissions query, so, unfortunately, I need to do another query to
		 * get the user type
		 */
		UserFilePermission userFilePermission;

		userFilePermission = new UserFilePermission(row.getColumn(8),
				row.getColumn(9),
				FilePermissionEnum.valueOf(IRODSDataConversionUtil
						.getIntOrZeroFromIRODSValue(row.getColumn(10))),
				UserTypeEnum.findTypeByString(row.getColumn(11)),
				row.getColumn(12));
		userFilePermissions.add(userFilePermission);
	}

	/**
	 * Build a select for a collection ACL with the given collection absolute
	 * path
	 * 
	 * @param irodsCollectionAbsolutePath
	 */

	public static String buildACLQueryForCollectionName(
			final String irodsCollectionAbsolutePath) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ZONE.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_ID.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_COLL_ACCESS_TYPE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(" = '");
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsCollectionAbsolutePath));
		query.append("'");
		return query.toString();
	}

}
