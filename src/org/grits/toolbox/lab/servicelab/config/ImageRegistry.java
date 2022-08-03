/**
 * 
 */
package org.grits.toolbox.lab.servicelab.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.lab.servicelab.Activator;

/**
 * 
 *
 */
public class ImageRegistry
{
	private static Logger logger = Logger.getLogger(ImageRegistry.class);
	private static final String IMAGE_PATH = "icons" + File.separator;
	private static Map<PluginIcon, ImageDescriptor> imageCache = new HashMap<PluginIcon, ImageDescriptor>();

	public static ImageDescriptor getImageDescriptor(PluginIcon pluginIcon)
	{
		logger.info("Get icon from service lab plugin : " + pluginIcon);

		ImageDescriptor imageDescriptor = null;
		if(pluginIcon == null)
		{
			logger.error("Error loading icon from service lab plugin "
					+ "(plugin icon parameter is null)");
		}
		else
		{
			imageDescriptor = imageCache.get(pluginIcon);
			if(imageDescriptor == null)
			{
				logger.info("ImageDescriptor not found in cache");
				URL fullPathString = FileLocator.find(
						Platform.getBundle(Activator.PLUGIN_ID),
						new Path(IMAGE_PATH + pluginIcon.iconName), null);

				logger.info("Loading icon from url : " + fullPathString);
				if(fullPathString != null)
				{
					imageDescriptor = ImageDescriptor.createFromURL(fullPathString);
					imageCache.put(pluginIcon, imageDescriptor);
				}
			}
		}
		return imageDescriptor;
	}


	/**
	 ***********************************
	 *			Icons
	 ***********************************
	 */
	public enum PluginIcon
	{
		// Alexandre Moore ​http://www.sa-ki.deviantart.com GNU LGPL license
		// ​https://www.iconfinder.com/icons/1974/cog_gear_icon
		MANAGER_ICON("gear.png"),
		// Victor Erixon ​http://www.victorerixon.com
		// ​https://www.iconfinder.com/icons/106199/list_icon
		CHECKLIST_ICON("list-icon.png"),
		// Saki ​http://sa-ki.deviantart.com GNU GPL license
		// ​http://www.iconarchive.com/show/snowish-icons-by-saki/Ok-icon.html
		TICK_ICON("tick-icon.png"),
		// Saki ​http://sa-ki.deviantart.com GNU GPL license
		// ​http://www.iconarchive.com/show/snowish-icons-by-saki/Button-important-icon.html
		ERROR_ICON("error-icon.png"),
		// Icons Land ​http://www.icons-land.com	
		// ​https://www.iconfinder.com/icons/34248/achtung_alert_attention_exclamation_warning_icon
		WARNING_ICON("warning-icon.png"),
		// Anastasya Bolshakova ​https://www.iconfinder.com/nastu_bol
		// ​https://www.iconfinder.com/icons/1167965/arrow_arrows_arrowup_up_upload_icon
		UP_ARROW("up-arrow-icon.png"),
		// Anastasya Bolshakova ​https://www.iconfinder.com/nastu_bol
		// ​https://www.iconfinder.com/icons/1167964/arrow_arrowdown_arrows_down_download_icon
		DOWN_ARROW("down-arrow-icon.png");

		private String iconName = null;
		private PluginIcon(String iconName)
		{
			this.iconName  = iconName;
		}
	}


}
