/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;

/**
 * 
 *
 */
@XmlRootElement(name="protocolNodes")
@XmlType(propOrder={"label", "templateUri"})
public class MinInfoProtocol
{
	private String label = null;
	private String templateUri = null;

	public MinInfoProtocol()
	{

	}

	public MinInfoProtocol(ProtocolNode protocolNode)
	{
		this.label = protocolNode.getLabel();
		this.templateUri = protocolNode.getTemplateUri();
	}

	/**
	 * @return the label
	 */
	@XmlAttribute(name="label", required=true)
	public String getLabel()
	{
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @return the templateUri
	 */
	@XmlAttribute(name="templateUri", required=false)
	public String getTemplateUri()
	{
		return templateUri;
	}

	/**
	 * @param templateUri the templateUri to set
	 */
	public void setTemplateUri(String templateUri)
	{
		this.templateUri = templateUri;
	}

	public String getUniqueKey()
	{
		return templateUri == null ? label : templateUri;
	}

	public MinInfoProtocol getACopy()
	{
		MinInfoProtocol copiedProtocolNode = new MinInfoProtocol();
		copiedProtocolNode.setLabel(label);
		copiedProtocolNode.setTemplateUri(templateUri);
		return copiedProtocolNode;
	}
}
