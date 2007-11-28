/**
 * 
 */
package de.innot.avreclipse.ui.views;

import static org.junit.Assert.assertNotNull;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * @author Thomas
 * 
 */
public class TestAVRDeviceView {

	@Test
	public final void testShowView() throws Exception {
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(
						"de.innot.avreclipse.views.AVRDeviceView");
		assertNotNull(view);
	}
}
