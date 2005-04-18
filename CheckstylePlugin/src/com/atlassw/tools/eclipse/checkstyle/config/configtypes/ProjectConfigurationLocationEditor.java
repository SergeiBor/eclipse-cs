//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * Implementation of a file based location editor. Contains a text field with
 * the config file path and a 'Browse...' button opening a file dialog.
 * 
 * @author Lars K�dderitzsch
 */
public class ProjectConfigurationLocationEditor implements IConfigurationLocationEditor
{

    //
    // attributes
    //

    /** text field containing the location. */
    private Text mLocation;

    /** browse button. */
    private Button mBtnBrowse;

    //
    // methods
    //

    /**
     * @see IConfigurationLocationEditor#createEditorControl(java.awt.Composite)
     */
    public Control createEditorControl(Composite parent, final Shell shell)
    {

        Composite contents = new Composite(parent, SWT.NULL);
        contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        contents.setLayout(layout);

        mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        mLocation.setLayoutData(gd);

        mBtnBrowse = new Button(contents, SWT.PUSH);
        mBtnBrowse.setText("Browse...");
        mBtnBrowse.setLayoutData(new GridData());

        mBtnBrowse.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
                        new WorkbenchLabelProvider(), new WorkbenchContentProvider());
                dialog.setTitle("Select checkstyle configuration file");
                dialog.setMessage("Select the configuration file for this check configuration.");
                dialog.setBlockOnOpen(true);
                dialog.setAllowMultiple(false);
                dialog.setInput(CheckstylePlugin.getWorkspace().getRoot());
                dialog.setValidator(new ISelectionStatusValidator()
                {
                    public IStatus validate(Object[] selection)
                    {
                        if (selection.length == 1 && selection[0] instanceof IFile)
                        {
                            return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.ERROR, "",
                                    null);
                        }
                        else
                        {
                            return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
                                    "", null);
                        }
                    }
                });
                if (ElementTreeSelectionDialog.OK == dialog.open())
                {
                    Object[] result = dialog.getResult();
                    IFile checkFile = (IFile) result[0];
                    mLocation.setText(checkFile.getProjectRelativePath().toString());
                }
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            //NOOP
            }
        });

        return contents;
    }

    /**
     * @see IConfigurationLocationEditor#getLocation()
     */
    public String getLocation()
    {
        return mLocation.getText();
    }

    /**
     * @see IConfigurationLocationEditor#setLocation(java.lang.String)
     */
    public void setLocation(String location)
    {
        mLocation.setText(location);
    }

    /**
     * @see IConfigurationLocationEditor#setEditable(boolean)
     */
    public void setEditable(boolean editable)
    {
        mLocation.setEditable(editable);
        mBtnBrowse.setEnabled(editable);
    }
}