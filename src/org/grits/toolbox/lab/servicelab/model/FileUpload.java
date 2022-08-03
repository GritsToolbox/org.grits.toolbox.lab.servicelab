/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name="fileUpload")
@XmlType(propOrder={"numberOfFiles", "fileCategory", "fileSubCategory"})
public class FileUpload
{
	private int numberOfFiles = 0;
	private String fileCategory = null;
	private String fileSubCategory = null;

	public FileUpload()
	{
		
	}

	public FileUpload(String fileCategory, String fileSubCategory)
	{
		this.fileCategory = fileCategory;
		this.fileSubCategory = fileSubCategory;

		// by default set number of files to 1
		this.numberOfFiles = 1;
	}

	/**
	 * @return the numberOfFiles
	 */
	@XmlAttribute(name="numberOfFiles", required=true)
	public int getNumberOfFiles()
	{
		return numberOfFiles;
	}

	/**
	 * @param numberOfFiles the numberOfFiles to set
	 */
	public void setNumberOfFiles(int numberOfFiles)
	{
		this.numberOfFiles = numberOfFiles;
	}

	/**
	 * @return the fileCategory
	 */
	@XmlAttribute(name="fileCategory", required=true)
	public String getFileCategory()
	{
		return fileCategory;
	}

	/**
	 * @param fileCategory the fileCategory to set
	 */
	public void setFileCategory(String fileCategory)
	{
		this.fileCategory = fileCategory;
	}

	/**
	 * @return the fileSubCategory
	 */
	@XmlAttribute(name="fileSubCategory", required=true)
	public String getFileSubCategory()
	{
		return fileSubCategory;
	}

	/**
	 * @param fileSubCategory the fileSubCategory to set
	 */
	public void setFileSubCategory(String fileSubCategory)
	{
		this.fileSubCategory = fileSubCategory;
	}
}
