/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

/**
 * The Target Configuration API.
 * <p>
 * A target configuration contains is basically a bag of attribute - value pairs that describe all
 * properties required to access a remote AVR MCU.
 * </p>
 * <p>
 * Each target configuration has a unique id and information on:
 * <ul>
 * <li>The name of the configuration + an optional description.</li>
 * <li>The target MCU type and its clock frequency.</li>
 * <li>An ID for the programmer hardware used to access the MCU, including its host and target
 * interface settings.</li>
 * <li>The ID of the programmer tool for uploading AVR applications to the target MCU.</li>
 * <li>The ID of the gdbserver tool for debugging AVR applications.</li>
 * </ul>
 * The attributes common for all target configurations are defined in {@link ITargetConfigConstants}
 * . The programmer tool and the gdbserver use their own custom attributes.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface ITargetConfiguration {

	/**
	 * Get the Id of this target configuration.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Get the name of this target configuration.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public String getName();

	/**
	 * Get the optional user supplied description of this target configuration.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * @return the id of the target MCU
	 */
	public String getMCU();

	/**
	 * @return the target MCU clock
	 */
	public int getFCPU();

	/**
	 * Get the list of all supported MCUs.
	 * <p>
	 * If the <code>filtered</code> argument is <code>true</code>, then only those MCUs are returned
	 * that are supported by both the image loader and the gdbserver (Intersection). If
	 * <code>filtered</code> is <code>false</code>, then all MCUs supported by avr-gcc as well as
	 * the selected imageloader and gdbserver are returned (Union).
	 * </p>
	 * 
	 * @param filtered
	 *            Restrict the list to the MCUs that are actually supported by the current
	 *            configuration.
	 * @return List of mcu id values in avr-gcc format
	 */
	public List<String> getSupportedMCUs(boolean filtered);

	/**
	 * Get the list of all supported Programmers.
	 * <p>
	 * If the <code>filtered</code> argument is <code>true</code>, then only those Programmers are
	 * returned that are supported by both the image loader and the gdbserver (Intersection). If
	 * <code>filtered</code> is <code>false</code>, then all MCUs supported by avrdude as well as
	 * the selected imageloader and gdbserver are returned (Union).
	 * </p>
	 * 
	 * @param filtered
	 *            Restrict the list to the MCUs that are actually supported by the current
	 *            configuration.
	 * @return List of mcu id values in avr-gcc format
	 */
	public List<IProgrammer> getSupportedProgrammers(boolean filtered);

	/**
	 * Get a specific programmer.
	 * <p>
	 * All target configuration tools (image loaders and gdbservers) are queried for the given id.
	 * Note that even tools not currently active are queried. This is used by the user interface to
	 * correctly show the information for an id that had been selected but has become invalid
	 * afterwards.
	 * </p>
	 * </p>
	 * 
	 * @param programmerid
	 *            The id value of a specific programmer.
	 * @return An <code>IProgrammer</code> object
	 */
	public IProgrammer getProgrammer(String programmerid);

	/**
	 * Checks if this target configuration is capable of debugging.
	 * <p>
	 * Debugging can be either on-chip or with a simulator. If <code>true</code> is returned, then
	 * the {@link #getGDBServerLaunchConfig()} will return the launch configuration for a GDB
	 * Server.
	 * </p>
	 * 
	 * @return <code>true</code> if this target configuration is capable of debugging.
	 */
	public boolean isDebugCapable();

	/**
	 * Checks if this target configuration is capable of uploading an AVR project to a target.
	 * <p>
	 * If <code>true</code> is returned, then the {@link #getLoaderLaunchConfig()} will return the
	 * launch configuration for a project up-loader.
	 * </p>
	 * 
	 * @return <code>true</code> if target configuration contains an image loader.
	 */
	public boolean isImageLoaderCapable();

	/**
	 * Returns the string-valued attribute with the given name.
	 * <p>
	 * Returns the given default value if the attribute is undefined.
	 * </p>
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @return the value or the default value if no value was found.
	 */
	public String getAttribute(String attributeName);

	/**
	 * Returns the boolean-valued attribute with the given name.
	 * <p>
	 * Returns the given default value if the attribute is undefined or if the value was not a
	 * boolean.
	 * </p>
	 * <p>
	 * This method is not type save, i.e. there is no checking whether the attribute is actually an
	 * boolean. It is up to the caller to ensure that the attribute only contains a boolean value.
	 * </p>
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @return the value or the default value if no valid value was found.
	 */
	public boolean getBooleanAttribute(String attributeName);

	/**
	 * Returns the integer-valued attribute with the given name.
	 * <p>
	 * Returns the given default value if the attribute is undefined or if the value was not a
	 * integer.
	 * </p>
	 * <p>
	 * This method is not type save, i.e. there is no checking whether the attribute is actually an
	 * integer. It is up to the caller to ensure that the attribute only contains integer values.
	 * </p>
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @return the value or the default value if no valid value was found.
	 */
	public int getIntegerAttribute(String attributeName);

	/**
	 * Returns a map containing the attributes in this target configuration. Returns an empty map if
	 * this configuration has no attributes.
	 * <p>
	 * Modifying the map does not affect this target configuration's attributes. A target
	 * configuration is modified by obtaining a working copy of that target configuration, modifying
	 * the working copy, and then saving the working copy.
	 * </p>
	 * 
	 * @return a map of attribute keys and values
	 * @exception CoreException
	 *                unable to generate/retrieve an attribute map
	 * @since 2.1
	 */
	public Map<String, String> getAttributes() throws CoreException;

	/**
	 * Adds a property change listener to this target configuration. Has no affect if the identical
	 * listener is already registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void addPropertyChangeListener(ITargetConfigChangeListener listener);

	/**
	 * Removes the given listener from this target configuration. Has no affect if the listener is
	 * not registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void removePropertyChangeListener(ITargetConfigChangeListener listener);

	/**
	 * Prepares the target configuration for deletion.
	 * <p>
	 * The implementation should remove all listeners and other references so that it can be garbage
	 * collected.
	 * </p>
	 * 
	 */
	public void dispose();

}
