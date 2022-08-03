
package org.grits.toolbox.lab.servicelab.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.validation.ValidationFilesLoader;
import org.grits.toolbox.lab.servicelab.wizard.invoice.InvoiceWizard;

/**
 * 
 *
 */
@SuppressWarnings("restriction")
public class GenerateBillHandler
{
	private static final Logger logger = Logger.getLogger(GenerateBillHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.lab.servicelab.command.generatebill";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			IGritsDataModelService gritsDataModelService,
			EHandlerService handlerService, ECommandService commandService)
	{
		logger.info("Generating bill for project");
		if(entry == null)
		{
			logger.info("try getting selection from data model service's last selection");
			// for case when eclipse context has changed and selection has been reset
			// try getting the last selection from grits data model service.
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

				File protocolPriceInfoFile = new File(serviceLabFolder, IConfig.PROTOCOL_PRICE_INFO_FILE_NAME);
				if(!protocolPriceInfoFile.exists())
				{
					logger.error("No \"" + IConfig.PROTOCOL_PRICE_INFO_FILE_NAME + "\" file found.");
					throw new LoadingException("Missing Price Information File",
							"There is no file containing price information related to Protocols."
									+ " Please open Price Manager first for setting values!\n"
									+ "For opening Price Manager use menu : "
									+ "Tools -> Service Lab -> Open Manager -> Price Manager");
				}

				InvoiceWizard wizard = new InvoiceWizard(entry,
						ProtocolManagerUtil.getPriceInfoProtocolList(protocolPriceInfoFile));
				wizard.setWindowTitle("Invoice Wizard");
				WizardDialog dialog = new WizardDialog(
						Display.getCurrent().getActiveShell(), wizard);
				if(dialog.open() == Window.OK) 
				{
					logger.info("downloading and saving invoice");
					String downloadLocation = wizard.getDownloadLocation();
					saveInvoice(wizard.getDocument(), downloadLocation);

					logger.info("opening saved invoice");
					Program.launch(downloadLocation);
				}
			} catch (LoadingException e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						e.getErrorTitle(), e.getErrorMessage());
			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Error Generating Invoice", "Error while generating invoice :\n" + e.getMessage());
			}
		}
	}

	private void saveInvoice(XWPFDocument document, String downloadLocation) throws IOException
	{
		logger.info("saving invoice");
		FileOutputStream outputStream = null;
		try
		{
			outputStream = new FileOutputStream(downloadLocation);
			document.write(outputStream);
		} catch (IOException e)
		{
			logger.fatal("Error saving docx file to : " + downloadLocation);
			throw e;
		} finally
		{
			if(outputStream != null)
			{
				try
				{
					outputStream.close();
				} catch (IOException e)
				{
					logger.fatal(e.getMessage(), e);
					throw e;
				}
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
			// try getting the last selection from grits data model service.
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