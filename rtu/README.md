# Nifty Modbus: RTU

This project adds Modbus RTU (serial, RS-485, etc.) protocol support to Nifty Modbus. Since Java
does not have native support for working with serial ports, this project relies on some other
project to provide a `net.solarnetwork.io.modbus.serial.SerialPortProvider` implementation to
provide access to serial devices. The [Nifty Modbus RTU (jSerialComm)](../rtu-jsc) projects
provides such an implementation, that uses the jSerialComm library for the native serial port
access.
