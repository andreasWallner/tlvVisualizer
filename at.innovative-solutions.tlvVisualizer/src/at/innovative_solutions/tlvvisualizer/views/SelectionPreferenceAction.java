package at.innovative_solutions.tlvvisualizer.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;

public class SelectionPreferenceAction extends Action {
	final private IEclipsePreferences fPreferences;
	final private String fProperty;
	final private String fSelectionValue;
	
	SelectionPreferenceAction(final String title, final IEclipsePreferences preferences, String property, String selectionValue) {
		super(title, AS_CHECK_BOX);
		if(preferences == null || property == null || selectionValue == null)
			throw new IllegalArgumentException();
		
		fPreferences = preferences;
		fProperty = property;
		fSelectionValue = selectionValue;
		
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if(!fProperty.equals(event.getKey()))
					return;
				
				setChecked(fSelectionValue.equals(event.getNewValue()));				
			}
		});
		
		setChecked(fSelectionValue.equals(fPreferences.get(fProperty, null)));
	}
	
	public void run() {
		fPreferences.put(fProperty, fSelectionValue);
		setChecked(true);
	}
}
