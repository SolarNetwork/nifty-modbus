/* ==================================================================
 * SerialParity.java - 2/12/2022 9:14:33 am
 *
 * Copyright 2022 SolarNetwork.net Dev Team
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

/**
 * Enumeration of serial parity settings.
 * 
 * @author matt
 * @version 1.0
 */
public enum SerialParity {

	/** No parity bit will be sent with each data character at all. */
	None(SerialParity.NO_PARITY),

	/**
	 * An odd parity bit will be sent with each data character, i.e. will be set
	 * to 1 if the data character contains an even number of bits set to 1.
	 */
	Odd(SerialParity.ODD_PARITY),

	/**
	 * An even parity bit will be sent with each data character, i.e. will be
	 * set to 1 if the data character contains an odd number of bits set to 1.
	 */
	Even(SerialParity.EVEN_PARITY),

	/** A 1 parity bit will be sent with each data character. */
	Mark(SerialParity.MARK_PARITY),

	/** A 0 parity bit will be sent with each data character. */
	Space(SerialParity.SPACE_PARITY),

	;

	/** Code value for no parity. */
	public static final int NO_PARITY = 0;

	/** Code value for odd parity. */
	public static final int ODD_PARITY = 1;

	/** Code value for even parity. */
	public static final int EVEN_PARITY = 2;

	/** Code value for mark parity. */
	public static final int MARK_PARITY = 3;

	/** Code value for space parity. */
	public static final int SPACE_PARITY = 4;

	private final int code;

	private SerialParity(int code) {
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
	 * Get an abbreviation for this parity.
	 * 
	 * @return an abbreviation, one of N, O, E, M, and S for None, Odd, Even,
	 *         Mark, and Space
	 */
	public String getAbbreviation() {
		switch (this) {
			case Odd:
				return "O";

			case Even:
				return "E";

			case Mark:
				return "M";

			case Space:
				return "S";

			default:
				return "N";
		}
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
	public static SerialParity forCode(int code) {
		switch (code) {
			case NO_PARITY:
				return SerialParity.None;

			case ODD_PARITY:
				return SerialParity.Odd;

			case EVEN_PARITY:
				return SerialParity.Even;

			case MARK_PARITY:
				return SerialParity.Mark;

			case SPACE_PARITY:
				return SerialParity.Space;

			default:
				throw new IllegalArgumentException("Unknown serial parity code [" + code + "]");
		}
	}

	/**
	 * Get an enum instance for an abbreviation value.
	 * 
	 * <p>
	 * The supported abbreviations are N, O, E, M, and S for None, Odd, Even,
	 * Mark, and Space. Lower-case versions of these are supported.
	 * </p>
	 * 
	 * @param code
	 *        the code
	 * @return the enum
	 * @throws IllegalArgumentException
	 *         if {@code code} is not a valid value
	 */
	public static SerialParity forAbbreviation(String abbreviation) {
		if ( abbreviation == null ) {
			return null;
		}
		switch (abbreviation) {
			case "n":
			case "N":
				return SerialParity.None;

			case "o":
			case "O":
				return SerialParity.Odd;

			case "e":
			case "E":
				return SerialParity.Even;

			case "m":
			case "M":
				return SerialParity.Mark;

			case "s":
			case "S":
				return SerialParity.Space;

			default:
				throw new IllegalArgumentException(
						"Unknown serial parity abbreviation [" + abbreviation + "]");
		}
	}

}
