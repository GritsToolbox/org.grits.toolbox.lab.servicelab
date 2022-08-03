/**
 * 
 */
package org.grits.toolbox.lab.servicelab.util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

/**
 * 
 *
 */
public class PriceCellEditor extends CellEditor
{
	/**
	 * creates a cell editor with spinner
	 * @param composite parent composite
	 * @param digits number of decimal places used by the spinner
	 * @param increment amount the spinner's value would be
	 * increased for each arrow press
	 */
	public PriceCellEditor(Composite composite,
			int digits, int increment)
	{
		super(composite);
		Spinner spinner = (Spinner) getControl();
		spinner.setDigits(digits);
		spinner.setIncrement(increment);
		spinner.setMinimum(0);
		spinner.setMaximum(Integer.MAX_VALUE);
	}

	@Override
	protected Control createControl(Composite parent)
	{
		return new Spinner(parent, SWT.BORDER);
	}

	@Override
	protected Object doGetValue()
	{
		Spinner spinner = (Spinner) getControl();
		return spinner.getSelection() /
				Math.pow(10, spinner.getDigits());
	}

	@Override
	protected void doSetFocus()
	{
		getControl().setFocus();
	}

	@Override
	protected void doSetValue(Object value)
	{
		Spinner spinner = (Spinner) getControl();
		double doubleValue = value instanceof Double ?
				((double) value) * Math.pow(10, spinner.getDigits()) : 0.0;
		spinner.setSelection((int) doubleValue);
	}
}
