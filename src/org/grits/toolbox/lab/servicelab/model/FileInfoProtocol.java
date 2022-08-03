/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;

/**
 * 
 *
 */
@XmlRootElement(name="protocolWithFileUpload")
@XmlType(propOrder={"fileUploads"})
public class FileInfoProtocol extends MinInfoProtocol
{
	private List<FileUpload> fileUploads = new ArrayList<FileUpload>();

	public FileInfoProtocol()
	{

	}

	public FileInfoProtocol(ProtocolNode protocolNode)
	{
		super(protocolNode);
	}

	/**
	 * @return the fileUploads
	 */
	@XmlElement(name="fileUploads", required=true)
	public List<FileUpload> getFileUploads()
	{
		return fileUploads;
	}

	/**
	 * @param fileUploads the fileUploads to set
	 */
	public void setFileUploads(List<FileUpload> fileUploads)
	{
		this.fileUploads = fileUploads;
	}
}
