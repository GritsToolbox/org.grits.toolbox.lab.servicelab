/**
 * 
 */
package org.grits.toolbox.lab.servicelab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTasklist;

/**
 * 
 *
 */
public class ProtocolTaskManagerUtil
{
	private static Logger logger = Logger.getLogger(ProtocolTaskManagerUtil.class);

	public static ServiceLabTasklist getServiceLabTaskList(File taskInfoFile)
			throws IOException, JAXBException, UnsupportedEncodingException
	{
		if(taskInfoFile == null)
		{
			logger.fatal("Protocol Task Info File : protocol list file is null");
			return null;
		}

		if(!taskInfoFile.exists())
		{
			logger.fatal("Protocol Task Info File : protocol list file does not exist");
			throw new FileNotFoundException("Protocol Task Info File : protocol list file does not exist");
		}

		ServiceLabTasklist serviceLabTasklist = null;
		FileInputStream inputStream = null;
		try
		{
			logger.info("Task Protocol Util : reading task list from file");
			inputStream = new FileInputStream(taskInfoFile);
			InputStreamReader reader = new InputStreamReader(inputStream,
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			Unmarshaller unmarshaller = JAXBContext.newInstance(
					ServiceLabTasklist.class).createUnmarshaller();

			serviceLabTasklist  = (ServiceLabTasklist) unmarshaller.unmarshal(reader);
		} finally
		{
			if(inputStream != null)
			{
				inputStream.close();
			}
		}
		return serviceLabTasklist;
	}
}
