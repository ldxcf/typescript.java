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
package ts.internal.client.protocol;

/**
 * Arguments for format messages.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class FormatRequestArgs extends FileLocationRequestArgs {

	public FormatRequestArgs(String fileName, int line, int offset, int endLine, int endOffset) {
		super(fileName, line, offset);
		super.add("endLine", endLine);
		super.add("endOffset", endOffset);
	}

}
