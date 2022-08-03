/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.core.datamodel.property.project.DateAdapter;

/**
 * 
 *
 */
@XmlRootElement(name="serviceLabTasklist")
@XmlType(propOrder={"version", "lastModifiedDate", "serviceLabTasks"})
public class ServiceLabTasklist
{
	public static final String CURRENT_VERSION = "1.0";

	private String version = CURRENT_VERSION;
	private Date lastModifiedDate = null;
	private List<ServiceLabTask> serviceLabTasks = new ArrayList<ServiceLabTask>();

	public ServiceLabTasklist()
	{

	}

	public ServiceLabTasklist(List<ServiceLabTask> serviceLabTasks)
	{
		this.setServiceLabTasks(serviceLabTasks);
	}

	/**
	 * 
	 * @return
	 */
	@XmlAttribute(name="version", required=true)
	public String getVersion()
	{
		return version;
	}

	/**
	 * 
	 * @param version
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * @return the lastModifiedDate
	 */
	@XmlAttribute(name="lastModified", required=true)
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getLastModifiedDate()
	{
		return lastModifiedDate;
	}

	/**
	 * @param lastModifiedDate the lastModifiedDate to set
	 */
	public void setLastModifiedDate(Date lastModifiedDate)
	{
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * @return the serviceLabTasks
	 */
	@XmlElement(name="serviceLabTask", required=true)
	public List<ServiceLabTask> getServiceLabTasks()
	{
		return serviceLabTasks;
	}

	/**
	 * @param serviceLabTasks the serviceLabTasks to set
	 */
	public void setServiceLabTasks(List<ServiceLabTask> serviceLabTasks)
	{
		this.serviceLabTasks = serviceLabTasks;
	}
}
