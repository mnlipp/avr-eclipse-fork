/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.AVRPlugin;

/**
 * @author U043192
 * 
 */
public class TestSignatures {

	private Signatures	fSigs	= null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		fSigs = Signatures.getDefault();
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.Signatures#getMCUInfo(java.lang.String)}.
	 */
	@Test
	public void testGetMCUInfo() {
		// test a few MCUs
		assertTrue("getMCUInfo(\"at86rf401\") != \"0x1E9181\"", fSigs.getMCUInfo("at86rf401")
				.equals("0x1E9181"));
		assertTrue("getMCUInfo(\"at90pwm324\") != \"0x1E9584\"", fSigs.getMCUInfo("at90pwm324")
				.equals("0x1E9584"));
		assertTrue("getMCUInfo(\"attiny861\") != \"0x1E930D\"", fSigs.getMCUInfo("attiny861")
				.equals("0x1E930D"));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.Signatures#getMCUList()}.
	 */
	@Test
	public void testGetMCUList() {
		Set<String> allsigs = fSigs.getMCUList();
		assertNotNull("getMCUList() returned null", allsigs);
		// at least a few sigs should be present
		assertTrue("getMCUList() list has only " + allsigs.size() + " items", allsigs.size() > 5);
		// and good old atmega16 should be present
		assertTrue("getMCUList() atmega16 missing", allsigs.contains("atmega16"));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.Signatures#hasMCU(java.lang.String)}.
	 */
	@Test
	public void testHasMCU() {
		// test a few MCUs
		assertTrue("hasMCU(\"atmega16\") failed", fSigs.hasMCU("atmega16"));
		assertTrue("hasMCU(\"at90s2313\") failed", fSigs.hasMCU("at90s2313"));
		assertTrue("hasMCU(\"attiny85\") failed", fSigs.hasMCU("attiny85"));
		// test that the comp MCUs are not present
		assertFalse("hasMCU(\"atmega161comp\") successfull", fSigs.hasMCU("atmega161comp"));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.Signatures#getMCUfromSignature(java.lang.String)}.
	 */
	@Test
	public void testGetMCU() {
		// test a few signatures
		String mcu = fSigs.getMCU("0x1E9781");
		assertTrue("getMCU(\"0x1E9781\") == \"" + mcu + "\" (expected \"at90can128\")", mcu
				.equals("at90can128"));
		mcu = fSigs.getMCU("0x1E9405");
		assertTrue("getMCU(\"0x1E9405\") == \"" + mcu + "\" (expected \"atmega169\")", mcu
				.equals("atmega169"));
		mcu = fSigs.getMCU("0x1E9510");
		assertTrue("getMCU(\"0x1E9510\") == \"" + mcu + "\" (expected \"atmega32hvb\")", mcu
				.equals("atmega32hvb"));
	}

	private final static IPath	INSTANCEPROPSFILE	= new Path("signatures.properties");

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.Signatures#addSignatures(java.lang.String)}.
	 */
	@Test
	public void testAddSignatures() throws IOException {
		fSigs.addSignature("test1", "0x123456");
		fSigs.addSignature("test2", "0x654321");

		assertTrue(fSigs.getMCUInfo("test1").equals("0x123456"));
		assertTrue(fSigs.getMCU("0x654321").equals("test2"));

		// Store signatures and check the the properties were created and
		// contain the two new entries
		fSigs.addSignature("atmega16", "0x1E9403"); // default value, should not be stored
		fSigs.storeSignatures();

		Properties signatureprops = new Properties();
		IPath propslocation = AVRPlugin.getDefault().getStateLocation().append(INSTANCEPROPSFILE);
		File propsfile = propslocation.toFile();
		assertTrue("Signature Properties not created", propsfile.exists());
		assertTrue("Signature Properties not readable", propsfile.canRead());

		InputStream is = new FileInputStream(propsfile);
		signatureprops.load(is);
		is.close();

		assertFalse("Stored properties empty", signatureprops.isEmpty());
		int numprops = signatureprops.keySet().size();
		assertTrue(numprops + " properties stored (expected 2)", numprops == 2);
		assertTrue("test1 signature wrong", signatureprops.getProperty("test1").equals("0x123456"));
		assertTrue("test2 mcuid missing", signatureprops.containsValue("0x654321"));
		assertFalse("atmega16 should not be there", signatureprops.containsKey("atmega16"));
	}

}
