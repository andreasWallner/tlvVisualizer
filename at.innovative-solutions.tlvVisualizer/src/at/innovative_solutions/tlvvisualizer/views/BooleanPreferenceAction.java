package at.innovative_solutions.tlvvisualizer.views;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.action.Action;
import org.osgi.service.prefs.BackingStoreException;

public class BooleanPreferenceAction extends Action {
	private IEclipsePreferences preferences;
	private String property;
	
	BooleanPreferenceAction(String title, IEclipsePreferences preferences, String property) {
		super(title, AS_CHECK_BOX);
		if(preferences == null || property == null)
			throw new IllegalArgumentException();
		
		this.preferences = preferences;
		this.property = property;
		
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent arg0) {
				if(property.equals(arg0.getKey())) {
					Boolean newVal = Boolean.parseBoolean((String)arg0.getNewValue());
					setChecked(Boolean.TRUE.equals(newVal));
				}
			}
		});
		setChecked(preferences.getBoolean(property, false));
	}
	
	public void run() {
		preferences.putBoolean(property, isChecked());
	}
}