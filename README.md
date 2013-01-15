#Niowire [![Build Status](http://jenkins.houliston.me/job/Niowire/badge/icon)](http://jenkins.houliston.me/job/Niowire/)
##Getting Started
Below is a very simple example which configures a single server on port 12345 which will echo any text you send to it back to you. If no message is recieved for 3 seconds it will kill the connection.

To implement your own server, you need a Serializer, an Inspector, and one or more services. You can either write these yourself or use one of the built in ones.

```
//Make a service
NioObjectFactory<NioService> service = new ReflectiveNioObjectFactory(EchoService.class);

//Create a server definition
NioServerDefinition def = new NioServerDefinition();
def.setId("SERVERX");
def.setName("Super Awesome Server");
def.setPort(12345);
def.setSerializerFactory(serializer);
def.setInspectorFactory(inspector);
def.setServiceFactories(collections.singletonList(service)));

//Make a server
NioSocketServer server = new NioSocketServer();
server.addServer(def);

//Start the server
new Thread(server).start();

//You're done!
```
##Overview
Niowire is a highly scaleable socket server framework built on Java's NIO libraries. It handles all of the details of accepting and handling connections so that you can focus on the implementation of your program. Being built on NIO it is highly scalable and is capable of maintaining thousands of concurrent connections.

The data flow can go in both directions at any time, to send data to the client, you write it to the write() method of the context which is set on all of the services.

![Sequence Diagram](http://www.websequencediagrams.com/cgi-bin/cdraw?lz=dGl0bGUgRmxvdyBvZiBEYXRhCmxvb3AKICAgIE5ldHdvcmstPlNlcmlhbGl6ZXI6IFNlbmQgUmF3ACYGICAgIAAUCi0-SW5zcGVjdG8AIQhQYXJzZWQAIgoAFwkAUAV2aWNlcwBMB0F1dGhlbnRpY2F0ACUMb3B0AIEHBSAgICBub3RlIHJpZ2h0IG9mIAA2CnMARAYgbQAWBXNlbmQgZGF0YSBiYWNrADQJAGcIAIE7E09iamVjAF0KAIFEDACBfAcAgWwLQnl0ZXMAghoFZW5kCmVuZAo&s=napkin)
##Features
###Modular Design
Niowire is built upon a modular design so that each of the components which are required to exist for it to run, have no dependencies on any other modules. This makes unit testing of the modules very easy as you will never need to execute any code apart from the code you've written.

###Live Reconfiguration
One of the awesome features of Niowire is that you can perform live reconfiguration of any of the servers without losing a connection. You can change your serializer, inspector, or add and remove services and the existing connections will adjust to the new state. You can even change the port that your server is listening on and the existing connections will still be maintained. This is ideal for the requirements of high availability servers.
####Example
```
NioServerDefinition def;
NioSocketServer server = new NioSocketServer();
server.add(def);

... some time later ... 

def.setSerializer(anotherSerializer);
server.update(def);

```
##Server Sources
###Built in Server Sources
####Directory Server Source
    io.niowire.serversource.DirectoryServerSource

#####Example File
```
{
	"name" : "global",
	"port" : 12012,

	"serializerFactory" : {
		"className" : "io.niowire.serializer.JsonSerializer",
		"configuration" : {
			"charset":"utf-8"
		}
	},

	"inspectorFactory" : {
		"className" : "io.niowire.inspection.NullInspector"
	},

	"serviceFactories" : [
	{
		"className" : "io.niowire.service.EchoService"
	}
	]
}
```
##Serializers
###Built in Serializers
####Delimited Serializer
    io.niowire.serializer.DelimitedSerializer
####Line Serializer
    io.niowire.serializer.LineSerializer
####Json Serializer
    io.niowire.serializer.JsonSerializer
####Split Serializer
    io.niowire.serializer.SplitSerializer
*Coming in a future version*
####SSL Serializer
    io.niowire.serializer.SSLSerializer
*Coming in a future version*
####GZIP Serializer
    io.niowire.serializer.GZIPSerializer
*Coming in a future version*

##Inspectors
###Built in Inspectors
####Null Inspector
    io.niowire.inspector.NullInspector
####Timeout Inspector
    io.niowire.inspector.TimeoutInspector

##Services
###Built in Services
####Echo Service
    io.niowire.service.EchoService
####File Logger Service
    io.niowire.service.FileLogger
*Coming in a future version*
####Repeater Service
    io.niowire.service.repeater.RepeaterInputService
    io.niowire.service.repeater.RepeaterOutputService
*Coming in a future version*
####Aggregator Service
    io.niowire.service.aggregator.AggregatorInputService
    io.niowire.service.aggregator.AggregatorOutputService
*Coming in a future version*

##Future Versions
The following changes are coming in future versions, Note that the API for Niowire is not considered stable yet and may change at any time (although the changes should become more and more minor and will be considered stable by 1.0)

- Moving to an annotations based configuration and context injection (rather then throught the interface) this means that rather then passing in a configuration object, the properties will be set directly by examining the incoming map and mapping the keys to fields using annotations. The context object will also be injected (removing the need for setContext)
- Changing the API to support a "Delayed Update" (updating services inspectors etc when they say they are ready to be updated)
- Implementing a SSL socket wrapper
- Implementing a Split Serializer (allows deserializing using one method, and serialization using another)
- Implementing a GZIP Serializer (compresses/decompresses the data coming in/out)
- Implementing a Repeater service (allows selecting existing connections to "Listen in to", repeating all data sent to it)
- Implementing an Aggregator service (allows listening to multiple channels)
- Implementing a File logger service (writes the data to files)