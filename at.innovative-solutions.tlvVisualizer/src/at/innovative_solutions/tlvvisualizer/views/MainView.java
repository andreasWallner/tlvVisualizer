package at.innovative_solutions.tlvvisualizer.views;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.DecodingFormatter;
import at.innovative_solutions.tlv.EMVValueDecoder;
import at.innovative_solutions.tlv.ErrorTLV;
import at.innovative_solutions.tlv.ID;
import at.innovative_solutions.tlv.InvalidEncodedValueException;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class MainView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "at.innovativesolutions.tlvvisualizer.views.MainView";

	public static final String PROP_ID = "ID";
	public static final String PROP_TYPE = "TYPE";
	public static final String PROP_SIZE = "SIZE";
	public static final String PROP_NAME = "NAME";
	public static final String PROP_DECODED = "DECODED";
	public static final String PROP_ENCODED = "ENCODED";
	public static final String[] PROPS = { PROP_ID, PROP_TYPE, PROP_SIZE, PROP_NAME,
		PROP_DECODED, PROP_ENCODED };

	public static final String PREFERENCE_NODE = "at.innovative-solutions.preferences.tlvVisualizer";
	public static final String PREFERENCE_AUTO_UPDATE = "auto-update";

	private TreeViewer fViewer;
	private Text fTextField;
	private Action fCopyParsedAction;
	private Action fDeleteAction;
	private Action fAddPrimitiveAction;
	private Action fAddConstructedAction;
	private Action fParseTextFieldAction;
	private Action fDoubleClickAction;
	private Timer fTimer;
	private final IEclipsePreferences fPreferences;
	Clipboard fClipboard;

	private HashMap<Long, TagInfo> fTagInfo;

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
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
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
			Long tagNum = e.getID() != null ? e.getID().toLong() : 0;
			String ret = "";
			switch (columnIndex) {
			case 0:
				if (e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case 1:
				ret = e instanceof ConstructedTLV ? "C" : "P";
				break;
			case 2:
				ret = String.valueOf(e.getLength());
				break;
			case 3:
				if (element instanceof ErrorTLV) {
					ret = "ERROR: " + ((ErrorTLV) element).getError();
				} else {
					if (fTagInfo.containsKey(tagNum))
						ret = fTagInfo.get(tagNum)._name;
				}
				break;
			case 4:
				if (e instanceof PrimitiveTLV && fTagInfo.containsKey(tagNum))
					try {
						ret = EMVValueDecoder.asString(
								((PrimitiveTLV) e).getData(),
								fTagInfo.get(tagNum)._format);
					} catch (UnsupportedEncodingException ex) {
					}
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
		private Text fSerialized;

		public TLVCellModifier(Viewer viewer, Text serialized) {
			fViewer = viewer;
			fSerialized = serialized;
		}

		@Override
		public boolean canModify(Object element, String property) {
			if (property == PROP_ID)
				return true;
			if ((property == PROP_ENCODED || property == PROP_DECODED)
					&& element instanceof PrimitiveTLV)
				return true;
			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			TLV e = (TLV) element;
			Long tagNum = e.getID() != null ? e.getID().toLong() : 0;
			String ret = "";
			switch (property) {
			case PROP_ID:
				if (e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case PROP_TYPE:
				ret = e instanceof ConstructedTLV ? "C" : "P";
				break;
			case PROP_SIZE:
				ret = String.valueOf(e.getLength());
				break;
			case PROP_NAME:
				if (element instanceof ErrorTLV) {
					ret = "ERROR: " + ((ErrorTLV) element).getError();
				} else {
					if (fTagInfo.containsKey(tagNum))
						ret = fTagInfo.get(tagNum)._name;
				}
				break;
			case PROP_DECODED:
				if (e instanceof PrimitiveTLV && fTagInfo.containsKey(tagNum))
					try {
						ret = EMVValueDecoder.asString(
								((PrimitiveTLV) e).getData(),
								fTagInfo.get(tagNum)._format);
					} catch (UnsupportedEncodingException ex) {
					}
				break;
			case PROP_ENCODED:
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
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			if (property == PROP_ID) {
				TLV tlv = (TLV) element;
				byte[] idBytes = Utils.hexStringToBytes(value.toString());
				ByteBuffer buffer = ByteBuffer.wrap(idBytes);
				tlv.setID(at.innovative_solutions.tlv.ID.parseID(buffer));
			} else if (property == PROP_DECODED) {
				PrimitiveTLV tlv = (PrimitiveTLV) element;
				Long tagNum = tlv.getID() != null ? tlv.getID().toLong() : 0;
				byte[] encoded = null;
				try {
					encoded = EMVValueDecoder.toValue(value.toString(), fTagInfo.get(tagNum)._format);
					tlv.setData(encoded);
				} catch(InvalidEncodedValueException ex)
				{
					MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					box.setText("Invalid input");
					box.setMessage("Invalid input for element of type " + fTagInfo.get(tagNum)._format + "\n" + ex.getMessage());
					box.open();
				}
			} else if (property == PROP_ENCODED) {
				PrimitiveTLV tlv = (PrimitiveTLV) element;
				tlv.setData(Utils.hexStringToBytes(value.toString()));
			}
			fViewer.refresh();
			Object[] input = ((TreeRootWrapper) fViewer.getInput())
					.getWrapped();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			for (Object o : input) {
				TLV t = (TLV) o;
				byte[] serialized = t.toBytes();
				stream.write(serialized, 0, serialized.length);
			}
			fSerialized.setText(Utils.bytesToHexString(stream.toByteArray()));
		}
	}

	/**
	 * The constructor.
	 */
	public MainView() {
		fPreferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE);
		Bundle bundle = Platform
				.getBundle("at.innovative-solutions.tlvVisualizer");
		URL fileURL = bundle.getEntry("resources/EMV.xml");
		try {
			File file = new File(FileLocator.resolve(fileURL).toURI());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);

			fTagInfo = TagInfo.loadXML(doc);

			System.out.println(fTagInfo.get(0x9F01));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		fClipboard = new Clipboard(parent.getDisplay());

		final GridLayout layout = new GridLayout();
		parent.setLayout(layout);

		fTextField = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.FILL);
		// text.setText("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E");
		fTextField
		.setText("8407A0000000041010A50F500A4D617374657243617264870101");
		final GridData labelLayoutData = new GridData();
		fTextField.setLayoutData(labelLayoutData);
		labelLayoutData.horizontalAlignment = SWT.FILL;
		labelLayoutData.grabExcessHorizontalSpace = true;

		final Tree tlvTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		tlvTree.setHeaderVisible(true);
		tlvTree.setLinesVisible(true);
		fViewer = new TreeViewer(tlvTree);

		final TreeColumn column1 = new TreeColumn(tlvTree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("ID");
		column1.setWidth(100);
		final TreeColumn column2 = new TreeColumn(tlvTree, SWT.CENTER);
		column2.setAlignment(SWT.CENTER);
		column2.setText("Type");
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
		final GridData viewerLayoutData = new GridData();
		viewerLayoutData.horizontalAlignment = GridData.FILL;
		viewerLayoutData.verticalAlignment = GridData.FILL;
		viewerLayoutData.grabExcessHorizontalSpace = true;
		viewerLayoutData.grabExcessVerticalSpace = true;
		tlvTree.setLayoutData(viewerLayoutData);

		final CellEditor[] editors = new CellEditor[PROPS.length];
		for(int i = 0; i < PROPS.length; i++)
			editors[i] = new TextCellEditor(tlvTree);

		fViewer.setColumnProperties(PROPS);
		fViewer.setCellModifier(new TLVCellModifier(fViewer, fTextField));
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
				if(tlv instanceof ConstructedTLV) {
					fAddPrimitiveAction.setEnabled(true);
					fAddConstructedAction.setEnabled(true);
				}
			}
		});

		// Create the help context id for the viewer's control
		PlatformUI
		.getWorkbench()
		.getHelpSystem()
		.setHelp(fViewer.getControl(),
				"at.innovative-solutions.tlvVisualizer.viewer");
		makeActions();
		hookContextMenu();
		// hookDoubleClickAction();
		contributeToActionBars();

		fTimer = new Timer();
		fTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.character == '\r') {
					fParseTextFieldAction.run();
				} else if (arg0.character != 0) {
					if (fPreferences.getBoolean(PREFERENCE_AUTO_UPDATE, false)) {
						fTimer.cancel();
						fTimer = new Timer();
						fTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								parent.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										final ByteBuffer input = ByteBuffer.wrap(Utils
												.hexStringToBytes(fTextField
														.getText()));
										final List<TLV> tlvs = TLV
												.parseTLVsWithErrors(input);

										fViewer.setInput(new TreeRootWrapper(
												tlvs));
										fViewer.refresh();
									}
								});
							}
						}, 500);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});
		parent.pack();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new BooleanPreferenceAction("Auto update", fPreferences,
				PREFERENCE_AUTO_UPDATE));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(fCopyParsedAction);
		manager.add(fDeleteAction);
		manager.add(fAddPrimitiveAction);
		manager.add(fAddConstructedAction);
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fParseTextFieldAction);
		manager.add(fCopyParsedAction);
	}

	private void makeActions() {
		final ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();

		fCopyParsedAction = new Action() {
			@Override
			public void run() {
				final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(fTextField.getText()));
				final List<TLV> tlvs = TLV.parseTLVs(input);
				String formatted = new DecodingFormatter("  ", fTagInfo).format(tlvs);

				fClipboard.setContents(new Object[] { formatted },
						new Transfer[] { TextTransfer.getInstance() });
			}
		};
		fCopyParsedAction.setText("Copy parsed");
		fCopyParsedAction.setToolTipText("Copies parsed and formatted text to clipboard");
		fCopyParsedAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		fDeleteAction = new Action() {
			@Override
			public void run() {
				if(fViewer.getSelection().isEmpty())
					return;
				if(!(fViewer.getSelection() instanceof IStructuredSelection))
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
				Object[] input = ((TreeRootWrapper) fViewer.getInput())
						.getWrapped();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				for (Object o : input) {
					TLV t = (TLV) o;
					byte[] serialized = t.toBytes();
					stream.write(serialized, 0, serialized.length);
				}
				fTextField.setText(Utils.bytesToHexString(stream.toByteArray()));
			}
		};
		fDeleteAction.setText("Delete");
		fDeleteAction.setToolTipText("Removes selected TLV and sub-TLVs");
		fDeleteAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		fAddPrimitiveAction = new Action() {
			@Override
			public void run() {
				addTlvToSelected(new PrimitiveTLV(new at.innovative_solutions.tlv.ID(at.innovative_solutions.tlv.ID.CLASS_APPLICATION, false, 0), new byte[] { }));
			}
		};
		fAddPrimitiveAction.setText("Add primitive TLV");
		fAddPrimitiveAction.setToolTipText("Adds new primitive TLV");
		fAddPrimitiveAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		fAddConstructedAction = new Action() {
			@Override
			public void run() {
				addTlvToSelected(new ConstructedTLV(new at.innovative_solutions.tlv.ID(at.innovative_solutions.tlv.ID.CLASS_APPLICATION, true, 0), new LinkedList<TLV>()));
			}
		};
		fAddConstructedAction.setText("Add constructed TLV");
		fAddConstructedAction.setToolTipText("Adds new constructed TLV");
		fAddConstructedAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		fParseTextFieldAction = new Action() {
			@Override
			public void run() {
				List<TLV> tlvs = null;
				byte[] data = Utils.hexStringToBytes(fTextField.getText());
				final ByteBuffer input = ByteBuffer.wrap(data);
				tlvs = TLV.parseTLVsWithErrors(input);

				fViewer.setInput(new TreeRootWrapper(tlvs));
				fViewer.refresh();
			}
		};
		fParseTextFieldAction.setText("Parse");
		fParseTextFieldAction.setToolTipText("Parse text given");
		fParseTextFieldAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

		fDoubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void addTlvToSelected(TLV tlv) {
		if(fViewer.getSelection().isEmpty())
			throw new RuntimeException("can't add without selected item");
		if(!(fViewer.getSelection() instanceof IStructuredSelection))
			return;

		IStructuredSelection sel = (IStructuredSelection)fViewer.getSelection();
		if(!(sel.getFirstElement() instanceof ConstructedTLV))
			return;

		ConstructedTLV selected = (ConstructedTLV)sel.getFirstElement();
		selected.appendChild(tlv);
		refreshViewer();
	}

	private void refreshViewer() {
		fViewer.refresh();
		Object[] input = ((TreeRootWrapper) fViewer.getInput())
				.getWrapped();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		for (Object o : input) {
			TLV t = (TLV) o;
			byte[] serialized = t.toBytes();
			stream.write(serialized, 0, serialized.length);
		}
		fTextField.setText(Utils.bytesToHexString(stream.toByteArray()));
	}

	private void hookDoubleClickAction() {
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				fDoubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(fViewer.getControl().getShell(),
				"TLV Visualizer View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		try {
			fPreferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}