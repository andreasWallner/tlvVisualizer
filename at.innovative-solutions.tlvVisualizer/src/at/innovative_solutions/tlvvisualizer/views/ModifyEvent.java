package at.innovative_solutions.tlvvisualizer.views;

import java.util.EventObject;

public class ModifyEvent extends EventObject {
	private static final long serialVersionUID = 3673792919997516310L;
	private String fNewTlvString;

	public ModifyEvent(Object source, String newTlvString) {
		super(source);
		fNewTlvString = newTlvString;
	}
	
	public String getNewTlvString() {
		return fNewTlvString;
	}
}
