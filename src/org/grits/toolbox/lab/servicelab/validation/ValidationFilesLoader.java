/**
 * 
 */
package org.grits.toolbox.lab.servicelab.validation;

import java.io.File;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;

/**
 * 
 *
 */
public class ValidationFilesLoader
{
	private static Logger logger = Logger.getLogger(ValidationFilesLoader.class);

	public static File loadServiceLabFolder(Entry entry) throws LoadingException
	{
		if(entry == null)
			throw new LoadingException("Null Entry",
					"Cannot find serviceLab folder from this null entry");
		
		Entry workspaceEntry = DataModelSearch.findParentByType(entry, WorkspaceProperty.TYPE);

		if(workspaceEntry == null)
		{
			throw new LoadingException("Workspace Entry not found",
					"Cannot find workspace location from this entry :" + entry.getDisplayName());
		}

		return loadServiceLabFolder(((WorkspaceProperty) workspaceEntry.getProperty()).getLocation());
	}

	public static File loadServiceLabFolder(String workspaceLocation) throws LoadingException
	{
		if(workspaceLocation == null || workspaceLocation.isEmpty())
		{
			throw new LoadingException("Invalid Workspace Location",
					"Workspace location is not valid (null/empty).");
		}

		File serviceLabFolder = new File(workspaceLocation + File.separator
				+ IConfig.SERVICE_LAB_FOLDER_NAME);
		if(serviceLabFolder == null || !serviceLabFolder.exists())
		{
			logger.error("No \"" + IConfig.SERVICE_LAB_FOLDER_NAME + "\" folder found.");
			throw new LoadingException(
					"Missing Service Lab Folder",
					"There is no folder containing price/file/task information related to protocols."
							+ " Please open Price Manager/File Manager/Task Manager first for setting values!\n"
							+ "For opening these Managers use menu : "
							+ "Tools -> Service Lab -> Open Manager -> ");
		}

		return serviceLabFolder;
	}

	public static File loadTaskInfoFile(File serviceLabFolder) throws LoadingException
	{
		// load task info file
		File taskInfoFile = new File(serviceLabFolder, IConfig.TASK_PROTOCOL_INFO_FILE_NAME);

		if(taskInfoFile == null || !taskInfoFile.exists())
		{
			logger.error("No \"" + IConfig.TASK_PROTOCOL_INFO_FILE_NAME + "\" file found.");
			throw new LoadingException("Missing Task Manager Information File",
					"The file containing task information related to protocols is missing."
							+ " Please open Task Manager first for setting values!\n"
							+ "For opening this Manager use menu : "
							+ "Tools -> Service Lab -> Open Manager -> Task Manager");
		}

		return taskInfoFile;
	}

	public static File loadProtocolFileInfoFile(File serviceLabFolder) throws LoadingException
	{
		// load protocol-file info file
		File protocolListFile = new File(serviceLabFolder, IConfig.PROTOCOL_FILE_UPLOAD_INFO_FILE_NAME);

		// check if file is valid
		if(protocolListFile == null || !protocolListFile.exists())
		{
			logger.error("No \"" + IConfig.PROTOCOL_FILE_UPLOAD_INFO_FILE_NAME + "\" file found.");
			throw new LoadingException("Missing File Manager Information File",
					"The file containing file upload information related to protocols is missing."
							+ " Please open File Manager first for setting values!\n"
							+ "For opening this Manager use menu : "
							+ "Tools -> Service Lab -> Open Manager -> File Manager");
		}

		return protocolListFile;
	}
}
