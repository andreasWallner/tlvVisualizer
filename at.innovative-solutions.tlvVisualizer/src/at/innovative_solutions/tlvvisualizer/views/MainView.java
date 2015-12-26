package at.innovative_solutions.tlvvisualizer.views;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
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
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;

public class MainView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "at.innovativesolutions.tlvvisualizer.views.MainView";
	
	public static final String PROP_ID = "ID";
	public static final String PROP_SIZE = "SIZE";
	public static final String PROP_NAME = "NAME";
	public static final String PROP_DECODED = "DECODED";
	public static final String PROP_ENCODED = "ENCODED";
	public static final String[] PROPS = { PROP_ID, PROP_SIZE, PROP_NAME, PROP_DECODED, PROP_ENCODED };
	
	public static final String PREFERENCE_NODE = "at.innovative-solutions.preferences.tlvVisualizer";
	public static final String PREFERENCE_AUTO_UPDATE = "auto-update";

	private TreeViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Timer timer;
	private final IEclipsePreferences _preferences;
	
	private HashMap<Long, TagInfo> _tagInfo;

	class TreeRootWrapper {
		Object[] _wrapped;
		@SuppressWarnings("rawtypes")
		TreeRootWrapper(Object toWrap) {
			if(toWrap instanceof List)
				_wrapped = ((List) toWrap).toArray();
			else
				_wrapped = new Object[] {toWrap};
		}
		Object[] getWrapped() {
			return _wrapped;
		}
	}
	
	class TLVContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof ConstructedTLV)
				return ((ConstructedTLV) parentElement).getTLVs().toArray();
			return new Object[0];
		}
		public Object getParent(Object element) {
			return ((TLV) element).getParent();
		}
		public boolean hasChildren(Object element) {
			if(element instanceof ConstructedTLV)
				return ((ConstructedTLV) element).getTLVs().size() > 0;
			return false;
		}
		public Object[] getElements(Object element) {
			if(element instanceof TreeRootWrapper)
				return ((TreeRootWrapper) element).getWrapped();
			return getChildren(element);
		}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	class TLVLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if(!(element instanceof TLV))
				return null;
			
			TLV e = (TLV)element;
			Long tagNum = e.getID() != null ? e.getID().toLong() : 0;
			String ret = "";
			switch(columnIndex) {
			case 0:
				if(e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case 1:
				ret = String.valueOf(e.getLength());
				break;
			case 2:
				if(element instanceof ErrorTLV) {
					ret = "ERROR: " + ((ErrorTLV) element).getError();
				} else {
					if(_tagInfo.containsKey(tagNum)) 
						ret = _tagInfo.get(tagNum)._name;
				}
				break;
			case 3:
				if(e instanceof PrimitiveTLV && _tagInfo.containsKey(tagNum))
					try {
						ret = EMVValueDecoder.asString(((PrimitiveTLV) e).getData(), _tagInfo.get(tagNum)._format);
					} catch(UnsupportedEncodingException ex) {}
				break;
			case 4:
				if(element instanceof PrimitiveTLV)
					ret += Utils.bytesToHexString(((PrimitiveTLV) e).getData());
				else if(element instanceof ErrorTLV)
					ret += Utils.bytesToHexString(((ErrorTLV) e).getRemainingData());
				break;
			}
			return ret;
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	class TLVCellModifier implements ICellModifier {
		private Viewer _viewer;
		private Text _serialized;
		
		public TLVCellModifier(Viewer viewer, Text serialized) {
			_viewer = viewer;
			_serialized = serialized;
		}
		
		public boolean canModify(Object element, String property) {
			if(property == PROP_ID)
				return true;
			if(property == PROP_ENCODED && element instanceof PrimitiveTLV)
				return true;
			return false;
		}
		
		public Object getValue(Object element, String property) {
			TLV e = (TLV)element;
			Long tagNum = e.getID() != null ? e.getID().toLong() : 0;
			String ret = "";
			switch(property) {
			case PROP_ID:
				if(e.getID() != null)
					ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case PROP_SIZE:
				ret = String.valueOf(e.getLength());
				break;
			case PROP_NAME:
				if(element instanceof ErrorTLV) {
					ret = "ERROR: " + ((ErrorTLV) element).getError();
				} else {
					if(_tagInfo.containsKey(tagNum)) 
						ret = _tagInfo.get(tagNum)._name;
				}
				break;
			case PROP_DECODED:
				if(e instanceof PrimitiveTLV && _tagInfo.containsKey(tagNum))
					try {
						ret = EMVValueDecoder.asString(((PrimitiveTLV) e).getData(), _tagInfo.get(tagNum)._format);
					} catch(UnsupportedEncodingException ex) {}
				break;
			case PROP_ENCODED:
				if(element instanceof PrimitiveTLV)
					ret += Utils.bytesToHexString(((PrimitiveTLV) e).getData());
				else if(element instanceof ErrorTLV)
					ret += Utils.bytesToHexString(((ErrorTLV) e).getRemainingData());
				break;
			}
			return ret;
		}
		
		public void modify(Object element, String property, Object value) {
			if(element instanceof Item) element = ((Item) element).getData();
			
			if(property == PROP_ID) {
				TLV tlv = (TLV)element;
				byte[] idBytes = Utils.hexStringToBytes(value.toString());
				ByteBuffer buffer = ByteBuffer.wrap(idBytes);
				tlv.setID(at.innovative_solutions.tlv.ID.parseID(buffer));
			}
			else if(property == PROP_ENCODED) {
				PrimitiveTLV tlv = (PrimitiveTLV)element;
				tlv.setData(Utils.hexStringToBytes(value.toString()));
			}
			_viewer.refresh();
			Object[] input = ((TreeRootWrapper) _viewer.getInput()).getWrapped();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			for(Object o : input) {
				TLV t = (TLV)o;
				byte[] serialized = t.toBytes();
				stream.write(serialized, 0, serialized.length);
			}
			_serialized.setText(Utils.bytesToHexString(stream.toByteArray()));
		}
	}

	/**
	 * The constructor.
	 */
	public MainView() {
		_preferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE);
		Bundle bundle = Platform.getBundle("at.innovative-solutions.tlvVisualizer");
		URL fileURL = bundle.getEntry("resources/EMV.xml");
		try {
			File file = new File(FileLocator.resolve(fileURL).toURI());
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			_tagInfo = TagInfo.loadXML(doc);
			
			System.out.println(_tagInfo.get(0x9F01));
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
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		final Clipboard clipboard = new Clipboard(parent.getDisplay());
		
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		
		final Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.FILL);
		//text.setText("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E");
		text.setText("8407A0000000041010A50F500A4D617374657243617264870101");
		GridData labelLayoutData = new GridData();
		text.setLayoutData(labelLayoutData);
		labelLayoutData.horizontalAlignment = SWT.FILL;
		labelLayoutData.grabExcessHorizontalSpace = true;
		
		Button cbButton = new Button(parent, SWT.NONE);
		cbButton.setText("parse to clipboard");
		GridData cbButtonLayoutData = new GridData();
		cbButton.setLayoutData(cbButtonLayoutData);
		cbButtonLayoutData.horizontalAlignment = SWT.END;
		
		final Tree tlvTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tlvTree.setHeaderVisible(true);
		tlvTree.setLinesVisible(true);
		viewer = new TreeViewer(tlvTree);
		
		TreeColumn column1 = new TreeColumn(tlvTree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("ID");
		column1.setWidth(100);
		TreeColumn column2 = new TreeColumn(tlvTree, SWT.RIGHT);
		column2.setAlignment(SWT.CENTER);
		column2.setText("Size");
		column2.setWidth(35);
		TreeColumn column3 = new TreeColumn(tlvTree, SWT.RIGHT);
		column3.setAlignment(SWT.LEFT);
		column3.setText("Name");
		column3.setWidth(300);
		TreeColumn column4 = new TreeColumn(tlvTree, SWT.RIGHT);
		column4.setAlignment(SWT.LEFT);
		column4.setText("Decoded");
		column4.setWidth(150);
		TreeColumn column5 = new TreeColumn(tlvTree, SWT.RIGHT);
		column5.setAlignment(SWT.LEFT);
		column5.setText("Encoded");
		column5.setWidth(300);	
		
		viewer.setContentProvider(new TLVContentProvider());
		viewer.setLabelProvider(new TLVLabelProvider());
		viewer.setInput(null);
		GridData viewerLayoutData = new GridData();
		viewerLayoutData.horizontalAlignment = GridData.FILL;
		viewerLayoutData.verticalAlignment = GridData.FILL;
		viewerLayoutData.grabExcessHorizontalSpace = true;
		viewerLayoutData.grabExcessVerticalSpace = true;
		tlvTree.setLayoutData(viewerLayoutData);

		CellEditor[] editors = new CellEditor[5];
		editors[0] = new TextCellEditor(tlvTree);
		editors[1] = new TextCellEditor(tlvTree);
		editors[2] = new TextCellEditor(tlvTree);
		editors[3] = new TextCellEditor(tlvTree);
		editors[4] = new TextCellEditor(tlvTree);
		
		viewer.setColumnProperties(PROPS);
		viewer.setCellModifier(new TLVCellModifier(viewer, text));
		viewer.setCellEditors(editors);
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "at.innovative-solutions.tlvVisualizer.viewer");
		makeActions();
		hookContextMenu();
		//hookDoubleClickAction();
		contributeToActionBars();
		
		timer = new Timer();
		text.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.character == '\r') {
					final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(text.getText()));
					final List<TLV> tlvs = TLV.parseTLVsWithErrors(input);
					
					viewer.setInput(new TreeRootWrapper(tlvs));
					viewer.refresh();					
				} else if(arg0.character != 0) {
					if(_preferences.getBoolean("auto-update", false)) {
						timer.cancel();
						timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								System.out.println("called");
								parent.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(text.getText()));
										final List<TLV> tlvs = TLV.parseTLVsWithErrors(input);
										
										viewer.setInput(new TreeRootWrapper(tlvs));
										viewer.refresh();					
									}
								});
							}
						}, 500);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {}
		});
		cbButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(text.getText()));
				final List<TLV> tlvs = TLV.parseTLVs(input);
				String formatted = new DecodingFormatter("  ", _tagInfo).format(tlvs);
				
				clipboard.setContents(new Object[] {formatted}, new Transfer[]{TextTransfer.getInstance()});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {	
			}
		});
		parent.pack();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new BooleanPreferenceAction("Auto update", _preferences, PREFERENCE_AUTO_UPDATE));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"TLV Visualizer View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		try {
			_preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}