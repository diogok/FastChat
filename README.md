# FastChat

[![Build Status](https://travis-ci.org/diogok/FastChat.png)](https://travis-ci.org/diogok/FastChat)

FastChat is a simple, fast and concise chat and messenger widget for your website or web application.

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

### On Docker

Run standalone on ephemeral redis:

    $ docker run -d -P -t diogok/fastchat

Or, have a redis server running and link to fastchat:

    $ docker run -d -P --name redis -t redis
    $ docker run -d -P --name fastchat --link redis:redis -t diogok/fastchat

Or, use a custom connection to redis:

    $ docker run -d -P --name fastchat -e REDIS_HOST="localhost" -e REDIS_PORT="6379" -e REDIS_PREFIX="instance1" -t diogok/fastchat

### Standalone mode

You can download the current [release of the fastchat](http://github.com/diogok/FastChat/releases/latest) to run on your server, all you need is Java 7 and [redis](http://github.com/antirez/redis).

To run it just invoke the jar:

    $ java -jar fastchat-2.0.0-standalone.jar

You can set the port and connections to redis:

    $ java -jar -DPORT=9090 -DREDIS_HOST=localhost -DREDIS_PORT=6379 -DREDIS_PREFIX=fastchat1 fastchat-2.0.0-standalone.jar


It will bind to all addresses available on the machine. The "chat" websocket interface is at "http://localhost:9090/chat" and the widget is at "http://localhost/fastchat.js". A test page is available at "http://localhost:9090/index.html".

## Development

You can use vagrant or setup java 7, lein and redis by your self.

Tasks:

    lein run # run the server, auto reload parts of the app
    lein midje # run the tests
    lein midje :autotest # keep the tests runing
    lein uberjar # generate the deploy artefact

## TODO

- Stop pooling online users
- Automate interface tests
- Add single big widget option

## License

Distribuited under the Eclipse Public License.

