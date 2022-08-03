/**
 * 
 */
package org.grits.toolbox.lab.servicelab.validation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;
import org.grits.toolbox.lab.servicelab.model.validation.TaskAssignment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 
 *
 */
public class PreviousTaskAssignmentsHandler
{
	private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

	private static Logger logger = Logger.getLogger(PreviousTaskAssignmentsHandler.class);

	/**
	 * It returns a file (may be non-existent) for task assignment or null if there was
	 * some error and file cannot be created (e.g entry is null, or <b>.servicelab</b> folder inside the
	 * project could not be created).
	 * @param projectEntry
	 * @return
	 */
	public static File loadTaskAssignmentFile(Entry projectEntry)
	{
		logger.info("load task assignment file for project");

		if(projectEntry == null || !ProjectProperty.TYPE.equals(projectEntry.getProperty().getType()))
			return null;

		logger.info("project name : " + projectEntry.getDisplayName());

		Entry workspaceEntry = DataModelSearch.findParentByType(projectEntry, WorkspaceProperty.TYPE);

		String projectServiceLabFolderLocation = ((WorkspaceProperty) workspaceEntry.getProperty()).getLocation()
				+ File.separator + projectEntry.getDisplayName()
				+ File.separator + IConfig.SERVICE_LAB_FOLDER_NAME;

		File projectServiceLabFolder = new File(projectServiceLabFolderLocation);
		if(!projectServiceLabFolder.exists())
		{
			// try creating the folder, if it does not exist
			if(!projectServiceLabFolder.mkdirs())
			{
				// if folder cannot be created return null
				return null;
			}
		}

		String fileLocation = projectServiceLabFolderLocation + File.separator
				+ IConfig.PROJECT_TASK_ASSIGNMENT_FILE_NAME;

		logger.info(fileLocation);

		return new File(fileLocation);
	}

	public static Document createDocument(List<TaskAssignment> projectTaskAssignments)
	{
		logger.info("creating task assignments document");

		if(projectTaskAssignments == null)
		{
			logger.error("null parameter is not valid : " + projectTaskAssignments);
			return null;
		}

		// create a new document
		Document document = new Document();
		Element rootElement = new Element("taskAssignments");
		rootElement.setAttribute("version", "1.0");
		rootElement.setAttribute("lastModfied",
				(new SimpleDateFormat(DATE_FORMAT).format(new Date())));
		document.setRootElement(rootElement);

		Element taskElement = null;
		// add each task to root element
		for(TaskAssignment projectTaskAssignment : projectTaskAssignments)
		{
			logger.debug(projectTaskAssignment.getServiceLabTask().getTaskName());
			taskElement = new Element("task")
					.setAttribute("name", projectTaskAssignment.getServiceLabTask().getTaskName());
			taskElement.setAttribute("assignedPerson", projectTaskAssignment.getAssignedPerson() == null ?
							"" : projectTaskAssignment.getAssignedPerson());
			taskElement.setAttribute("numberOfTasks", projectTaskAssignment.getNumberOfTasks() + "");

			// add each assigned sample to tasks
			for(Entry sampleExpEntry : projectTaskAssignment.getSampleExpEntries())
			{
				logger.debug(sampleExpEntry.getDisplayName());
				taskElement.addContent(new Element("sample")
						.setAttribute("name", sampleExpEntry.getDisplayName()));
			}

			rootElement.addContent(taskElement);
		}

		return document;
	}

	public static List<TaskAssignment> loadPreviousTaskAssignments(Entry projectEntry,
			List<Entry> sampleExpEntries, Map<String, ServiceLabTask> allServiceLabTaskMap)
	{
		logger.info("loading task assignments from previous file");

		if(projectEntry == null || sampleExpEntries == null || allServiceLabTaskMap == null)
		{
			logger.error("null parameters not allowed : project "
					+ projectEntry + " samples " + sampleExpEntries +
					" all task info map " + allServiceLabTaskMap);
			return null;
		}

		logger.info("Reading previous task assignments from file for project : "
				+ projectEntry.getDisplayName());

		File previousAssignmentFile = loadTaskAssignmentFile(projectEntry);

		// check if there was any file
		if(previousAssignmentFile == null || !previousAssignmentFile.exists())
		{
			logger.info("No task assignment file found.");
			return null;
		}

		logger.info("Task assignment file : " + previousAssignmentFile.getAbsolutePath());
		List<TaskAssignment> previousAssignments = new ArrayList<TaskAssignment>();
		try
		{
			SAXBuilder builder = new SAXBuilder();
			Document xmlDocument = builder.build(previousAssignmentFile);
			Element rootElement = xmlDocument.getRootElement();

			logger.info("Root Element " + rootElement.getName());
			logger.info("Version - " + rootElement.getAttributeValue("version"));

			if("taskAssignments".equals(rootElement.getName())
					&& "1.0".equals(rootElement.getAttribute("version")))
			{
				logger.error("Different root/version for this task assignment file.");
				throw new Exception("Different root/version for this task assignment file.");
			}

			// create a map of sample name to entry
			Map<String, Entry> sampleExpNameToEntryMap = new HashMap<String, Entry>();
			for(Entry sampleExpEntry : sampleExpEntries)
			{
				sampleExpNameToEntryMap.put(sampleExpEntry.getDisplayName(), sampleExpEntry);
			}

			TaskAssignment taskAssignment = null;
			ServiceLabTask serviceLabTask = null;
			String assignedPerson;
			Entry sampleExpEntry = null;
			Element taskElement = null;
			Element sampleElement = null;
			String errorMessage = null;
			for(Object childObject : rootElement.getChildren())
			{
				// read as many tasks as possible
				try
				{
					taskElement = (Element) childObject;
					serviceLabTask = allServiceLabTaskMap.get(taskElement.getAttributeValue("name"));

					if(serviceLabTask == null)
					{
						logger.error("Cannot find this saved task : "
								+ taskElement.getAttributeValue("name"));
						continue;
					}

					taskAssignment = new TaskAssignment(serviceLabTask);

					assignedPerson = taskElement.getAttributeValue("assignedPerson");
					assignedPerson = assignedPerson != null && assignedPerson.isEmpty() ? null : assignedPerson;
					taskAssignment.setAssignedPerson(assignedPerson);

					// try parsing number of tasks
					taskAssignment.setNumberOfTasks(
							Integer.parseInt(taskElement.getAttributeValue("numberOfTasks")));

					for(Object taskChildObject : taskElement.getChildren())
					{
						sampleElement = (Element) taskChildObject;

						// check if sample is still there in the projects
						// else throw exception from this task
						sampleExpEntry = sampleExpNameToEntryMap.get(
								sampleElement.getAttributeValue("name"));
						if(sampleExpEntry == null)
						{
							errorMessage = "This sample from saved file no longer exists : "
									+ sampleElement.getAttributeValue("name");
							logger.error(errorMessage);
							throw new Exception(errorMessage);
						}

						taskAssignment.getSampleExpEntries().add(sampleExpEntry);
					}

					if(taskAssignment.getNumberOfTasks() ==
							taskAssignment.getSampleExpEntries().size())
					{
						// after successful loading add this to list
						previousAssignments.add(taskAssignment);
					}
					else
					{
						logger.error("Number of tasks and number of samples"
								+ " do not match in file for this task : "
								+ taskElement.getAttributeValue("name"));
					}

				} catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
		catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
		}

		return previousAssignments;
	}
}