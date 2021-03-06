/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2012 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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

package tigase.util;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Pattern;

//~--- classes ----------------------------------------------------------------

/**
 * Created: Feb 4, 2010 9:31:23 AM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 644 $
 */
public class XMPPStringPrepSimple implements XMPPStringPrepIfc {
	private static final Pattern p = Pattern.compile("[ @&()\\[\\]\t\n\r\f\\a\\e]");

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param domain
	 *
	 * @return
	 *
	 * @throws TigaseStringprepException
	 */
	@Override
	public String nameprep(String domain) throws TigaseStringprepException {
		String result = domain.trim().toLowerCase();

		if ( !checkString(result)) {
			throw new TigaseStringprepException("Illegal characters in string.");
		}

		return result;
	}

	/**
	 * Method description
	 *
	 *
	 * @param localpart
	 *
	 * @return
	 *
	 * @throws TigaseStringprepException
	 */
	@Override
	public String nodeprep(String localpart) throws TigaseStringprepException {
		String result = localpart.trim();

		if ( !checkString(result)) {
			throw new TigaseStringprepException("Illegal characters in string.");
		}

		return result;
	}

	/**
	 * Method description
	 *
	 *
	 * @param resource
	 *
	 * @return
	 *
	 * @throws TigaseStringprepException
	 */
	@Override
	public String resourceprep(String resource) throws TigaseStringprepException {
		return resource.trim();
	}

	private boolean checkString(String input) {
		//return !p.matcher(input).matches();
		return !p.matcher(input).find();
		// return !input.matches("[ @&()\\[\\]\t\n\r\f\\a\\e]");
	}
}


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
