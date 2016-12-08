package at.innovative_solutions.tlvvisualizer.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;

import at.innovative_solutions.tlv.ValueDecoder;

public class MainView extends ViewPart {
	public static final String ID = "at.innovativesolutions.tlvvisualizer.views.MainView";

	public static final String PREFERENCE_NODE = "at.innovative-solutions.preferences.tlvVisualizer";
	public static final String PREFERENCE_AUTO_UPDATE = "auto-update";
	public static final String PREFERENCE_DECODER = "decoder";
	public static final String DECODER_ATTRIBUTE_NAME = "decoder_name";
	public static final long AUTO_UPATE_TIMEOUT = 500; // ms
	public static final String IVALUEDECODER_ID = "at.innovative_solutions.tlvvisualizer.extensionpoint.valuedecoder";

	final static private HashMap<String, IConfigurationElement> fDecoders = new HashMap<String, IConfigurationElement>();

	TLVViewer fTlvViewer;
	Text fTextField;
	
	Action fParseTextFieldAction;
	TimerTask fTimerTask;
	
	final IEclipsePreferences fPreferences;
	Clipboard fClipboard;
	
	public MainView() {
		// TODO correctly dispose of this using event?
		fPreferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE);
	}
	
	private void initDecoders() {
		IConfigurationElement decoderElements[] = Platform.getExtensionRegistry().getConfigurationElementsFor(IVALUEDECODER_ID);
		for(IConfigurationElement e : decoderElements) {
			fDecoders.put(e.getAttribute("class"), e);
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		fClipboard = new Clipboard(parent.getDisplay());
		initDecoders();

		final GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		
		fTextField = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.FILL);
		fTextField.setText("6F1A840E315041592E5359532E4444463031A5088801025F2D02656E");
		//fTextField.setText("8407A0000000041010A50F500A4D617374657243617264870101");
		final GridData labelLayoutData = new GridData();
		fTextField.setLayoutData(labelLayoutData);
		labelLayoutData.horizontalAlignment = SWT.FILL;
		labelLayoutData.grabExcessHorizontalSpace = true;

		fTlvViewer = new TLVViewer(parent, SWT.NONE);
		final GridData tlvViewerLayout = new GridData();
		fTlvViewer.setLayoutData(tlvViewerLayout);
		tlvViewerLayout.horizontalAlignment = SWT.FILL;
		tlvViewerLayout.verticalAlignment = SWT.FILL;
		tlvViewerLayout.grabExcessVerticalSpace = true;
		tlvViewerLayout.grabExcessHorizontalSpace = true;
		
		setDecoder(fPreferences.get(PREFERENCE_DECODER, null));
		makeActions();
		contributeToActionBars();

		fTlvViewer.addModifyListener(new ModifyListener() {
			@Override
			public void modified(ModifyEvent e) {
				fTextField.setText(e.getNewTlvString());
			}
		});
		
		// setup automatic update on textfield change
		final Timer timer = new Timer();
		final Runnable timeoutEvent = new Runnable() {
			@Override
			public void run() {
				fParseTextFieldAction.run();
			}
		};
		fTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					fParseTextFieldAction.run();
				} else if (e.character != 0 && 
					       fPreferences.getBoolean(PREFERENCE_AUTO_UPDATE, false)) {
					if(fTimerTask != null)
						fTimerTask.cancel();
					
					fTimerTask = new TimerTask() {
						@Override
						public void run() {
							parent.getDisplay().asyncExec(timeoutEvent);
						}
					}; 
					timer.schedule(fTimerTask, AUTO_UPATE_TIMEOUT);
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});

		fPreferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if(!PREFERENCE_DECODER.equals(event.getKey()))
					return;
				setDecoder((String)event.getNewValue());
			}
		});
		
		parent.pack();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new BooleanPreferenceAction("Auto update", fPreferences,
				PREFERENCE_AUTO_UPDATE));
		
		final MenuManager decoderMenu = new MenuManager("Decoder", null);
		
		manager.add(decoderMenu);
		for(Map.Entry<String, IConfigurationElement> e : fDecoders.entrySet()) {
			decoderMenu.add(new SelectionPreferenceAction(e.getValue().getAttribute(DECODER_ATTRIBUTE_NAME), fPreferences, PREFERENCE_DECODER, e.getKey()));
		}
		decoderMenu.add(new SelectionPreferenceAction("None", fPreferences, PREFERENCE_DECODER, "null"));
	}
	
	private void setDecoder(String name) {
		if(fDecoders.containsKey(name)) {
			try {
				IConfigurationElement e = fDecoders.get(name);
				fTlvViewer.setDecoder((ValueDecoder)e.createExecutableExtension("class"));
				return;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		fTlvViewer.setDecoder(null);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fParseTextFieldAction);
		// TODO manager.add(fCopyParsedAction);
	}

	private void makeActions() {
		final ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		
		// TODO reintroduce copy parsed action

		fParseTextFieldAction = new Action() {
			@Override
			public void run() {
				fTlvViewer.setTLV(fTextField.getText());
			}
		};
		fParseTextFieldAction.setText("Parse");
		fParseTextFieldAction.setToolTipText("Parse text given");
		fParseTextFieldAction.setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
	}
	
	public void setTlvString(String s) {
		fTextField.setText(s);
		fTlvViewer.setTLV(s);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		fTlvViewer.setFocus();
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