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
@XmlRootElement(name="fileInfoProtocolList")
@XmlType(propOrder={"version", "lastModifiedDate",
		"standardOntologyVersion", "localOntologyVersion", "fileInfoProtocols"})
public class FileInfoProtocolList
{
	public static final String CURRENT_VERSION = "1.0";

	private String version = CURRENT_VERSION;
	private Date lastModifiedDate = null;
	private String standardOntologyVersion = null;
	private String localOntologyVersion = null;
	private List<FileInfoProtocol> fileInfoProtocols = new ArrayList<FileInfoProtocol>();

	public FileInfoProtocolList()
	{

	}

	public FileInfoProtocolList(List<FileInfoProtocol> fileInfoProtocols)
	{
		this.fileInfoProtocols = fileInfoProtocols;
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
	 * 
	 * @return
	 */
	@XmlAttribute(name="lastModified", required=true)
	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getLastModifiedDate()
	{
		return lastModifiedDate;
	}

	/**
	 * 
	 * @param lastModifiedDate
	 */
	public void setLastModifiedDate(Date lastModifiedDate)
	{
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * @return the ontologyVersions
	 */
	public String getStandardOntologyVersion()
	{
		return standardOntologyVersion;
	}

	/**
	 * 
	 * @param standardOntologyVersion
	 */
	public void setStandardOntologyVersion(String standardOntologyVersion)
	{
		this.standardOntologyVersion = standardOntologyVersion;
	}

	/**
	 * @return the ontologyVersions
	 */
	public String getLocalOntologyVersion()
	{
		return localOntologyVersion;
	}

	/**
	 * 
	 * @param localOntologyVersion
	 */
	public void setLocalOntologyVersion(String localOntologyVersion)
	{
		this.localOntologyVersion = localOntologyVersion;
	}

	/**
	 * 
	 * @return list of protocol with file upload
	 */
	@XmlElement(name="fileInfoProtocol")
	public List<FileInfoProtocol> getFileInfoProtocols()
	{
		return fileInfoProtocols;
	}

	/**
	 * 
	 * @param fileInfoProtocols
	 */
	public void setFileInfoProtocols(List<FileInfoProtocol> fileInfoProtocols)
	{
		this.fileInfoProtocols = fileInfoProtocols;
	}
}
