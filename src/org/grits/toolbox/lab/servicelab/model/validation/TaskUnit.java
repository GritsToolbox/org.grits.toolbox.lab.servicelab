/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model.validation;

import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;

/**
 * A class that makes a unit task i.e. a break-up of one task assigned to sample(s)
 * into multiple simple task units. This class helps in distinguishing similar named tasks
 * to assign different protocols and still being valid.
 * <p>
 * <b>Example</b><br/>
 * Task Assignment :- NMR tasks assigned to some person for sample A with number of tasks = 2.
 * This Task Assignment is broken into 2 Task Units, each comprising of
 * 1 NMR to some person in sample a. This facilitates adding two different protocols
 * to these similar task units and being able to validate protocols in each unit independently.
 * 
 * 
 *
 */
public class TaskUnit
{
	private ServiceLabTask serviceLabTask = null;
	private String assignedPerson = null;

	public TaskUnit(TaskAssignment taskAssignment)
	{
		setAssignedPerson(taskAssignment.getAssignedPerson());
		setServiceLabTask(taskAssignment.getServiceLabTask());
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
}
