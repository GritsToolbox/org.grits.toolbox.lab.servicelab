/**
 * 
 */
package org.grits.toolbox.lab.servicelab.config;

/**
 * 
 *
 */
public interface IConfig
{
	// date format for various fields in this plugin
	public static final String DATE_FORMAT = "MM/dd/yyyy";

	// name of the folder inside workspace folder for service lab
	public static final String SERVICE_LAB_FOLDER_NAME = ".servicelab";

	// name of the file, inside service lab folder, containing the list of protocols
	public static final String PROTOCOL_PRICE_INFO_FILE_NAME = "protocol_price_info.xml";

	// name of the file, inside service lab folder, containing the list of tasks and related protocols
	public static final String TASK_PROTOCOL_INFO_FILE_NAME = "task_protocol_info.xml";

	// name of the file, inside service lab folder,
	// containing the list of protocols and related files to upload
	public static final String PROTOCOL_FILE_UPLOAD_INFO_FILE_NAME = "protocol_file_upload_info.xml";

	// name of the file, inside project's service lab folder,
	// containing a record of task assignment to various samples
	public static final String PROJECT_TASK_ASSIGNMENT_FILE_NAME = "task_sample_assignment.xml";

	// name of the file, inside project's service lab folder,
	// containing project statistics
	public static final String PROJECT_STATS_FILE_NAME = "project_stats.txt";

	// project stats key for bill amount
	public static final String PROJECT_STATS_BILL_AMOUNT = "project.bill.amount";
}
