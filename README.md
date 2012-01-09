# FastChat

FastChat is a simple and concise chat widget for your website or web application.

[Take a look!](http://67.23.230.58:8081/index.html)

## Usage

To use it in your page you just need to load the fastchat widget from the server:

    <script src="http://67.23.230.58:8081/fastchat.js" type="text/javascript"></script>

And them initialize it:
    
    <script type="text/javascript">
        fastchat();
    </script>

You can also pass in an object of config, all options below:

    <script type="text/javascript">
        fastchat({ server: "http://your.fastchat.server.com",
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

- Small widget so it loads fast (no dependency).
- Simple websocket protocol, so it has low load and can keep many clients connected.
- Can customize using just css (look at default css).
- Wrapped in it self so it doesn't get in your way.
- You can roll your own using your server to keep it closer.
- Allow interop with your custom system using it's simple protocol.
- Small lived history when user come backs.
- A chat window per user.
- Just works!
    
## Roll your own

You can download the current [server of the fastchat](http://github.com/downloads/diogok/FastChat/fastchat-0.2-standalone.jar) to run on your server, all you need is Java 6 or greater(I guess) and [redis](http://github.com/antirez/redis).

To run it just invoke the jar passing the port to bind:

    $ java -jar fastchat-0.2-standalone.jar 8081

It will bind to all addresses available on the machine. The "chat" websocket interface is at "http://localhost:8081/chat" and the widget is at "http://localhost/fastchat.js". A test page is available at "http://localhost:8081/index.html".

## TODO

- Refactor CSS
- Write way more tests
- Automate interface tests
- Allow redis config on server

