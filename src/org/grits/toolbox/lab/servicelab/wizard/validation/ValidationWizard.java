/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.lab.servicelab.exception.InitializationException;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;
import org.grits.toolbox.lab.servicelab.wizard.validation.pages.FileUploadValidationPage;
import org.grits.toolbox.lab.servicelab.wizard.validation.pages.SampleValidationPage;
import org.grits.toolbox.lab.servicelab.wizard.validation.pages.TaskAssignmentPage;

/**
 * 
 *
 */
public class ValidationWizard extends Wizard
{
	private static Logger logger = Logger.getLogger(ValidationWizard.class);

	private String projectName = null;
	private Entry projectEntry = null;
	private File taskInfoFile = null;
	private File fileUploadInfoFile = null;

	private TaskAssignmentPage taskAssignmentPage = null;
	private SampleValidationPage sampleValidationPage = null;
	private IWizardPage fileUploadValidationPage = null;

	public ValidationWizard(Entry projectEntry, File taskInfoFile, File fileUploadInfoFile)
			throws InitializationException
	{
		logger.info("Creating Validation Wizard");
		if(projectEntry == null)
			throw new InitializationException("Error opening wizard",
					"Invalid selected entry. Selected entry is null.");

		if(!ProjectProperty.TYPE.equals(projectEntry.getProperty().getType()))
			throw new InitializationException("Error opening wizard",
					"Invalid selected entry. Selected entry is not a"
					+ " project entry : " + projectEntry.getDisplayName());

		if(taskInfoFile == null)
			throw new LoadingException("Error opening wizard",
					"Invalid Task Information File. TaskInfoFile : " + taskInfoFile);

		if(fileUploadInfoFile == null)
			throw new LoadingException("Error opening wizard",
					"Invalid File Upload Information File. FileUploadInfoFile : " + fileUploadInfoFile);

		projectName = projectEntry.getDisplayName();

		this.projectEntry = projectEntry;
		this.taskInfoFile = taskInfoFile;
		this.fileUploadInfoFile  = fileUploadInfoFile;
	}

	@Override
	public void addPages()
	{
		addPage(taskAssignmentPage  = new TaskAssignmentPage(projectEntry, taskInfoFile));
		addPage(sampleValidationPage  = new SampleValidationPage());
		addPage(fileUploadValidationPage = new FileUploadValidationPage(projectEntry, fileUploadInfoFile));

		getShell().setSize(850, 700);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		if(page == taskAssignmentPage)
		{
			return sampleValidationPage;
		}
		else if(page == sampleValidationPage)
		{
			return fileUploadValidationPage;
		}
		return null;
	}

	@Override
	public boolean performFinish()
	{
		return taskAssignmentPage.getErrorMessage() == null
				&& sampleValidationPage.getErrorMessage() == null
				&& fileUploadValidationPage.getErrorMessage() == null;
	}

	@Override
	public boolean canFinish()
	{
		// only finish from sample validation page 
		return getContainer().getCurrentPage() == fileUploadValidationPage
				&& fileUploadValidationPage.isPageComplete();
	}

	public String getProjectName()
	{
		return projectName;
	}
}
