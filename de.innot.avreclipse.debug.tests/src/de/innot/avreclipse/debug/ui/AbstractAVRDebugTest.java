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

import static org.junit.Assert.fail;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import de.innot.avreclipse.debug.TestProjectCreator;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AbstractAVRDebugTest {

	protected static ICProject	fTestProject;
	protected static String		fProjectName;
	protected static boolean	fOneTimeSetupDone	= false;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);

		IPath zippedProjectPath = getProjectZipPath();
		fProjectName = zippedProjectPath.removeFileExtension().lastSegment();
		fTestProject = TestProjectCreator.getAVRProject(fProjectName, zippedProjectPath);
		if (fTestProject == null)
			fail("Unable to create project");

		/* Build the test project.. */
		fTestProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		fOneTimeSetupDone = true;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Get the path to a zipped project.
	 * <p>
	 * By default this will get the path to a very simple AVR Project. Override to load another
	 * zipped project.
	 * </p>
	 * 
	 * @return Path to zipped Project.
	 */
	protected static IPath getProjectZipPath() {
		return new Path("testprojects/testproject_1.zip");
	}

}
