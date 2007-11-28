/**
 * 
 */
package de.innot.avreclipse.devicedescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import de.innot.avreclipse.devicedescription.ICategory;
import de.innot.avreclipse.devicedescription.IDeviceDescription;
import de.innot.avreclipse.devicedescription.IDeviceDescriptionProvider;
import de.innot.avreclipse.devicedescription.IEntry;
import de.innot.avreclipse.devicedescription.avrio.DeviceDescription;
import de.innot.avreclipse.devicedescription.avrio.DeviceDescriptionProvider;

/**
 * @author Thomas
 * 
 */
public class TestDeviceDescriptionProviderAVRioh extends TestCase {

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.devicedescription.avrio.DeviceDescriptionProvider#getDefault()}.
	 */
	public void testGetDefault() {
		IDeviceDescriptionProvider ddp = DeviceDescriptionProvider.getDefault();
		assertNotNull(ddp);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.devicedescription.avrio.DeviceDescriptionProvider#getDeviceList()}.
	 */
	public void testGetDevices() {
		IDeviceDescriptionProvider ddp = DeviceDescriptionProvider.getDefault();
		List<String> devices = ddp.getDeviceList();
		assertNotNull(devices);
		assertTrue(devices.size() > 0);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.devicedescription.avrio.DeviceDescriptionProvider#getDevice(java.lang.String)}.
	 */
	public void testGetDevice() {
		IDeviceDescription dev = null;
		IDeviceDescriptionProvider ddp = DeviceDescriptionProvider.getDefault();
		dev = ddp.getDevice(null);
		assertNull(dev); // but no exception
		dev = ddp.getDevice("foobar");
		assertNull(dev); // but no exception

		// AT90CAN64 because it includes another file which has lots of correct
		// comments
		dev = ddp.getDevice("AT90CAN64");
		assertNotNull(dev);
		// See if the three categories are there
		List<ICategory> catlist = dev.getCategories();
		assertTrue(catlist.size() == 3);
		// Test some random elements that should be there
		ICategory cat = catlist.get(0); // registers
		List<IEntry> entrylist = cat.getChildren();
		assertNotNull(entrylist);
		Map<String, IEntry> entrymap = list2map(entrylist);

		IEntry e;
		assertNotNull(entrymap.get("TIFR0")); // the first register in the
		// include file
		assertNotNull(entrymap.get("CANMSG")); // the last register in the
		// include file
		assertNotNull(e = entrymap.get("TCNT0")); // some register examined
		// more closely
		if (dev instanceof DeviceDescription) {
			assertEquals("Timer/Counter Register", e.getColumnData(1)); // Description
			assertEquals("0x26", e.getColumnData(3)); // Addr
			assertEquals("8", e.getColumnData(4)); // Bits
		}

		dev = ddp.getDevice("ATmega16");
		assertNotNull(dev);
		catlist = dev.getCategories();
		assertTrue(catlist.size() == 3);
		cat = catlist.get(2); // IVecs
		assertNotNull(entrylist = cat.getChildren());
		entrymap = list2map(entrylist);
		assertNotNull(entrymap.containsKey("INT0_vect")); // first Vector
		assertNotNull(entrymap.containsKey("SPM_RDY_vect")); // last Vector
		assertNotNull(e = entrymap.get("TIMER0_OVF_vect"));
		if (dev instanceof DeviceDescription) {
			assertEquals("SIG_OVERFLOW0", e.getColumnData(1)); // Old Name
			assertEquals("9", e.getColumnData(3)); // Vector
		}

		// At the ATmega1280 currently the port elements PINA, DDRA, PORTA, PINB
		// are missing and I dont know why. Here a test as a reminder to fix this.
		// fixed!
		dev = ddp.getDevice("ATmega1280");
		catlist = dev.getCategories();
		cat = catlist.get(1); // Ports
		assertNotNull(entrylist = cat.getChildren());
		entrymap = list2map(entrylist);
		assertNotNull(entrymap.containsKey("DDRB")); // first item correctly loaded
		assertNotNull(entrymap.containsKey("PINB")); // last item missing
		assertNotNull(entrymap.containsKey("PINA")); // first item missing
	}

	private Map<String, IEntry> list2map(List<IEntry> entrylist) {
		Map<String, IEntry> entrymap = new HashMap<String, IEntry>();
		for (IEntry e : entrylist) {
			entrymap.put(e.getName(), e);
		}
		return entrymap;
	}

}
