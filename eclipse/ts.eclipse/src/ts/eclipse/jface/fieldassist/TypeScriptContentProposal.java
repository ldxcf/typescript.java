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
package ts.eclipse.jface.fieldassist;

import org.eclipse.jface.fieldassist.IContentProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.completions.ICompletionEntryMatcher;

/**
 * {@link IContentProposal} implementation with TypeScript completion entry.
 */
public class TypeScriptContentProposal extends CompletionEntry implements IContentProposal {

	private final String content;
	private final String description;

	public TypeScriptContentProposal(String name, String kind, String kindModifiers, String sortText, String prefix,
			String fileName, int line, int offset, ICompletionEntryMatcher matcher, ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, fileName, line, offset, matcher, client);
		this.content = name.substring(prefix.length(), name.length());
		this.description = null;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public int getCursorPosition() {
		return content.length();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getLabel() {
		return getName();
	}

}
