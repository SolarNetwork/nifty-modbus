/* ==================================================================
 * JscModbusShell.java - 2/12/2022 12:23:23 pm
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.solarnetwork.io.modbus.ModbusBlockType;
import net.solarnetwork.io.modbus.ModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.BitsModbusMessage;
import net.solarnetwork.io.modbus.netty.msg.RegistersModbusMessage;
import net.solarnetwork.io.modbus.rtu.netty.NettyRtuModbusClientConfig;
import net.solarnetwork.io.modbus.rtu.netty.RtuNettyModbusClient;
import net.solarnetwork.io.modbus.serial.BasicSerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParameters;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Basic Modbus shell using jSerialComm.
 * 
 * @author matt
 * @version 1.0
 */
public class JscModbusShell {

	private final String deviceName;
	private final SerialParameters serialParameters;
	private final BufferedReader in;
	private final PrintWriter out;
	private RtuNettyModbusClient client;

	private boolean wireLogging = true;

	/**
	 * Constructor.
	 * 
	 * @param deviceName
	 *        the serial device name to use
	 * @param serialParameters
	 *        the serial parameters to use
	 * @param in
	 *        the input stream for shell input
	 * @param out
	 *        the output stream for shell output
	 */
	public JscModbusShell(String deviceName, SerialParameters serialParameters, BufferedReader in,
			PrintWriter out) {
		super();
		this.deviceName = deviceName;
		this.serialParameters = serialParameters;
		NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig(deviceName, serialParameters);
		client = new RtuNettyModbusClient(config, new JscSerialPortProvider());
		this.in = in;
		this.out = out;
	}

	private static final Pattern SHELL_LINE_SPLIT_REGEX = Pattern.compile("\\s+");

	/**
	 * Start the shell.
	 */
	public void start() {
		client.setWireLogging(wireLogging);
		try {
			client.start().get();
			out.printf("Connected to %s %s\n", deviceName, serialParameters.bitsShortcut());
			while ( true ) {
				out.print("> ");
				out.flush();
				String line = in.readLine();
				if ( line == null ) {
					return;
				}
				line = line.trim();
				if ( line.isEmpty() ) {
					continue;
				}
				String[] args = SHELL_LINE_SPLIT_REGEX.split(line);
				String cmd = args[0].toLowerCase();
				switch (cmd) {
					case "e":
					case "q":
					case "exit":
					case "quit":
						return;

					case "r":
					case "read":
						read(args);
						break;

					default:
						out.println(String.format("Unknown command [%s]", cmd));
						break;
				}
			}
		} catch ( ExecutionException | InterruptedException e ) {
			out.println("Error opening serial port: " + e.getCause().toString());
		} catch ( IOException e ) {
			out.println("Communication error: " + e.toString());
		} finally {
			client.stop();
		}
	}

	private void read(String... args) {
		if ( args.length < 2 ) {
			out.println("Must provide register type to read from (coil, discrete, input, holding).");
			return;
		}
		final String type = args[1].toLowerCase();
		ModbusBlockType blockType = null;
		switch (type) {
			case "c":
			case "coil":
			case "coils":
				blockType = ModbusBlockType.Coil;
				break;

			case "d":
			case "discrete":
			case "discretes":
				blockType = ModbusBlockType.Discrete;
				break;

			case "i":
			case "input":
			case "inputs":
				blockType = ModbusBlockType.Input;
				break;

			case "h":
			case "holding":
			case "holdings":
				blockType = ModbusBlockType.Holding;
				break;

			default:
				out.printf(
						"Unsupported block type [%s]; must be one of coil, discrete, input, or holding.\n",
						type);
				return;
		}
		boolean oneBased = false;
		int addrBase = 10;
		int unitId = 0;
		int addr = 0;
		int count = 1;
		for ( int i = 2, len = args.length; i < len; i++ ) {
			try {
				switch (args[i]) {
					case "-1":
						oneBased = true;
						break;

					case "-a":
					case "--unit":
						unitId = Integer.parseInt(args[++i]);
						break;

					case "-r":
					case "--register":
						addr = Integer.parseInt(args[++i]);
						break;

					case "-r:hex":
					case "--register:hex":
						addr = Integer.parseInt(args[++i], 16);
						addrBase = 16;
						break;

					case "-c":
					case "--count":
						count = Integer.parseInt(args[++i]);
						break;

				}
			} catch ( ArrayIndexOutOfBoundsException e ) {
				out.printf("Missing value for %s argument.\n", args[i - 1]);
				return;
			} catch ( NumberFormatException e ) {
				out.printf("Expected a number value for %s argument.\n", args[i - 1]);
				return;
			} catch ( IllegalArgumentException e ) {
				out.printf("Invalid value for %s argument: %s\n", args[i - 1], e.getMessage());
				return;
			}
		}

		if ( unitId < 1 ) {
			out.println("Must provide unit ID (--unit).");
			return;
		}
		if ( addr < (oneBased ? 1 : 0) ) {
			out.printf("Invalid starting register address (--register): minimum is %d",
					(oneBased ? 1 : 0));
			return;
		}
		if ( count < 1 ) {
			out.println("Must provide count of registers to read (--count).");
			return;
		}
		Future<ModbusMessage> f = null;
		switch (blockType) {
			case Coil:
			case Discrete:
				f = client.sendAsync(BitsModbusMessage.readBitsRequest(blockType, unitId,
						(oneBased ? addr - 1 : addr), count));
				break;

			case Input:
			case Holding:
				f = client.sendAsync(RegistersModbusMessage.readRegistersRequest(blockType, unitId,
						(oneBased ? addr - 1 : addr), count));
				break;

			default:
				// shouldn't get here
				break;
		}
		if ( f == null ) {
			return;
		}
		final int addrEnd = addr + count - 1;
		final int addrWidth = Integer.toString(addrEnd, addrBase).length();
		try {
			ModbusMessage res = f.get(15, TimeUnit.SECONDS);
			switch (blockType) {
				case Coil:
				case Discrete: {
					net.solarnetwork.io.modbus.BitsModbusMessage bits = res
							.unwrap(net.solarnetwork.io.modbus.BitsModbusMessage.class);
					if ( bits != null ) {
						String tmpl = "[" + (addrBase == 16 ? "0x" : "") + "%0" + addrWidth
								+ (addrBase == 16 ? "X" : "d") + "]: %d\n";
						for ( int i = 0; i < count; i++ ) {
							out.printf(tmpl, addr + i, bits.isBitEnabled(i) ? 1 : 0);
						}
					} else {
						out.printf("Unexpected response: %s\n", res);
					}
				}
					break;

				case Input:
				case Holding: {
					net.solarnetwork.io.modbus.RegistersModbusMessage regs = res
							.unwrap(net.solarnetwork.io.modbus.RegistersModbusMessage.class);
					if ( regs != null ) {
						String tmpl = "[" + (addrBase == 16 ? "0x" : "") + "%0" + addrWidth
								+ (addrBase == 16 ? "X" : "d") + "]: 0x%04X\n";
						short[] data = regs.dataDecode();
						for ( int i = 0, len = data.length; i < len; i++ ) {
							out.printf(tmpl, addr + i, data[i]);
						}
					} else {
						out.printf("Unexpected response: %s\n", res);
					}
				}
					break;

				default:
					// shouldn't get here

			}
		} catch ( TimeoutException e ) {
			out.println("Timeout waiting for response.");
		} catch ( InterruptedException e ) {
			out.println("Interrupted waiting for response.");
		} catch ( ExecutionException e ) {
			out.println("Error: " + e.getCause().toString());
		}
	}

	private static final Pattern BITS_REGEX = Pattern.compile("(\\d)([EON])(\\d)",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Toggle the "wire logging" flag.
	 * 
	 * @param wireLogging
	 *        {@literal true} to enable wire logging; defaults to
	 *        {@literal false}
	 */
	public void setWireLogging(boolean wireLogging) {
		this.wireLogging = wireLogging;
	}

	/**
	 * Main entry.
	 * 
	 * @param args
	 *        the arguments
	 */
	public static void main(String... args) {
		if ( args == null || args.length < 1 ) {
			System.err.println("Must provide the serial port name -port argument.");
		}
		String deviceName = null;
		BasicSerialParameters params = new BasicSerialParameters();
		boolean wireLogging = false;
		for ( int i = 0, len = args.length; i < len; i++ ) {
			try {
				switch (args[i]) {
					case "--dev":
					case "--device":
						deviceName = args[++i];
						break;

					case "-b":
					case "--baud":
						params.setBaudRate(Integer.parseInt(args[++i]));
						break;

					case "-d":
					case "--data":
						params.setDataBits(Integer.parseInt(args[++i]));
						break;

					case "-s":
					case "--stop":
						params.setStopBits(SerialStopBits.forCode(Integer.parseInt(args[++i])));
						break;

					case "-P":
					case "--parity":
						params.setParity(SerialParity.forCode(Integer.parseInt(args[++i])));
						break;

					case "--bits": {
						Matcher m = BITS_REGEX.matcher(args[++i]);
						if ( !m.matches() ) {
							throw new IllegalArgumentException(
									"Must match DPS for data bits, parity, stop bits, for example 8N1");
						}
						params.setDataBits(Integer.parseInt(m.group(1)));
						params.setParity(SerialParity.forAbbreviation(m.group(2)));
						params.setStopBits(SerialStopBits.forCode(Integer.parseInt(m.group(3))));
					}
						break;

					case "--debug":
						wireLogging = true;
						break;
				}
			} catch ( ArrayIndexOutOfBoundsException e ) {
				System.err.printf("Missing value for %s argument.\n", args[i - 1]);
				System.exit(1);
			} catch ( NumberFormatException e ) {
				System.err.printf("Expected a number value for %s argument.\n", args[i - 1]);
				System.exit(1);
			} catch ( IllegalArgumentException e ) {
				System.err.printf("Invalid value for %s argument: %s\n", args[i - 1], e.getMessage());
				System.exit(1);
			}
		}
		if ( deviceName == null || deviceName.trim().isEmpty() ) {
			System.err.println("Must provide --device argument.");
			System.exit(1);
		}
		JscModbusShell shell = new JscModbusShell(deviceName, params,
				new BufferedReader(new InputStreamReader(System.in)), new PrintWriter(System.out, true));
		shell.setWireLogging(wireLogging);
		shell.start();
	}

}
