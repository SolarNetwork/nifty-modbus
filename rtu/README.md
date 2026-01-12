# Nifty Modbus: RTU

This project adds Modbus RTU (serial, RS-485, etc.) protocol support to Nifty Modbus. It includes
both client and server components.

Since Java does not have native support for working with serial ports, this project relies on some
other project to provide a `net.solarnetwork.io.modbus.serial.SerialPortProvider` implementation to
provide access to serial devices. The [Nifty Modbus RTU (jSerialComm)](../rtu-jsc) projects provides
such an implementation, that uses the jSerialComm library for the native serial port access.

[![RTU JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-rtu/JavaDoc%20RTU.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-rtu)
