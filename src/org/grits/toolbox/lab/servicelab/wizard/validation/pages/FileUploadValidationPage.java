/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.pages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.validation.FileUploadError;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.SampleExpValidationComparator;
import org.grits.toolbox.lab.servicelab.validation.ProtocolFileMatcher;
import org.grits.toolbox.lab.servicelab.wizard.validation.ValidationWizard;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.SampleExpProtocolsContentProvider;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.SampleFileValidationLabelProvider;

/**
 * 
 *
 */
public class FileUploadValidationPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(FileUploadValidationPage.class);
	private static final String PAGE_NAME = "File Upload Validation";

	private Entry projectEntry = null;
	private File fileUploadInfoFile = null;

	// a class that matches files to protocols for validation
	private ProtocolFileMatcher protocolFileMatcher = null;

	// map of sample and their sampleExpValidation object from previous page
	private Map<Entry, SampleExpValidation> entryToSampleValidationMap = null;

	// list of errors for protocols
	private List<FileUploadError> fileUploadErrors = new ArrayList<FileUploadError>();

	private TableViewer sampleListTableViewer = null;
	private TableViewer sampleProtocolsTableViewer = null;
	private TableViewer protocolFilesTableViewer = null;
	private Text fileInfoText = null;
	private Button downloadErrorButton = null;

	public FileUploadValidationPage(Entry projectEntry, File fileUploadInfoFile)
	{
		super(PAGE_NAME);
		setTitle(PAGE_NAME);
		this.projectEntry  = projectEntry;
		this.fileUploadInfoFile = fileUploadInfoFile;
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		// it is an independent page so initialize it only once
		if(visible && protocolFileMatcher == null)
		{
			initialize();
		}
	}

	private void initialize()
	{
		this.entryToSampleValidationMap =
				((SampleValidationPage) getPreviousPage()).getEntryToSampleValidationMap();
		List<SampleExpValidation> sampleExpValidations =
				new ArrayList<SampleExpValidation>(entryToSampleValidationMap.values());

		Collections.sort(sampleExpValidations, new SampleExpValidationComparator());
		// initialize protocol file matcher
		protocolFileMatcher = new ProtocolFileMatcher(
				projectEntry, fileUploadInfoFile, sampleExpValidations);
		// match files with protocols
		protocolFileMatcher.matchFiles();

		// get error messages to display
		fileUploadErrors.clear();
		fileUploadErrors.addAll(protocolFileMatcher.getFileUploadErrors());

		// checks for error messages from sampleToErrorMap while setting input
		sampleListTableViewer.setInput(sampleExpValidations);
		StructuredSelection selection = sampleExpValidations.isEmpty() ?
				null : new StructuredSelection(sampleExpValidations.iterator().next());
		sampleListTableViewer.setSelection(selection);

		// add all sample protocols as input
		protocolFilesTableViewer.setInput(new ArrayList<ArchivedFile>(
				protocolFileMatcher.getFileToProtocolMap().keySet()));

		refreshErrorStatus();
	}

	protected void refreshErrorStatus()
	{
		String errorMessage = null;
		String projectError = protocolFileMatcher.getProjectError();

		if(!fileUploadErrors.isEmpty())
		{
			// check first if there was a project error
			errorMessage = projectError != null ? projectError
					: fileUploadErrors.iterator().next().getFullErrorMessage();

			// add number of errors remaining
			if(fileUploadErrors.size() > 1)
			{
				errorMessage += " ...and " + (fileUploadErrors.size() - 1) + " more error";
				// remaining error is one less than its size
				if(fileUploadErrors.size() > 2)
					errorMessage += "s";
			}
		} // if it has no file upload error then ignore it as error
		else if(projectError != null)
		{
			// set it as a simple message
			setMessage(projectError);
		}

		sampleListTableViewer.refresh();

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
		downloadErrorButton.setEnabled(errorMessage != null);
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("creating file validation page for the wizard");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		// add all the ui components
		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		logger.info("file validation page created");
	}

	private void addUIParts(Composite container)
	{
		addLabel(container, ((ValidationWizard) getWizard()).getProjectName(), 3);

		// add a table for samples
		addSampleExpTablePart(container);

		// a table for protocols in a sample
		addProtocolsPart(container);

		// a table displaying all files that were assigned to selected protocol
		addProtocolFilesPart(container);

		// a text displaying information related to selected file in the file table
		addFileInfoPart(container);

		// add button for downloading errors
		addErrorDownloadButton(container);
	}

	private void addSampleExpTablePart(Composite container)
	{
		// this part displays all the samples that are there in a project
		logger.info("creating sample list table");

		sampleListTableViewer = createTableViewer(container,
				SWT.SINGLE| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 1, 3);

		TableViewerColumn columnViewer = new TableViewerColumn(sampleListTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Sample");
		columnViewer = new TableViewerColumn(sampleListTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Status");

		sampleListTableViewer.setContentProvider(new GenericListContentProvider());
		sampleListTableViewer.setLabelProvider(new SampleFileValidationLabelProvider(fileUploadErrors));
		sampleListTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 5, 2));
		sampleListTableViewer.getTable().setHeaderVisible(true);

		sampleListTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!sampleListTableViewer.getSelection().isEmpty())
				{
					SampleExpValidation sampleExpValidation = (SampleExpValidation) ((StructuredSelection)
							sampleListTableViewer.getSelection()).getFirstElement();
					// set this sample validation as input to table displaying tasks
					sampleProtocolsTableViewer.setInput(sampleExpValidation);

					// select a protocol for this sample
					StructuredSelection selection = sampleExpValidation.getProtocols().iterator().hasNext() ?
							new StructuredSelection(sampleExpValidation.getProtocols().iterator().next()) : null;

							sampleProtocolsTableViewer.setSelection(selection);
							sampleProtocolsTableViewer.refresh();
							fileInfoText.setText("");
				}
			}
		});
	}

	private void addProtocolsPart(Composite container)
	{
		// this part displays a table with all the protocols in a sample
		logger.info("creating sample protocols table");

		sampleProtocolsTableViewer = createTableViewer(container,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 1, 3);

		TableViewerColumn columnViewer = new TableViewerColumn(
				sampleProtocolsTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Protocols");

		sampleProtocolsTableViewer.setContentProvider(new SampleExpProtocolsContentProvider());
		sampleProtocolsTableViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof MinInfoProtocol ?
						((MinInfoProtocol) element).getLabel() : null;
			}
		});
		sampleProtocolsTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 1));
		sampleProtocolsTableViewer.getTable().setHeaderVisible(true);

		sampleProtocolsTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!sampleProtocolsTableViewer.getSelection().isEmpty())
				{
					protocolFilesTableViewer.refresh();
					fileInfoText.setText("");
				}
			}
		});
	}

	private void addProtocolFilesPart(Composite container)
	{
		// this part displays a table with all files set to a protocol
		logger.info("creating possible files for protocols table");

		protocolFilesTableViewer = createTableViewer(container,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 1, 1);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 1;
		tableLayoutData.verticalSpan = 1;
		tableLayoutData.heightHint = 350;
		protocolFilesTableViewer.getTable().setLayoutData(tableLayoutData);

		TableViewerColumn columnViewer = new TableViewerColumn(protocolFilesTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Matched Files");

		protocolFilesTableViewer.getTable().setHeaderVisible(true);

		protocolFilesTableViewer.setContentProvider(new GenericListContentProvider());
		protocolFilesTableViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof ArchivedFile ?
						((ArchivedFile) element).getTypicalArchivedFile().getFileName() : null;
			}
		});
		protocolFilesTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 1));

		protocolFilesTableViewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				if(element instanceof ArchivedFile)
				{
					MinInfoProtocol selectedProtocol = (MinInfoProtocol) ((StructuredSelection)
							sampleProtocolsTableViewer.getSelection()).getFirstElement();
					// if this file is assigned to selected protocol
					return protocolFileMatcher.getFileToProtocolMap().get(element) == selectedProtocol;
				}
				return false;
			}
		});

		protocolFilesTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!protocolFilesTableViewer.getSelection().isEmpty())
				{
					// set this protocol as input to text displaying file information
					ArchivedFile archivedFile = (ArchivedFile) ((StructuredSelection)
							protocolFilesTableViewer.getSelection()).getFirstElement();
					fileInfoText.setText(getFileInfo(archivedFile));
				}
			}
		});
	}

	private void addFileInfoPart(Composite container)
	{
		// this part displays a information for selected file in the above table
		logger.info("creating file information table");
		addLabel(container, "File Info", 1);

		fileInfoText  = new Text(container,
				SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 1;
		tableLayoutData.verticalSpan = 1;
		tableLayoutData.heightHint = 350;
		fileInfoText.setLayoutData(tableLayoutData);
	}

	private String getFileInfo(ArchivedFile archivedFile)
	{
		StringBuilder detailInfo = new StringBuilder();
		if(archivedFile != null)
		{
			detailInfo.append(archivedFile.getTypicalArchivedFile().getFileName());
			if(archivedFile.getTypicalArchivedFile().getDocumentType() != null)
			{
				detailInfo.append(System.lineSeparator()).append("----")
				.append(System.lineSeparator()).append("Document Type : ")
				.append(archivedFile.getTypicalArchivedFile().getDocumentType().getLabel())
				.append(System.lineSeparator()).append("Sub Type : ")
				.append(archivedFile.getTypicalArchivedFile().getDocumentType().getSelectedSubType());
			}

			if(archivedFile.getDescription() != null)
			{
				detailInfo.append(System.lineSeparator()).append("----")
				.append(System.lineSeparator()).append(archivedFile.getDescription());
			}
		}
		return detailInfo.toString();
	}

	private void addErrorDownloadButton(Composite container)
	{
		logger.info("creating download error button");
		// this part shows a button for removing or adding samples for a task
		downloadErrorButton = new Button(container, SWT.PUSH);
		downloadErrorButton.setText("Download Errors");
		downloadErrorButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.ERROR_ICON).createImage());
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.END;
		gd.verticalAlignment = SWT.BEGINNING;
		downloadErrorButton.setLayoutData(gd);
		final String initialFileName = ((ValidationWizard) getWizard()).getProjectName()
				+" File Errors";
		downloadErrorButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fileDialog = new FileDialog(container.getShell(), SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{".txt"});
				fileDialog.setText("Download Errors");
				fileDialog.setOverwrite(true);
				fileDialog.setFileName(initialFileName);
				String fileLocation = null;
				if((fileLocation = fileDialog.open()) != null)
				{
					downloadErrors(fileLocation);
					Program.launch(fileLocation);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
	}

	private void downloadErrors(String fileLocation)
	{
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(fileLocation);

			fileWriter.append("*****Errors Matching Protocols with Files*****").append(System.lineSeparator())
			.append("PROJECT - ").append(((ValidationWizard) getWizard()).getProjectName())
			.append(System.lineSeparator()).append("" + new Date()).append(System.lineSeparator())
			.append("----------------------------------------------").append(System.lineSeparator());

			for(FileUploadError fileUploadError : fileUploadErrors)
			{
				// add error message for this sample
				fileWriter.append(System.lineSeparator()).append("=> ")
				.append(fileUploadError.getFullErrorMessage()).append(System.lineSeparator());
			}

			String errorMessage = protocolFileMatcher.getProjectError();
			if(errorMessage != null)
			{
				fileWriter.append(System.lineSeparator()).append("--")
				.append(errorMessage).append(System.lineSeparator());
			}
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}
		finally
		{
			if(fileWriter != null)
			{
				try
				{
					fileWriter.close();
				} catch (IOException ex)
				{
					logger.error(ex.getMessage(), ex);
				}
			}
		}
	}

	private TableViewer createTableViewer(Composite container, int style,
			int horizontalSpan, int verticalSpan)
	{
		TableViewer tableViewer = new TableViewer(container, style);
		Table table = tableViewer.getTable();
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = horizontalSpan;
		tableLayoutData.verticalSpan = verticalSpan;
		tableLayoutData.heightHint = 350;
		table.setLayoutData(tableLayoutData);
		return tableViewer;
	}

	private void addLabel(Composite container, String labelTitle, int horizontalSpan)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		label.setText(labelTitle);
		GridData gd = new GridData();
		gd.horizontalSpan = horizontalSpan;
		gd.verticalAlignment = SWT.BEGINNING;
		label.setLayoutData(gd);
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
