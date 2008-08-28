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
package de.innot.avreclipse.ui.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;

/**
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class FuseFileDocumentProvider extends ForwardingDocumentProvider {

	private static FuseFileDocumentProvider fInstance;

	private static Map<Object, DocumentByteValuesConnector> fConnectorsMap = new HashMap<Object, DocumentByteValuesConnector>();
	private static Map<Object, Integer> fConnectorsCount = new HashMap<Object, Integer>();

	private static class InternalDocumentSetupParticipant implements
			IDocumentSetupParticipant {

		public void setup(IDocument document) {
			// nothing to setup yet
			// In the future we could set up a partitioner here, as well as a content assist.
		}
	}

	public static FuseFileDocumentProvider getDefault() {

		if (fInstance == null) {
			fInstance = createProvider();
		}

		return fInstance;

	}

	private static FuseFileDocumentProvider createProvider() {

		String partitioning = "__fuses"; // don't yet know what this is for

		IDocumentSetupParticipant setupparticipant = new IDocumentSetupParticipant() {

			public void setup(IDocument document) {
			}
		};

		IDocumentProvider parentprovider = new TextFileDocumentProvider();

		FuseFileDocumentProvider provider = new FuseFileDocumentProvider(
				partitioning, setupparticipant, parentprovider);

		return provider;

	}

	public FuseFileDocumentProvider() {
		super("__fuses", new InternalDocumentSetupParticipant(),
				new TextFileDocumentProvider());

	}

	private FuseFileDocumentProvider(String partitioning,
			IDocumentSetupParticipant documentSetupParticipant,
			IDocumentProvider parentProvider) {
		super(partitioning, documentSetupParticipant, parentProvider);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.editors.text.ForwardingDocumentProvider#connect(java.lang.Object)
	 */
	@Override
	public void connect(Object element) throws CoreException {

		super.connect(element);

		DocumentByteValuesConnector connector = fConnectorsMap.get(element);
		if (connector == null) {
			connector = new DocumentByteValuesConnector(this, element,
					getDocument(element));
			fConnectorsMap.put(element, connector);
			fConnectorsCount.put(element, 1);
		} else {
			// someone already connected to this element
			// increase use counter
			int usecount = fConnectorsCount.get(element);
			fConnectorsCount.put(element, usecount + 1);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.editors.text.ForwardingDocumentProvider#disconnect(java.lang.Object)
	 */
	@Override
	public void disconnect(Object element) {

		// TODO Auto-generated method stub
		super.disconnect(element);
		Integer usecount = fConnectorsCount.get(element);
		if (usecount == null) {
			// the element was never connected

			return;
		} else {
			--usecount;
			if (usecount == 0) {
				// no active connections anymore
				DocumentByteValuesConnector dbvp = fConnectorsMap.get(element);
				dbvp.dispose();
				fConnectorsMap.remove(element);
				fConnectorsCount.remove(element);
			} else {
				// there are still some active connections
				fConnectorsCount.put(element, usecount);
			}
		}

	}

	public ByteValues getByteValues(Object element) throws CoreException {

		DocumentByteValuesConnector connector = fConnectorsMap.get(element);
		if (connector == null) {
			// probably not connected yet

			return null;
		}

		return connector.getByteValues();

	}

	public void setByteValues(Object element, ByteValues newvalues) {

		DocumentByteValuesConnector connector = fConnectorsMap.get(element);
		if (connector == null) {
			// probably not connected yet

			return;
		}
		connector.writeByteValues(newvalues);

	}
}
