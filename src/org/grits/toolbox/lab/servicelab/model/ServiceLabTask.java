/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name="serviceLabTask")
@XmlType(propOrder={"taskName", "minProtocols", "maxProtocols", "protocolNodes"})
public class ServiceLabTask
{
	private String taskName = null;
	private int minProtocols = 0;
	private int maxProtocols = 0;
	private List<MinInfoProtocol> protocolNodes = new ArrayList<MinInfoProtocol>();

	public ServiceLabTask()
	{

	}

	public ServiceLabTask(String taskName)
	{
		this.taskName = taskName;
	}

	/**
	 * @return the taskName
	 */
	@XmlAttribute(name="taskName", required=true)
	public String getTaskName()
	{
		return taskName;
	}

	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName)
	{
		this.taskName = taskName;
	}

	/**
	 * @return the minProtocols
	 */
	@XmlAttribute(name="minProtocols", required=true)
	public int getMinProtocols()
	{
		return minProtocols;
	}

	/**
	 * @param minProtocols the minProtocols to set
	 */
	public void setMinProtocols(int minProtocols)
	{
		this.minProtocols = minProtocols;
	}

	/**
	 * @return the maxProtocols
	 */
	@XmlAttribute(name="maxProtocols", required=true)
	public int getMaxProtocols()
	{
		return maxProtocols;
	}

	/**
	 * @param maxProtocols the maxProtocols to set
	 */
	public void setMaxProtocols(int maxProtocols)
	{
		this.maxProtocols = maxProtocols;
	}

	/**
	 * @return the protocolNodes
	 */
	@XmlElement(name="protocolNodes", required=true)
	public List<MinInfoProtocol> getProtocolNodes()
	{
		return protocolNodes;
	}

	/**
	 * @param protocolNodes the protocolNodes to set
	 */
	public void setProtocolNodes(List<MinInfoProtocol> protocolNodes)
	{
		this.protocolNodes = protocolNodes;
	}
}
