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
package de.innot.avreclipse.core.paths;

public enum AVRPath {
	// The compiler
	AVRGCC(true, "AVR-GCC",
			"Directory containing 'avr-gcc' and the other tools of the AVR-GCC toolchain",
			"avr-gcc"),

	// Make
	MAKE(true, "GNU make", "Directory containing 'make' executable", "make"),

	// The avr header files
	AVRINCLUDE(true, "AVR Header Files", "Directory containing 'avr/io.h' include file", "avr/io.h"),

	// AVRDude executable
	AVRDUDE(false, "AVRDude", "Directory containing 'avrdude' executable", "avrdude"),

	// AVRDude config is not used - We get the path from AVRDude itself
	// AVRDUDECONFIG(false, "AVRDude.conf", "Directory containing 'avrdude.conf' configuration
	// file", "avrdude.conf"),

	// Atmel part description files
	PDFPATH(false, "Atmel Part Description Files",
			"(currently unused) Directory containing the Atmel Part Description Files",
			"ATmega16.xml");

	private boolean	fRequired;
	private String	fName;
	private String	fDescription;
	private String	fTest;

	/**
	 * Default Enum constructor. Sets the internal fields according to the selected enum value.
	 */
	AVRPath(boolean required, String name, String description, String test) {
		fRequired = required;
		fName = name;
		fDescription = description;
		fTest = test;
	}

	public String getDescription() {
		return fDescription;
	}

	public String getName() {
		return fName;
	}

	public boolean isOptional() {
		return !fRequired;
	}

	public String getTest() {
		return fTest;
	}

	@Override
	public String toString() {
		return fName;
	}
}
