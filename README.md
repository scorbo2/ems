# EMS - Easy Messaging Service

EMS provides a simple, very lightweight mechanism for adding a socket API to your Java application.
The base EMS server can easily be extended with custom commands to implement whatever functionality
you would like to add to your application's API.

**EMS IS NOT RECOMMENDED FOR PRODUCTION USE**. Seriously. There are probably security holes everywhere.
This was a little learning project that I undertook to gain a better understanding of how threading
and socket programming works in Java. I've made use of this in some of my side projects, and it works,
but it's intended more as a learning tool than as a serious product. 

This repo houses the EMS library, which you can use to either embed an EMS server in your own 
application, or to make use of the client library to access a remote application running EMS.

For an example application that actually uses both of those, see the `ems-example-app` repo instead.

## The general idea - what problem does EMS solve?

Sometimes it's nice to enable a Java application running on machine A to talk to a different Java
application running on machine B on the same network. For example, to tell machine B to perform
some task, or to request some information that is stored on machine B.

An obvious answer is to spin up a REST API and make simple REST calls from machine A to machine B.
REST provides a very well-documented, secure, and configurable mechanism for this.

But that would be too easy.

So, in the spirit of doing things the hard way to learn more about how things work under the hood 
just for the fun of it, I decided to implement my own miniature version of that. The result is EMS.

## Spinning up an EMS server

Starting up an EMS server within your Java application is pretty straightforward:

```java
EMSServer server = new EMSServer("localhost", 1975);
server.startServer();
```

You can specify an optional hostname or IP address to bind to (default is `localhost`), and
a port to use. Then, you just invoke `startServer()` and EMS will start listening for
clients. The `startServer` method is asynchronous, so it will return immediately while EMS
starts up. While the server is running, you have options to check its status or to shut it down:

```java
if (server.isUp()) {
  logger.info("Hooray! Server is still up and running.");
}

// ... later ...
server.stopServer();
```

## Okay, it's running... so now what?

Out of the box, `EMSServer` comes with three built-in commands:

```
ECHO - Simply echoes whatever parameters you supply.
HELP (alias ?) - Lists available commands, or shows detailed help for a specific command.
WHO (alias WHOAMI) - Returns the client id for this client connection.
```

This is the absolutely bare minimum functionality that `EMSServer` provides. You can add to this
list by implementing your own commands and registering them with the server before startup.
We do this by extending the `AbstractCommandHandler` class. Let's write a very basic command
that just says "hello" whenever it's invoked.

```java
public class HelloHandler extends AbstractCommandHandler {

    public HelloHandler() {
        super("HELLO");
    }

    public HelloHandler(String name) {
        super(name);
    }

    public HelloHandler(String name, String alias) {
        super(name, alias);
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {
        return createOkResponse("Hello.");
    }

    @Override
    public int getMinParameterCount() {
        return 0;
    }

    @Override
    public int getMaxParameterCount() {
        return 0;
    }

    @Override
    public String getHelpText() {
        return "This command says hello. It's very polite.";
    }

    @Override
    public String getUsageText() {
        return name;
    }
}
```

Once we have our command, we can register it with the EMSServer (before the server starts up):

```java
EMSServer server = new EMSServer("localhost", 1975);
server.registerCommandHandler(new HelloHandler());
server.startServer();
```

Great! But... how do we actually use this command?

## EMSClient - connecting to a server and issuing commands

The `EMSClient` class is fairly straightforward. You can either instantiate it and use it as-is, or you
can subclass it to override the `sendCommand` and `parseCommand` methods to make the interface to it
a little easier for your code to go through. Let's start by just using it as-is:

```java
EMSClient client = new EMSClient();
if (client.connect("localhost", 1975)) {
  // Great, we are connected. Let's say hello:
  EMSServerResponse response = client.sendCommand("hello");
  
  if (response.isSuccess()) {
      logger.info(response.getMessage()); // "Hello."
  }
  
  client.disconnect();
}
```

We connect to the server running on localhost, send our "hello" command (it's case-insensitive),
and get back our hello response. Not the most useful example, but you can see how quickly you
can implement a custom command!

But can we make it easier to work with?

## Subclassing EMSClient to make it easier to work with

In the previous example, we had to deal with `EMSServerResponse` objects and checking the success
status of each command. We can avoid this complexity by subclassing `EMSClient` and hiding
some of these implementation details behind friendlier methods. Let's see how we can do this to make
saying hello a bit easier for our client code:

```java
public class CustomClient extends EMSClient {
    
    public String sayHello() {
        EMSServerResponse response = sendCommand("hello");
        return response.isSuccess() ? response.getMessage() : null;
    }
}
```

We have successfully hidden `EMSServerResponse` from our client code, making it slightly easier
on the eyes:

```java
EMSClient client = new CustomClient();
if (client.connect("localhost", 1975)) {
  // Great, we are connected. Let's say hello:
  String response = client.sayHello();
  if (response != null) {
      logger.info(response); // "Hello."
  }
  
  client.disconnect();
}
```

Also, maybe your code needs to transfer more than just simple string messages back and forth.
Maybe you want to send back a more complex response, or maybe you need to be able to 
parse a response and summarize it in an easier way. Can we do that in our custom client? Sure!

```java
public class CustomClient extends EMSClient {
    public MyModelObject getSomeModelObject(String id) {
        EMSServerResponse response = executeCommand("GETOBJECT", id);
        if (response.isSuccess()) {
            // Retrieve response.getMessage() as json text
            // parse json into an instance of MyModelObject
            return myModelObject;
        }
        return null;
    }
}
```

Again, the client code never has to see `EMSServerResponse`. You can add wrapper methods that
accept whatever parameters you need to send up to the `EMSServer` and you can write it such
that your custom client returns an instance of a Java class or something more useful to your code.

## Implementing security (just kidding)

Yes, you could probably implement a custom command handler that requires you to send up a
username/password or some kind of authentication token in order to unlock other commands
on an `EMSServer`, but at that point, you should probably just implement a REST API.
Again, **EMSServer is not recommended for production, or for any environment where security
is a concern**. This is a little toy project that was useful to me as a learning tool,
and which may be useful in certain simple situations on a LAN or on a secure intranet.
It's very easy to spin up a server and add custom command handlers to it for simple tasks.
But if you want something secure and performant that will scale to meet millions of requests
per day, you are probably not looking for EMS.

## Where can I get it?

If you're using maven, you can just add it as a dependency:

```xml
<dependencies>
  <dependency>
    <groupId>ca.corbett</groupId>
    <artifactId>ems</artifactId>
    <version>1.1.0</version>
  </dependency>
</dependencies>
```

Or you can clone this repo to browse through the code or generate the javadocs locally.
Also check out the `ems-example-app` to take a look at a small application that actually
uses EMS, both as a server and as a client. 

## License

swing-extras is made available under the MIT license: https://opensource.org/license/mit

## Revision history

EMS was written as a learning project late in 2023 and was published on github in 2025.

1.1 [2025-05-12]
- moved the sub/unsub functionality from the example app into this library
- cleaned up client disconnect handling so we actually get notified of it
- Added a version handler with a configurable server name (stolen from the example app)
