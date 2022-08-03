/**
 * 
 */
package org.grits.toolbox.lab.servicelab.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.model.validation.TaskUnit;

/**
 * This class matches existing protocols to each assigned task in a sample. The input to this class is a
 * list of sampleExpValidations. For each sampleExpValidation object in the list, there is a sample
 * and a list of protocols and assigned tasks to this sample. This class allocates protocols to
 * assigned tasks in a sample.
 * <p>
 * It creates a map of protocol to possible tasks in the init method called from constructor. Later these
 * protocols can be allocated to any of these tasks when assignment is made by calling any of the methods
 * starting with allocate. 
 * 
 *
 */
public class SampleProtocolTaskMatcher
{
	private static Logger logger = Logger.getLogger(SampleProtocolTaskMatcher.class);

	private List<SampleExpValidation> sampleExpValidations =
			new ArrayList<SampleExpValidation>();

	public SampleProtocolTaskMatcher(List<SampleExpValidation> sampleExpValidations)
	{
		logger.info("Initializing Sample Protocol Task Matcher");
		this.sampleExpValidations = sampleExpValidations;
		initProtocolToAvailableTasksMap();
	}

	private void initProtocolToAvailableTasksMap()
	{
		List<TaskUnit> allTaskOptions = null;
		for(SampleExpValidation sampleExpValidation : sampleExpValidations)
		{
			// clear any protocol task mapping
			sampleExpValidation.getProtocolToMatchingTasksMap().clear();
			sampleExpValidation.getProtocolToTaskMap().clear();

			// first put all protocols in the map that belong to some tasks
			for(TaskUnit assignedTask : sampleExpValidation.getAssignedTasks())
			{
				for(MinInfoProtocol taskProtocol : sampleExpValidation.getMatchingProtocols(assignedTask.getServiceLabTask()))
				{
					allTaskOptions = sampleExpValidation.getProtocolToMatchingTasksMap().get(taskProtocol);

					// if this protocol is not yet in the map put an empty list for this protocol
					if(allTaskOptions == null)
					{
						allTaskOptions = new ArrayList<TaskUnit>();
						sampleExpValidation.getProtocolToMatchingTasksMap().put(taskProtocol, allTaskOptions);
					}

					// add this task to its possible tasks for this task protocol
					allTaskOptions.add(assignedTask);
				}
			}
		}
	}

	/**
	 * It allocates protocols to different tasks for each sample in the list. If allocation
	 * is not possible for a particular sample, it moves to next sample.
	 * <p> For each sample, there should be no unmatchable protocol
	 * (a protocol that cannot be allocated to any task).
	 * It tries allocation of protocols to various tasks using <b>backtracking</b>
	 * and is an <b>exhaustive</b> strategy.
	 */
	public void allocateProtocols()
	{
		for(SampleExpValidation sampleExpValidation : sampleExpValidations)
		{
			// extra protocol in sample
			if(checkForExtraProtocols(sampleExpValidation) != null)
			{
				// error for this sample move to next sample
				logger.error("extra protocols in sample : " + sampleExpValidation.getSampleName());
				continue;
			}

			// allocate protocols using backtracking
			backTrackAllocation(sampleExpValidation);
		}
	}

	/**
	 * uses back tracking to make sure all protocols are assigned a possible task
	 * without exceeding the min-max range
	 * @param sampleExpValidation
	 */
	private void backTrackAllocation(SampleExpValidation sampleExpValidation)
	{
		sampleExpValidation.getProtocolToTaskMap().clear();
		Map<TaskUnit, Integer> taskProtocolCountMap =
				new HashMap<TaskUnit, Integer>();
		for(TaskUnit taskUnit : sampleExpValidation.getAssignedTasks())
		{
			taskProtocolCountMap.put(taskUnit, 0);
		}
		ListIterator<MinInfoProtocol> protocolIterator =
				sampleExpValidation.getProtocols().listIterator();
		assignProtocolsBackTrack(sampleExpValidation, protocolIterator, taskProtocolCountMap);
	}

	/**
	 * tries all possible task for each protocol and backtracks until a matching valid map is set
	 * or no such map is possible at all.
	 * @param sampleExpValidation the sample whose protocols are to be set
	 * @param protocolListIterator list containing the next protocol in the list
	 * @param taskProtocolCountMap map containing updated count of protocols for each task
	 * @return true if allocation was successful else returns false
	 */
	private boolean assignProtocolsBackTrack(SampleExpValidation sampleExpValidation,
			ListIterator<MinInfoProtocol> protocolListIterator,
			Map<TaskUnit, Integer> taskProtocolCountMap)
	{
		boolean allMatched = false;
		if(protocolListIterator.hasNext())
		{
			MinInfoProtocol nextProtocol = protocolListIterator.next();
			// try each of the possible tasks until allocation is valid
			for(TaskUnit protocolTask : sampleExpValidation
					.getProtocolToMatchingTasksMap().get(nextProtocol))
			{
				sampleExpValidation.getProtocolToTaskMap().put(nextProtocol, protocolTask);
				taskProtocolCountMap.put(protocolTask, taskProtocolCountMap.get(protocolTask) + 1);
				if(assignProtocolsBackTrack(sampleExpValidation, sampleExpValidation.getProtocols()
						.listIterator(protocolListIterator.nextIndex()), taskProtocolCountMap))
				{
					allMatched = true;
					break;
				}
				else
				{
					sampleExpValidation.getProtocolToTaskMap().remove(nextProtocol);
					taskProtocolCountMap.put(protocolTask, taskProtocolCountMap.get(protocolTask) - 1);
				}
			}
		}
		else // validate allocation after end is reached
		{
			allMatched = validate(sampleExpValidation, taskProtocolCountMap);
		}
		return allMatched;
	}

	private boolean validate(SampleExpValidation sampleExpValidation,
			Map<TaskUnit, Integer> taskProtocolCount)
	{
		boolean allMatched = false;
		TaskUnit assignedTask = null;
		boolean protocolMatched = true;
		for(MinInfoProtocol protocol : sampleExpValidation.getProtocols())
		{
			assignedTask = sampleExpValidation.getProtocolToTaskMap().get(protocol);
			if(assignedTask == null) // if it has no assigned task
			{
				protocolMatched = false;
				break;
			}
		}

		if(protocolMatched)
		{
			boolean taskMatched = true;
			int protocolCount = 0;
			for(TaskUnit taskUnit : sampleExpValidation.getAssignedTasks())
			{
				protocolCount = taskProtocolCount.get(taskUnit) == null ?
						0 : taskProtocolCount.get(taskUnit);
				// check if it is outside task min-max range
				if(protocolCount < taskUnit.getServiceLabTask().getMinProtocols()
						|| protocolCount > taskUnit.getServiceLabTask().getMaxProtocols())
				{
					taskMatched = false;
					break;
				}
			}
			allMatched = taskMatched;
		}

		return allMatched;
	}

	private String checkForExtraProtocols(SampleExpValidation sampleExpValidation)
	{
		String errorMessage = null;
		for(MinInfoProtocol protocol : sampleExpValidation.getProtocols())
		{
			// check if it could be assigned to any task
			if(sampleExpValidation.getProtocolToMatchingTasksMap().get(protocol) == null
					|| sampleExpValidation.getProtocolToMatchingTasksMap().get(protocol).isEmpty())
			{
				errorMessage = "Extra protocol with no matching tasks : " + protocol.getLabel();
				break;
			}
		}
		return errorMessage;
	}
}
