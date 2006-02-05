//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars K�dderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class FinalParameterQuickfix implements ICheckstyleMarkerResolution
{

    public String getDescription()
    {
        return "Add final modifier to parameter declaration";
    }

    public String getLabel()
    {
        return "Change to final parameter";
    }

    public boolean canFix(IMarker marker)
    {
        if (marker.getAttribute("MessageKey", "-").equals("final.parameter"))
        {
            return true;
        }
        return false;
    }

    public Image getImage()
    {
        // TODO Remove dependency to jdt internal class
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    public void run(IMarker marker)
    {
        IResource resource = marker.getResource();

        int charStart = marker.getAttribute("charStart", -1);

        if (resource instanceof IFile && charStart >= 0)
        {
            IFile file = (IFile) resource;
            // TODO Remove dependency to jdt internal class
            JavaPlugin plugin = JavaPlugin.getDefault();

            IEditorInput input = new FileEditorInput(file);

            IDocument doc = plugin.getCompilationUnitDocumentProvider().getDocument(input);
            if (doc != null)
            {
                final InsertEdit edit = new InsertEdit(charStart, "final ");
                try
                {
                    edit.apply(doc);
                }
                catch (MalformedTreeException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (BadLocationException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
