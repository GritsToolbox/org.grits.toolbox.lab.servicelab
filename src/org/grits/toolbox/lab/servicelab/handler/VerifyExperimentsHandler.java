/**
 * 
 */
package org.grits.toolbox.lab.servicelab.handler;

import java.io.File;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.lab.servicelab.exception.InitializationException;
import org.grits.toolbox.lab.servicelab.validation.ValidationFilesLoader;
import org.grits.toolbox.lab.servicelab.wizard.validation.ValidationWizard;

/**
 * 
 *
 */
public class VerifyExperimentsHandler
{
	private static Logger logger = Logger.getLogger(VerifyExperimentsHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.lab.servicelab.command.verifyexperiments";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			IGritsDataModelService gritsDataModelService)
	{
		logger.info("Verifying experiments in project");
		if(entry == null)
		{
			logger.info("try getting selection from data model service's last selection");
			// for case when eclipse context has changed and selection has been reset
			// try getting the last selection from grits data model service
			if(gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().size() == 1
					&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			{
				logger.info("retrieved selection from data model service last selection");
				entry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
			}
		}

		if(entry != null && entry.getProperty() != null
				&& ProjectProperty.TYPE.equals(entry.getProperty().getType()))
		{
			logger.info(entry.getDisplayName());
			try
			{
				// check for service lab folder
				File serviceLabFolder = ValidationFilesLoader.loadServiceLabFolder(entry);
				//load task info file
				File taskInfoFile = ValidationFilesLoader.loadTaskInfoFile(serviceLabFolder);
				// load protocol upload file
				File fileUploadInfoFile = ValidationFilesLoader.loadProtocolFileInfoFile(serviceLabFolder);

				ValidationWizard wizard = new ValidationWizard(entry, taskInfoFile, fileUploadInfoFile);
				wizard.setWindowTitle("Validation Wizard");
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				if(dialog.open() == Window.OK) 
				{
					logger.info("successfully validated project : " + entry.getDisplayName());
				}

			} catch (InitializationException e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						e.getErrorTitle(), e.getErrorMessage());
			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Error verifying experiments",
						"Error verifying experiments in the project. Please contact"
						+ " developers for more information!");
			}
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IGritsDataModelService gritsDataModelService)
	{
		if(object == null)
		{
			// for case when eclipse context has changed and selection has been reset
			// try getting the last selection from grits data model service
			if(gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().size() == 1)
			{
				object = gritsDataModelService.getLastSelection().getFirstElement();
			}
		}

		if(object instanceof Entry)
		{
			Entry entry = (Entry) object;
			return entry.getProperty() instanceof ProjectProperty
					&& ((ProjectProperty) entry.getProperty()).isOpen();
		}
		return false;
	}
}
