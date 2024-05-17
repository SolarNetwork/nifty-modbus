/* ==================================================================
 * JscSerialPort.java - 2/12/2022 10:57:46 am
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

package net.solarnetwork.io.modbus.rtu.jsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * jSerialComm implementation of
 * {@link net.solarnetwork.io.modbus.serial.SerialPort}.
 *
 * @author matt
 * @version 1.0
 */
public class JscSerialPort implements net.solarnetwork.io.modbus.serial.SerialPort {

	private static final Logger log = LoggerFactory.getLogger(JscSerialPort.class);

	private final String name;
	private SerialPort serialPort;

	/**
	 * Constructor.
	 *
	 * @param name
	 *        the device name of the serial port to use, such as
	 *        {@literal /dev/ttyUSB0} or {@literal COM1}
	 */
	public JscSerialPort(String name) {
		super();
		if ( name == null || name.isEmpty() ) {
			throw new IllegalArgumentException("The serialPort argument must be provided.");
		}
		this.name = name;
	}

	@Override
	public String getName() {
		return serialPort.getSystemPortName();
	}

	@Override
	public synchronized void open(SerialParameters parameters) throws IOException {
		if ( serialPort != null ) {
			return; // throw exception?
		}
		try {
			serialPort = SerialPort.getCommPort(name);
			setupSerialPortParameters(serialPort, parameters);
			if ( !serialPort.openPort() ) {
				throw new IOException("Serial port [" + name + "] failed to open");
			}
		} catch ( SerialPortInvalidPortException e ) {
			try {
				SerialPort[] ports = SerialPort.getCommPorts();
				if ( ports != null ) {
					log.warn("Invalid serial port [{}]; known ports are: [{}]", name,
							Arrays.stream(ports).map(p -> p.getSystemPortName())
									.collect(Collectors.joining(",\n\t", "\n\t", "\n")));
				}
			} catch ( Exception e2 ) {
				log.warn("Invalid serial port [{}]; failed to get list of available ports: {}", name,
						e2.toString());
			}
			throw new IOException("Invalid serial port [" + name + "]");
		} catch ( RuntimeException e ) {
			try {
				close();
			} catch ( Exception e2 ) {
				// ignore this
			}
			throw new IOException("Error opening serial port [" + name + "]:" + e.toString(), e);
		}
	}

	private void setupSerialPortParameters(SerialPort serialPort, SerialParameters serialParams) {
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
				stopBitsCode = SerialPort.ONE_POINT_FIVE_STOP_BITS;
				break;

			case Two:
				stopBitsCode = SerialPort.TWO_STOP_BITS;
				break;

			default:
				stopBitsCode = SerialPort.ONE_STOP_BIT;
				break;
		}

		int parityCode;
		switch (parity) {
			case Even:
				parityCode = SerialPort.EVEN_PARITY;
				break;

			case Odd:
				parityCode = SerialPort.ODD_PARITY;
				break;

			case Mark:
				parityCode = SerialPort.MARK_PARITY;
				break;

			case Space:
				parityCode = SerialPort.SPACE_PARITY;
				break;

			default:
				parityCode = SerialPort.NO_PARITY;
				break;
		}

		serialPort.setComPortParameters(serialParams.getBaudRate(), serialParams.getDataBits(),
				stopBitsCode, parityCode);

		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
				serialParams.getReadTimeout(), 0);
	}

	@Override
	public synchronized void close() throws IOException {
		if ( serialPort == null ) {
			return;
		}
		try {
			log.debug("Closing serial port [{}]", name);
			serialPort.closePort();
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
			return serialPort.getInputStreamWithSuppressedTimeoutExceptions();
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
