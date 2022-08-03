/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model.validation;

import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;

/**
 * This is an intermediate object that has information related to all the
 * samples that are assigned a particular task. 
 * 
 *
 */
public class TaskAssignment
{
	private ServiceLabTask serviceLabTask = null;
	private String assignedPerson = null;
	private List<Entry> sampleExpEntries = new ArrayList<Entry>();
	private int numberOfTasks = 1;

	public TaskAssignment()
	{

	}

	public TaskAssignment(ServiceLabTask serviceLabTask)
	{
		this.serviceLabTask = serviceLabTask;
	}

	/**
	 * @return the serviceLabTask
	 */
	public ServiceLabTask getServiceLabTask()
	{
		return serviceLabTask;
	}

	/**
	 * @param serviceLabTask the serviceLabTask to set
	 */
	public void setServiceLabTask(ServiceLabTask serviceLabTask)
	{
		this.serviceLabTask = serviceLabTask;
	}

	/**
	 * @return the assignedPerson
	 */
	public String getAssignedPerson()
	{
		return assignedPerson;
	}

	/**
	 * @param assignedPerson the assignedPerson to set
	 */
	public void setAssignedPerson(String assignedPerson)
	{
		this.assignedPerson = assignedPerson;
	}

	/**
	 * @return the sampleExpEntries
	 */
	public List<Entry> getSampleExpEntries()
	{
		return sampleExpEntries;
	}

	/**
	 * @param sampleExpEntries the sampleExpEntries to set
	 */
	public void setSampleExpEntries(List<Entry> sampleExpEntries)
	{
		this.sampleExpEntries = sampleExpEntries;
	}

	/**
	 * @return the numberOfTasks
	 */
	public int getNumberOfTasks()
	{
		return numberOfTasks;
	}

	/**
	 * @param numberOfTasks the numberOfTasks to set
	 */
	public void setNumberOfTasks(int numberOfTasks)
	{
		this.numberOfTasks = numberOfTasks;
	}

	public String getErrorMessage()
	{
		if(sampleExpEntries.size() != numberOfTasks)
		{
			StringBuilder errorBuilder = new StringBuilder();

			// check the category of error
			if(numberOfTasks == 0)
			{
				errorBuilder.append("number of tasks is 0")
				.append(System.lineSeparator());
			}
			else if(sampleExpEntries.size() == 0)
			{
				errorBuilder.append("task could not be matched with samples")
				.append(System.lineSeparator());
			}
			else
			{
				errorBuilder.append("mismatch in no. of Tasks and no. of Samples")
				.append(" (#Tasks = ").append(numberOfTasks)
				.append(", #Samples = ").append(sampleExpEntries.size()).append(") ")
				.append(System.lineSeparator());
			}

			return errorBuilder.toString();
		}

		return null;
	}

	public String getTaskHeader()
	{
		StringBuilder headerBuilder = new StringBuilder().append("Task [")
				.append(serviceLabTask.getTaskName()).append(" - ");

		if(assignedPerson != null)
			headerBuilder.append(assignedPerson);
		else
			headerBuilder.append("--NONE--");

		headerBuilder.append("]");
		return headerBuilder.toString();
	}
}
