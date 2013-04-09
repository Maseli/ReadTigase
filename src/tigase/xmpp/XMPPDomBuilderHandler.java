/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2012 "Artur Hefczyc" <artur.hefczyc@tigase.org>
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
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.xmpp;

//~--- non-JDK imports --------------------------------------------------------

import tigase.xml.DefaultElementFactory;
import tigase.xml.Element;
import tigase.xml.ElementFactory;
import tigase.xml.SimpleHandler;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 * <code>XMPPDomBuilderHandler</code> - implementation of
 *  <code>SimpleHandler</code> building <em>DOM</em> strctures during parsing
 *  time.
 *  It also supports creation multiple, sperate document trees if parsed
 *  buffer contains a few <em>XML</em> documents. As a result of work it returns
 *  always <code>Queue</code> containing all found <em>XML</em> trees in the
 *  same order as they were found in network data.<br/>
 *  Document trees created by this <em>DOM</em> builder consist of instances of
 *  <code>Element</code> class or instances of class extending
 *  <code>Element</code> class. To receive trees built with instances of proper
 *  class user must provide <code>ElementFactory</code> implementation creating
 *  instances of required <code>ELement</code> extension.
 *
 * <p>
 * Created: Sat Oct  2 22:01:34 2004
 * </p>
 * @param <RefObject>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class XMPPDomBuilderHandler<RefObject> implements SimpleHandler {
	private static Logger log = Logger.getLogger(XMPPDomBuilderHandler.class.getName());

	public static int ELEMENTS_NUMBER_LIMIT = 1000;
	public static final String ELEMENTS_NUMBER_LIMIT_PROP_KEY =
		"tigase.xmpp.elements_number_limit";

	private static final String ELEM_STREAM_STREAM = "stream:stream";
	private static ElementFactory defaultFactory = new DefaultElementFactory();

	//~--- fields ---------------------------------------------------------------

	private ElementFactory customFactory = null;
	private Object parserState = null;
	private XMPPIOService<RefObject> service = null;
	private String top_xmlns = null;
	private Map<String, String> namespaces = new TreeMap<String, String>();
	private boolean error = false;
	private ArrayDeque<Element> el_stack = new ArrayDeque<Element>(10);
	private ArrayDeque<Element> all_roots = new ArrayDeque<Element>(1);

	/**
	 * Protection from the system overload and DOS attack. We want to limit number
	 * of elements created within a single XMPP stanza.
	 *
	 */
	private int elements_number_limit = 0;

	//~--- constructors ---------------------------------------------------------

	/**
	 * Constructs ...
	 *
	 *
	 * @param ioserv
	 */
	public XMPPDomBuilderHandler(XMPPIOService<RefObject> ioserv) {
		customFactory = defaultFactory;
		service = ioserv;
		ELEMENTS_NUMBER_LIMIT =
			Integer.getInteger(ELEMENTS_NUMBER_LIMIT_PROP_KEY, ELEMENTS_NUMBER_LIMIT);
	}

	/**
	 * Constructs ...
	 *
	 *
	 * @param ioserv
	 * @param factory
	 */
	public XMPPDomBuilderHandler(XMPPIOService<RefObject> ioserv, ElementFactory factory) {
		customFactory = factory;
		service = ioserv;
		ELEMENTS_NUMBER_LIMIT =
			Integer.getInteger(ELEMENTS_NUMBER_LIMIT_PROP_KEY, ELEMENTS_NUMBER_LIMIT);
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param cdata
	 */
	@Override
	public void elementCData(StringBuilder cdata) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Element CDATA: " + cdata);
		}

		Element elem = el_stack.peek();
		if (elem != null) {
			elem.setCData(cdata.toString());
		}
	}

	/**
	 * Method description
	 *
	 *
	 * @param name
	 */
	@Override
	public void endElement(StringBuilder name) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("End element name: " + name);
		}

		String tmp_name = name.toString();

		if (tmp_name.equals(ELEM_STREAM_STREAM)) {
			service.xmppStreamClosed();

			return;
		}    // end of if (tmp_name.equals(ELEM_STREAM_STREAM))

		if (el_stack.isEmpty()) {
			el_stack.push(newElement(tmp_name, null, null, null));
		}    // end of if (tmp_name.equals())

		Element elem = el_stack.pop();

		if (el_stack.isEmpty()) {
			elements_number_limit = 0;
			all_roots.offer(elem);

			if (log.isLoggable(Level.FINEST)) {
				log.finest("Adding new request: " + elem.toString());
			}
		} else {
			el_stack.peek().addChild(elem);
		}    // end of if (el_stack.isEmpty()) else
	}

	/**
	 * Method description
	 *
	 *
	 * @param errorMessage
	 */
	@Override
	public void error(String errorMessage) {
		log.warning("XML content parse error.");

		if (log.isLoggable(Level.FINE)) {
			log.fine(errorMessage);
		}

		error = true;
	}

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public Queue<Element> getParsedElements() {
		return all_roots;
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param other
	 */
	@Override
	public void otherXML(StringBuilder other) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Other XML content: " + other);
		}

		// Just ignore
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public boolean parseError() {
		return error;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public Object restoreParserState() {
		return parserState;
	}

	/**
	 * Method description
	 *
	 *
	 * @param state
	 */
	@Override
	public void saveParserState(Object state) {
		parserState = state;
	}

	/**
	 * Method description
	 *
	 *
	 * @param name
	 * @param attr_names
	 * @param attr_values
	 */
	@Override
	public void startElement(StringBuilder name, StringBuilder[] attr_names,
			StringBuilder[] attr_values) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Start element name: " + name);
			log.finest("Element attributes names: " + Arrays.toString(attr_names));
			log.finest("Element attributes values: " + Arrays.toString(attr_values));
		}

		// Look for 'xmlns:' declarations:
		if (attr_names != null) {
			for (int i = 0; i < attr_names.length; ++i) {

				// Exit the loop as soon as we reach end of attributes set
				if (attr_names[i] == null) {
					break;
				}

				if (attr_names[i].toString().startsWith("xmlns:")) {

					// TODO should use a StringCache instead of intern() to avoid potential
					// DOS by exhausting permgen
					namespaces.put(attr_names[i].substring("xmlns:".length(),
							attr_names[i].length()).intern(), attr_values[i].toString());

					if (log.isLoggable(Level.FINEST)) {
						log.finest("Namespace found: " + attr_values[i].toString());
					}
				}    // end of if (att_name.startsWith("xmlns:"))
			}      // end of for (String att_name : attnames)
		}        // end of if (attr_names != null)

		String tmp_name = name.toString();

		if (tmp_name.equals(ELEM_STREAM_STREAM)) {
			Map<String, String> attribs = new HashMap<String, String>();

			if (attr_names != null) {
				for (int i = 0; i < attr_names.length; i++) {
					if ((attr_names[i] != null) && (attr_values[i] != null)) {
						attribs.put(attr_names[i].toString(), attr_values[i].toString());
					} else {
						break;
					}    // end of else
				}      // end of for (int i = 0; i < attr_names.length; i++)
			}        // end of if (attr_name != null)

			service.xmppStreamOpened(attribs);

			return;
		}          // end of if (tmp_name.equals(ELEM_STREAM_STREAM))

		String new_xmlns = null;
		String prefix = null;
		String tmp_name_prefix = null;
		int idx = tmp_name.indexOf(':');

		if (idx > 0) {
			tmp_name_prefix = tmp_name.substring(0, idx);

			if (log.isLoggable(Level.FINEST)) {
				log.finest("Found prefixed element name, prefix: " + tmp_name_prefix);
			}
		}

		if (tmp_name_prefix != null) {
			for (String pref : namespaces.keySet()) {
				if (tmp_name_prefix.equals(pref)) {
					new_xmlns = namespaces.get(pref);
					tmp_name = tmp_name.substring(pref.length() + 1, tmp_name.length());
					prefix = pref;

					if (log.isLoggable(Level.FINEST)) {
						log.finest("new_xmlns = " + new_xmlns);
					}
				}    // end of if (tmp_name.startsWith(xmlns))
			}      // end of for (String xmlns: namespaces.keys())
		}

		Element elem = newElement(tmp_name, null, attr_names, attr_values);
		String ns = elem.getXMLNS();

		if (ns == null) {
			if (el_stack.isEmpty() || (el_stack.peek().getXMLNS() == null)) {

				// elem.setDefXMLNS(top_xmlns);
			} else {
				elem.setDefXMLNS(el_stack.peek().getXMLNS());

				if (log.isLoggable(Level.FINEST)) {
					log.finest("DefXMLNS assigned: " + elem.toString());
				}
			}
		}

		if (new_xmlns != null) {
			elem.setXMLNS(new_xmlns);
			elem.removeAttribute("xmlns:" + prefix);

			if (log.isLoggable(Level.FINEST)) {
				log.finest("new_xmlns assigned: " + elem.toString());
			}
		}

		el_stack.push(elem);
	}

	private Element newElement(String name, String cdata, StringBuilder[] attnames,
			StringBuilder[] attvals) {
		++elements_number_limit;
		if (elements_number_limit > ELEMENTS_NUMBER_LIMIT) {
			throw new XMPPParserException("Too many elements for staza, possible DOS attack.");
		}
		return customFactory.elementInstance(name, cdata, attnames, attvals);
	}
}    // XMPPDomBuilderHandler


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com