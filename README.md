# FastChat

FastChat is a simple, fast and concise chat widget for your website or web application.

Icons by [awesome glyphicons](http://glyphicons.com/).

## Usage

To use it in your page you just need to load the fastchat widget from the server:

    <script src="http://localhost:9090/fastchat.js" type="text/javascript"></script>

And them initialize it:
    
    <script type="text/javascript">
        fastchat();
    </script>

You can also pass in an object of config, all options below:

    <script type="text/javascript">
        fastchat({ 
                   server: "http://your.fastchat.server.com",
                   room: "A room to join",
                   user: "Username",
                   open: false
                });
    </script>

The "server" option you can define your own server (see below). Defaults to my own server.

With the "room" option you can define wich room you will join. Defaults to current domain.

On the "user" option you can define a user to use on the room. Defaults to "anonymous", useful it is to roll your own getting it dynamic on your application.

The "open" option defines if the chat window will start opened or closed. Defaults to false (closed).

## Features

- Small widget so it loads fast (no dependency, not even jquery).
- Simple websocket protocol, so it has low load and can keep many clients connected.
- Can customize using just css (look at default css).
- Wrapped in it self so it doesn't get in your way.
- You can roll your own using your server to keep it closer.
- Allow interop with your custom system using it's simple protocol.
- Small lived history when user come backs.
- A chat window per user.
- Just works!
    
## Roll your own

You can download the current [server of the fastchat](http://github.com/downloads/diogok/FastChat/fastchat-1.0beta1-standalone.jar) to run on your server, all you need is Java 7 and [redis](http://github.com/antirez/redis).

To run it just invoke the jar passing the port to bind:

    $ java -jar -DPORT=9090 fastchat-1.0beta1-standalone.jar

It will bind to all addresses available on the machine. The "chat" websocket interface is at "http://localhost:9090/chat" and the widget is at "http://localhost/fastchat.js". A test page is available at "http://localhost:9090/index.html".

## TODO

- Stop pooling online users
- Automate interface tests
- Allow redis config on server
- Add single big widget

