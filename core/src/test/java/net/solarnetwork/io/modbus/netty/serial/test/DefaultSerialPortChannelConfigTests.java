/* ==================================================================
 * DefaultSerialPortChannelConfigTests.java - 6/12/2022 4:20:41 pm
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

package net.solarnetwork.io.modbus.netty.serial.test;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMaxBytesRecvByteBufAllocator;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import net.solarnetwork.io.modbus.netty.serial.DefaultSerialPortChannelConfig;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannel;
import net.solarnetwork.io.modbus.netty.serial.SerialPortChannelOption;
import net.solarnetwork.io.modbus.serial.SerialFlowControl;
import net.solarnetwork.io.modbus.serial.SerialParity;
import net.solarnetwork.io.modbus.serial.SerialPort;
import net.solarnetwork.io.modbus.serial.SerialPortProvider;
import net.solarnetwork.io.modbus.serial.SerialStopBits;

/**
 * Test cases for the {@link DefaultSerialPortChannelConfig} class.
 *
 * @author matt
 * @version 1.0
 */
public class DefaultSerialPortChannelConfigTests {

	private SerialPortChannel ch;
	private DefaultSerialPortChannelConfig config;

	@BeforeEach
	public void setup() {
		ch = new SerialPortChannel(new SerialPortProvider() {

			@Override
			public SerialPort getSerialPort(String name) {
				return null;
			}
		});

		config = (DefaultSerialPortChannelConfig) ch.config();
	}

	@Test
	public void stringValue() {
		assertThat("String value", config.toString(),
				matchesRegex(format("SerialPortChannelConfig\\{%d %s\\}", config.getBaudRate(),
						quote(config.bitsShortcut()))));
	}

	@Test
	public void options() {
		// WHEN
		Map<ChannelOption<?>, Object> options = config.getOptions();

		// THEN
		assertThat("Map contains serial options", options.keySet(),
				hasItems(SerialPortChannelOption.BAUD_RATE, SerialPortChannelOption.DATA_BITS,
						SerialPortChannelOption.PARITY, SerialPortChannelOption.STOP_BITS,
						SerialPortChannelOption.FLOW_CONTROL, SerialPortChannelOption.WAIT_TIME,
						SerialPortChannelOption.READ_TIMEOUT, SerialPortChannelOption.RS485,
						SerialPortChannelOption.RS485_RTS_HIGH,
						SerialPortChannelOption.RS485_TERMINATION, SerialPortChannelOption.RS485_ECHO,
						SerialPortChannelOption.RS485_BEFORE_SEND_DELAY,
						SerialPortChannelOption.RS485_AFTER_SEND_DELAY));
	}

	@Test
	public void setOptions() {
		// GIVEN
		final int baudRate = 4;
		final int dataBits = 3;
		final SerialParity parity = SerialParity.Odd;
		final SerialStopBits stopBits = SerialStopBits.Two;
		final int waitTime = 1;
		final int readTimeout = 2;
		final Set<SerialFlowControl> flowControl = EnumSet.of(SerialFlowControl.RTS,
				SerialFlowControl.CTS);
		final Boolean rs485 = Boolean.TRUE;
		final boolean rs485RtsHigh = true;
		final boolean rs485Term = true;
		final boolean rs485Echo = true;
		final int rs485BeforeSendDelay = 1234;
		final int rs485AfterSendDelay = 2345;

		// WHEN
		config.setOption(SerialPortChannelOption.BAUD_RATE, baudRate);
		config.setOption(SerialPortChannelOption.DATA_BITS, dataBits);
		config.setOption(SerialPortChannelOption.PARITY, parity);
		config.setOption(SerialPortChannelOption.STOP_BITS, stopBits);
		config.setOption(SerialPortChannelOption.WAIT_TIME, waitTime);
		config.setOption(SerialPortChannelOption.READ_TIMEOUT, readTimeout);
		config.setOption(SerialPortChannelOption.FLOW_CONTROL, flowControl);
		config.setOption(SerialPortChannelOption.RS485, rs485);
		config.setOption(SerialPortChannelOption.RS485_RTS_HIGH, rs485RtsHigh);
		config.setOption(SerialPortChannelOption.RS485_TERMINATION, rs485Term);
		config.setOption(SerialPortChannelOption.RS485_ECHO, rs485Echo);
		config.setOption(SerialPortChannelOption.RS485_BEFORE_SEND_DELAY, rs485BeforeSendDelay);
		config.setOption(SerialPortChannelOption.RS485_AFTER_SEND_DELAY, rs485AfterSendDelay);

		config.setOption(ChannelOption.AUTO_CLOSE, true);

		// THEN
		assertThat("Baud rate saved", config.getOption(SerialPortChannelOption.BAUD_RATE),
				is(equalTo(baudRate)));
		assertThat("Data bits saved", config.getOption(SerialPortChannelOption.DATA_BITS),
				is(equalTo(dataBits)));
		assertThat("Parity saved", config.getOption(SerialPortChannelOption.PARITY),
				is(equalTo(parity)));
		assertThat("Stop bits saved", config.getOption(SerialPortChannelOption.STOP_BITS),
				is(equalTo(stopBits)));
		assertThat("Wait time saved", config.getOption(SerialPortChannelOption.WAIT_TIME),
				is(equalTo(waitTime)));
		assertThat("Read timeout saved", config.getOption(SerialPortChannelOption.READ_TIMEOUT),
				is(equalTo(readTimeout)));
		assertThat("Flow control saved", config.getOption(SerialPortChannelOption.FLOW_CONTROL),
				is(equalTo(flowControl)));
		assertThat("RS-485 mode saved", config.getOption(SerialPortChannelOption.RS485),
				is(equalTo(rs485)));
		assertThat("RS-485 RTS high saved", config.getOption(SerialPortChannelOption.RS485_RTS_HIGH),
				is(equalTo(rs485RtsHigh)));
		assertThat("RS-485 termination saved",
				config.getOption(SerialPortChannelOption.RS485_TERMINATION), is(equalTo(rs485Term)));
		assertThat("RS-485 echo saved", config.getOption(SerialPortChannelOption.RS485_ECHO),
				is(equalTo(rs485Echo)));
		assertThat("RS-485 before send delay saved",
				config.getOption(SerialPortChannelOption.RS485_BEFORE_SEND_DELAY),
				is(equalTo(rs485BeforeSendDelay)));
		assertThat("RS-485 after send delay saved",
				config.getOption(SerialPortChannelOption.RS485_AFTER_SEND_DELAY),
				is(equalTo(rs485AfterSendDelay)));

		assertThat("Auto close saved (super class)", config.getOption(ChannelOption.AUTO_CLOSE),
				is(equalTo(true)));
	}

	@Test
	public void setters() {
		// GIVEN
		final int baudRate = 4;
		final int dataBits = 3;
		final SerialParity parity = SerialParity.Odd;
		final SerialStopBits stopBits = SerialStopBits.Two;
		final int waitTime = 1;
		final int readTimeout = 2;
		final Set<SerialFlowControl> flowControl = EnumSet.of(SerialFlowControl.RTS,
				SerialFlowControl.CTS);
		final Boolean rs485 = Boolean.TRUE;
		final boolean rs485RtsHigh = true;
		final boolean rs485Term = true;
		final boolean rs485Echo = true;
		final int rs485BeforeSendDelay = 1234;
		final int rs485AfterSendDelay = 2345;

		// WHEN
		config.setBaudRate(baudRate);
		config.setDataBits(dataBits);
		config.setParity(parity);
		config.setStopBits(stopBits);
		config.setWaitTime(waitTime);
		config.setReadTimeout(readTimeout);
		config.setFlowControl(flowControl);
		config.setRs485ModeEnabled(rs485);
		config.setRs485RtsHighEnabled(rs485RtsHigh);
		config.setRs485TerminationEnabled(rs485Term);
		config.setRs485EchoEnabled(rs485Echo);
		config.setRs485BeforeSendDelay(rs485BeforeSendDelay);
		config.setRs485AfterSendDelay(rs485AfterSendDelay);

		// THEN
		assertThat("Baud rate saved", config.getBaudRate(), is(equalTo(baudRate)));
		assertThat("Data bits saved", config.getDataBits(), is(equalTo(dataBits)));
		assertThat("Parity saved", config.getParity(), is(equalTo(parity)));
		assertThat("Stop bits saved", config.getStopBits(), is(equalTo(stopBits)));
		assertThat("Wait time saved", config.getWaitTime(), is(equalTo(waitTime)));
		assertThat("Read timeout saved", config.getReadTimeout(), is(equalTo(readTimeout)));
		assertThat("Flow control saved", config.getFlowControl(), is(equalTo(flowControl)));
		assertThat("RS-485 mode saved", config.getRs485ModeEnabled(), is(equalTo(rs485)));
		assertThat("RS-485 RTS high saved", config.isRs485RtsHighEnabled(), is(equalTo(rs485RtsHigh)));
		assertThat("RS-485 termination saved", config.isRs485TerminationEnabled(),
				is(equalTo(rs485Term)));
		assertThat("RS-485 echo saved", config.isRs485EchoEnabled(), is(equalTo(rs485Echo)));
		assertThat("RS-485 before send delay saved", config.getRs485BeforeSendDelay(),
				is(equalTo(rs485BeforeSendDelay)));
		assertThat("RS-485 after send delay saved", config.getRs485AfterSendDelay(),
				is(equalTo(rs485AfterSendDelay)));
	}

	@Test
	public void setWaitTime_negative() {
		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			config.setWaitTime(-1);
		}, "Negative value is not allowed");
	}

	@Test
	public void setReadTimeout_negative() {
		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			config.setReadTimeout(-1);
		}, "Negative value is not allowed");
	}

	@Test
	public void setRs485BeforeSendDelay_negative() {
		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			config.setRs485BeforeSendDelay(-1);
		}, "Negative value is not allowed");
	}

	@Test
	public void setRs485AfterSendDelay_negative() {
		// WHEN
		assertThrows(IllegalArgumentException.class, () -> {
			config.setRs485AfterSendDelay(-1);
		}, "Negative value is not allowed");
	}

	@Test
	public void setConnectTimeoutMillis() {
		// GIVEN
		int value = 123;
		config.setConnectTimeoutMillis(value);

		// THEN
		assertThat("Value saved", config.getConnectTimeoutMillis(), is(equalTo(value)));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void setMaxMessagesPerRead() {
		// GIVEN
		int value = 123;
		config.setMaxMessagesPerRead(value);

		// THEN
		assertThat("Value saved", config.getMaxMessagesPerRead(), is(equalTo(value)));
	}

	@Test
	public void setWriteSpinCount() {
		// GIVEN
		int value = 123;
		config.setWriteSpinCount(value);

		// THEN
		assertThat("Value saved", config.getWriteSpinCount(), is(equalTo(value)));
	}

	@Test
	public void setAllocator() {
		// GIVEN
		ByteBufAllocator value = new UnpooledByteBufAllocator(false);
		config.setAllocator(value);

		// THEN
		assertThat("Value saved", config.getAllocator(), is(equalTo(value)));
	}

	@Test
	public void setRecvByteBufAllocator() {
		// GIVEN
		RecvByteBufAllocator value = new DefaultMaxBytesRecvByteBufAllocator();
		config.setRecvByteBufAllocator(value);

		// THEN
		assertThat("Value saved", config.getRecvByteBufAllocator(), is(equalTo(value)));
	}

	@Test
	public void setAutoRead() {
		// GIVEN
		boolean value = true;
		config.setAutoRead(value);

		// THEN
		assertThat("Value saved", config.isAutoRead(), is(equalTo(value)));
	}

	@Test
	public void setAutoClose() {
		// GIVEN
		boolean value = true;
		config.setAutoClose(value);

		// THEN
		assertThat("Value saved", config.isAutoClose(), is(equalTo(value)));
	}

	@Test
	public void setWriteBufferHighWaterMark() {
		// GIVEN
		config.setWriteBufferLowWaterMark(1); // must be lower than high watermark
		int value = 123;
		config.setWriteBufferHighWaterMark(value);

		// THEN
		assertThat("Value saved", config.getWriteBufferHighWaterMark(), is(equalTo(value)));
	}

	@Test
	public void setWriteBufferLowWaterMark() {
		// GIVEN
		int value = 123;
		config.setWriteBufferLowWaterMark(value);

		// THEN
		assertThat("Value saved", config.getWriteBufferLowWaterMark(), is(equalTo(value)));
	}

	@Test
	public void setMessageSizeEstimator() {
		// GIVEN
		MessageSizeEstimator value = new DefaultMessageSizeEstimator(8);
		config.setMessageSizeEstimator(value);

		// THEN
		assertThat("Value saved", config.getMessageSizeEstimator(), is(equalTo(value)));
	}

}
