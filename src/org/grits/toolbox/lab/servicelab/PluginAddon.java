 
package org.grits.toolbox.lab.servicelab;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.grits.toolbox.lab.servicelab.preference.ServiceLabPreferenceStore;

/**
 * This class initializes local preference store class injected with grits preference store
 * to make it available to be used by preference dialog page. Local preference store class
 * caches preference values to minimize reading from file in grits main preference store.
 * 
 */
public class PluginAddon
{
	private Logger logger = Logger.getLogger(PluginAddon.class);

	@Inject
	@Optional
	public void applicationStarted(IEclipseContext eclipseContext)
	{
		try
		{
			logger.info("Loading Service Lab plugin Addon");
			logger.info("adding local preference store");
			eclipseContext.set(ServiceLabPreferenceStore.class,
					ContextInjectionFactory.make(ServiceLabPreferenceStore.class, eclipseContext));
			logger.info("Service Lab plugin Addon loaded");
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
	}

}
