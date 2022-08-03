package org.grits.toolbox.lab.servicelab.model;

import java.util.Comparator;


public class InvoiceComponentComparator implements Comparator<InvoiceComponent>
{
	@Override
	public int compare(InvoiceComponent invoiceComponent1, InvoiceComponent invoiceComponent2)
	{
		int comparedValue = 0;
		if(invoiceComponent1.getProtocolName() == null)
		{
			comparedValue = invoiceComponent2.getProtocolName() == null ? 0 : 1;
		}
		else
		{
			if(invoiceComponent2.getProtocolName() == null)
			{
				comparedValue = -1;
			}
			else
			{
				comparedValue = invoiceComponent1.getProtocolName().compareTo(invoiceComponent2.getProtocolName());
			}
		}
		return comparedValue;
	}
}