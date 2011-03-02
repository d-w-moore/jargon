package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.datautils.tree.FileTreeDiffEntry.DiffType;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileTreeDiffUtilityTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileTreeDiffUtilityTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@Test
	public void testFileTreeDiffNoDiff() throws Exception {

		String rootCollection = "testFileTreeDiffNoDiff";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);
 
		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot,
				targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndNoDiffs(childrenOfLocal, nodes);

	}

	@Test
	public void testFileTreeDiffIrodsPlusOneDir() throws Exception {

		String rootCollection = "testFileTreeDiffIrodsPlusOneDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = destFile.listFiles();
		if (children.length > 1) {
			File childFile = children[0];
			IRODSFile newChildOfChild = irodsFileFactory.instanceIRODSFile(
					childFile.getAbsolutePath(), "newChild");
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot,
				targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.RIGHT_HAND_PLUS, "newChild");
	}

	@Test
	public void testFileTreeDiffLocalPlusOneDir() throws Exception {

		String rootCollection = "testFileTreeDiffLocalPlusOneDir";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testPutThenGetMultipleCollectionsMultipleFiles", 2, 3,
						2, "testFile", ".txt", 3, 2, 1, 2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// navigate down a couple of levels and put a file somewhere
		destFile = irodsFileFactory.instanceIRODSFile(destFile
				.getAbsolutePath() + "/" + rootCollection);
		File[] children = localFile.listFiles();
		if (children.length > 1) {
			File childFile = children[0];
			File newChildOfChild = new File(childFile.getAbsolutePath(),
					"newChild");
			newChildOfChild.mkdirs();
		} else {
			Assert.fail("test setup failed, no children");
		}

		File localFileRoot = new File(localCollectionAbsolutePath);
		String targetIrodsAbsolutePath = irodsCollectionRootAbsolutePath + "/"
				+ rootCollection;

		// files now put, set up and call for the diff
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localFileRoot,
				targetIrodsAbsolutePath, 0);
		irodsFileSystem.close();
		Assert.assertNotNull("null diffModel", diffModel);
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
				.getUserObject();
		Assert.assertEquals("did not get the root no-diff entry",
				FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
				fileTreeDiffEntry.getDiffType());

		File[] childrenOfLocal = localFile.listFiles();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.LEFT_HAND_PLUS, "newChild");
	}

	@Test
	public void testFileTreeDiffTimestampAfterCutoff() throws Exception {

		String rootCollection = "testFileTreeDiffTimestampAfterCutoff";
		String testFileName1 = "testFileName1";
		String testFileName2 = "testFileName2";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsRootFile = irodsFileFactory.instanceIRODSFile(
				irodsCollectionRootAbsolutePath, rootCollection);
		irodsRootFile.mkdirs();

		File localRootFile = new File(localCollectionAbsolutePath);
		localRootFile.mkdirs();

		String absFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName1, 2);
		String absFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName2, 2);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		dataTransferOperationsAO.putOperation(new File(absFileName2),
				irodsRootFile, null, null);

		// grab the time, then put the file again after a pause so that the
		// timestamp is updated
		long cutoffTimestamp = System.currentTimeMillis();
		Thread.sleep(5000);
		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localRootFile,
				irodsRootFile.getAbsolutePath(), cutoffTimestamp);
		irodsFileSystem.close();

		File[] childrenOfLocal = localRootFile.listFiles();
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndExpectADiff(childrenOfLocal,
				nodes, DiffType.FILE_OUT_OF_SYNCH, testFileName1);
	}

	@Test
	public void testFileTreeDiffNoCutoffSet() throws Exception {

		String rootCollection = "testFileTreeDiffNoCutoffSet";
		String testFileName1 = "testFileName1";
		String testFileName2 = "testFileName2";

		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);

		IRODSFile irodsRootFile = irodsFileFactory.instanceIRODSFile(
				irodsCollectionRootAbsolutePath, rootCollection);
		irodsRootFile.mkdirs();

		File localRootFile = new File(localCollectionAbsolutePath);
		localRootFile.mkdirs();

		String absFileName1 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName1, 2);
		String absFileName2 = FileGenerator.generateFileOfFixedLengthGivenName(
				localCollectionAbsolutePath, testFileName2, 2);

		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		dataTransferOperationsAO.putOperation(new File(absFileName2),
				irodsRootFile, null, null);

		// grab the time, then put the file again after a pause so that the
		// timestamp is updated
		long cutoffTimestamp = 0;
		Thread.sleep(2000);
		dataTransferOperationsAO.putOperation(new File(absFileName1),
				irodsRootFile, null, null);
		
		FileTreeDiffUtility fileTreeDiffUtility = new FileTreeDiffUtilityImpl(irodsAccount, irodsFileSystem.getIRODSAccessObjectFactory());
		FileTreeModel diffModel = fileTreeDiffUtility.generateDiffLocalToIRODS(
				localRootFile,
				irodsRootFile.getAbsolutePath(), cutoffTimestamp);
		irodsFileSystem.close();

		File[] childrenOfLocal = localRootFile.listFiles();
		FileTreeNode fileTreeNode = (FileTreeNode) diffModel.getRoot();
		Enumeration<?> nodes = fileTreeNode.children();
		compareFileTreeToNodesForDirMatchesAndNoDiffs(childrenOfLocal, nodes);
	}

	private void compareFileTreeToNodesForDirMatchesAndNoDiffs(
			final File[] files, final Enumeration<?> nodes) {
		for (File child : files) {
			if (child.isDirectory()) {
				if (nodes.hasMoreElements()) {
					FileTreeNode fileTreeNode = (FileTreeNode) nodes
							.nextElement();
					FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
							.getUserObject();
					Assert.assertEquals("nodes out of synch", child
							.getAbsolutePath(), fileTreeDiffEntry
							.getCollectionAndDataObjectListingEntry()
							.getFormattedAbsolutePath());
					Assert.assertEquals(
							"node is not a no-diff directory entry",
							FileTreeDiffEntry.DiffType.DIRECTORY_NO_DIFF,
							fileTreeDiffEntry.getDiffType());
					File[] childrenOfLocal = child.listFiles();
					Enumeration<?> childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndNoDiffs(
							childrenOfLocal, childNodes);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}

	private void compareFileTreeToNodesForDirMatchesAndExpectADiff(
			final File[] files, final Enumeration<?> nodes,
			final FileTreeDiffEntry.DiffType diffType, final String fileName) {
		for (File child : files) {
			if (child.isDirectory()) {
				if (nodes.hasMoreElements()) {
					FileTreeNode fileTreeNode = (FileTreeNode) nodes
							.nextElement();
					FileTreeDiffEntry fileTreeDiffEntry = (FileTreeDiffEntry) fileTreeNode
							.getUserObject();

					if (fileTreeDiffEntry.getDiffType() != DiffType.DIRECTORY_NO_DIFF) {
						if (fileTreeDiffEntry.getDiffType() != diffType) {
							Assert.fail("unexpectedDiffType");
						}
						if (fileTreeDiffEntry
								.getCollectionAndDataObjectListingEntry()
								.getFormattedAbsolutePath().indexOf(fileName) == -1) {
							Assert.fail("a file name that I didn't anticipate was found to have a diff errror");
						}
					}

					File[] childrenOfLocal = child.listFiles();
					Enumeration<?> childNodes = fileTreeNode.children();
					compareFileTreeToNodesForDirMatchesAndExpectADiff(
							childrenOfLocal, childNodes, diffType, fileName);
				} else {
					Assert.fail("node is out of synch (missing) for file:"
							+ child.getAbsolutePath());
				}
			}
		}
	}

}