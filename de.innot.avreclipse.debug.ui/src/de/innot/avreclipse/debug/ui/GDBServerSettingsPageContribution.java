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
/**
 * 
 */
package de.innot.avreclipse.debug.ui;

/**
 * @author Thomas Holland
 * @since 2.4
 *
 */
public class GDBServerSettingsPageContribution {
	
	private String fPageId;
	private String fName;
	private String fClassname;
	private String fBundleName;
	private IGDBServerSettingsPage fSettingsPage;
	
	
	/**
	 * @return the PageId
	 */
	public String getPageId() {
		return fPageId;
	}
	/**
	 * @param pageId the id attribute form the extension
	 */
	protected void setPageId(String pageId) {
		fPageId = pageId;
	}
	/**
	 * @return the name of the gdbserver for the user interface
	 */
	public String getName() {
		return fName;
	}
	/**
	 * @param name the name attribute from the extension
	 */
	protected void setName(String name) {
		fName = name;
	}
	/**
	 * @return the name of the java class implementing the extension
	 */
	public String getClassName() {
		return fClassname;
	}
	/**
	 * @param class1 the fClass to set
	 */
	protected void setClassName(String classname) {
		fClassname = classname;
	}
	/**
	 * @return the name of the bundle supplying the extension
	 */
	public String getBundleName() {
		return fBundleName;
	}
	/**
	 * @param bundleName the name of the bundle supplying the exptension
	 */
	protected void setBundleName(String bundleName) {
		fBundleName = bundleName;
	}
	/**
	 * @return the SettingsPage
	 */
	public IGDBServerSettingsPage getSettingsPage() {
		return fSettingsPage;
	}
	/**
	 * @param settingsPage the fSettingsPage to set
	 */
	protected void setSettingsPage(IGDBServerSettingsPage settingsPage) {
		fSettingsPage = settingsPage;
	}
	

	
}
