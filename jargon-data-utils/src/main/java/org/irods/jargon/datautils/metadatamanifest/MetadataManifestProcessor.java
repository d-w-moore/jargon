package org.irods.jargon.datautils.metadatamanifest;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkAVUOperationResponse;

public interface MetadataManifestProcessor {

	/**
	 * Convert a <code>MetadataManifest</code> to string-ified json
	 *
	 * @param metadataManifest
	 *            {@link MetadataManifest}
	 * @return {@code String} containing JSON
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	String metadataManifestToJson(MetadataManifest metadataManifest) throws JargonException;

	/**
	 * Convert a json string to a <code>MetadataManifest</code>
	 *
	 * @param jsonString
	 *            {@code String} containing json
	 * @return {@link MetadataManifest}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	MetadataManifest stringJsonToMetadataManifest(String jsonString) throws JargonException;

	List<BulkAVUOperationResponse> processManifest(MetadataManifest metadataManifest) throws JargonException;

}