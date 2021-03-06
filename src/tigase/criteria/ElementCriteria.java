/*  Tigase Project
 *  Copyright (C) 2004-2012 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev: 644 $
 * Last modified by $Author: wojtek $
 * $Date: 2012-08-21 06:20:24 +0800 (Tue, 21 Aug 2012) $
 */
package tigase.criteria;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import tigase.xml.Element;

/**
 * 
 * <p>
 * Created: 2007-06-19 20:34:57
 * </p>
 * 
 * @author bmalkow
 * @version $Rev: 644 $
 */
public class ElementCriteria implements Criteria {

	public static final ElementCriteria empty() {
		return new ElementCriteria(null, null, null, null);
	}

	public static final ElementCriteria name(String name) {
		return new ElementCriteria(name, null, null, null);
	}

	public static final ElementCriteria name(String name, String xmlns) {
		return new ElementCriteria(name, null, new String[] { "xmlns" }, new String[] { xmlns });
	}

	public static final ElementCriteria name(String name, String cdata, String[] attNames, String[] attValues) {
		return new ElementCriteria(name, cdata, attNames, attValues);
	}

	public static final ElementCriteria name(String name, String[] attNames, String[] attValues) {
		return new ElementCriteria(name, null, attNames, attValues);
	}

	public static final ElementCriteria nameType(String name, String type) {
		return new ElementCriteria(name, null, new String[] { "type" }, new String[] { type });
	}

	public static final ElementCriteria xmlns(String xmlns) {
		return new ElementCriteria(null, null, new String[] { "xmlns" }, new String[] { xmlns });
	}

	private Map<String, String> attrs = new TreeMap<String, String>();

	private String cdata = null;

	private String name;

	private Criteria nextCriteria;

	public ElementCriteria(String name, String cdata, String[] attname, String[] attValue) {
		this.cdata = cdata;
		this.name = name;
		if (attname != null && attValue != null) {
			for (int i = 0; i < attname.length; i++) {
				attrs.put(attname[i], attValue[i]);
			}
		}
	}

	public Criteria add(Criteria criteria) {
		if (this.nextCriteria == null) {
			this.nextCriteria = criteria;
		} else {
			Criteria c = this.nextCriteria;
			c.add(criteria);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tigase.criteria.Criteria#match(tigase.xml.Element)
	 */
	public boolean match(Element element) {
		if (name != null && name != element.getName()) {
			return false;
		}
		if (cdata != null &&
			(element.getCData() == null || !cdata.equals(element.getCData()))) {
			return false;
		}
		boolean result = true;
		for (Entry<String, String> entry : this.attrs.entrySet()) {
			String x = element.getAttribute(entry.getKey());
			if (x == null || !x.equals(entry.getValue())) {
				result = false;
				break;
			}
		}

		if (this.nextCriteria != null) {
			List<Element> children = element.getChildren();
			boolean subres = false;
			if (children != null) {
				for (Element sub : children) {
					if (this.nextCriteria.match(sub)) {
						subres = true;
						break;
					}
				}
			}
			result &= subres;
		}

		return result;
	}

}
