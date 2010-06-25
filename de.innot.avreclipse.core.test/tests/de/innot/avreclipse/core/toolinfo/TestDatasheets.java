/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author U043192
 * 
 */
public class TestDatasheets {

	private Datasheets	fDatasheets	= null;

	@BeforeClass
	public static void showProgressView() throws PartInitException {
		// Open the progress view
		IViewPart progressview = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView("org.eclipse.ui.views.ProgressView");
		progressview.getSite().getPage().activate(progressview);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		fDatasheets = Datasheets.getDefault();

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.Datasheets#getMCUInfo(java.lang.String)}.
	 */
	@Test
	public void testGetMCUInfo() {
		// test a few MCUs
		assertTrue("getMCUInfo(\"attiny43u\") != \"\"", fDatasheets.getMCUInfo("attiny43u").equals(
				"http://www.atmel.com/dyn/resources/prod_documents/doc8048.pdf"));
		assertTrue("getMCUInfo(\"at90pwm2b\") not correct", fDatasheets.getMCUInfo("at90pwm2b")
				.equals("http://www.atmel.com/dyn/resources/prod_documents/doc4317.pdf"));
		assertTrue("getMCUInfo(\"attiny861\") not correct", fDatasheets.getMCUInfo("attiny861")
				.equals("http://www.atmel.com/dyn/resources/prod_documents/doc2588.pdf"));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.Datasheets#getMCUList()}.
	 */
	@Test
	public void testGetMCUList() {
		Set<String> allsheets = fDatasheets.getMCUList();
		assertNotNull("getMCUList() returned null", allsheets);
		// at least a few sigs should be present
		assertTrue("getMCUList() list has only " + allsheets.size() + " items",
				allsheets.size() > 5);
		// and good old atmega16 should be present
		assertTrue("getMCUList() atmega16 missing", allsheets.contains("atmega16"));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.Datasheets#hasMCU(java.lang.String)}.
	 */
	@Test
	public void testHasMCU() {
		// test a few MCUs
		assertTrue("hasMCU(\"atmega16\") failed", fDatasheets.hasMCU("atmega16"));
		assertTrue("hasMCU(\"at90s2313\") failed", fDatasheets.hasMCU("at90s2313"));
		assertTrue("hasMCU(\"attiny85\") failed", fDatasheets.hasMCU("attiny85"));
		// test that the comp MCUs are not present
		assertFalse("hasMCU(\"atmega161comp\") successfull", fDatasheets.hasMCU("atmega161comp"));
	}

}
