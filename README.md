#Niowire [![Build Status](http://jenkins.houliston.me/job/Niowire/badge/icon)](http://jenkins.houliston.me/job/Niowire/)
##Getting Started
Below is a very simple example which configures a single server on port 12345 which will echo any text you send to it back to you. If no message is recieved for 3 seconds it will kill the connection.

To implement your own server, you need a Serializer, an Inspector, and one or more services. You can either write these yourself or use one of the built in ones. If you don't specify a Serializer or Inspector it will use the default ones (a LineSerializer using utf-8 and a NullInspector)

```
//Make a service
NioObjectFactory<NioService> service = new ReflectiveNioObjectFactory(EchoService.class);

//Create a server definition
NioServerDefinition def = new NioServerDefinition();
def.setId("SERVERX");
def.setName("Super Awesome Server");
def.setPort(12345);
def.setServiceFactories(collections.singletonList(service)));

//Make a server
NioSocketServer server = new NioSocketServer();
server.addServer(def);

//Start the server
new Thread(server).start();

//You're done! there is now a service listening on port 12345!
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
Server sources are an optional but useful part of the API. They allow you to write a class which can update the server with new servers (or modify or remove the servers). This can be done manually however by using a Source, you are able to entirely contain the functionalty of Niowire within the Niowire server. This means when Niowire's shutdown() method is run, all resources relating to niowire will be closed down.
###Built in Server Sources
There is currently only one built in server source (which uses a directory to gather data)
####Directory Server Source
    io.niowire.serversource.DirectoryServerSource
    
The directory server source uses JSON files which are stored in a directory on the hard drive as the configuration for the servers. It monitors the directory and whenever there is a change (file added, file removed, file updated) it will pass these onto the servers. The ID of the server is the name of the file (which will always be unique)
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
The serializer is the first step in the process. It recieves a ByteBuffer and is responsible for converting this data into useable objects for the rest of the system. It is also responsible for converting data in the other direction (converting java objects into bytebuffers)
###Built in Serializers
There are several built in serializers, as well as one base class which can be extended
####Delimited Serializer
    io.niowire.serializer.DelimitedSerializer
    
The delimited serializer is a abstract base class which can be extended for use. It will monitor and buffer the byte stream and return chunks of the byte buffer when it finds a series of pre determined bytes. This segmented stream can then be used by a subclass. On the return trip it will add these delimiters back in between serialized chunks
####Line Serializer
    io.niowire.serializer.LineSerializer

The LineSerializer is a specialization of the DelimitedSerializer, it looks for newline characters and when it finds them it then it sends the line converted to a string (using the configured charset)
####Json Serializer
    io.niowire.serializer.JsonSerializer
    
The Json serializer is a specialized line serializer which is used to parse incoming Json objects into java objects. It will default to parsing these objects into a LinkedHashMap, however if another class is provided it will parse them into that object instead.
####Split Serializer
    io.niowire.serializer.SplitSerializer
*Coming in a future version*
This serializer is one which is made up of two other serializes conbined. It will use one of these serializers for all the serialization operations, and the other for deserialization.
####SSL Serializer
    io.niowire.serializer.SSLSerializer
*Coming in a future version*
The SSL Serializer is used for handling SSL streams, it will within it contain another serializer which will be used for the normal serialization operations once the stream has been decrypted
####GZIP Serializer
    io.niowire.serializer.GZIPSerializer
*Coming in a future version*
The GZIP serializer is used to compress data streams, it will wrap another serializer and compress its output and decompress incoming data.

##Inspectors
The inspectors are the second step which the data must go through in the system. They are responsible for three main operations.

Firstly they are responsible for timing out connections when they have been inactive. Every 1 second their timeout method will be run, and if it returns true then the connection will be closed

Secondly they are responsible for authentication. If a connection is unable to authenticate then they will return false (closing the connection)

Thirdly are also responsible for any operations which require the incoming packet to be altered. They can alter the packets which are coming in before they go to the services.
###Built in Inspectors
There is only one inspector, It implements a basic timeout
####Timeout Inspector
    io.niowire.inspector.TimeoutInspector

The Timeout inspector is configured with a timeout and if this period of time is reached without recieving a packet then it will timeout.

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