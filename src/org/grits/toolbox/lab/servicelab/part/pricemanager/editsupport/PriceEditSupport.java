package org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.util.PriceCellEditor;

/**
 * 
 *
 */
public class PriceEditSupport extends EditingSupport
{
	private static final Logger logger = Logger.getLogger(PriceEditSupport.class);

	public enum PriceCategory
	{
		INDUSTRY_PRICE, NON_PROFIT_PRICE;
	}

	private MDirtyable dirtyable = null;
	private PriceCellEditor priceCellEditor = null;
	private PriceCategory priceCategory = PriceCategory.INDUSTRY_PRICE;

	public PriceEditSupport(TableViewer tableViewer,
			MDirtyable dirtyable)
	{
		super(tableViewer);
		this.dirtyable  = dirtyable;
		priceCellEditor = new PriceCellEditor(tableViewer.getTable(), 2, 10);
	}

	public PriceEditSupport(TableViewer tableViewer,
			MDirtyable dirtyable, PriceCategory priceCategory)
	{
		this(tableViewer, dirtyable);
		this.priceCategory = priceCategory;
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return priceCellEditor;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}

	@Override
	protected Object getValue(Object element)
	{
		if(element instanceof CostParameter)
		{
			CostParameter costParameter = (CostParameter) element;
			switch (priceCategory)
			{
				case INDUSTRY_PRICE:
					return costParameter.getIndustryPrice();
				case NON_PROFIT_PRICE:
					return costParameter.getNonProfitPrice();
				default:
					logger.fatal("price category not known : " + priceCategory);
			}
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		double doubleValue = (double) value;
		if (element instanceof CostParameter)
		{
			CostParameter costParameter = (CostParameter) element;
			if(!costParameter.getCostParameterName().equals(doubleValue))
			{
				switch (priceCategory)
				{
					case INDUSTRY_PRICE:
						if(costParameter.getIndustryPrice() != doubleValue)
						{
							costParameter.setIndustryPrice(doubleValue);
							getViewer().update(element, null);
							dirtyable.setDirty(true);
						}
						break;

					case NON_PROFIT_PRICE:
						if(costParameter.getNonProfitPrice() != doubleValue)
						{
							costParameter.setNonProfitPrice(doubleValue);
							getViewer().update(element, null);
							dirtyable.setDirty(true);
						}
						break;

					default:
						logger.fatal("price category not known : " + priceCategory);
						break;
				}
			}
		}
	}
}
