/* ==================================================================
 * SerialFlowControl.java - 27/08/2024 3:45:29 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.io.modbus.serial;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Enumeration of serial flow control settings.
 *
 * @author matt
 * @version 1.0
 */
public enum SerialFlowControl {

	/** No flow control. */
	None(SerialFlowControl.NO_FLOW_CONTROL),

	/** No flow control. */
	RTS(SerialFlowControl.RTS_FLOW_CONTROL),

	/** No flow control. */
	CTS(SerialFlowControl.CTS_FLOW_CONTROL),

	/** No flow control. */
	DSR(SerialFlowControl.DSR_FLOW_CONTROL),

	/** No flow control. */
	DTR(SerialFlowControl.DTR_FLOW_CONTROL),

	/** No flow control. */
	XonXoffIn(SerialFlowControl.XONXOFF_IN_FLOW_CONTROL),

	/** No flow control. */
	XonXoffOut(SerialFlowControl.XONXOFF_OUT_FLOW_CONTROL),

	;

	/** Code value for no flow control. */
	public static final int NO_FLOW_CONTROL = 0;

	/** Code value for RTS flow control. */
	public static final int RTS_FLOW_CONTROL = 1;

	/** Code value for CTS flow control. */
	public static final int CTS_FLOW_CONTROL = 2;

	/** Code value for DSR flow control. */
	public static final int DSR_FLOW_CONTROL = 3;

	/** Code value for DTR flow control. */
	public static final int DTR_FLOW_CONTROL = 4;

	/** Code value for Xon Xoff in flow control. */
	public static final int XONXOFF_IN_FLOW_CONTROL = 5;

	/** Code value for Xon Xoff out flow control. */
	public static final int XONXOFF_OUT_FLOW_CONTROL = 6;

	/** Abbreviation value for no flow control. */
	public static final String NO_FLOW_CONTROL_ABBREVIATION = "no";

	/** Abbreviation value for RTS flow control. */
	public static final String RTS_FLOW_CONTROL_ABBREVIATION = "RTS";

	/** Abbreviation value for CTS flow control. */
	public static final String CTS_FLOW_CONTROL_ABBREVIATION = "CTS";

	/** Abbreviation value for DSR flow control. */
	public static final String DSR_FLOW_CONTROL_ABBREVIATION = "DSR";

	/** Abbreviation value for DTR flow control. */
	public static final String DTR_FLOW_CONTROL_ABBREVIATION = "DTR";

	/** Abbreviation value for Xon Xoff in flow control. */
	public static final String XONXOFF_IN_FLOW_CONTROL_ABBREVIATION = "Xon";

	/** Abbreviation value for Xon Xoff out flow control. */
	public static final String XONXOFF_OUT_FLOW_CONTROL_ABBREVIATION = "Xoff";

	private final int code;

	SerialFlowControl(int code) {
		this.code = code;
	}

	/**
	 * Get the code value.
	 *
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get an enum instance for a code value.
	 *
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static SerialFlowControl forCode(int code) {
		switch (code) {
			case NO_FLOW_CONTROL:
				return SerialFlowControl.None;

			case RTS_FLOW_CONTROL:
				return SerialFlowControl.RTS;

			case CTS_FLOW_CONTROL:
				return SerialFlowControl.CTS;

			case DSR_FLOW_CONTROL:
				return SerialFlowControl.DSR;

			case DTR_FLOW_CONTROL:
				return SerialFlowControl.DTR;

			case XONXOFF_IN_FLOW_CONTROL:
				return SerialFlowControl.XonXoffIn;

			case XONXOFF_OUT_FLOW_CONTROL:
				return SerialFlowControl.XonXoffOut;

			default:
				throw new IllegalArgumentException("Unknown serial flow control code [" + code + "]");
		}
	}

	/**
	 * Get an enumeration set for an abbreviation value.
	 *
	 * <p>
	 * Multiple abbreviations can be provided by using a {@code /} delimiter,
	 * for example {@code RTS/CTS} or {@code Xon/Xoff}.
	 * </p>
	 *
	 * @param value
	 *        the slash-delimited list of abbreviations to parse
	 * @return the enum set, never {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code value} is not empty but contains no valid abbreviation
	 */
	public static Set<SerialFlowControl> forAbbreviation(String value) {
		if ( value == null || value.isEmpty() ) {
			return EnumSet.of(None);
		}
		Set<SerialFlowControl> result = new HashSet<>(2);
		String[] components = value.split("\\s*/\\s*");
		for ( String cmp : components ) {
			if ( NO_FLOW_CONTROL_ABBREVIATION.equalsIgnoreCase(cmp) ) {
				result.add(None);
			} else if ( XONXOFF_IN_FLOW_CONTROL_ABBREVIATION.equalsIgnoreCase(cmp) ) {
				result.add(XonXoffIn);
			} else if ( XONXOFF_OUT_FLOW_CONTROL_ABBREVIATION.equalsIgnoreCase(cmp) ) {
				result.add(XonXoffOut);
			} else {
				// all others have abbreviation same as name
				for ( SerialFlowControl e : values() ) {
					if ( e.name().equalsIgnoreCase(cmp) ) {
						result.add(e);
						break;
					}
				}
			}
		}
		if ( result.isEmpty() ) {
			throw new IllegalArgumentException(
					"Unknown serial flow control abbreviation [" + value + "]");
		}
		EnumSet<SerialFlowControl> enumResult = EnumSet.copyOf(result);
		if ( enumResult.size() > 1 && enumResult.contains(None) ) {
			// remove None
			enumResult.remove(None);
		}
		return enumResult;
	}

}
