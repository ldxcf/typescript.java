/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.resources;

import java.util.ArrayList;
import java.util.List;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.ITypeScriptAsynchCollector;
import ts.client.ITypeScriptServiceClient;
import ts.client.Location;
import ts.client.codefixes.ITypeScriptGetCodeFixesCollector;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.client.format.FormatOptions;
import ts.client.format.ITypeScriptFormatCollector;
import ts.client.navbar.ITypeScriptNavBarCollector;
import ts.client.navbar.NavigationBarItemRoot;
import ts.client.occurrences.ITypeScriptOccurrencesCollector;
import ts.client.references.ITypeScriptReferencesCollector;
import ts.internal.LocationReader;
import ts.internal.client.protocol.ConfigureRequestArguments;

/**
 * Abstract TypeScript file.
 *
 */
public abstract class AbstractTypeScriptFile implements ITypeScriptFile {

	private final ITypeScriptProject tsProject;
	private boolean dirty;
	protected final Object synchLock = new Object();
	private boolean opened;

	private final List<INavbarListener> listeners;
	private NavigationBarItemRoot navbar;
	private TypeScriptNavBarCollector navBarCollector;
	private FormatOptions formatOptions;
	private boolean configureAlreadyDone;

	public AbstractTypeScriptFile(ITypeScriptProject tsProject) {
		this.tsProject = tsProject;
		this.listeners = new ArrayList<INavbarListener>();
		this.navBarCollector = new TypeScriptNavBarCollector();
		this.setDirty(false);
		this.configureAlreadyDone = false;
	}

	@Override
	public ITypeScriptProject getProject() {
		return tsProject;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Location getLocation(int position) throws TypeScriptException {
		return new LocationReader(getContents(), position).getLineOffset();
	}

	@Override
	public int getPosition(int line, int offset) throws TypeScriptException {
		// TODO: implement that
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPosition(Location loc) throws TypeScriptException {
		return getPosition(loc.getLine(), loc.getOffset());
	}

	@Override
	public void open() throws TypeScriptException {
		((TypeScriptProject) tsProject).openFile(this);
		this.opened = true;
	}

	@Override
	public void close() throws TypeScriptException {
		((TypeScriptProject) tsProject).closeFile(this);
		this.opened = false;
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	void setOpened(boolean opened) {
		this.opened = opened;
	}

	@Override
	public void completions(int position, ITypeScriptCompletionCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		String prefix = null;
		client.completions(this.getName(), line, offset, prefix, collector);
	}

	@Override
	public void definition(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.definition(this.getName(), line, offset, collector);
	}

	@Override
	public void format(int startPosition, int endPosition, ITypeScriptFormatCollector collector)
			throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		this.ensureFormatOptions(client);
		Location start = this.getLocation(startPosition);
		Location end = this.getLocation(endPosition);
		client.format(this.getName(), start.getLine(), start.getOffset(), end.getLine(), end.getOffset(), collector);
	}

	private void ensureFormatOptions(ITypeScriptServiceClient client) throws TypeScriptException {
		FormatOptions oldFormatOptions = formatOptions;
		FormatOptions newFormatOptions = getFormatOptions();
		if (!configureAlreadyDone || !newFormatOptions.equals(oldFormatOptions)) {
			configureAlreadyDone = true;
			client.configure(new ConfigureRequestArguments(newFormatOptions, getName()));
		}
	}

	@Override
	public FormatOptions getFormatOptions() {
		formatOptions = tsProject.getProjectSettings().getFormatOptions();
		return formatOptions;
	}

	public void setFormatOptions(FormatOptions formatOptions) {
		this.formatOptions = formatOptions;
	}

	@Override
	public void references(int position, ITypeScriptReferencesCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.references(this.getName(), line, offset, collector);
	}

	@Override
	public void occurrences(int position, ITypeScriptOccurrencesCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.occurrences(this.getName(), line, offset, collector);
	}

	@Override
	public void navbar(ITypeScriptNavBarCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		if (tsProject.canSupport(CommandNames.NavTree)) {
			// when TypeScript 2.0.6 is consummed, use "navtree" to fill the
			// Outline
			// see
			// https://github.com/Microsoft/TypeScript/pull/11532#issuecomment-254804923
			client.navtree(this.getName(), this, collector);
		} else {
			client.navbar(this.getName(), this, collector);
		}
	}

	@Override
	public void implementation(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location location = this.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.implementation(this.getName(), line, offset, collector);
	}

	@Override
	public void getCodeFixes(int startPosition, int endPosition, String[] errorCodes,
			ITypeScriptGetCodeFixesCollector collector) throws TypeScriptException {
		this.synch();
		ITypeScriptServiceClient client = tsProject.getClient();
		Location startLocation = this.getLocation(startPosition);
		int startLine = startLocation.getLine();
		int startOffset = startLocation.getOffset();
		Location endLocation = this.getLocation(endPosition);
		int endLine = endLocation.getLine();
		int endOffset = endLocation.getOffset();
		client.getCodeFixes(this.getName(), this, startLine, startOffset, endLine, endOffset, errorCodes, collector);
	}

	private class TypeScriptNavBarCollector implements ITypeScriptNavBarCollector, ITypeScriptAsynchCollector {

		@Override
		public void setNavBar(NavigationBarItemRoot navbar) {
			AbstractTypeScriptFile.this.navbar = navbar;
			fireNavBarListeners(navbar);
		}

		@Override
		public void startCollect() {

		}

		@Override
		public void endCollect() {

		}

		@Override
		public void onError(TypeScriptException e) {

		}
	}

	@Override
	public void addNavbarListener(INavbarListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
		if (navbar != null) {
			listener.navBarChanged(navbar);
		} else {
			fireNavBarListenersIfNeeded();
		}
	}

	private void fireNavBarListeners(NavigationBarItemRoot navbar) {
		synchronized (listeners) {
			for (INavbarListener listener : listeners) {
				listener.navBarChanged(navbar);
			}
		}
	}

	protected void fireNavBarListenersIfNeeded() {
		if (!listeners.isEmpty()) {
			try {
				navbar(navBarCollector);
			} catch (TypeScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeNavbarListener(INavbarListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public NavigationBarItemRoot getNavBar() {
		return navbar;
	}

	// @Override
	// public List<NavigationBarItem> getNavBar() throws TypeScriptException {
	// TypeScriptNavBarCollector c = new TypeScriptNavBarCollector();
	// navbar(c);
	// return c.getNavBar();
	// }

	@Override
	public synchronized void synch() throws TypeScriptException {
		if (!isDirty()) {
			// no need to synchronize the file content with tsserver.
			return;
		}
		switch (tsProject.getProjectSettings().getSynchStrategy()) {
		case RELOAD:
			// reload strategy : store the content of the ts file in a temporary
			// file and call reload command.
			tsProject.getClient().updateFile(this.getName(), this.getContents());
			setDirty(false);
			break;
		case CHANGE:
			// change strategy: wait until "change" command is not finished.
			while (isDirty()) {
				try {
					synchronized (synchLock) {
						synchLock.wait(5);
					}
				} catch (InterruptedException e) {
					throw new TypeScriptException(e);
				}
			}
			break;
		}

	}
}
