/**
 * 
 */
package org.grits.toolbox.lab.servicelab.validation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.property.ExperimentProperty;
import org.grits.toolbox.entry.sample.property.SampleProperty;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.model.validation.TaskAssignment;
import org.grits.toolbox.lab.servicelab.model.validation.TaskUnit;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.util.ProtocolTaskManagerUtil;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class assigns project tasks to all samples in a project. The input to this class is a project entry
 * and the <b>task_protocol_info.xml</b> file. The output are generally the generate methods containing
 * intermediate sampleExpValidation results in different formats.
 * <p>
 * All the init methods are called from the constructor. All the getters are immediately
 * available after initialization (for <b>getErrors()</b>, more errors would be added as later
 * methods are called and best way to get all errors would be to call it after all load methods).
 * All the load methods are lazy methods that fills more information.
 * All the generate methods should be called at the end (after calling load methods)
 * as they have the results of various assignments.
 * 
 * 
 *
 */
public class ProjectTaskAssigner
{
	private static Logger logger = Logger.getLogger(ProjectTaskAssigner.class);

	private Entry projectEntry = null;
	private List<Entry> sampleExpEntries = new ArrayList<Entry>();
	private List<TaskAssignment> projectTaskAssignments = new ArrayList<TaskAssignment>();
	private Set<String> errorMessages = new HashSet<String>();

	private Map<Entry, SampleExpValidation> entryToSampleValidationMap =
			new HashMap<Entry, SampleExpValidation>();

	private Map<String, ServiceLabTask> allServiceLabTaskMap =
			new HashMap<String, ServiceLabTask>();

	public ProjectTaskAssigner(Entry projectEntry, File taskInfoFile)
	{
		logger.info("Initializing Task Assigner");
		try
		{
			if(projectEntry == null)
				throw new Exception("entry : " + projectEntry);

			if(!ProjectProperty.TYPE.equals(projectEntry.getProperty().getType()))
				throw new Exception("Selected entry is not a project entry "
						+ projectEntry.getDisplayName());

			if(taskInfoFile == null)
				throw new Exception("taskInfoFile : " + taskInfoFile);

			this.projectEntry = projectEntry;
			// create a map of task in the project to service lab task defined in manager
			allServiceLabTaskMap  = new HashMap<String, ServiceLabTask>();
			for(ServiceLabTask serviceLabTask : ProtocolTaskManagerUtil
					.getServiceLabTaskList(taskInfoFile).getServiceLabTasks())
			{
				allServiceLabTaskMap.put(serviceLabTask.getTaskName(), serviceLabTask);
			}

			// create project task to task assignment mapping
			initTaskAssignments(projectEntry, allServiceLabTaskMap);

			// load sample experiment entries list
			initSampleEntries(projectEntry);

			// initialize a map containing sampleExpEntry to sampleValidation
			initEntryToSampleValidationMap();

		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
			errorMessages.add("Error loading values from files :\n" + ex.getMessage());
		}
	}

	private void initSampleEntries(Entry projectEntry)
	{
		// get sample experiment entries from the project
		for(Entry projectChildEntry : projectEntry.getChildren())
		{
			if(SampleProperty.TYPE.equals(projectChildEntry.getProperty().getType()))
			{
				for(Entry sampleChildEntry : projectChildEntry.getChildren())
				{
					// only add entries with experiment property
					if(ExperimentProperty.TYPE.equals(sampleChildEntry.getProperty().getType()))
					{
						sampleExpEntries.add(sampleChildEntry);
					}
				}
			}
		}
	}

	private void initTaskAssignments(Entry projectEntry,
			Map<String, ServiceLabTask> allServiceLabTaskMap) throws IOException
	{
		ServiceLabTask serviceLabTask = null;
		TaskAssignment taskAssignment = null;
		String errorMessage = null;
		for(ProjectTasklist projectTask :
			ProjectDetailsHandler.getProjectDetails(projectEntry).getTasklists())
		{
			serviceLabTask = allServiceLabTaskMap.get(projectTask.getTask());
			if(serviceLabTask == null)
			{
				errorMessage = "No protocol information for this task : " + projectTask.getTask();
				logger.error(errorMessage);
				errorMessages.add(errorMessage);
			}
			else
			{
				taskAssignment = new TaskAssignment(serviceLabTask);
				taskAssignment.setAssignedPerson(projectTask.getPerson());
				taskAssignment.setNumberOfTasks(projectTask.getNumberOfTasks());
				projectTaskAssignments.add(taskAssignment);
			}
		}

		if(projectTaskAssignments.isEmpty())
		{
			// make sure project has a task to be validated
			errorMessage = "No assigned task in the project";
			logger.error(errorMessage);
			errorMessages.add(errorMessage);
		}
	}

	private void initEntryToSampleValidationMap()
	{
		// this map contains a sampleValidation object for each sampleExpEntry
		entryToSampleValidationMap.clear();
		SampleExpValidation sampleExpValidation = null;
		MinInfoProtocol minInfoProtocol = null;
		String errorMessage = null;
		for(Entry sampleExpEntry : sampleExpEntries)
		{
			try
			{
				sampleExpValidation = new SampleExpValidation(sampleExpEntry.getDisplayName());
				for(ProtocolNode protocolNode : ProtocolManagerUtil
						.getProtocolNodesForExperiment(sampleExpEntry))
				{
					minInfoProtocol = new MinInfoProtocol(protocolNode);
					sampleExpValidation.getProtocols().add(minInfoProtocol);
				}
				entryToSampleValidationMap.put(sampleExpEntry, sampleExpValidation);
			} catch (Exception ex)
			{
				errorMessage = "Error loading sample : " + sampleExpEntry.getDisplayName();
				logger.error(errorMessage);
				errorMessages.add(errorMessage);
			}
		}
	}

	public List<Entry> getSampleExpEntries()
	{
		return sampleExpEntries;
	}

	public Set<String> getErrorMessages()
	{
		return errorMessages;
	}

	/**
	 * adds samples to each task assignment and returns all the task assignments
	 * @return list of task assignments with their corresponding samples
	 */
	public List<TaskAssignment> loadTaskAssignments()
	{
		int numOfSamples = sampleExpEntries.size();

		// should have samples to distribute tasks
		if(numOfSamples == 0)
		{
			String errorMessage = "No sample experiment created in the project";
			logger.error(errorMessage);
			errorMessages.add(errorMessage);
		}
		else	
		{
			boolean assignmentSuccessful = false;
			
			try
			{
				assignmentSuccessful = tryWithPreviousAssignments();
			} catch (Exception e)
			{
				logger.error("Error using previous task assignments.\n" + e.getMessage(), e);
			}

			if(!assignmentSuccessful)
			{
				for(TaskAssignment taskAssignment : projectTaskAssignments)
				{
					taskAssignment.getSampleExpEntries().clear();
					tryDefaultAssignment(taskAssignment);
				}
			}
		}

		return projectTaskAssignments;
	}

	private boolean tryWithPreviousAssignments()
	{
		List<TaskAssignment> previousAssignments = PreviousTaskAssignmentsHandler
				.loadPreviousTaskAssignments(projectEntry, sampleExpEntries, allServiceLabTaskMap);

		boolean assignmentSuccessful = false;
		if(previousAssignments != null)
		{
			logger.info("matching task assignments from previous file to current tasks");
			boolean pastAssignmentUsable = false;

			String taskKey;
			// a map containing task name to taskAssignment 
			Map<String, TaskAssignment> taskToPreviousAssignmentMap =
					new HashMap<String, TaskAssignment>();
			for(TaskAssignment taskAssignment : previousAssignments)
			{
				// key containing task name combined with assigned person
				taskKey = getUniqueKeyForTask(taskAssignment);

				// not possible to distinguish similar tasks in version 1.0
				if(taskToPreviousAssignmentMap.get(taskKey) != null)
				{
					logger.error("similar tasks : " + taskKey);
					pastAssignmentUsable = false;
					break;
				}

				// check task assignment has no error
				pastAssignmentUsable = taskAssignment.getErrorMessage() == null;
				if(!pastAssignmentUsable)
					break;

				// if successful add this to map
				taskToPreviousAssignmentMap.put(taskKey, taskAssignment);
			}

			if(pastAssignmentUsable) // from here it returns success
			{
				assignmentSuccessful = true;

				// assign as many as possible from previous assignments
				// or using default strategies
				TaskAssignment previousAssignment = null;
				for(TaskAssignment projectTaskAssignment : projectTaskAssignments)
				{
					projectTaskAssignment.getSampleExpEntries().clear();

					// key containing task name combined with assigned person
					taskKey = getUniqueKeyForTask(projectTaskAssignment);
					previousAssignment = taskToPreviousAssignmentMap.get(taskKey);

					// check if this task was there
					if(previousAssignment != null)
					{
						if(projectTaskAssignment.getNumberOfTasks()
								== previousAssignment.getNumberOfTasks())
						{
							projectTaskAssignment.getSampleExpEntries().addAll(
									previousAssignment.getSampleExpEntries());
						}
						else // if number of tasks have changed try default
						{
							logger.error("NUMBER of tasks different for task : "
									+ projectTaskAssignment.getServiceLabTask().getTaskName()
									+ " current NUMBER " + projectTaskAssignment.getNumberOfTasks()
									+ " previous NUMBER " + previousAssignment.getNumberOfTasks());
							tryDefaultAssignment(projectTaskAssignment);
						}
					}
					// use this backup method to not fail so that
					// as many previous assignments as possible can be used
					// backup method is : if a task was not there try assigning task to each sample n times
					else
					{
						tryDefaultAssignment(projectTaskAssignment);
					}
				}
			}
		}

		return assignmentSuccessful;
	}

	private String getUniqueKeyForTask(TaskAssignment taskAssignment)
	{
		String taskKey = taskAssignment.getServiceLabTask().getTaskName() + "=>";
		if(taskAssignment.getAssignedPerson() != null)
			taskKey += taskAssignment.getAssignedPerson();
		return taskKey.trim();
	}

	private void tryDefaultAssignment(TaskAssignment projectTaskAssignment)
	{
		int numOfSamples = sampleExpEntries.size();

		logger.info("using default approach to assign tasks : "
				+ projectTaskAssignment.getServiceLabTask().getTaskName()
				+ " number of tasks " + projectTaskAssignment.getNumberOfTasks()
				+ " number of samples " + numOfSamples);

		if(numOfSamples > 0
				&& projectTaskAssignment.getNumberOfTasks() % numOfSamples == 0)
		{
			int taskPerSample = projectTaskAssignment.getNumberOfTasks() / numOfSamples;
			// add each sample to this task "taskPerSample" number of times
			for(int i = 0; i < taskPerSample; i++)
			{
				for(Entry sampleExpEntry : sampleExpEntries)
				{
					projectTaskAssignment.getSampleExpEntries().add(sampleExpEntry);
				}
			}
		}
	}

	/**
	 * generate sampleExpValidations by adding assigned tasks to each sampleExpValidation.
	 * All its protocols are already added while initializing maps. 
	 * @return a list of sampleExpValidation with their assigned tasks
	 */
	public Map<Entry, SampleExpValidation> generateSampleExpValidations()
	{
		// clear all the tasks for each sample
		for(Entry sampleExpEntry : sampleExpEntries)
		{
			entryToSampleValidationMap.get(sampleExpEntry).getAssignedTasks().clear();
		}

		// add all the tasks from the assignment
		for(TaskAssignment taskAssignment : projectTaskAssignments)
		{
			for(Entry sampleExpEntry : taskAssignment.getSampleExpEntries())
			{
				entryToSampleValidationMap.get(sampleExpEntry)
				.getAssignedTasks().add(new TaskUnit(taskAssignment));
			}
		}

		return entryToSampleValidationMap;
	}

	public boolean saveTaskAssignments()
	{
		try
		{
			File fileLocation = PreviousTaskAssignmentsHandler
					.loadTaskAssignmentFile(projectEntry);

			if(fileLocation == null)
				return false;

			FileOutputStream fileOutputStream = null;
			try
			{
				fileOutputStream = new FileOutputStream(fileLocation);
				XMLOutputter xmlOutPutter = new XMLOutputter();
				xmlOutPutter.setFormat(Format.getPrettyFormat());
				xmlOutPutter.output(PreviousTaskAssignmentsHandler
						.createDocument(projectTaskAssignments), fileOutputStream);
				return true;
			} catch (IOException e)
			{
				logger.error("Task Assignments could not be saved to the file.\n" + e.getMessage(), e);
				return false;
			} finally
			{
				if(fileOutputStream != null)
				{
					fileOutputStream.close();
				}
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
	}
}
