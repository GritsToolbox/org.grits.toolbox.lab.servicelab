/**
 * 
 */
package org.grits.toolbox.lab.servicelab.preference;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;

/**
 * 
 *
 */
@Singleton
public class ServiceLabPreferenceStore
{
	private static final Logger logger = Logger.getLogger(ServiceLabPreferenceStore.class);

	@Inject private static IGritsPreferenceStore gritsPreferenceStore;

	/**
	 * map storing preference values as a cache to minimize file
	 * transactions from grits preference store
	 */
	private static HashMap<String, String> preferenceCacheMap = new HashMap<String, String>();

	/**
	 * This enum contains a set of preference variables for billing and other
	 * related information.
	 * It has a preference variable name and a preset default value - the
	 * value that is loaded on restore defaults.
	 */
	public static enum Preference
	{
		INVOICE_LOCATION(Preference.class.getCanonicalName() + ".invoiceLocation",
				System.getProperty("user.home") + File.separator + "Invoice.docx");

		private String preferenceName = null;
		private String presetDefaultValue = null;
		private Preference(String preferenceName, String presetDefaultValue)
		{
			this.preferenceName  = preferenceName;
			this.presetDefaultValue = presetDefaultValue;
		}

		public String getPreferenceName()
		{
			return preferenceName;
		}

		public String getPresetDefaultValue()
		{
			return presetDefaultValue;
		}
	}

	/**
	 * returns default invoice download location or null if not in the preference file
	 * @return
	 */
	public static String getDefaultInvoiceLocation()
	{
		String preferenceName = Preference.INVOICE_LOCATION.getPreferenceName();
		String preferenceValue = preferenceCacheMap.get(preferenceName);
		if(preferenceValue == null)
		{
			try // try loading from preference file
			{
				PreferenceEntity preferenceEntity =
						gritsPreferenceStore.getPreferenceEntity(preferenceName);
				if(preferenceEntity != null)
				{
					preferenceValue = preferenceEntity.getValue();
					preferenceCacheMap.put(preferenceName, preferenceValue);
				}
			} catch (UnsupportedVersionException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		return preferenceValue;
	}

	/**
	 * save a preference variable to preference file
	 * @param preference
	 * @param value
	 * @return
	 */
	public static boolean savePreference(Preference preference, String value)
	{
		logger.info("Saving billing preference : " + preference.getPreferenceName());
		PreferenceEntity preferenceEntity = new PreferenceEntity(
				preference.getPreferenceName());
		preferenceEntity.setValue(value);

		// update preference cache
		preferenceCacheMap.put(preference.getPreferenceName(), value);
		return gritsPreferenceStore.savePreference(preferenceEntity);
	}

	/**
	 * This contains information about billing price type and each of its
	 * enum constants has methods to read and save values
	 */
	public static enum BillingPriceType
	{
		INDUSTRY_PRICE("Industry Price"), NON_PROFIT_PRICE("Non-Profit Price");

		// name of the variable that is stored in preference file
		public static final String variableName =
				BillingPriceType.class.getCanonicalName() + ".default";
		// value that is the fall back for default value, similar to values
		// stored in default preference file and used for Restore Defaults
		public static final BillingPriceType PRESET_DEFAULT_TYPE = INDUSTRY_PRICE;
		// a default selected value among all the enum constants
		private static BillingPriceType defaultType = null;
		private String displayName = null;

		private BillingPriceType(String name)
		{
			this.displayName = name;
		}

		/**
		 * @return the displayName
		 */
		public String getDisplayName()
		{
			return displayName;
		}

		/**
		 * @return the defaultType
		 */
		public static BillingPriceType getDefaultType()
		{
			if(defaultType == null) // load from preference file
			{
				try // try loading from file
				{
					logger.info("loading default billing type from file");
					PreferenceEntity preferenceEntity =
							gritsPreferenceStore.getPreferenceEntity(variableName);
					// set the value to the variable
					if(preferenceEntity != null)
					{
						defaultType = getBillTypeByDisplayName(preferenceEntity.getValue());
					}
				} catch (UnsupportedVersionException e)
				{
					logger.error(e.getMessage(), e);
				}

				// if it was not saved yet set the preset default value as default 
				if(defaultType == null)
				{
					PRESET_DEFAULT_TYPE.saveAsDefaultType();
					defaultType = PRESET_DEFAULT_TYPE;
				}
			}

			logger.debug("default billing type : " + defaultType);
			return defaultType;
		}

		@Optional @Inject
		public boolean saveAsDefaultType()
		{
			logger.info("Saving default price type as " + displayName);
			PreferenceEntity preferenceEntity = new PreferenceEntity(variableName);
			preferenceEntity.setValue(displayName);
			if(gritsPreferenceStore.savePreference(preferenceEntity))
			{
				// update the variable and return true
				defaultType = this;
				return true;
			}
			else // if not successful
				return false;
		}

		public static BillingPriceType getBillTypeByDisplayName(final String preferenceValue)
		{
			logger.info("find preference " + preferenceValue);
			BillingPriceType billingPriceType = null;
			for(BillingPriceType thisBillType : values())
			{
				// check which of the bill type equals this preference value
				if(thisBillType.equals(preferenceValue))
				{
					billingPriceType = thisBillType;
					break;
				}
			}
			return billingPriceType;
		}

		public boolean equals(final BillingPriceType preferenceValue)
		{
			logger.info("compare preference \"" + preferenceValue);
			logger.info("\" with \"" + name() +"\"");
			return preferenceValue!= null &&
					(name().equals(preferenceValue.name())
					|| displayName.equals(preferenceValue.getDisplayName()));
		}

		public boolean equals(final String preferenceValue)
		{
			logger.info("compare preference \"" + preferenceValue);
			logger.info("\" with \"" + name() +"\"");
			return name().equalsIgnoreCase(preferenceValue)
					|| displayName.equalsIgnoreCase(preferenceValue);
		}
	}
}
