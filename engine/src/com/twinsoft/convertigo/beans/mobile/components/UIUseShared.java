/*
 * Copyright (c) 2001-2019 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.beans.mobile.components;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;

import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EngineException;

public class UIUseShared extends UIElement {

	private static final long serialVersionUID = -6355983207888274045L;

	public UIUseShared() {
		super();
	}

	@Override
	public UIUseShared clone() throws CloneNotSupportedException {
		UIUseShared cloned = (UIUseShared) super.clone();
		cloned.target = null;
		return cloned;
	}
	
	private String sharedcomponent = "";
	
	public String getSharedComponentQName() {
		return sharedcomponent;
	}

	public void setSharedComponentQName(String sharedComponent) {
		this.sharedcomponent = sharedComponent;
	}

	@Override
	protected void addUIComponent(UIComponent uiComponent, Long after) throws EngineException {
		if (!(uiComponent instanceof UIControlVariable)) {
			throw new EngineException("You can only add Variable to this component");
		}
		
		super.addUIComponent(uiComponent, after);
	}
	
	@Override
	public List<UIComponent> getUIComponentList() {
		return super.getUIComponentList();
	}
	
	public UIControlVariable getVariable(String variableName) {
		Iterator<UIComponent> it = getUIComponentList().iterator();
		while (it.hasNext()) {
			UIComponent component = (UIComponent)it.next();
			if (component instanceof UIControlVariable) {
				UIControlVariable variable = (UIControlVariable)component;
				if (variable.getName().equals(variableName)) {
					return variable;
				}
			}
		}
		return null;
	}
	
	@Override
	public String computeTemplate() {
		String computed = "";
		if (isEnabled()) {
			UISharedComponent uisc = getTargetSharedComponent();
			if (uisc != null) {
				
				StringBuilder compVars = new StringBuilder();
				Iterator<UIComponent> it1 = uisc.getUIComponentList().iterator();
				while (it1.hasNext()) {
					UIComponent component = (UIComponent)it1.next();
					if (component instanceof UICompVariable) {
						UICompVariable uicv = (UICompVariable)component;
						if (uicv.isEnabled()) {
							String varValue = uicv.getVariableValue();
							if (!varValue.isEmpty()) {
								compVars.append(compVars.length() > 0 ? ", ":"");
								compVars.append(uicv.getVariableName()).append(": ");
								compVars.append(varValue);
							}
						}
					}
				}
				
				StringBuilder useVars = new StringBuilder();
				Iterator<UIComponent> it2 = getUIComponentList().iterator();
				while (it2.hasNext()) {
					UIComponent component = (UIComponent)it2.next();
					if (component instanceof UIControlVariable) {
						UIControlVariable uicv = (UIControlVariable)component;
						if (uicv.isEnabled()) {
							String varValue = uicv.getVarValue();
							if (!varValue.isEmpty()) {
								useVars.append(useVars.length() > 0 ? ", ":"");
								useVars.append(uicv.getVarName()).append(": ");
								useVars.append(varValue);
							}
						}
					}
				}
				
				String params = "merge({" + compVars.toString() + "},{" + useVars.toString() + "})";
				computed += "<ng-container *ngFor=\"let params"+ uisc.priority +" of ["+params+"]\" >" + System.getProperty("line.separator");
				computed += uisc.computeTemplate(this);
				computed += "</ng-container>" + System.getProperty("line.separator");
			}
		}
		return computed;
	}
	
	@Override
	public void computeScripts(JSONObject jsonScripts) {
		if (isEnabled()) {
			UISharedComponent uisc = getTargetSharedComponent();
			if (uisc != null) {
				uisc.computeScripts(this, jsonScripts);
			}
		}
	}

	@Override
	public String computeStyle() {
		UISharedComponent uisc = getTargetSharedComponent();
		if (uisc != null) {
			return uisc.computeStyle(this);
		}
		return "";
	}
	
	
	@Override
	public void addPageEvent(Set<UIComponent> done, List<UIPageEvent> eventList) {
		UISharedComponent uisc = getTargetSharedComponent();
		if (uisc != null) {
			if (!done.add(this)) {
				return;
			}
			uisc.addPageEvent(this, done, eventList);
		}
	}

	
	@Override
	public void addEventSubscriber(Set<UIComponent> done, List<UIEventSubscriber> eventList) {
		UISharedComponent uisc = getTargetSharedComponent();
		if (uisc != null) {
			if (!done.add(this)) {
				return;
			}
			uisc.addEventSubscriber(this, done, eventList);
		}
	}

	@Override
	protected void addContributors(Set<UIComponent> done, List<Contributor> contributors) {
		UISharedComponent uisc = getTargetSharedComponent();
		if (uisc != null) {
			if (!done.add(this)) {
				return;
			}
			uisc.addContributors(this, done, contributors);
		}
	}
	
	@Override
	protected void addInfos(Set<UIComponent> done, Map<String, Set<String>> infoMap) {
		UISharedComponent uisc = getTargetSharedComponent();
		if (uisc != null) {
			if (!done.add(this)) {
				return;
			}
			uisc.addInfos(this, done, infoMap);
		}
	}
	
	transient private UISharedComponent target = null;
	
	public UISharedComponent getTargetSharedComponent() {
		String qname =  getSharedComponentQName();
		if (target == null || !target.getQName().equals(qname)) {
			target = null;
			if (qname.indexOf('.') != -1) {
				String p_name = qname.substring(0, qname.indexOf('.'));
				Project project = this.getProject();
				if (project != null) {
					Project p = null;
					try {
						p = p_name.equals(project.getName()) ? project: Engine.theApp.databaseObjectsManager.getOriginalProjectByName(p_name);
					} catch (EngineException e) {
						Engine.logBeans.warn("(UIUseShared) For \""+  this.toString() +"\", targeted project \""+ p_name +"\" is missing !");
					}
					if (p != null) {
						for (UISharedComponent uisc: p.getMobileApplication().getApplicationComponent().getSharedComponentList()) {
							if (uisc.getQName().equals(qname)) {
								target = uisc;
							}
						}
						if (target == null) {
							Engine.logBeans.warn("(UIUseShared) For \""+  this.toString() +"\", targeted component \""+ qname +"\" is missing !");
						}
					}
				}
			} else {
				Engine.logBeans.warn("(UIUseShared) Component \""+ this.toString() +"\" has no target shared component defined !");
			}
		}
		return target;
	}
	
	@Override
	public String toString() {
		String compName = this.sharedcomponent.isEmpty() ? "?" : this.sharedcomponent.substring(this.sharedcomponent.lastIndexOf('.') + 1);
		return "use " + compName;
	}
}
