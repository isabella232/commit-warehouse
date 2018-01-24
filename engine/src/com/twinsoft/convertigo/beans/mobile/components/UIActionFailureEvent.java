/*
 * Copyright (c) 2001-2016 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.beans.mobile.components;

import java.util.Iterator;

public class UIActionFailureEvent extends UIComponent implements IEventGenerator{

	private static final long serialVersionUID = 5515679807142668317L;

	public UIActionFailureEvent() {
		super();
	}
	
	@Override
	public UIActionFailureEvent clone() throws CloneNotSupportedException {
		UIActionFailureEvent cloned = (UIActionFailureEvent) super.clone();
		return cloned;
	}

	@Override
	public String computeTemplate() {
		return "";
	}

	protected int numberOfActions() {
		int num = 0;
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIDynamicAction || component instanceof UICustomAction) {
				if (component.isEnabled()) {
					num++;
				}
			}
		}
		return num;
	}
	
	@Override
	public String computeEvent() {
		if (isEnabled()) {
			int num = numberOfActions();
			StringBuilder sb = new StringBuilder();
			Iterator<UIComponent> it = getUIComponentList().iterator();
			while (it.hasNext()) {
				UIComponent component = (UIComponent)it.next();
				if (component instanceof IAction) {
					String s = "";
					if (component instanceof UIDynamicAction) {
						s = ((UIDynamicAction)component).computeActionContent();
					}
					if (component instanceof UICustomAction) {
						s = ((UICustomAction)component).computeActionContent();
					}
					
					if (!s.isEmpty()) {
						sb.append(sb.length()>0 && num > 1 ? "\t\t,"+ System.lineSeparator() :"")
						.append(s);
					}
				}
			}
			
			String tsCode = "";
			if (sb.length() > 0) {
				if (num > 1) {
					tsCode += "\t\treturn Promise.all(["+ System.lineSeparator();
					tsCode += sb.toString();
					tsCode += "\t\t])"+ System.lineSeparator();
				} else {
					tsCode += "\t\treturn "+ sb.toString().replaceFirst("\t\t", "");
				}
			}
			
			return tsCode;
		}
		return "";
	}

	@Override
	public String toString() {
		return "onFailure";
	}
}
