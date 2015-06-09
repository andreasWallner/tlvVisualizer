package at.innovative_solutions.tlvvisualizer.views;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.DecodingFormatter;
import at.innovative_solutions.tlv.EMVValueDecoder;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;
import at.innovative_solutions.tlv.Utils;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class MainView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "at.innovativesolutions.tlvvisualizer.views.MainView";

	private TreeViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Clipboard _clipboard;
	
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
			Long tagNum = e.getID().toLong();
			String ret = "";
			switch(columnIndex) {
			case 0:
				ret = Utils.bytesToHexString(e.getID().toBytes());
				break;
			case 1:
				ret = String.valueOf(e.getLength());
				break;
			case 2:
				if(_tagInfo.containsKey(tagNum)) 
					ret = _tagInfo.get(tagNum)._name;
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

	/**
	 * The constructor.
	 */
	public MainView() {
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
		_clipboard = new Clipboard(parent.getDisplay());
		
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.FILL);
		//text.setText("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E");
		text.setText("8407A0000000041010A50F500A4D617374657243617264870101");
		GridData labelLayoutData = new GridData();
		text.setLayoutData(labelLayoutData);
		labelLayoutData.horizontalAlignment = SWT.FILL;
		labelLayoutData.grabExcessHorizontalSpace = true;
		
		Button button = new Button(parent, SWT.NONE);
		button.setText("parse");
		GridData buttonLayoutData = new GridData();
		button.setLayoutData(buttonLayoutData);
		buttonLayoutData.horizontalAlignment = SWT.END;
		
		Button cbButton = new Button(parent, SWT.NONE);
		cbButton.setText("parse to clipboard");
		GridData cbButtonLayoutData = new GridData();
		cbButton.setLayoutData(cbButtonLayoutData);
		cbButtonLayoutData.horizontalAlignment = SWT.END;
		
		Tree tlvTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
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
		

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "at.innovative-solutions.tlvVisualizer.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		//contributeToActionBars();
		
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(text.getText()));
				final List<TLV> tlvs = TLV.parseTLVs(input);
				
				viewer.setInput(new TreeRootWrapper(tlvs));
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {	
			}
		});
		cbButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final ByteBuffer input = ByteBuffer.wrap(Utils.hexStringToBytes(text.getText()));
				final List<TLV> tlvs = TLV.parseTLVs(input);
				String formatted = new DecodingFormatter("  ", _tagInfo).format(tlvs);
				
				_clipboard.setContents(new Object[] {formatted}, new Transfer[]{TextTransfer.getInstance()});
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
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
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
}