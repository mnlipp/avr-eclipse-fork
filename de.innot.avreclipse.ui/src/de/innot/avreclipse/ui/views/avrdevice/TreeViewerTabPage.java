/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.ui.views.avrdevice;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CTabItem;

public class TreeViewerTabPage {

	private TreeViewer fTreeViewer = null;
	private CTabItem fTabItem = null;
	
	public void setTreeViewer(TreeViewer treeviewer) {
		fTreeViewer = treeviewer;
	}
	
	public TreeViewer getTreeViewer() {
		return fTreeViewer;
	}
	
	public void setCTabItem(CTabItem tabitem) {
		fTabItem = tabitem;
	}
	
	public CTabItem getCTabItem() {
		return fTabItem;
	}
}
