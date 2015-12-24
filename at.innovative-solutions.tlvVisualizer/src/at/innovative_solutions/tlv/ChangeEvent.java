package at.innovative_solutions.tlv;

import java.util.EventObject;

public class ChangeEvent extends EventObject {
	private static final long serialVersionUID = 766735159222168478L;

	public ChangeEvent(TLV source) {
		super(source);
	}
	
	public boolean equals(final Object other) {
		if(!(other instanceof ChangeEvent))
			return false;
		
		return ((ChangeEvent)other).source == source;
	}
}
