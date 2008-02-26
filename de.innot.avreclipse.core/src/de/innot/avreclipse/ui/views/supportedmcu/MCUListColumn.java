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
package de.innot.avreclipse.ui.views.supportedmcu;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.toolinfo.Datasheets;
import de.innot.avreclipse.core.toolinfo.GCC;
import de.innot.avreclipse.core.toolinfo.MCUNames;
import de.innot.avreclipse.core.toolinfo.Signatures;
import de.innot.avreclipse.devicedescription.avrio.AVRiohDeviceDescriptionProvider;

/**
 * This enum contains all columns to be shown in the MCUListView.
 * <p>
 * This is the interface between the GUI and the IMCUProviders. Each column in
 * this enum knows how to get a IMCUProvider ({@link #getMCUProvider()}),
 * knows how to set up a given TableViewerColumn for display ({@link #initColumn()})
 * and has a User visible column name.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public enum MCUListColumn {

	/**
	 * The names column represents the {@link MCUNames} and the
	 * {@link Datasheets} IMCUProviders.
	 * <p>
	 * It will show all known MCU names and also shows those with a datasheet
	 * URL available as clickable links.
	 * </p>
	 */
	NAMES("MCU Name", MCUNames.getDefault()) {
		private IMCUProvider fLinkprovider = Datasheets.getDefault();

		protected ColumnWeightData initColumn(TableViewerColumn column) {
			URLColumnLabelProvider labelprovider = new URLColumnLabelProvider(fMCUProvider,
			        fLinkprovider);
			fLabelProvider = labelprovider;
			column.setLabelProvider(fLabelProvider);
			return new ColumnWeightData(20, 60);
		}

		@Override
		protected void internalUpdate(TableViewer tableviewer, TableViewerColumn viewercolumn) {
			((URLColumnLabelProvider) fLabelProvider).updateColumn(tableviewer, viewercolumn);
		}
	},
	/**
	 * Column with all MCUs supported by avr-gcc, shown as Yes/No images.
	 */
	AVRGCC("AVR-GCC", GCC.getDefault()) {
		protected ColumnWeightData initColumn(TableViewerColumn column) {
			column.setLabelProvider(new BooleanColumnLabelProvider(fMCUProvider));
			column.getColumn().setAlignment(SWT.CENTER);
			return new ColumnWeightData(5, 60);
		}
	},
	/**
	 * Column with all MCUs supported in the &lt;avr/io.h&gt; header file, shown
	 * as Yes/No images.
	 */
	AVRINCLUDE("<avr/io.h>", AVRiohDeviceDescriptionProvider.getDefault()) {
		protected ColumnWeightData initColumn(TableViewerColumn column) {
			column.setLabelProvider(new BooleanColumnLabelProvider(fMCUProvider));
			column.getColumn().setAlignment(SWT.CENTER);
			return new ColumnWeightData(5, 60);
		}
	},
	/**
	 * Column with all MCUs supported by AVR Studio (which have a Part
	 * Description File).
	 * <p>
	 * This currently uses the {@link Signatures} provider, so it has the same
	 * info as the {@link #SIGNATURE} column, just a different graphical
	 * pesentation (Yes/No images).
	 * </p>
	 */
	AVRSTUDIO("AVR Studio", Signatures.getDefault()) {
		protected ColumnWeightData initColumn(TableViewerColumn column) {
			column.setLabelProvider(new BooleanColumnLabelProvider(fMCUProvider));
			column.getColumn().setAlignment(SWT.CENTER);
			return new ColumnWeightData(5, 60);
		}
	},
	/**
	 * Column with all known Signatures.
	 */
	SIGNATURE("Signature", Signatures.getDefault()) {
		protected ColumnWeightData initColumn(TableViewerColumn column) {
			column.setLabelProvider(new StringColumnLabelProvider(fMCUProvider));
			return new ColumnWeightData(15, 60);
		}
	},
	/**
	 * the filler column is here, so that the other columns do not get stretched
	 * out over the the whole width, but kept together.
	 */
	FILLER("", null) {
		protected ColumnWeightData initColumn(TableViewerColumn column) {
			column.setLabelProvider(new NullColumnLabelProvider());
			return new ColumnWeightData(50, 5);
		}
	};

	/** User visible column name */
	protected String fName;

	/**
	 * TableViewer this MCU column is associated with. Required for the
	 * URLColumnLabelProvider to hook its TableEditors to the table.
	 */
	protected TableViewer fTableViewer;

	/**
	 * TableViewerColumn this MCU column is associated with. Required for the
	 * URLColumnLabelProvider to hook its TableEditors to the table.
	 */
	protected TableViewerColumn fViewerColumn;

	/** The ColumnLabelProvider of this MCU column */
	protected ColumnLabelProvider fLabelProvider;

	/** The referenced IMCUProvider of the MCU column */
	protected IMCUProvider fMCUProvider;

	/**
	 * The constructor is called from the enum values and sets the user visible
	 * column name and the underlying provider.
	 * 
	 * @param name
	 * @param provider
	 */
	private MCUListColumn(String name, IMCUProvider provider) {
		fName = name;
		fMCUProvider = provider;
	}

	/**
	 * Add this MCU column to the given TableViewer.
	 * <p>
	 * A new <code>TableViewerColumn</code> is created, initialized and added
	 * to the TableViewer. Also a <code>ColumnWeightData</code> for the new
	 * column is added to the given <code>TableColumnLayout</code>.
	 * </p>
	 * 
	 * @param tableviewer
	 *            The <code>TableViewer</code> for which to create the column.
	 * 
	 * @param layout
	 *            The <code>TableColumnLayout</code> for the TableViewer.
	 */
	public void addColumn(TableViewer tableviewer, TableColumnLayout layout) {
		fTableViewer = tableviewer;
		fViewerColumn = new TableViewerColumn(tableviewer, SWT.NONE);
		fViewerColumn.getColumn().setText(fName);

		// Do the column type specific stuff
		ColumnWeightData cwd = initColumn(fViewerColumn);

		layout.setColumnData(fViewerColumn.getColumn(), cwd);

	}

	/**
	 * Gets the underlying IMCUProvider for the MCU column.
	 * 
	 * @return <code>IMCUProvider</code>
	 */
	public IMCUProvider getMCUProvider() {
		return fMCUProvider;
	}

	/**
	 * Update the Column.
	 * <p>
	 * This needs to be called after the table has been filled with data (after
	 * the <code>TableViewer.setInput()</code>) method, and after each
	 * content change.
	 * </p>
	 * <p>
	 * This method is required to do some magic stuff with a TableViewer, like
	 * showing Hyperlinks.
	 * </p>
	 */
	public void updateColumn() {
		internalUpdate(fTableViewer, fViewerColumn);
	}

	/**
	 * Update the given columnn as required. Used by the {@link #NAMES} column
	 * to hook the URLs to the table.
	 * 
	 * @param tableviewer
	 * @param column
	 */
	protected void internalUpdate(TableViewer tableviewer, TableViewerColumn column) {
		// Enums that have something to update will override
		// this method
	};

	/**
	 * Initialize the given column as required.
	 * <p>
	 * This includes setting a ColumnLabelProvider for the Column and returning
	 * a {@link ColumnWeightData} object with the desired dimensions of the
	 * column.
	 * </p>
	 * 
	 * @param column
	 *            <code>TableViewerColumn</code> to adjust
	 * @return <code>ColumnWeightData</code> with the desired weight and
	 *         minimum pixel size.
	 */
	protected abstract ColumnWeightData initColumn(TableViewerColumn column);

	/**
	 * A ColumnLabelProvider that always returns empty Strings as Labels. Used
	 * by the {@ MCUListColumn#FILLER} column.
	 */
	private class NullColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			return "";
		}
	}
}
