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
