/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.invoice.pages;

import java.io.File;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.lab.servicelab.invoice.MSWordInvoiceGenerator;
import org.grits.toolbox.lab.servicelab.model.ServiceLabInvoice;
import org.grits.toolbox.lab.servicelab.preference.ServiceLabPreferenceStore;
import org.grits.toolbox.lab.servicelab.wizard.invoice.InvoiceWizard;

/**
 * 
 *
 */
public class DownloadPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(DownloadPage.class);
	private static final String PAGE_NAME = "Download";

	public DownloadPage()
	{
		super(PAGE_NAME);
	}

	private ServiceLabInvoice serviceLabInvoice = null;
	private String downloadLocation = null;

	private Text downloadText = null;
	private ComboViewer fileTypeCombo = null;

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		// if navigating to this page refresh the values
		if(visible)
		{
			initialize();
		}
	}

	@Override
	public boolean isPageComplete()
	{
		refreshErrorStatus();
		return serviceLabInvoice != null && getErrorMessage() == null;
	}

	private void refreshErrorStatus()
	{
		if(downloadLocation == null || downloadLocation.isEmpty())
		{
			setErrorMessage("Select a valid download location.");
		}
		try
		{
			File downloadFile = Paths.get(downloadLocation).toFile();
			if(downloadFile.exists())
			{
				setMessage("There is already an existing file at download location."
						+ " Select another location or choose to overwrite.");
			}
			else
				setErrorMessage(null);
		} catch (Exception e)
		{
			setErrorMessage("Select a valid download location.");
		}
	}

	public void setServiceLabInvoice(ServiceLabInvoice serviceLabInvoice)
	{
		this.serviceLabInvoice = serviceLabInvoice;
	}

	private void initialize()
	{
		downloadText.setText(downloadLocation);
		setPageComplete(true);
	}

	public XWPFDocument getDocument()
	{
		return MSWordInvoiceGenerator.getInvoiceDocument(serviceLabInvoice);
	}

	public String getDownloadLocation()
	{
		return downloadLocation;
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("creating download page for the wizard");

		setTitle("Download Information ( " + ((InvoiceWizard) getWizard()).getProjectName() +" )");
		setDescription("Download information for the invoice");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		logger.info("download page created");
	}

	private void addUIParts(Composite container)
	{
		addLabel(container, "Download");
		downloadText = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 3;
		layoutData.minimumWidth = 200;
		downloadText.setLayoutData(layoutData);
		downloadText.setEnabled(false);

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setText("Browse Location");
		browseButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog saveDialog = new FileDialog(
						Display.getCurrent().getActiveShell(), SWT.SAVE);
				saveDialog.setFilterExtensions(new String[]{".docx"});
				saveDialog.setOverwrite(true);
				String location = downloadLocation;
				saveDialog.setFileName(location);
				if((location = saveDialog.open()) != null)
				{
					downloadLocation = location;
					downloadText.setText(downloadLocation);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});

		addLabel(container, "File Type");
		fileTypeCombo = new ComboViewer(container, SWT.BORDER | SWT.READ_ONLY);
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 4;
		layoutData.minimumWidth = 500;
		layoutData.heightHint = 100;
		fileTypeCombo.getCombo().setLayoutData(layoutData);
		fileTypeCombo.getCombo().setItems(new String[]{"MS Word Document"});
		fileTypeCombo.getCombo().select(0);
		fileTypeCombo.getCombo().setEnabled(false);

		// load location from preference
		downloadLocation = ServiceLabPreferenceStore.getDefaultInvoiceLocation();
		if(downloadLocation == null)
		{
			// if not in file set it to preset value
			downloadLocation = ServiceLabPreferenceStore.Preference
					.INVOICE_LOCATION.getPresetDefaultValue();
		}
		downloadText.setText(downloadLocation);
	}

	private void addLabel(Composite container, String labelTitle)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setText(labelTitle);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		gd.verticalAlignment = SWT.BEGINNING;
		label.setLayoutData(gd);
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
