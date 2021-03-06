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
package ts.client.navbar;

import java.util.List;

/**
 * Navigation bar item.
 *
 */
public class NavigationBarItem implements INavigationBarItem {

	private String text;
	private String kind;
	private String kindModifiers;
	private List<TextSpan> spans;
	private List<NavigationBarItem> childItems;
	private boolean parentAlreadyUpdated;
	NavigationBarItem parent;

	public String getText() {
		return text;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getKindModifiers() {
		return kindModifiers;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setSpans(List<TextSpan> spans) {
		this.spans = spans;
		this.parentAlreadyUpdated = false;
	}

	public List<TextSpan> getSpans() {
		updateParentIfNeeded();
		return spans;
	}

	public boolean hasSpans() {
		return spans != null && spans.size() > 0;
	}

	public List<NavigationBarItem> getChildItems() {
		updateParentIfNeeded();
		return childItems;
	}

	private void updateParentIfNeeded() {
		if (!parentAlreadyUpdated) {
			if (childItems != null) {
				for (NavigationBarItem item : childItems) {
					item.parent = this;
				}
			}
			if (spans != null) {
				for (TextSpan span : spans) {
					span.parent = this;
				}
			}
			parentAlreadyUpdated = true;
		}
	}

	public void setChildItems(List<NavigationBarItem> childItems) {
		this.childItems = childItems;
		this.parentAlreadyUpdated = false;
	}

	public boolean hasChildItems() {
		return childItems != null && childItems.size() > 0;
	}

	public NavigationBarItem getParent() {
		return parent;
	}
}
