package at.innovative_solutions.tlvvisualizer.views;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	public static final String PLUGIN_ID = "at.innovative-solutions.tlvVisualizer";
	
	private static Activator fPlugin;
	
	public Activator() {}
	
	public final void start(final BundleContext context) throws Exception {
		super.start(context);
		fPlugin = this;
	}
	
	public final void stop(final BundleContext context) throws Exception {
		fPlugin = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return fPlugin;
	}
}
