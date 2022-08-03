package org.grits.toolbox.lab.servicelab.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.lab.servicelab.preference.ServiceLabPreferenceStore.BillingPriceType;

public class BillingPreferencePage extends PreferencePage
{
	private static final Logger logger = Logger.getLogger(BillingPreferencePage.class);

	private Button [] billingTypeButtons = null;
	private Text downloadLocationText = null;

	public void initializeValues()
	{
		logger.info("initializing preferences : "
				+ ServiceLabPreferenceStore.Preference.INVOICE_LOCATION.getPreferenceName()
				+ ", " + BillingPriceType.variableName);

		// initialize default billing type from preference store
		BillingPriceType billingPriceType = BillingPriceType.getDefaultType();
		// get the name of this enum
		String billingTypeValue = billingPriceType.getDisplayName();

		logger.info("preference default billing type " + billingTypeValue);
		for(Button billingTypeButton : billingTypeButtons)
		{
			billingTypeButton.setSelection(
					billingTypeValue.equals(billingTypeButton.getText()));
		}

		// initialize default download location from preference store
		String downloadLocation = ServiceLabPreferenceStore.getDefaultInvoiceLocation();
		if(downloadLocation == null)
		{
			// copy the default location value and save it
			downloadLocation = ServiceLabPreferenceStore
					.Preference.INVOICE_LOCATION.getPresetDefaultValue();
			ServiceLabPreferenceStore.savePreference(
					ServiceLabPreferenceStore.Preference
					.INVOICE_LOCATION, downloadLocation);
		}
		logger.info("preference default download location " + downloadLocation);
		downloadLocationText.setText(downloadLocation);
	}

	@Override
	protected Control createContents(Composite parent)
	{
		logger.info("adding ui for billing preference page");

		Composite container = new Composite(parent, SWT.BEGINNING);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		container.setLayout(layout);

		final Font boldFont = JFaceResources.getFontRegistry()
				.getBold(JFaceResources.DEFAULT_FONT);

		// default type
		Composite defaultTypeContainer = new Composite(container, SWT.NONE);
		defaultTypeContainer.setLayout(new GridLayout(2, true));
		defaultTypeContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Label label = new Label(defaultTypeContainer, SWT.NONE);
		label.setFont(boldFont);
		label.setText("Default Price ");
		label.setToolTipText("Default price type for billing protocols");
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);

		billingTypeButtons = new Button[BillingPriceType.values().length];
		int i = 0;
		for(BillingPriceType billingPriceType : BillingPriceType.values())
		{
			billingTypeButtons[i] = new Button(defaultTypeContainer, SWT.RADIO);
			billingTypeButtons[i].setText(billingPriceType.getDisplayName());
			billingTypeButtons[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			i++;
		}

		// default invoice download
		Composite downloadContainer = new Composite(container, SWT.FILL);
		downloadContainer.setLayout(new GridLayout(3, false));
		downloadContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		label = new Label(downloadContainer, SWT.NONE);
		label.setFont(boldFont);
		label.setText("Download Invoice");
		label.setToolTipText("Default location for downloading invoice");
		layoutData = new GridData();
		layoutData.horizontalSpan = 3;
		label.setLayoutData(layoutData);

		downloadLocationText = new Text(downloadContainer, SWT.BORDER | SWT.READ_ONLY);
		downloadLocationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Button browseButton = new Button(downloadContainer, SWT.PUSH);
		browseButton.setText("Browse");
		browseButton.setToolTipText("Select default location for downloading invoice");
		GridData buttonLayoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
		buttonLayoutData.widthHint = 100;
		browseButton.setLayoutData(buttonLayoutData);

		browseButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// open a file browser in save mode
				FileDialog saveDialog = new FileDialog(
						Display.getCurrent().getActiveShell(), SWT.SAVE);
				saveDialog.setFilterExtensions(new String[]{".docx"});
				String location = downloadLocationText.getText();
				saveDialog.setFileName(location);
				if((location = saveDialog.open()) != null)
				{
					downloadLocationText.setText(location);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});

		logger.info("added ui for billing preference page");

		// initialize the ui with values
		initializeValues();
		return container;
	}

	@Override
	protected void performDefaults()
	{
		logger.info("resetting preferences to default");
		// select the button with preset default value
		for(Button billingTypeButton : billingTypeButtons)
		{
			billingTypeButton.setSelection(BillingPriceType.PRESET_DEFAULT_TYPE
					.equals(billingTypeButton.getText()));
		}

		// set the value to preset default value
		downloadLocationText.setText(ServiceLabPreferenceStore
				.Preference.INVOICE_LOCATION.getPresetDefaultValue());
	}

	@Override
	public boolean performOk()
	{
		logger.info("ok pressed");
		logger.info("Errors on page : " + getErrorMessage());
		return getErrorMessage() == null ? save() : false;
	}

	private boolean save()
	{
		logger.info("saving preferences");
		// get the set values from displayed values in controls

		// choose the value from the radio button that is selected
		String billingTypeValue = null;
		for(Button billingTypeButton : billingTypeButtons)
		{
			if(billingTypeButton.getSelection())
			{
				billingTypeValue = billingTypeButton.getText();
				break;
			}
		}

		BillingPriceType billingPriceType = BillingPriceType
				.getBillTypeByDisplayName(billingTypeValue);
		if(billingPriceType == null)
		{
			// if selected name was invalid and not found set it to preset default value
			billingPriceType = BillingPriceType.PRESET_DEFAULT_TYPE;
		}

		// get download location from text control
		String downloadLocation = downloadLocationText.getText().trim();

		// save these values using preference store
		return billingPriceType.saveAsDefaultType()
				&& ServiceLabPreferenceStore.savePreference(ServiceLabPreferenceStore
						.Preference.INVOICE_LOCATION, downloadLocation);
	}
}
