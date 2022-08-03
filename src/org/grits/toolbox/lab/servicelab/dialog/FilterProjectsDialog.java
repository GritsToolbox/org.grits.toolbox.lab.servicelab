package org.grits.toolbox.lab.servicelab.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.core.datamodel.property.project.ProjectTasklist;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.service.IGritsDataModelService;

public class FilterProjectsDialog extends TitleAreaDialog {
	static final Logger logger = Logger.getLogger(FilterProjectsDialog.class);
	
	private Calendar calendar = Calendar.getInstance();
	
	List<Entry> selectedProjects = new ArrayList<>();

	//private DateTime fromDate;

	//private DateTime fromTime;

	//private DateTime toDate;

	//private DateTime toTime;

	private CDateTime from;
	private CDateTime to;
	private ComboViewer statusCombo;
	private IGritsDataModelService modelService;

	public FilterProjectsDialog(Shell parentShell, IGritsDataModelService gritsDataModelService) {
		super(parentShell);
		this.modelService = gritsDataModelService;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		logger.debug("START : Opening the filter Project Dialog");
		
		setTitle("Filter Projects");
		setMessage("Select timeframe to filter projects");
		// has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout(2, false);
		parent.setLayout(gridLayout);
		
		//first row
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		label.setText("Timeframe");
		
		//second row
		Label fromDateLabel = new Label(parent, SWT.NONE);
		fromDateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		fromDateLabel.setText("From");
		
		from = new CDateTime(parent, CDT.BORDER | CDT.COMPACT | CDT.DROP_DOWN | CDT.DATE_LONG | CDT.TIME_MEDIUM);
		calendar.add(Calendar.DATE, -1);
		from.setSelection(calendar.getTime());
		
		/*fromDate = new DateTime(parent, SWT.DATE);
		fromDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		fromDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fromTime = new DateTime(parent, SWT.TIME);
		fromTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
		fromTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));*/
		
		//third row
		Label toDateLabel = new Label(parent, SWT.NONE);
		toDateLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		toDateLabel.setText("To");
		
		to = new CDateTime(parent, CDT.BORDER | CDT.COMPACT | CDT.DROP_DOWN | CDT.DATE_LONG | CDT.TIME_MEDIUM);
		to.setSelection(new Date());
		
		/*toDate = new DateTime(parent, SWT.DATE);
		toDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		toDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		toTime = new DateTime(parent, SWT.TIME);
		toTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
		toTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));*/
		
		//fourth row
		Label label2 = new Label(parent, SWT.NONE);
		label2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
		label2.setText("Status of Tasks");
		
		new Label(parent, SWT.NONE);
		
		//fifth row
		statusCombo = new ComboViewer(parent, SWT.DROP_DOWN);
		statusCombo.getCombo().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
		statusCombo.setContentProvider(new ArrayContentProvider());
		statusCombo.setInput(ProjectPreferenceStore.getSingleChoicePreference(
				ProjectPreferenceStore.Preference.STATUS).getAllValues());
		
		new Label(parent, SWT.NONE);
		
		logger.debug("END   : Opening the filter Project Dialog");
		
		return parent;
	}
	
	@Override
	protected void okPressed() {
		// validate the input
		Date fromDate = from.getSelection();
		Date toDate = to.getSelection();
		int selected = statusCombo.getCombo().getSelectionIndex();
		
		if (fromDate == null) 
			setErrorMessage("Please select a \"from\" date");
		else if (toDate == null) 
			setErrorMessage("Please select a \"to\" date");
		else if (selected == -1)
			setErrorMessage("Please select a status");
		else {
			setErrorMessage(null);
			filterProjects (fromDate, toDate, statusCombo.getCombo().getItem(selected));
			super.okPressed();
		}
	}

	private void filterProjects(Date fromDate, Date toDate, String status) {
		Entry workspaceEntry = modelService.getRootEntry();
		if (workspaceEntry != null) {
			List<Entry> projectEntries = workspaceEntry.getChildren();
			if (projectEntries != null) {
				for (Entry project: projectEntries) {
					try {
						ProjectDetails projectDetails = ProjectDetailsHandler.getProjectDetails(project);
						Date modificationDate = projectDetails.getModificationTime();
						if (modificationDate != null && modificationDate.after(fromDate) && modificationDate.before(toDate)) {
							// check the status of the tasks
							List<ProjectTasklist> tasks = projectDetails.getTasklists();
							for (ProjectTasklist task: tasks) {
								if (task.getStatus().equals(status)) {
									if (task.getModifiedTime() != null && 
											task.getModifiedTime().after(fromDate) && task.getModifiedTime().before(toDate)) {
										// match
										selectedProjects.add(project);
										break;
									}
								}
							}
						}
					} catch (IOException e) {
						logger.error("Cannot get project details for project: " + project.getDisplayName(), e);
					}
				}
			}
		}
	}

	public List<Entry> getProjects() {
		return selectedProjects;
	}

}
