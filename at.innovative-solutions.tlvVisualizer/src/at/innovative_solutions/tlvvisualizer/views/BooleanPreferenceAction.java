package at.innovative_solutions.tlvvisualizer.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;
import org.osgi.service.prefs.BackingStoreException;

public class BooleanPreferenceAction extends Action {
	final private IEclipsePreferences fPreferences;
	final private String fProperty;
	
	BooleanPreferenceAction(String title, IEclipsePreferences preferences, String property) {
		super(title, AS_CHECK_BOX);
		if(preferences == null || property == null)
			throw new IllegalArgumentException();
		
		this.fPreferences = preferences;
		this.fProperty = property;
		
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if(fProperty.equals(event.getKey())) {
					Boolean newVal = Boolean.parseBoolean((String)event.getNewValue());
					setChecked(Boolean.TRUE.equals(newVal));
				}
			}
		});
		setChecked(preferences.getBoolean(property, false));
	}
	
	public void run() {
		fPreferences.putBoolean(fProperty, isChecked());
	}
}