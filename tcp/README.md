# Nifty Modbus: TCP

This project adds Modbus TCP protocol support to Nifty Modbus. It includes both client and server
components.

# TCP client example use

Here is an example of using the Nifty Modbus TCP client to read some Modbus registers:

```java
// create the TCP client
NettyTcpModbusClientConfig config = new NettyTcpModbusClientConfig(hostName, hostPort);
config.setAutoReconnect(false);
ModbusClient client = new TcpNettyModbusClient(config);
try {
	// connect the client
	client.start().get();

	// request holding registers
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
	// close the connection
	client.stop();
}
```

The above snippet was taken from the [TcpClientReadRegistersExample][ex-tcp-client] class.

# TCP server example use

The [NettyTcpModbusServer][NettyTcpModbusServer] class implements a basic Modbus TCP server. The
server will listen for Modbus requests and pass them over to a consumer of your own making. That
consumer can also provide any necessary Modbus response to send back to the connected client.

Here is an example of using the Nifty Modbus TCP server to respond to read holding register
requests (returning fake data):

```java
NettyTcpModbusServer server = new NettyTcpModbusServer(bindPort);
server.setMessageHandler((msg, sender) -> {
	// this handler only supports read holding registers requests
	RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
	if ( req != null && req.getFunction().blockType() == ModbusBlockType.Holding ) {

		// generate some fake data that matches the request register count;
		// a real server would read these values from somewhere like an in-memory
		// map or external database
		short[] resultData = new short[req.getCount()];
		for ( int i = 0; i < resultData.length; i++ ) {
			resultData[i] = (short) i;
		}

		// respond with the fake data
		sender.accept(readHoldingsResponse(req.getUnitId(), req.getAddress(), resultData));
	} else {
		// send back error that we don't handle that request
		sender.accept(new BaseModbusMessage(msg.getUnitId(), msg.getFunction(),
				ModbusErrorCode.IllegalFunction));
	}
});

try {
	server.start();
	while ( true ) {
		Thread.sleep(60_000);
	}
} finally {
	server.stop();
}
```

The above snippet was taken from the  [TcpServerExample][ex-tcp-server] class.

[ex-tcp-client]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/test/java/net/solarnetwork/io/modbus/tcp/example/TcpClientReadRegistersExample.java
[ex-tcp-server]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/test/java/net/solarnetwork/io/modbus/tcp/example/TcpServerExample.java
[NettyTcpModbusServer]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/main/java/net/solarnetwork/io/modbus/tcp/netty/NettyTcpModbusServer.java
