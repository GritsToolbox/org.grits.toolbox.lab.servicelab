/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.invoice;

import java.io.File;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocolList;
import org.grits.toolbox.lab.servicelab.wizard.invoice.pages.DownloadPage;
import org.grits.toolbox.lab.servicelab.wizard.invoice.pages.InvoicePreviewPage;
import org.grits.toolbox.lab.servicelab.wizard.invoice.pages.WarningPage;

/**
 * 
 *
 */
public class InvoiceWizard extends Wizard
{
	private static Logger logger = Logger.getLogger(DownloadPage.class);

	private WarningPage warningPage = null;
	private InvoicePreviewPage invoicePreviewPage = null;
	private DownloadPage downloadPage = null;

	private Entry projectEntry = null;
	private PriceInfoProtocolList priceInfoProtocolList = null;

	private XWPFDocument document = null;
	private String downloadLocation = null;

	public InvoiceWizard(Entry entry, PriceInfoProtocolList priceInfoProtocolList) throws Exception
	{
		logger.info("creating invoice wizard");
		if(entry == null)
		{
			logger.error("null entry : " + entry);
			throw new Exception("null entry : " + entry);
		}

		if(priceInfoProtocolList == null)
		{
			logger.error("null priceInfoProtocol List : " + priceInfoProtocolList);
			throw new Exception("null priceInfoProtocol List : " + priceInfoProtocolList);
		}

		logger.info("selected entry : " + entry.getDisplayName());

		if(!ProjectProperty.TYPE.equals(entry.getProperty().getType()))
		{
			logger.error("selected entry id not a project but of type - "
					+ entry.getProperty().getType());
			throw new Exception("Not a project Entry " + entry.getDisplayName());
		}

		this.projectEntry  = entry;
		this.priceInfoProtocolList = priceInfoProtocolList;
	}

	@Override
	public void addPages()
	{
		addPage(warningPage = new WarningPage(projectEntry, priceInfoProtocolList));
		addPage(invoicePreviewPage = new InvoicePreviewPage());
		addPage(downloadPage = new DownloadPage());

		getShell().setSize(700, 750);
	}

	@Override
	public boolean performFinish()
	{
		logger.info("finishing invoice wizard");

		// check if document is there
		document = downloadPage.getDocument();
		if(document == null)
		{
			logger.error("cannot finish wizard with null document.");
			return false;
		}

		// proceed to verify download location
		try
		{
			downloadLocation = downloadPage.getDownloadLocation();
			logger.info("selected download location " + downloadLocation);

			File downloadFile = Paths.get(downloadLocation).toFile();
			// if there is already a file, then check for overwriting existing file
			if(downloadFile.exists())
			{
				logger.info("download location already exists" + downloadLocation);
				// if user selects to overwrite existing file, then finish it
				if(MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
						"Overwrite Existing File", "There is already an existing file at download location."
								+ " Do you want to overwrite the existing file?"))
				{
					logger.info("overwriting file at download location");
					return true;
				}
			}
			else // download location is valid
			{
				logger.info("verified invoice doument and download location");
				return true;
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			downloadPage.setErrorMessage("Select a valid download location.");
		}

		getContainer().showPage(downloadPage);
		return false;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		if(page == warningPage)
		{
			return invoicePreviewPage;
		}
		else if(page == invoicePreviewPage)
		{
			return downloadPage;
		}
		return null;
	}

	public String getProjectName()
	{
		return projectEntry.getDisplayName();
	}

	public XWPFDocument getDocument()
	{
		return document;
	}

	public String getDownloadLocation()
	{
		return downloadLocation ;
	}
}
