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
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.ErrorTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.InvalidEncodedValueException;
import at.innovative_solutions.tlv.NullValueDecoder;
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
	
	TreeViewer fViewer;
	ValueDecoder fDecoder;
	boolean fPinned;
	
	private Vector<ModifyListener> fModifyListeners = new Vector<ModifyListener>();
	
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

	class TLVLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof TLV))
				return null;

			TLV e = (TLV) element;
			String ret = "";
			switch (columnIndex) {
			case 0:
				if (e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case 1:
				ret = e.getID().isPrimitive() ? "P" : "C"; // TODO error tlv
				break;
			case 2:
				ret = String.valueOf(e.getLength());
				break;
			case 3:
				if (element instanceof ErrorTLV) {
					ret = "ERROR: " + ((ErrorTLV) element).getError();
				} else {
					ret = TLVViewer.this.fDecoder.getName(e);
				}
				break;
			case 4:
				ret = TLVViewer.this.fDecoder.toString(e);
				break;
			case 5:
				if (element instanceof PrimitiveTLV)
					ret += Utils.bytesToHexString(((PrimitiveTLV) e).getData());
				else if (element instanceof ErrorTLV)
					ret += Utils.bytesToHexString(((ErrorTLV) e)
							.getRemainingData());
				break;
			}
			return ret;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	class TLVCellModifier implements ICellModifier {
		private Viewer fViewer;

		public TLVCellModifier(Viewer viewer) {
			fViewer = viewer;
		}

		@Override
		public boolean canModify(Object element, String property) {
			TLV tlv = (TLV)element;
			
			if(fPinned && tlv.getParent() == null)
				return false;
			
			switch(property) {
			case PROP_ID:
				return true;
			case PROP_DECODED:
				return tlv.getID().isPrimitive() && (TLVViewer.this.fDecoder.isValueParsable(tlv));
			case PROP_ENCODED:
				return element instanceof PrimitiveTLV;
			}

			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			TLV e = (TLV) element;
			String ret = "";
			switch (property) {
			case PROP_ID:
				if (e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case PROP_DECODED:
				ret = TLVViewer.this.fDecoder.toString(e);
				break;
			case PROP_ENCODED:
				if (element instanceof PrimitiveTLV)
					ret += Utils.bytesToHexString(((PrimitiveTLV) e).getData());
				else if (element instanceof ErrorTLV)
					ret += Utils.bytesToHexString(((ErrorTLV) e)
							.getRemainingData());
				break;
			default:
				throw new RuntimeException("can't edit read-only value");
			}
			return ret;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			if (property == PROP_ID) {
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
			} else if (property == PROP_DECODED) {
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
			} else if (property == PROP_ENCODED) {
				PrimitiveTLV tlv = (PrimitiveTLV) element;
				tlv.setData(Utils.hexStringToBytes(value.toString()));
			}
			fViewer.refresh();
			modified();
		}
	}

	public TLVViewer(Composite parent, int style) {
		super(parent, style);
		createContents(parent);
		fDecoder = new NullValueDecoder();
	}

	public void createContents(final Composite parent) {
		final FillLayout gridLayout = new FillLayout();
		this.setLayout(gridLayout);
		
		final Tree tlvTree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tlvTree.setHeaderVisible(true);
		tlvTree.setLinesVisible(true);
		
		fViewer = new TreeViewer(tlvTree);
		
		final TreeColumn column1 = new TreeColumn(tlvTree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("ID");
		column1.setWidth(100);
		final TreeColumn column2 = new TreeColumn(tlvTree, SWT.CENTER);
		column2.setAlignment(SWT.CENTER);
		column2.setText("P/C");
		column2.setWidth(35);
		final TreeColumn column3 = new TreeColumn(tlvTree, SWT.RIGHT);
		column3.setAlignment(SWT.CENTER);
		column3.setText("Size");
		column3.setWidth(35);
		final TreeColumn column4 = new TreeColumn(tlvTree, SWT.RIGHT);
		column4.setAlignment(SWT.LEFT);
		column4.setText("Name");
		column4.setWidth(300);
		final TreeColumn column5 = new TreeColumn(tlvTree, SWT.RIGHT);
		column5.setAlignment(SWT.LEFT);
		column5.setText("Decoded");
		column5.setWidth(150);
		final TreeColumn column6 = new TreeColumn(tlvTree, SWT.RIGHT);
		column6.setAlignment(SWT.LEFT);
		column6.setText("Encoded");
		column6.setWidth(300);

		fViewer.setContentProvider(new TLVContentProvider());
		fViewer.setLabelProvider(new TLVLabelProvider());
		fViewer.setInput(null);
		
		final CellEditor[] editors = new CellEditor[PROPS.length];
		for(int i = 0; i < PROPS.length; i++)
			editors[i] = new TextCellEditor(tlvTree);
		
		fViewer.setColumnProperties(PROPS);
		fViewer.setCellModifier(new TLVCellModifier(fViewer));
		fViewer.setCellEditors(editors);
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				fDeleteAction.setEnabled(false);
				fAddPrimitiveAction.setEnabled(false);
				fAddConstructedAction.setEnabled(false);

				if(!event.getSelection().isEmpty())
					fDeleteAction.setEnabled(true);
				else
					return;

				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				TLV tlv = (TLV)sel.getFirstElement();
				if(!tlv.getID().isPrimitive()) {
					fAddPrimitiveAction.setEnabled(true);
					fAddConstructedAction.setEnabled(true);
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
