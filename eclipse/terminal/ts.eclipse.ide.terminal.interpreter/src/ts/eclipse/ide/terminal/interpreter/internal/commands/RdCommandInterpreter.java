package ts.eclipse.ide.terminal.interpreter.internal.commands;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.progress.UIJob;

import ts.eclipse.ide.terminal.interpreter.AbstractCommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.internal.jobs.RefreshContainerJob;

public class RdCommandInterpreter extends AbstractCommandInterpreter {

	private final String path;

	public RdCommandInterpreter(String path, String workingDir) {
		super(workingDir);
		this.path = path;
	}

	@Override
	public void execute() {
		final IContainer[] c = ResourcesPlugin.getWorkspace().getRoot()
				.findContainersForLocation(getWorkingDirPath().append(path));
		if (c != null && c.length > 0) {
			for (int i = 0; i < c.length; i++) {
				UIJob job = new RefreshContainerJob(c[i].getParent(), true);
				job.schedule();
			}
		}
	}

	@Override
	public void onTrace(String line) {
		// Do nothing
	}

}
