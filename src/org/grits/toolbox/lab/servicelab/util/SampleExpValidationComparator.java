/**
 * 
 */
package org.grits.toolbox.lab.servicelab.util;

import java.util.Comparator;

import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;

/**
 * 
 *
 */
public class SampleExpValidationComparator implements Comparator<SampleExpValidation>
{
	@Override
	public int compare(SampleExpValidation sev1, SampleExpValidation sev2)
	{
		int comparedValue = 0;
		if(sev1 == null || sev1.getSampleName() == null)
		{
			comparedValue = sev2 == null || sev2.getSampleName() == null ? 0 : -1;
		}
		else
		{
			if(sev2 == null || sev2.getSampleName() == null)
			{
				comparedValue = 1;
			}
			else
			{
				comparedValue = sev1.getSampleName().compareToIgnoreCase(sev2.getSampleName());
			}
		}
		return comparedValue;
	}
}
