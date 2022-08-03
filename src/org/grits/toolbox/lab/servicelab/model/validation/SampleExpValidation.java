/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;

/** 
 * This is an intermediate object that has information of all the
 * tasks assigned and protocols contained in a given sample.
 * 
 *
 */
public class SampleExpValidation
{
	private String sampleName = null;
	private List<TaskUnit> assignedTasks = new ArrayList<TaskUnit>();
	private List<MinInfoProtocol> protocols = new ArrayList<MinInfoProtocol>();
	private Map<MinInfoProtocol, TaskUnit> protocolToTaskMap =
			new HashMap<MinInfoProtocol, TaskUnit>();
	private Map<MinInfoProtocol, List<TaskUnit>> protocolToMatchingTasksMap =
			new HashMap<MinInfoProtocol, List<TaskUnit>>();

	public SampleExpValidation()
	{

	}

	public SampleExpValidation(String sampleExpName)
	{
		this.sampleName = sampleExpName;
	}

	/**
	 * @return the sampleName
	 */
	public String getSampleName()
	{
		return sampleName;
	}

	/**
	 * @param sampleName the sampleName to set
	 */
	public void setSampleName(String sampleName)
	{
		this.sampleName = sampleName;
	}

	/**
	 * @return the assignedTasks
	 */
	public List<TaskUnit> getAssignedTasks()
	{
		return assignedTasks;
	}

	/**
	 * @param assignedTasks the assignedTasks to set
	 */
	public void setAssignedTasks(List<TaskUnit> assignedTasks)
	{
		this.assignedTasks = assignedTasks;
	}

	/**
	 * @return the protocols
	 */
	public List<MinInfoProtocol> getProtocols()
	{
		return protocols;
	}

	/**
	 * @param protocols the protocols to set
	 */
	public void setProtocols(List<MinInfoProtocol> protocols)
	{
		this.protocols = protocols;
	}

	/**
	 * @return the protocolToTaskMap
	 */
	public Map<MinInfoProtocol, TaskUnit> getProtocolToTaskMap()
	{
		return protocolToTaskMap;
	}

	/**
	 * @param protocolToTaskMap the protocolToTaskMap to set
	 */
	public void setProtocolToTaskMap(Map<MinInfoProtocol, TaskUnit> protocolToTaskMap)
	{
		this.protocolToTaskMap = protocolToTaskMap;
	}

	/**
	 * @return the protocolToMatchingTasksMap
	 */
	public Map<MinInfoProtocol, List<TaskUnit>> getProtocolToMatchingTasksMap()
	{
		return protocolToMatchingTasksMap;
	}

	/**
	 * @param protocolToMatchingTasksMap the protocolToMatchingTasksMap to set
	 */
	public void setProtocolToMatchingTasksMap(Map<MinInfoProtocol,
			List<TaskUnit>> protocolToMatchingTasksMap)
	{
		this.protocolToMatchingTasksMap = protocolToMatchingTasksMap;
	}

	/**
	 * returns list of protocols that matches (unique key matches) with the given protocol
	 * or an empty list if none matches or the given protocol node is null
	 * @param protocolNode
	 * @return a list contianing all matching protocols
	 */
	public List<MinInfoProtocol> getMatchingProtocols(MinInfoProtocol protocolNode)
	{
		List<MinInfoProtocol> matchingProtocols = new ArrayList<MinInfoProtocol>();
		if(protocolNode != null)
		{
			// add all matching protocols to the list
			for(MinInfoProtocol protocol : protocols)
			{
				// matching criterion : unique key should match
				if(protocol.getUniqueKey().equals(protocolNode.getUniqueKey()))
				{
					matchingProtocols.add(protocol);
				}
			}
		}
		return matchingProtocols;
	}

	/**
	 * as list of protocols that matches one or more protocols in the given task
	 * (a subset of protocols containing its intersection with task protocols)
	 * @param serviceLabTask task whose protocols is to be matched
	 * @return a list of protocols that matches (their unique key matches) any of the task protocols
	 */
	public List<MinInfoProtocol> getMatchingProtocols(ServiceLabTask serviceLabTask)
	{
		List<MinInfoProtocol> matchingProtocols = new ArrayList<MinInfoProtocol>();
		if(serviceLabTask != null)
		{
			for(MinInfoProtocol protocol : protocols)
			{
				// if a protocol matches any of the task protocol,
				// then add it and move to next protocol
				for(MinInfoProtocol taskProtocol : serviceLabTask.getProtocolNodes())
				{
					// matching criterion : unique key should match
					if(protocol.getUniqueKey().equals(taskProtocol.getUniqueKey()))
					{
						matchingProtocols.add(protocol);
						break;
					}
				}
			}
		}
		return matchingProtocols;
	}

	/**
	 * returns error message for protocol allocation to task
	 * @return error message or null if no error was found
	 */
	public String getErrorMessage()
	{
		StringBuilder errorStringBuilder = new StringBuilder();
		if(assignedTasks.size() == 0)
		{
			errorStringBuilder.append("No task assigned to this sample.").append(System.lineSeparator());
		}
		else if(protocols.size() == 0)
		{
			errorStringBuilder.append("No protocols in this sample experiment.").append(System.lineSeparator());
		}
		else
		{
			Map<TaskUnit, Integer> taskProtocolCountMap = new HashMap<TaskUnit, Integer>();
			Integer taskProtocolCount = null;
			TaskUnit taskUnit = null;
			for(MinInfoProtocol protocol : protocols)
			{
				// check if it could be assigned to a task
				if(protocolToMatchingTasksMap.get(protocol) == null
						|| protocolToMatchingTasksMap.get(protocol).isEmpty())
				{
					errorStringBuilder.append("Extra protocol \"").append(protocol.getLabel())
					.append("\" with no matching tasks.").append(System.lineSeparator());
					continue;
				}
				else 
				{
					taskUnit = protocolToTaskMap.get(protocol);
					if(taskUnit == null) // if it has no assigned task
					{
						errorStringBuilder.append("This protocol \"").append(protocol.getLabel())
						.append("\" is not yet matched to any task.").append(System.lineSeparator());
						continue;
					}
					else // increase count for specific assigned task
					{
						taskProtocolCount = taskProtocolCountMap.get(taskUnit) == null ?
								0 : taskProtocolCountMap.get(taskUnit);
						taskProtocolCountMap.put(taskUnit, taskProtocolCount + 1);
					}
				}
			}

			int protocolCount;
			for(TaskUnit assignedTask : assignedTasks)
			{
				protocolCount = taskProtocolCountMap.get(assignedTask) == null ?
						0 : taskProtocolCountMap.get(assignedTask);
				// check if it is outside task min-max range
				if(protocolCount < assignedTask.getServiceLabTask().getMinProtocols())
				{
					errorStringBuilder.append("Insufficient protocols for Task [")
					.append(assignedTask.getServiceLabTask().getTaskName()).append(" - ");

					if(assignedTask.getAssignedPerson() != null)
					{
						errorStringBuilder.append(assignedTask.getAssignedPerson());
					}
					else
					{
						errorStringBuilder.append("--NONE--");
					}

					errorStringBuilder.append("] Min. required = ")
					.append(assignedTask.getServiceLabTask().getMinProtocols())
					.append(" Protocol count = ").append(protocolCount).append(System.lineSeparator());
					continue;
				}
				else if(protocolCount > assignedTask.getServiceLabTask().getMaxProtocols())
				{
					errorStringBuilder.append(System.lineSeparator())
					.append("Extra protocols for Task [")
					.append(assignedTask.getServiceLabTask().getTaskName()).append(" - ");

					if(assignedTask.getAssignedPerson() != null)
					{
						errorStringBuilder.append(assignedTask.getAssignedPerson());
					}
					else
					{
						errorStringBuilder.append("--NONE--");
					}

					errorStringBuilder.append("] Max. allowed = ")
					.append(assignedTask.getServiceLabTask().getMinProtocols())
					.append(" Protocol count = ").append(protocolCount).append(System.lineSeparator());
					continue;
				}
			}

		}

		// check if any error was added
		return errorStringBuilder.length() > 0
				? errorStringBuilder.toString() : null;
	}
}
