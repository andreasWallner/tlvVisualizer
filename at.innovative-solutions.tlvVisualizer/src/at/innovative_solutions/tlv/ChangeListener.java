package at.innovative_solutions.tlv;

import java.util.EventListener;

public interface ChangeListener extends EventListener {
	void changed(ChangeEvent event);
}
