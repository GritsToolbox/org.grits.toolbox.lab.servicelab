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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.grits.toolbox.editor.experimentdesigner.model.Paper;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;

/**
 * 
 *
 */
@XmlRootElement(name="priceInfoProtocol")
@XmlType(propOrder={"commonName", "knownProtocol", "costParameters"})
public class PriceInfoProtocol extends ProtocolNode
{
	private static final long serialVersionUID = 1L;

	private String commonName = null;
	private boolean knownProtocol = false;
	private List<CostParameter> costParameters = new ArrayList<CostParameter>();

	public PriceInfoProtocol()
	{
		// TODO Auto-generated constructor stub
	}

	public PriceInfoProtocol(ProtocolNode protocolNode)
	{
		setCreator(protocolNode.getCreator());
		setDescription(protocolNode.getDescription());
		setFile(protocolNode.getFile());
		setId(protocolNode.getId());
		setLabel(protocolNode.getLabel());
		setTemplate(protocolNode.getTemplate());
		setTemplateChanged(protocolNode.isTemplateChanged());
		setTemplateUri(protocolNode.getTemplateUri());
		setUri(protocolNode.getUri());
		setUrl(protocolNode.getUrl());

		// set its common name same as its label
		setCommonName(protocolNode.getLabel());

		setLocation(protocolNode.getLocation());
		setMyColor(protocolNode.getMyColor());
		if(protocolNode.getCategory() != null)
			setCategory(protocolNode.getCategory().getACopy());
		if(protocolNode.getColor() != null)
		{
			setColor(new Color(protocolNode.getColor().getDevice(),
					protocolNode.getColor().getRGB()));
		}
		if(protocolNode.getSize() != null)
		{
			setSize(new Dimension(protocolNode.getSize().width,
					protocolNode.getSize().height));
		}
		if(protocolNode.getPapers() != null)
		{
			List<Paper> papers = new ArrayList<Paper>();
			for(Paper paper : protocolNode.getPapers())
			{
				papers.add(paper.getACopy());
			}
			setPapers(papers);
		}
		if(protocolNode.getParameters() != null)
		{
			List<Parameter> parameters = new ArrayList<Parameter>();
			for(Parameter parameter : protocolNode.getParameters())
			{
				parameters.add(parameter.getACopy());
			}
			setParameters(parameters);
		}
		if(protocolNode.getParameterGroups() != null)
		{
			List<ParameterGroup> parameterGroups = new ArrayList<ParameterGroup>();
			for(ParameterGroup parameterGroup : protocolNode.getParameterGroups())
			{
				parameterGroups.add(parameterGroup.getACopy());
			}
			setParameterGroups(parameterGroups);
		}
	}

	/**
	 * returns common name used in service lab for this protocol
	 * @return common name
	 */
	@XmlAttribute(name="commonName", required=true)
	public String getCommonName()
	{
		return commonName;
	}

	/**
	 * 
	 * @param commonName
	 */
	public void setCommonName(String commonName)
	{
		this.commonName = commonName;
	}

	/**
	 * returns if this protocol has been provided with cost
	 * parameter information or even if left empty deliberately
	 * @return true if cost parameter information is known else returns false
	 */
	@XmlAttribute(name="knownProtocol", required=true)
	public boolean isKnownProtocol()
	{
		return knownProtocol;
	}

	/**
	 * sets true when cost parameters are provided or left empty deliberately
	 * @param knownProtocol is to be set true when cost information has been provided
	 */
	public void setKnownProtocol(boolean knownProtocol)
	{
		this.knownProtocol = knownProtocol;
	}

	/**
	 * returns list of cost parameters associated with this protocol
	 * @return list of cost parameters
	 */
	@XmlElement(name="costParameter")
	public List<CostParameter> getCostParameters()
	{
		return costParameters;
	}

	/**
	 * 
	 * @param costParameters
	 */
	public void setCostParameters(List<CostParameter> costParameters)
	{
		this.costParameters = costParameters;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
}
