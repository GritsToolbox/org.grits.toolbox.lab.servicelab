
package org.grits.toolbox.lab.servicelab.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.core.utils.WorkspaceXMLHandler;
import org.grits.toolbox.lab.servicelab.dialog.NewServiceLabProjectDialog;

@SuppressWarnings("restriction")
public class NewServiceLabProjectHandler
{
	private static final Logger	logger	= Logger.getLogger(NewServiceLabProjectHandler.class);
	
	@Inject
	ESelectionService			selectionService;
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, ECommandService commandService,
			EHandlerService handlerService, EPartService partService, IGritsDataModelService gritsDataModelService,
			IGritsUIService gritsUIService)
	{
		logger.info("creating a new project");
		NewServiceLabProjectDialog dialog = new NewServiceLabProjectDialog(PropertyHandler.getModalDialog(shell));
		if (dialog.open() == Window.OK)
		{
			try
			{
				Entry projectEntry = ProjectFileHandler.createProject(dialog.getProjectName(),
						dialog.getDescription(), dialog.getKeywords());

				try
				{
					WorkspaceXMLHandler.updateWorkspaceXMLFile(projectEntry);
					gritsDataModelService.addProjectEntry(projectEntry);
					gritsUIService.openEntryInPart(projectEntry);
				}
				catch (FileNotFoundException e)
				{
					logger.fatal(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(shell, "Workspace file is missing.\n" + e.getMessage(), e);
				}
				catch (Exception e)
				{
					logger.fatal(e.getMessage(), e);
					ErrorUtils.createErrorMessageBox(shell, "Error updating workspace.\n" + e.getMessage(), e);
				}
			}
			catch (FileAlreadyExistsException e)
			{
				logger.fatal(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(shell,
						"There is a project folder already in the workspace with this name." + dialog.getProjectName()
								+ "Please delete this project first.\n" + e.getMessage(),
						e);
			}
			catch (IOException e)
			{
				logger.fatal(e.getMessage(), e);
				ErrorUtils.createErrorMessageBox(shell, "Error creating project.\n" + e.getMessage(), e);
			}
		}
	}
}