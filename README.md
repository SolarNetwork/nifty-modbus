# Nifty Modbus

Nifty Modbus is a delightful little Modbus library for Java. It aims to be easy to use, reliable,
and cover the most common Modbus use cases. It also is designed around some
[core APIs](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-api) that make it easy to
extend.

Nifty Modbus is built on top of the [Netty](https://netty.io/) asynchronous network library.

[![API JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-api/JavaDoc%20API.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-api)
[![Core JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-core/JavaDoc%20Core.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-core)
[![RTU JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-rtu/JavaDoc%20RTU.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-rtu)
[![TCP JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-tcp/JavaDoc%20TCP.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-tcp)

# Example use

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
	ModbusMessage req = readHoldingsRequest(unitId, addr, count);
	RegistersModbusMessage res = client.send(req).unwrap(RegistersModbusMessage.class);

	// print out the results
	short[] data = res.dataDecode();
	for ( int i = 0, len = data.length; i < len; i++ ) {
		System.out.printf("%d: 0x%04X\n", addr + i, data[i]);
	}
} finally {
	client.stop();
}
```

The above snippet was taken from the [TcpClientReadRegistersExample][ex-tcp-client] class. See the
[examples project](examples/) project for more samples.


# Modbus Shell

The [shell](./shell/) component provides a basic interactive command-line application for reading
from and writing to Modbus devices. It supports both RTU and TCP connections.

![Modbus Shell](shell/docs/nifty-modbus-shell.gif)


# Modbus TCP Server

The [tcp](./tcp/) component provides a basic Modbus server in addition to a Modbus client, in the
[NettyTcpModbusServer][NettyTcpModbusServer] class. This server will listen for Modbus requests
and pass them over to a consumer of your own making. That consumer can also provide any necessary
Modbus response to send back to the connected client.

Here is an example of using the Nifty Modbus TCP server to respond to read holding register
requests:

```java
NettyTcpModbusServer server = new NettyTcpModbusServer(bindPort);
server.setMessageHandler((msg, sender) -> {
	// this handler only supports read holding registers requests
	RegistersModbusMessage req = msg.unwrap(RegistersModbusMessage.class);
	if ( req != null && req.getFunction().blockType() == ModbusBlockType.Holding ) {

		// generate some fake data that matches the request register count
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

# Dependencies

Nifty Modbus requires a Java 8 or later runtime and has core dependencies on Netty 4.1 and slf4j
1.7. For Modbus RTU (serial port) the [rtu-jsc](./rtu-jsc/) component depends on
[jSerialComm][jSerialComm] 2.9. More specifically, it requires the following direct dependencies:

| Dependency | Version | Description |
|:-----------|:--------|:------------|
| Java                       | 8   | Java 8 is the _minimum_ runtime needed. |
| `io.netty:netty-codec`     | 4.1 | Nifty Modbus implements decoders/encoders for the Modbus protocol. |
| `io.netty:netty-handler`   | 4.1 | Provides wire-level logging support. |
| `org.slf4j:slf4j-api`      | 1.7 | For logging. |
| `com.fazecast:jSerialComm` | 2.9 | For RTU serial support. Not needed for TCP. |

[![RTU JSC JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-rtu-jsc/JavaDoc%20RTU%20jSerialComm.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-rtu-jsc)

The [rtu-pjc](./rtu-pjc/) component is an alternative RTU implementation that relies on the
[PureJavaComm][PureJavaComm] serial library, and can be used instead of the jSerialComm component.
That requires the following dependencies:

| Dependency | Version | Description |
|:-----------|:--------|:------------|
| `net.solarnetwork.external:net.solarnetwork.external.pjc` | 1.0.2 | Alternative RTU serial support. Not needed for TCP. |
| `net.java.dev.jna:jna` | 5.6.0 | Required by PureJavaComm. |

[![RTU PJC JavaDoc](https://javadoc.io/badge2/net.solarnetwork.common/nifty-modbus-rtu-pjc/JavaDoc%20RTU%20PureJavaComm.svg)](https://javadoc.io/doc/net.solarnetwork.common/nifty-modbus-rtu-pjc)

# Maven Central Repository coordinates

Nifty Modbus can be integrated into your project using the following coordinates,
all of which use the `net.solarnetwork.common` Group identifier:

| Artifact | Notes |
|:---------|:------|
| `nifty-modbus-api` | Required in all cases. |
| `nifty-modbus-core` | Required in all cases. |
| `nifty-modbus-rtu` | Required for Modbus RTU support, along with a serial port implementation. |
| `nifty-modbus-rtu-jsc` | Provides a serial port implementation based on [jSerialComm][jSerialComm]. |
| `nifty-modbus-rtu-pjc` | Provides a serial port implementation based on [PureJavaComm][PureJavaComm]. |
| `nifty-modbus-shell` | Provides an interactive Modbus command-line application.  |
| `nifty-modbus-tcp` | Required for Modbus TCP support. |

Usually it is sufficient to declare just the RTU and/or TCP components in your project, and the
others will be pulled in automatically. For example to include both RTU using jSerialComm and TCP
support in a Gradle project:

```gradle
dependencies {
	implementation "net.solarnetwork.common:nifty-modbus-rtu-jsc:1.0.0"
	implementation "net.solarnetwork.common:nifty-modbus-tcp:1.0.0"
}
```

# Building from source

To build Nifty Modbus yourself, clone or download this repository. Then:

```sh
# Linux/macOS/etc
./gradlew build -x test

# Or Windows
.\gradlew.bat build -x test
```

The component artifacts will be created within the `build/libs` directory of each component:

 * `api/build/libs/nifty-modbus-api-X.Y.Z.jar`
 * `core/build/libs/nifty-modbus-core-X.Y.Z.jar`
 * `rtu/build/libs/nifty-modbus-rtu-X.Y.Z.jar`
 * `rtu-jsc/build/libs/nifty-modbus-rtu-jsc-X.Y.Z.jar`
 * `rtu-pjc/build/libs/nifty-modbus-rtu-pjc-X.Y.Z.jar`
 * `tcp/build/libs/nifty-modbus-tcp-X.Y.Z.jar`

# Test coverage

[![codecov](https://codecov.io/github/SolarNetwork/nifty-modbus/branch/main/graph/badge.svg?token=VPVD1Z35YK)](https://codecov.io/github/SolarNetwork/nifty-modbus)

Having a well-tested and reliable Modbus library is a core goal of this project. Unit tests are
executed automatically after every push into this repository and their associated code coverage is
uploaded to [Codecov](https://codecov.io/github/SolarNetwork/nifty-modbus/).

[![codecov](https://codecov.io/github/SolarNetwork/nifty-modbus/branch/main/graphs/sunburst.svg?token=VPVD1Z35YK)](https://codecov.io/github/SolarNetwork/nifty-modbus)


[jSerialComm]: https://fazecast.github.io/jSerialComm/
[ex-tcp-client]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/test/java/net/solarnetwork/io/modbus/tcp/example/TcpClientReadRegistersExample.java
[ex-tcp-server]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/test/java/net/solarnetwork/io/modbus/tcp/example/TcpServerExample.java
[NettyTcpModbusServer]: https://github.com/SolarNetwork/nifty-modbus/blob/main/tcp/src/main/java/net/solarnetwork/io/modbus/tcp/netty/NettyTcpModbusServer.java
[PureJavaComm]: http://www.sparetimelabs.com/purejavacomm/purejavacomm.php
