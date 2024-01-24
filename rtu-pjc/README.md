# Nifty Modbus: RTU (PureJavaComm)

This project adds Modbus RTU (serial, RS-485, etc.) protocol support to Nifty Modbus, using the
[PureJavaComm][pjc] library to provide native serial port access.

# Example use

Here is an example of using the Nifty Modbus RTU client to read some Modbus registers:

```java
// configure the serial port settings
BasicSerialParameters params = new BasicSerialParameters();
params.setBaudRate(9600);
NettyRtuModbusClientConfig config = new NettyRtuModbusClientConfig("/dev/ttyUSB0", params);

// create the client, passing in the PJC SerialPortProvider
ModbusClient client = new RtuNettyModbusClient(config, new PjcSerialPortProvider());

try {
	// open the serial port
	client.start().get();

	// request the first 10 holding register values
	RegistersModbusMessage req = RegistersModbusMessage.readRegistersRequest(
		ModbusBlockType.Holding, 1, 0, 10);
	RegistersModbusMessage res = client.send(req).unwrap(RegistersModbusMessage.class);

	// print out the results
	short[] data = res.dataDecode();
	for ( int i = 0, len = data.length; i < len; i++ ) {
		System.out.printf("%d: 0x%04X\n", addr + i, data[i]);
	}
} finally {
	// close the serial port
	client.stop();
}

```

[pjc]: https://github.com/nyholku/purejavacomm/
