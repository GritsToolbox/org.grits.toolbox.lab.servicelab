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
@XmlRootElement(name="priceInfoProtocolList")
@XmlType(propOrder={"version", "lastModifiedDate",
		"standardOntologyVersion", "localOntologyVersion", "priceInfoProtocols"})
public class PriceInfoProtocolList
{
	public static final String CURRENT_VERSION = "1.0";

	private String version = CURRENT_VERSION;
	private Date lastModifiedDate = null;
	private String standardOntologyVersion = null;
	private String localOntologyVersion = null;
	private List<PriceInfoProtocol> priceInfoProtocols = new ArrayList<PriceInfoProtocol>();

	public PriceInfoProtocolList()
	{

	}

	public PriceInfoProtocolList(List<PriceInfoProtocol> priceInfoProtocols)
	{
		this.priceInfoProtocols = priceInfoProtocols;
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
	 * @return the priceInfoProtocols
	 */
	@XmlElement(name="priceInfoProtocol")
	public List<PriceInfoProtocol> getPriceInfoProtocols()
	{
		return priceInfoProtocols;
	}

	/**
	 * @param priceInfoProtocols the priceInfoProtocols to set
	 */
	public void setPriceInfoProtocols(List<PriceInfoProtocol> priceInfoProtocols)
	{
		this.priceInfoProtocols = priceInfoProtocols;
	}
}
