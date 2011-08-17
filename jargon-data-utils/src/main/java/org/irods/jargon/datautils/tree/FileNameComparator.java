package org.irods.jargon.datautils.tree;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator to do case-insensitive comparison of file names.  Used to resolve collating sequence issues based on case
 * between iRODS and local file systems.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileNameComparator implements Comparator<File>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1446774734347341929L;

	/**
	 * 
	 */
	public FileNameComparator() {
		
	}

	@Override
	public int compare(File file1, File file2) {
		return file1.getName().compareToIgnoreCase( file2.getName() );
	}
}
