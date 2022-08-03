/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.model.TypicalArchivedFile;
import org.grits.toolbox.entry.archive.preference.KeywordPreference;
import org.grits.toolbox.entry.archive.preference.doctype.DocTypePreference;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelComparator;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelProvider;

/**
 * 
 *
 */
public class UploadBillDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(UploadBillDialog.class);

	private Text locationText = null;
	private Text displayNameText = null;
	private Text amountText = null;
	private ComboViewer personCombo = null;
	private ComboViewer documentTypeCombo = null;
	private ComboViewer documentSubTypeCombo = null;

	private ArchivedFile archivedFile = null;
	private double billAmount = 0.0;

	private String projectName = null;
	protected Set<String> existingNames = new HashSet<String>();

	private File sourceFile = null;

	public UploadBillDialog(Shell parentShell, String projectName, Set<String> existingNames)
	{
		super(parentShell);
		this.projectName = projectName;
		this.existingNames = existingNames;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Upload Bill ( " + projectName + " )");
		setMessage("Upload bill to project archive");
		getShell().setText("Upload Bill");
		getButton(OK).setText("Upload");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.info("creating upload bill dialog");

		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite container = new Composite(composite, SWT.BORDER);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout layout = new GridLayout(4, false);
		layout.marginTop = 10;
		layout.marginBottom = 40;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		container.setLayout(layout);

		locationText =  new Text(container, SWT.BORDER|SWT.BORDER_SOLID);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 3;
		textData.minimumWidth = 450;
		locationText.setLayoutData(textData);
		locationText.setEnabled(false);

		Button browseFileButton = new Button(container, SWT.PUSH);
		browseFileButton.setText(" Browse File ");
		GridData browseButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseButtonGridData.horizontalSpan = 1;
		browseFileButton.setLayoutData(browseButtonGridData);

		Label displayLabel = new Label(container, SWT.NONE);
		displayLabel.setText("Archive Name");
		displayNameText = new Text(container, SWT.BORDER);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		buttonData.horizontalAlignment = SWT.FILL;
		displayNameText.setLayoutData(buttonData);

		Label amountLabel = new Label(container, SWT.NONE);
		amountLabel.setText("Amount ($)");
		amountText = new Text(container, SWT.BORDER);
		buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		buttonData.horizontalAlignment = SWT.FILL;
		amountText.setLayoutData(buttonData);

		personCombo = createComboLine(container, "Person", 3);
		personCombo.setContentProvider(new ArrayContentProvider());

		documentTypeCombo = createComboLine(container, "Document Type", 3);
		documentTypeCombo.setContentProvider(new ArrayContentProvider());
		documentTypeCombo.setLabelProvider(new DocumentTypeLabelProvider());

		documentSubTypeCombo = createComboLine(container, "Sub Type", 3);
		documentSubTypeCombo.setContentProvider(new ArrayContentProvider());

		// initialize values for all the controls
		initializeValues();

		final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setText("Select File");
		browseFileButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				String selected = fileDialog.open();
				if (selected != null)
				{
					selected = selected.trim();
					locationText.setText(selected);
					try
					{
						sourceFile = Paths.get(locationText.getText()).toFile();
						displayNameText.setText(sourceFile.getName());
						displayNameText.selectAll();
						displayNameText.setFocus();
					} catch (InvalidPathException ex)
					{
						logger.error(ex);
						setErrorMessage("Select a valid file");
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		amountText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setErrorMessage(null);
				String doubleValueString = amountText.getText().trim();
				if(!doubleValueString.isEmpty())
				{
					try
					{
						billAmount = Double.parseDouble(doubleValueString);
						
					} catch (NumberFormatException ex)
					{
						logger.error(ex);
						setErrorMessage("Amount is not a valid number");
					}
				}
				else
				{
					billAmount = 0.0;
				}
			}
		});

		displayNameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setErrorMessage(null);
				String archiveName = displayNameText.getText();
				if(archiveName.isEmpty())
				{
					setErrorMessage("Archive file name is empty.");
				}
				else
				{
					if(existingNames.contains(archiveName))
					{
						setErrorMessage("Archive file name already exists. Please choose another name.");
					}
					else
					{
						archivedFile.getTypicalArchivedFile().setFileName(archiveName);
					}
				}
			}
		});

		personCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				String person = personCombo.getCombo().getText();
				person = person.isEmpty() ? null : person;
				archivedFile.setPerson(person);
			}
		});

		documentTypeCombo.getCombo().setEnabled(false);
		documentSubTypeCombo.getCombo().setEnabled(false);
		browseFileButton.setFocus();

		logger.info("upload bill dialog created");
		return composite;
	}

	private void initializeValues()
	{
		logger.info("initializing values in upload bill dialog");

		// create a new archived file with default values
		archivedFile = new ArchivedFile();
		TypicalArchivedFile typicalArchivedFile = new TypicalArchivedFile();
		archivedFile.setTypicalArchivedFile(typicalArchivedFile);
		Date currentDate = new Date();
		archivedFile.setSetDate(currentDate);
		archivedFile.setModifiedDate(currentDate);
		KeywordPreference.loadPreferences();
		archivedFile.setKeywords(new HashSet<String>(KeywordPreference.DEFAULT_KEYWORDS));

		// keep the file location empty
		locationText.setText("");

		// Bill archive name
		final String FINAL_BILL_LABEL = "Final Bill";
		String archiveName = FINAL_BILL_LABEL + ".pdf";
		int index = 2;
		while(existingNames.contains(archiveName))
		{
			archiveName = FINAL_BILL_LABEL + " (" + index + ").pdf";
			index++;
		}
		displayNameText.setText(archiveName);
		// set this value also to archived file
		typicalArchivedFile.setFileName(archiveName);

		// Bill Amount
		amountText.setText("0.0");
		// set this value to variable
		billAmount = 0.0;

		// Person
		SingleChoicePreference personPreference =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.PERSON);
		List<String> personList = new ArrayList<String>(personPreference.getAllValues());
		Collections.sort(personList, String.CASE_INSENSITIVE_ORDER);
		personCombo.setInput(personList);
		if(personPreference.getDefaultValue() != null)
		{
			personCombo.setSelection(new StructuredSelection(personPreference.getDefaultValue()));
			// set this value also to archived file
			archivedFile.setPerson(personPreference.getDefaultValue());
		}

		// Document type and subtype
		final String BILL_LABEL = "Bill";
		DocTypePreference.loadPreferences();
		List<DocumentType> documentTypes = new ArrayList<DocumentType>(
				DocTypePreference.ALL_DOCUMENT_TYPES);
		DocumentType billDocument = null;
		for(DocumentType documentType : documentTypes)
		{
			if(BILL_LABEL.equals(documentType.getLabel()))
			{
				billDocument = documentType;
				break;
			}
		}

		if(billDocument == null) // add new document type for bill
		{
			billDocument = new DocumentType();
			billDocument.setLabel(BILL_LABEL);
			billDocument.setSelectedSubType(null);
			documentTypes.add(billDocument);
		}
		else // copy this document type for bill
		{
			documentTypes.remove(billDocument);
			billDocument = billDocument.clone();
			billDocument.setSelectedSubType(null);
			documentTypes.add(billDocument);
		}

		Collections.sort(documentTypes, new DocumentTypeLabelComparator());
		List<String> subTypes = new ArrayList<String>(billDocument.getSubTypes());
		Collections.sort(subTypes, String.CASE_INSENSITIVE_ORDER);
		documentTypeCombo.setInput(documentTypes.toArray(new DocumentType[documentTypes.size()]));
		documentTypeCombo.setSelection(new StructuredSelection(billDocument));
		documentSubTypeCombo.setInput(subTypes.toArray(new String[subTypes.size()]));
		// set this value also to archived file
		typicalArchivedFile.setDocumentType(billDocument);

		logger.info("upload bill dialog values initialized");
	}

	protected void verifyInput()
	{
		String errorMessage = null;
		if(sourceFile == null)
		{
			errorMessage = "File location for bill is not set";
		}

		if(errorMessage == null)
		{
			// next try validating archive name
			displayNameText.setText(displayNameText.getText().trim());
			displayNameText.selectAll();
			displayNameText.setFocus();
			errorMessage = getErrorMessage();
		}

		if(errorMessage == null)
		{
			// next try validating amount
			amountText.setText(amountText.getText().trim());
			amountText.selectAll();
			amountText.setFocus();
			errorMessage = getErrorMessage();
		}

		setErrorMessage(errorMessage);
	}

	private ComboViewer createComboLine(Composite composite, String label, int horizontalSpan)
	{
		logger.info("create combo for " + label);

		Label labelLabel = new Label(composite, SWT.NONE);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.BEGINNING;
		labelLabel.setLayoutData(labelGridData);
		labelLabel.setText(label);

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData comboGridData = new GridData();
		comboGridData.grabExcessHorizontalSpace = true;
		comboGridData.horizontalAlignment = SWT.FILL;
		comboGridData.horizontalSpan = horizontalSpan;
		comboViewer.getCombo().setLayoutData(comboGridData);
		return comboViewer;
	}

	@Override
	public void setErrorMessage(String newErrorMessage)
	{
		super.setErrorMessage(newErrorMessage);
		getButton(OK).setEnabled(newErrorMessage == null);
	}

	@Override
	protected void okPressed()
	{
		verifyInput();
		if(getErrorMessage() == null)
			super.okPressed();
	}

	public File getSourceFile()
	{
		return sourceFile;
	}

	public ArchivedFile getArchivedFile()
	{
		return archivedFile;
	}

	public double getBillAmount()
	{
		return billAmount;
	}
}
