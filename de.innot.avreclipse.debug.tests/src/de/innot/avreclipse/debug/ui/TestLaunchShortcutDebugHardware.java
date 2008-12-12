/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id$
 *     
 *******************************************************************************/

package de.innot.avreclipse.debug.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestLaunchShortcutDebugHardware extends AbstractAVRDebugTest {

	private final static String			DEBUG	= ILaunchManager.DEBUG_MODE;
	private final static String			RUN		= ILaunchManager.RUN_MODE;
	private final static String			PROFILE	= ILaunchManager.PROFILE_MODE;

	private TestLaunchShortcut			fTestLaunchShortcut;

	private IBinary						fTestResultBinary;
	private String						fTestResultMode;

	private List<IBinary>				fTestSelectBinaries;
	private List<ILaunchConfiguration>	fTestSelectLaunchConfigurations;

	private class TestLaunchShortcut extends LaunchShortcutDebugHardware {

		@Override
		public void launch(IBinary binary, String mode) {
			fTestResultBinary = binary;
			fTestResultMode = mode;
		}

		@Override
		public IBinary selectBinary(List<IBinary> binList, String mode) {
			// Don't know how to test the UI.
			// for know we log the list of binaries and return the first one
			fTestSelectBinaries = binList;
			return binList.get(0);
		}

		@Override
		public ILaunchConfiguration selectConfiguration(IBinary bin,
				List<ILaunchConfiguration> configs, String mode) {
			// Don't know how to test the UI.
			// for know we log the list of binaries and return the first one
			fTestSelectLaunchConfigurations = configs;
			return configs.get(0);
		}

		@Override
		public IBinary searchBinary(Object[] elements, String mode) {
			// TODO Auto-generated method stub
			return super.searchBinary(elements, mode);
		}

		@Override
		protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
			// TODO Auto-generated method stub
			return super.findLaunchConfiguration(bin, mode);
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@Before
	public void setUp() throws Exception {

		fTestLaunchShortcut = new TestLaunchShortcut();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.ui.LaunchShortcutDebugHardware#searchBinary(java.lang.Object[], java.lang.String)}
	 * .
	 */
	@Test
	public void testSearchBinary() {

		Object[] elements = new Object[1];

		// Test with valid inputs
		elements[0] = fTestProject.getProject();

		IBinary binary = fTestLaunchShortcut.searchBinary(elements, DEBUG);
		assertNotNull("Could not find binary", binary);
		assertTrue(fTestProject.equals(binary.getCProject()));

		assertEquals("found wrong IBinary extension", "elf", binary.getPath().getFileExtension());

		assertNull("More than one binary found", fTestSelectBinaries);

		fTestResultBinary = binary;

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.ui.LaunchShortcutDebugHardware#findLaunchConfiguration(org.eclipse.cdt.core.model.IBinary, java.lang.String)}
	 * .
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testFindLaunchConfiguration() throws CoreException {

		if (fTestResultBinary == null) {
			testSearchBinary();
		}

		// This should return a valid launch configuration
		ILaunchConfiguration lc = fTestLaunchShortcut.findLaunchConfiguration(fTestResultBinary,
				ILaunchManager.DEBUG_MODE);
		assertNotNull(lc);
		assertNull("More than one launch configuration found", fTestSelectLaunchConfigurations);

		// Test the attributes
		assertEquals("Debug/" + fProjectName + ".elf", lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""));
		assertEquals(fProjectName, lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));

		assertEquals(CDebugCorePlugin.getDefault().getDebugConfiguration(
				IAVRGDBConstants.DEBUGGER_ID).getID(), lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""));

		ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(
				fTestProject.getProject());
		String buildConfigID = projDes.getActiveConfiguration().getId();

		assertEquals(buildConfigID, lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""));

		assertNull(lc.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				(String) null));

		assertEquals(true, lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false));
		assertEquals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN, lc.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ""));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.ui.LaunchShortcutDebugHardware#createNewConfiguration(org.eclipse.cdt.core.model.IBinary, java.lang.String)}
	 * .
	 */
	@Test
	public void testCreateNewConfiguration() {
		// already tested with testFindLaunchConfiguration()
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.ui.LaunchShortcutDebugHardware#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)}
	 * .
	 */
	@Test
	public void testLaunch() {

		IStructuredSelection selection = new StructuredSelection(new Object[] { fTestProject
				.getProject() });
		fTestResultMode = null; // will be overwritten by our custom launch(binary, mode)
		fTestResultBinary = null; // dito

		// These two will log an error
		fTestLaunchShortcut.launch(selection, RUN);
		fTestLaunchShortcut.launch(selection, PROFILE);
		assertNull("Did not abort on RUN or PROFILE", fTestResultMode);

		// Now a normal launch
		fTestLaunchShortcut.launch(selection, DEBUG);
		assertEquals("Magic mode change", DEBUG, fTestResultMode);
		assertNotNull("No binary found", fTestResultBinary);

		assertEquals("Wrong binary", "elf", fTestResultBinary.getPath().getFileExtension());

	}

}
