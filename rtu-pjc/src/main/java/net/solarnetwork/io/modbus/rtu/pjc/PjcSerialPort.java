/* ==================================================================
 * PjcSerialPort.java - 20/12/2023 10:57:46 am
 *
 * Copyright 2023 SolarNetwork.net Dev Team
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

package net.solarnetwork.io.modbus.rtu.pjc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * PureJavaComm implementation of
 * {@link net.solarnetwork.io.modbus.serial.SerialPort}.
 *
 * @author matt
 * @version 1.0
 */
public class PjcSerialPort implements net.solarnetwork.io.modbus.serial.SerialPort {

	private static final Logger log = LoggerFactory.getLogger(PjcSerialPort.class);

	private final String name;
	private SerialPort serialPort;

	/**
	 * Constructor.
	 *
	 * @param name
	 *        the device name of the serial port to use, such as
	 *        {@literal /dev/ttyUSB0} or {@literal COM1}
	 */
	public PjcSerialPort(String name) {
		super();
		if ( name == null || name.isEmpty() ) {
			throw new IllegalArgumentException("The serialPort argument must be provided.");
		}
		this.name = name;
	}

	@Override
	public String getName() {
		return (serialPort != null ? serialPort.getName() : name);
	}

	@Override
	public synchronized void open(SerialParameters parameters) throws IOException {
		if ( serialPort != null ) {
			return; // throw exception?
		}

		CommPortIdentifier portId = getCommPortIdentifier(name);
		try {
			serialPort = (SerialPort) portId.open(name, 2000);
			setupSerialPortParameters(serialPort, parameters);
		} catch ( PortInUseException e ) {
			throw new IOException("Error opening serial port [" + name + "]: in use", e);
		} catch ( UnsupportedCommOperationException e ) {
			throw new IOException("Error opening serial port [" + name + "]: unsupported configuration: "
					+ e.getMessage(), e);
		} catch ( TooManyListenersException e ) {
			try {
				close();
			} catch ( Exception e2 ) {
				// ignore this
			}
			throw new IOException("Error opening serial port [" + name + "]: too many listeners", e);
		}
	}

	private CommPortIdentifier getCommPortIdentifier(final String portId) throws IOException {
		// first try directly
		CommPortIdentifier commPortId = null;
		try {
			commPortId = CommPortIdentifier.getPortIdentifier(portId);
			if ( commPortId != null ) {
				log.debug("Found port identifier: {}", portId);
				return commPortId;
			}
		} catch ( NoSuchPortException e ) {
			log.debug("Port {} not found, inspecting available ports...", portId);
		}
		Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		List<String> foundNames = new ArrayList<String>(5);
		while ( portIdentifiers.hasMoreElements() ) {
			CommPortIdentifier commPort = portIdentifiers.nextElement();
			log.trace("Inspecting available port identifier: {}", commPort.getName());
			foundNames.add(commPort.getName());
			if ( commPort.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& portId.equals(commPort.getName()) ) {
				commPortId = commPort;
				log.debug("Found port identifier: {}", portId);
				break;
			}
		}

		if ( commPortId == null ) {
			log.warn("Invalid serial port [{}]; known ports are: [{}]", name,
					foundNames.stream().collect(Collectors.joining(",\n\t", "\n\t", "\n")));
			throw new IOException("Invalid serial port [" + portId + "]");
		}
		return commPortId;
	}

	private void setupSerialPortParameters(SerialPort serialPort, SerialParameters serialParams)
			throws TooManyListenersException, UnsupportedCommOperationException {
		final SerialStopBits stopBits = (serialParams.getStopBits() != null ? serialParams.getStopBits()
				: SerialParameters.DEFAULT_STOP_BITS);
		final SerialParity parity = (serialParams.getParity() != null ? serialParams.getParity()
				: SerialParameters.DEFAULT_PARITY);
		if ( log.isDebugEnabled() ) {
			log.debug("Setting serial port [{}] baud = {}, dataBits = {}, stopBits = {}, parity = {}",
					new Object[] { name, serialParams.getBaudRate(), serialParams.getDataBits(),
							stopBits, parity });
		}

		int stopBitsCode;
		switch (stopBits) {
			case OnePointFive:
				stopBitsCode = SerialPort.STOPBITS_1_5;
				break;

			case Two:
				stopBitsCode = SerialPort.STOPBITS_2;
				break;

			default:
				stopBitsCode = SerialPort.STOPBITS_1;
				break;
		}

		int parityCode;
		switch (parity) {
			case Even:
				parityCode = SerialPort.PARITY_EVEN;
				break;

			case Odd:
				parityCode = SerialPort.PARITY_ODD;
				break;

			case Mark:
				parityCode = SerialPort.PARITY_MARK;
				break;

			case Space:
				parityCode = SerialPort.PARITY_SPACE;
				break;

			default:
				parityCode = SerialPort.PARITY_NONE;
				break;
		}

		serialPort.setSerialPortParams(serialParams.getBaudRate(), serialParams.getDataBits(),
				stopBitsCode, parityCode);

		if ( serialParams.getReadTimeout() >= 0 ) {
			serialPort.enableReceiveTimeout(serialParams.getReadTimeout());
			if ( !serialPort.isReceiveTimeoutEnabled() ) {
				log.warn("Receive timeout configured as {} but not supported by driver.",
						serialParams.getReadTimeout());
			} else if ( log.isDebugEnabled() ) {
				log.debug("Receive timeout set to {}", serialParams.getReadTimeout());
			}
		} else {
			serialPort.disableReceiveTimeout();
		}

		serialPort.disableReceiveThreshold();
	}

	@Override
	public synchronized void close() throws IOException {
		if ( serialPort == null ) {
			return;
		}
		try {
			log.debug("Closing serial port [{}]", name);
			serialPort.close();
			log.trace("Serial port [{}] closed", name);
		} finally {
			serialPort = null;
		}
	}

	@Override
	public synchronized boolean isOpen() {
		return serialPort != null;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		try {
			if ( !isOpen() ) {
				throw new IOException("Serial port [" + name + "] is not open.");
			}
			return serialPort.getInputStream();
		} catch ( IOException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new IOException("Error opening serial port [" + name + "] input stream: " + e, e);
		}
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException {
		try {
			if ( !isOpen() ) {
				throw new IOException("Serial port [" + name + "] is not open.");
			}
			return serialPort.getOutputStream();
		} catch ( IOException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new IOException("Error opening serial port [" + name + "] output stream: " + e, e);
		}
	}

}
