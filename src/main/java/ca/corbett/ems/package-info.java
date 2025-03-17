/**
 * <H2>EMS - Easy Messaging Service</H2>
 *
 * EMS is an extremely lightweight and simple messaging service that allows applications
 * to expose a very simple and highly customizable remote API. This is like an extremely
 * simplified version of REST in some ways.
 *
 * <p>
 *     <b>EMS is not recommended for production use.</b> Seriously. There are probably
 *     security holes everywhere. This was a learning project to get a better understanding
 *     of how threading and socket programming works in Java. If you actually need to
 *     expose an API from your Java application in production, just use REST.
 * </p>
 *
 * <H3>Using EMS as a client</H3>
 * The ca.corbett.ems.client.EMSClient class provides an extensible access point
 * for talking to a remote EMS server. You have two options for working with EMSClient:
 * <ol>
 * <li><b>Use EMSClient as-is</b>: You can instantiate an EMSClient instance and point it
 * at the remote server, and then use the sendCommand() method to send commands to
 * the remote server. This approach is easy to set up but forces your code to
 * understand the command and response syntax used by the remote EMS server.</li>
 * <li><b>Extend EMSClient</b>: You can extend the EMSClient class to wrap the sendCommand()
 * and parseResponse() methods behind a more application-friendly API. This approach is more
 * work but allows your application code to interact with the server in a much more
 * natural way.</li>
 * </ol>
 *
 * <H3>Using EMS as a server</H3>
 * The ca.corbett.ems.server.EMSServer class can be used to spin up a new EMSServer
 * and embed it into your application. Out of the box, EMSServer doesn't know how to respond
 * to very many commands. So, you can use the registerCommandHandler() method to register
 * some custom commands that your server can respond to. This is effectively defining
 * the API of your embedded EMS server. See AbstractCommandHandler for an overview of how
 * to implement a custom command. Invoking stopServer() will kill all current client connections,
 * shut down the server, and free up the listening port it was using.
 *
 * <H3>Example server implementation</H3>
 * The <b>ems-example-app</b> project contains an example implementation of an EMS Server
 * that is configured to act like a mini-Kafka message broker, allowing clients to subscribe
 * to arbitrary "channels", and allowing clients to send messages to those channels. When a
 * message arrives on a given channel, any listeners to that channel receive the message
 * that was sent.
 *
 * <H3>Implementing security (you can, but why?)</H3>
 * Theoretically, you could implement a LoginHandler that requires a username and password
 * or some other kind of access token to be provided in order to unlock other command
 * handlers. But, at that point, maybe you should look at implementing an actual REST
 * solution instead of playing with this API?
 * <p>
 * Again, EMS is not recommended for production use, or for any use case where security
 * is a concern.
 * </p>
 */
package ca.corbett.ems;
