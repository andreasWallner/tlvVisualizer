package at.innovative_solutions.tlvvisualizer.views;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.ErrorTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;
import at.innovative_solutions.tlv.ValueDecoder;

//TODO cache byte/string serialization?

public class TLVViewer extends Composite {
	public static final String PROP_ID = "ID";
	public static final String PROP_TYPE = "TYPE";
	public static final String PROP_SIZE = "SIZE";
	public static final String PROP_NAME = "NAME";
	public static final String PROP_DECODED = "DECODED";
	public static final String PROP_ENCODED = "ENCODED";
	public static final String[] PROPS = { PROP_ID, PROP_TYPE, PROP_SIZE, PROP_NAME,
		PROP_DECODED, PROP_ENCODED };
	
	Action fDeleteAction;
	Action fAddPrimitiveAction;
	Action fAddConstructedAction;
	Action fExpandAllBelowAction;
	Action fCollapseAllBelowAction;
	
	TreeViewer fViewer;
	ValueDecoder fDecoder;
	boolean fPinned;
	
	private Vector<ModifyListener> fModifyListeners = new Vector<ModifyListener>();
	final public Font fTooltipFont;
	
	class TreeRootWrapper {
		Object[] fWrapped;

		@SuppressWarnings("rawtypes")
		TreeRootWrapper(Object toWrap) {
			if (toWrap instanceof List)
				fWrapped = ((List) toWrap).toArray();
			else
				fWrapped = new Object[] { toWrap };
		}

		Object[] getWrapped() {
			return fWrapped;
		}
	}

	class TLVContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ConstructedTLV)
				return ((ConstructedTLV) parentElement).getTLVs().toArray();
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return ((TLV) element).getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ConstructedTLV)
				return ((ConstructedTLV) element).getTLVs().size() > 0;
				return false;
		}

		@Override
		public Object[] getElements(Object element) {
			if (element instanceof TreeRootWrapper)
				return ((TreeRootWrapper) element).getWrapped();
			return getChildren(element);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	class IdLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			String text = (e.getID() != null) ? Utils.bytesToHexString(e.getID().toBytes()) : "";

			cell.setText(text);
		}
	}
	
	class TypeLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			String text = e.getID().isPrimitive() ? "P" : "C"; // TODO error tlv
			cell.setText(text);
		}
	}
	
	class SizeLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			String text = String.valueOf(e.getLength());
			cell.setText(text);
		}
	}

	class NameLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			
			String text;
			if (element instanceof ErrorTLV) {
				text = "ERROR: " + ((ErrorTLV) element).getError();
			} else {
				text = TLVViewer.this.fDecoder.getName(e);
			}
			
			cell.setText(text);
		}
	}
	
	class DecodedLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			String text = TLVViewer.this.fDecoder.toString(e);
			cell.setText(text);
		}

		@Override
		public int getToolTipTimeDisplayed(Object object) {
			return 20000;
		}

		@Override
		public String getToolTipText(Object o) {
			if (!(o instanceof TLV))
				return null;

			return TLVViewer.this.fDecoder.getSimpleDecoded((TLV) o).replaceAll("&", "&&");
		}

		@Override
		public int getToolTipDisplayDelayTime(Object object) {
			return 100;
		}
		
		@Override
		public Font getToolTipFont(Object object) {
			return fTooltipFont;
		}
	}
	
	class EncodedLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (!(element instanceof TLV))
				cell.setText("");
			
			TLV e = (TLV)element;
			String text = "";
			if (element instanceof PrimitiveTLV)
				text = Utils.bytesToHexString(((PrimitiveTLV) e).getData());
			else if (element instanceof ErrorTLV)
				text = Utils.bytesToHexString(((ErrorTLV) e)
						.getRemainingData());
			cell.setText(text);
		}
	}
	
	class IDEditingSupport extends EditingSupport {
		final private TextCellEditor cellEditor;
		
		public IDEditingSupport(ColumnViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(((TreeViewer)viewer).getTree());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}
		
		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();
			
			TLV tlv = (TLV) element;
			byte[] idBytes = Utils.hexStringToBytes(value.toString());
			ByteBuffer buffer = ByteBuffer.wrap(idBytes);
			ID newID = at.innovative_solutions.tlv.ID.parseID(buffer);
			if(newID.isPrimitive() == tlv.getID().isPrimitive()) {
				tlv.setID(newID);
			} else {
				String message;
				if(tlv.getID().isPrimitive())
					message = "'" + Utils.bytesToHexString(newID.toBytes()) + "' is an invalid ID for a primitive TLV\n"
					        + "Should the tag be converted to a constructed TLV (loosing the current TLV content), the ID\n"
							+ "be adapted for a primitive TLV (to '" + Utils.bytesToHexString(newID.withChangedPC().toBytes()) + ")', or no action taken?";
				else
					message = "'" + Utils.bytesToHexString(newID.toBytes()) + "' is an invalid ID for a constructed TLV\n"
					        + "Should the tag be converted to a primitive TLV (loosing all subtags), the ID\n"
							+ "be adapted for a constructed TLV (to '" + Utils.bytesToHexString(newID.withChangedPC().toBytes()) + ")', or no action taken?";
				
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog dialog = new MessageDialog(shell, "Invalid ID", null, message, MessageDialog.QUESTION, new String[] {"Convert", "Adopt", "Cancel"}, 0);
				int result = dialog.open();
				
				switch(result) {
				case 0:
					TLV newTlv = newID.isPrimitive() ? new PrimitiveTLV(newID) : new ConstructedTLV(newID);
					ConstructedTLV parent = (ConstructedTLV)tlv.getParent();
					if(parent != null) {
						parent.replaceChild(tlv, newTlv);
					} else {
						List<TLV> newTlvs = new LinkedList<TLV>();
						Object[] oldTlvs = ((TreeRootWrapper)fViewer.getInput()).getWrapped();
						for(Object o : oldTlvs) {
							if(o != tlv)
								newTlvs.add((TLV)o);
							else
								newTlvs.add(newTlv);
						}
						fViewer.setInput(new TreeRootWrapper(newTlvs));
					}
					break;
				case 1:
					tlv.setID(newID.withChangedPC());
					break;
				case 2:
					return;
				}
			}
			fViewer.refresh();
			modified();
		}

		@Override
		protected boolean canEdit(Object element) {
			TLV tlv = (TLV)element;
			if(fPinned && tlv.getParent() == null)
				return false;
			
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			TLV e = (TLV) element;
			if (e.getID() == null)
				return "";
			return Utils.bytesToHexString(e.getID().toBytes());
		}
	}

	class DecodedEditingSupport extends EditingSupport {
		final private TextCellEditor cellEditor;
		
		public DecodedEditingSupport(ColumnViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(((TreeViewer)viewer).getTree());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			TLV tlv = (TLV)element;
			if(fPinned && tlv.getParent() == null)
				return false;

			return tlv.getID().isPrimitive() && (TLVViewer.this.fDecoder.isValueParsable(tlv));
		}

		@Override
		protected Object getValue(Object element) {
			TLV e = (TLV) element;
			return TLVViewer.this.fDecoder.toString(e);
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			PrimitiveTLV tlv = (PrimitiveTLV) element;
			byte[] encoded = null;
			try {
				encoded = TLVViewer.this.fDecoder.toValue(value.toString(), tlv);
				tlv.setData(encoded);
			} catch(InvalidEncodedValueException ex)
			{
				MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				box.setText("Invalid input");
				box.setMessage("Invalid input for element of type " + TLVViewer.this.fDecoder.getFormat(tlv) + "\n" + ex.getMessage());
				box.open();
			}
			fViewer.refresh();
			modified();
		}
	}
	
	class EncodedEditingSupport extends EditingSupport {
		final private TextCellEditor cellEditor;
		
		public EncodedEditingSupport(ColumnViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor(((TreeViewer)viewer).getTree());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			TLV tlv = (TLV)element;
			if(fPinned && tlv.getParent() == null)
				return false;
			
			return tlv.getID().isPrimitive();
		}

		@Override
		protected Object getValue(Object element) {
			TLV e = (TLV) element;
			String ret = "";
			if (element instanceof PrimitiveTLV)
				ret += Utils.bytesToHexString(((PrimitiveTLV) e).getData());
			else if (element instanceof ErrorTLV)
				ret += Utils.bytesToHexString(((ErrorTLV) e)
						.getRemainingData());
			return ret;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			PrimitiveTLV tlv = (PrimitiveTLV) element;
			tlv.setData(Utils.hexStringToBytes(value.toString()));
			
			fViewer.refresh();
			modified();
		}
		
	}
	
	public TLVViewer(Composite parent, int style) {
		super(parent, style);
		createContents(parent);
		fDecoder = new NullValueDecoder();
		fTooltipFont = new Font(parent.getDisplay(), "Consolas", 8, SWT.NONE);
	}

	public void createContents(final Composite parent) {
		final FillLayout gridLayout = new FillLayout();
		this.setLayout(gridLayout);
		
		final Tree tlvTree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tlvTree.setHeaderVisible(true);
		tlvTree.setLinesVisible(true);
		
		fViewer = new TreeViewer(tlvTree);
		
		final TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setAlignment(SWT.LEFT);
		column1.getColumn().setText("ID");
		column1.getColumn().setWidth(100);
		column1.setEditingSupport(new IDEditingSupport(fViewer));
		column1.setLabelProvider(new IdLabelProvider());
		final TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.CENTER);
		column2.getColumn().setAlignment(SWT.CENTER);
		column2.getColumn().setText("P/C");
		column2.getColumn().setWidth(35);
		column2.setLabelProvider(new TypeLabelProvider());
		final TreeViewerColumn column3 = new TreeViewerColumn(fViewer, SWT.RIGHT);
		column3.getColumn().setAlignment(SWT.CENTER);
		column3.getColumn().setText("Size");
		column3.getColumn().setWidth(35);
		column3.setLabelProvider(new SizeLabelProvider());
		final TreeViewerColumn column4 = new TreeViewerColumn(fViewer, SWT.RIGHT);
		column4.getColumn().setAlignment(SWT.LEFT);
		column4.getColumn().setText("Name");
		column4.getColumn().setWidth(300);
		column4.setLabelProvider(new NameLabelProvider());
		final TreeViewerColumn column5 = new TreeViewerColumn(fViewer, SWT.RIGHT);
		column5.getColumn().setAlignment(SWT.LEFT);
		column5.getColumn().setText("Decoded");
		column5.getColumn().setWidth(150);
		column5.setEditingSupport(new DecodedEditingSupport(fViewer));
		column5.setLabelProvider(new DecodedLabelProvider());
		final TreeViewerColumn column6 = new TreeViewerColumn(fViewer, SWT.RIGHT);
		column6.getColumn().setAlignment(SWT.LEFT);
		column6.getColumn().setText("Encoded");
		column6.getColumn().setWidth(300);
		column6.setEditingSupport(new EncodedEditingSupport(fViewer));
		column6.setLabelProvider(new EncodedLabelProvider());

		fViewer.setContentProvider(new TLVContentProvider());
		fViewer.setInput(null);
		ColumnViewerToolTipSupport.enableFor(fViewer);
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				fDeleteAction.setEnabled(false);
				fAddPrimitiveAction.setEnabled(false);
				fAddConstructedAction.setEnabled(false);
				fExpandAllBelowAction.setEnabled(false);
				fCollapseAllBelowAction.setEnabled(false);

				if(!event.getSelection().isEmpty())
					fDeleteAction.setEnabled(true);
				else
					return;

				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				TLV tlv = (TLV)sel.getFirstElement();
				if(!tlv.getID().isPrimitive()) {
					fAddPrimitiveAction.setEnabled(true);
					fAddConstructedAction.setEnabled(true);
					fExpandAllBelowAction.setEnabled(true);
					fCollapseAllBelowAction.setEnabled(true);
				}
				if(tlv.getParent() == null && fPinned)
					fDeleteAction.setEnabled(false);
			}
		});
		
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}
	
	@Override
	public boolean setFocus() {
		return fViewer.getControl().setFocus();
	}
	
	public void setDecoder(ValueDecoder decoder) {
		if(decoder == null)
			fDecoder = new NullValueDecoder();
		else
			fDecoder = decoder;
		fViewer.refresh();
	}
	
	public void setTLV(String str) {
		final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(str));
		final List<TLV> tlvs = TLV.parseTLVsWithErrors(input);
		TreeRootWrapper w = new TreeRootWrapper(tlvs);
		fViewer.setInput(w);
		fViewer.refresh();
	}
	
	public String getTLV() { 
		return Utils.bytesToHexString(serializeTlv());
	}
	
	public void addModifyListener(ModifyListener listener) {
		fModifyListeners.addElement(listener);
	}
	
	public void removeModifyListener(ModifyListener listener) {
		fModifyListeners.removeElement(listener);
	}
	
	public void pinRootTLV(boolean doPin) {
		fPinned = doPin;
	}
	
	private byte[] serializeTlv() {
		Object[] input = ((TreeRootWrapper) fViewer.getInput())
				.getWrapped();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		for (Object o : input) {
			TLV t = (TLV) o;
			byte[] serialized = t.toBytes();
			stream.write(serialized, 0, serialized.length);
		}
		
		return stream.toByteArray();
	}
	
	private void modified() {
		ModifyEvent e = new ModifyEvent(this, Utils.bytesToHexString(serializeTlv()));
		for(ModifyListener listener : fModifyListeners)
			listener.modified(e);
	}

	// TODO rename setTLV to setTLVString, implement setTLV really setting a TLV object 

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				TLVViewer.this.fillContextMenu(manager);
			}
		});
		final Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		//getSite().registerContextMenu(menuMgr, fViewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(fDeleteAction);
		manager.add(fAddPrimitiveAction);
		manager.add(fAddConstructedAction);
		manager.add(fExpandAllBelowAction);
		manager.add(fCollapseAllBelowAction);
	}
	
	private void contributeToActionBars() {
		
	}
	
	private void makeActions() {
		final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		fDeleteAction = new Action() {
			@Override
			public void run() {
				if(fViewer.getSelection().isEmpty() || !(fViewer.getSelection() instanceof IStructuredSelection))
					return;
				
				IStructuredSelection sel = (IStructuredSelection)fViewer.getSelection();
				TLV tlv = (TLV)sel.getFirstElement();
				ConstructedTLV parent = (ConstructedTLV)tlv.getParent();
				if(parent != null) {
					parent.removeChild(tlv);
				} else {
					List<TLV> newTlvs = new LinkedList<TLV>();
					Object[] oldTlvs = ((TreeRootWrapper)fViewer.getInput()).getWrapped();
					for(Object o : oldTlvs)
						if(o != tlv)
							newTlvs.add((TLV)o);
					fViewer.setInput(new TreeRootWrapper(newTlvs));
				}
				
				fViewer.refresh();
				modified();
			}
		};
		fDeleteAction.setText("Delete");
		fDeleteAction.setToolTipText("Removes selected TLV and sub-TLVs");
		fDeleteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		fAddPrimitiveAction = new Action() {
			@Override
			public void run() {
				TLV tlv = new PrimitiveTLV(new ID(ID.CLASS_APPLICATION, true, 0), new byte[] {});
				addTlvToSelected(tlv);
			}
		};
		fAddPrimitiveAction.setText("Add primitive TLV");
		fAddPrimitiveAction.setToolTipText("Adds new primitive TLV");
		fAddPrimitiveAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD)); // TODO better icon
		
		fAddConstructedAction = new Action() {
			@Override
			public void run() {
				TLV tlv = new ConstructedTLV(new at.innovative_solutions.tlv.ID(at.innovative_solutions.tlv.ID.CLASS_APPLICATION, false, 0), new LinkedList<TLV>());
				addTlvToSelected(tlv);
			}
		};
		fAddConstructedAction.setText("Add constructed TLV");
		fAddConstructedAction.setToolTipText("Adds new constructed TLV");
		fAddConstructedAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		
		fExpandAllBelowAction = new Action() {
			@Override
			public void run() {
				if(fViewer.getSelection().isEmpty() || !(fViewer.getSelection() instanceof IStructuredSelection))
					return;
				
				Object sel = ((IStructuredSelection)fViewer.getSelection()).getFirstElement();
				if(sel instanceof ConstructedTLV)
					setExpandedRecursive((ConstructedTLV)sel, true);
			}
		};
		fExpandAllBelowAction.setText("Expand All");
		fExpandAllBelowAction.setToolTipText("Opens tree for elements all below the selected");
		
		fCollapseAllBelowAction = new Action() {
			@Override
			public void run() {
				if(fViewer.getSelection().isEmpty() || !(fViewer.getSelection() instanceof IStructuredSelection))
					return;
				
				Object sel = ((IStructuredSelection)fViewer.getSelection()).getFirstElement();
				if(sel instanceof ConstructedTLV)
					setExpandedRecursive((ConstructedTLV)sel, false);
			}
		};
		fCollapseAllBelowAction.setText("Collapse All");
		fCollapseAllBelowAction.setToolTipText("Closes tree for all elements below the selected");
	}
	
	private void setExpandedRecursive(ConstructedTLV element, boolean expanded) {
		fViewer.setExpandedState(element, expanded);
		for(TLV subelement : element.getTLVs()) {
			if(subelement instanceof ConstructedTLV)
				setExpandedRecursive((ConstructedTLV)subelement, expanded);
		}
	}
	
	private void addTlvToSelected(TLV tlv) {
		if(fViewer.getSelection().isEmpty() || !(fViewer.getSelection() instanceof IStructuredSelection))
			return;
		
		IStructuredSelection sel = (IStructuredSelection)fViewer.getSelection();
		if(!(sel.getFirstElement() instanceof ConstructedTLV))
			return; // TODO throw?
		
		ConstructedTLV selected = (ConstructedTLV)sel.getFirstElement();
		selected.appendChild(tlv);
		fViewer.refresh();
	}
}
