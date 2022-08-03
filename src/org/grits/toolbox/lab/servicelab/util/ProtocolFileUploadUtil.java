/**
 * 
 */
package org.grits.toolbox.lab.servicelab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocolList;

/**
 * 
 *
 */
public class ProtocolFileUploadUtil
{
	private static Logger logger = Logger.getLogger(ProtocolFileUploadUtil.class);

	public static FileInfoProtocolList getFileInfoProtocolList(File protocolListFile)
			throws IOException, JAXBException
	{
		if(protocolListFile == null)
		{
			logger.fatal("Protocol File manager : protocol list file is null");
			return null;
		}

		if(!protocolListFile.exists())
		{
			logger.fatal("Protocol File manager : protocol list file does not exist");
			return null;
		}

		FileInfoProtocolList fileInfoProtocolList = null;
		FileInputStream inputStream = null;
		try
		{
			logger.info("Protocol File manager : reading protocols list from file");
			inputStream = new FileInputStream(protocolListFile);
			InputStreamReader reader = new InputStreamReader(inputStream,
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			Unmarshaller unmarshaller = JAXBContext.newInstance(
					FileInfoProtocolList.class).createUnmarshaller();

			fileInfoProtocolList  = (FileInfoProtocolList) unmarshaller.unmarshal(reader);
		} finally
		{
			if(inputStream != null)
			{
				inputStream.close();
			}
		}
		return fileInfoProtocolList;
	}
}
