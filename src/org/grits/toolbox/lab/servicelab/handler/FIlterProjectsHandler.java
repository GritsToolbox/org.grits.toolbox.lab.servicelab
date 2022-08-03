package org.grits.toolbox.lab.servicelab.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.lab.servicelab.dialog.FilterProjectsDialog;
import org.grits.toolbox.lab.servicelab.dialog.FilterProjectsResultsDialog;

public class FIlterProjectsHandler {
	
	private static final Logger logger = Logger.getLogger(FIlterProjectsHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.lab.servicelab.command.filterprojects";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IGritsDataModelService gritsDataModelService) {
		logger.debug("BEGIN: executing " + COMMAND_ID);
		try {
			FilterProjectsDialog dialog = new FilterProjectsDialog(shell, gritsDataModelService);
			if (dialog.open() == Window.OK) {
				FilterProjectsResultsDialog resultDialog = new FilterProjectsResultsDialog(shell);
				resultDialog.setResults (dialog.getProjects());
				resultDialog.open();
			}
		} catch (Exception e) {
			logger.error("Error executing " + COMMAND_ID, e);
		}
		
		logger.debug("END: executing " + COMMAND_ID);
	}
}
