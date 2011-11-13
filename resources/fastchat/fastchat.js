var fastchat = (function() {
    var chat = {
        ws: null,
        loaded: false,
        connected: false,
        windows: {},
        elements: {
            users: null,
            hide: null
        },
        opts: {
            server: "67.23.230.58:8081",
            room: location.host,
            user: "anonymous",
            open: false 
        },
        send: function(user) {
            var text = "@"+ user+" "+chat.windows[user].input.value.trim();
            var msg  = {type:"message", message: text};
            if(text.length >= 1) chat.ws.send(JSON.stringify(msg));
            chat.windows[user].input.value = "";
        },
        reorder: function() {
            var c = 1;
            for(var user in chat.windows) {
                if(chat.windows[user].attached) {
                    chat.windows[user].root.style.marginRight = ( 250*c ) + "px";
                    c++;
                }
            }
        },
        toggle: function(user) {
            if(chat.windows[user].open) {
                chat.windows[user].root.setAttribute("class",'x-fastchat-window x-fastchat-closed');
                chat.windows[user].hide.innerHTML = "-";
                chat.windows[user].open = false;
            } else {
                chat.windows[user].root.setAttribute("class",'x-fastchat-window');
                chat.windows[user].hide.innerHTML = "_";
                chat.windows[user].open = true;
            }
        },
        closeChat: function(user) {
            document.body.removeChild(chat.windows[user].root);
            chat.windows[user].attached = false;
            chat.reorder();
        },
        createChat: function(user) {
            var window = {open: true, attached: true};

            var root = document.createElement("div");
            root.setAttribute("class","x-fastchat-window");
            window.root = root;

            var top = document.createElement("div");
            top.innerHTML = user;
            top.setAttribute("class","x-fastchat-top");

            var close = document.createElement("button");
            close.setAttribute("class","x-fastchat-hide");
            close.addEventListener('click', function() {chat.closeChat(user);} ,false);
            close.innerHTML = "X";

            var hide = document.createElement("button");
            hide.setAttribute("class","x-fastchat-hide");
            hide.addEventListener('click', function() {chat.toggle(user);} ,false);
            hide.innerHTML = "_";
            window.hide = hide;

            var clear = document.createElement("button");
            clear.setAttribute("class","x-fastchat-hide");
            clear.addEventListener('click', function() {chat.clear(user);} ,false);
            clear.innerHTML = "Clear";

            var ul = document.createElement("ul") ;
            ul.setAttribute("class","x-fastchat-chat");
            window.messages = ul;

            var input = document.createElement("input");
            input.onkeyup = function(e) {
                if(e.keyCode == 13) chat.send(user);
            }
            window.input = input;

            top.appendChild(close);
            top.appendChild(hide);
            top.appendChild(clear);
            root.appendChild(top);
            root.appendChild(ul);
            root.appendChild(input);
            document.body.appendChild(root);
            return window;
        },
        openChat: function(user){
            if(typeof chat.windows[user] != "object")  chat.windows[user] = chat.createChat(user);
            if(!chat.windows[user].attached) {
                document.body.appendChild(chat.windows[user].root);
                chat.windows[user].attached = true;
            }
            if(!chat.windows[user].open) chat.toggle(user);
            chat.reorder();
        },
        showMessage: function(user,msg) {
            var line = document.createElement("li");
            line.innerHTML = "<span class='x-fastchat-from'>"+msg.from+"</span>: "+ msg.message.substring(msg.to.length +2);
            if(typeof chat.windows[user] != "object") chat.openChat(user);
            chat.windows[user].messages.appendChild(line);
            chat.scroll(user);
        },
        clear: function(user) {
            chat.windows[user].messages.innerHTML = "";
            chat.ws.send(JSON.stringify({type:"command", command: "clear", from: user}));
        },
        scroll: function(user){
            var ul = chat.windows[user].messages;
            var listHeight = 0;
            for(var i =0 ; i<ul.childNodes.length;i++) {
                listHeight += ul.childNodes[i].clientHeight;
            }
            var ulHeight = ul.clientHeight;
            if(listHeight >= ulHeight) {
                if(ul.scrollTop > (listHeight - ulHeight - 32)) {
                    ul.scrollTop = ulHeight + 50;
                }
            }
        },
        onMessage: function(e) {
            if(e.data == "ok" || e.data == "bye") {
                if(e.data == "ok" && !chat.connected) {
                    chat.onlineUsers();
                    setInterval(chat.onlineUsers,5000);
                    chat.connected = true;
                }
                return;
            }            
            var msg = JSON.parse(e.data);
            if(msg.type == "message" || msg.type == "private") {
                if(msg.from == chat.opts.user) {
                    chat.showMessage(msg.to,msg);
                } else  {
                    chat.showMessage(msg.from,msg);
                }
            } else if(msg.type == "users") {
                chat.onlineUsers(msg);
            }
        },
        onOpen: function(e) {
            chat.ws.send(JSON.stringify({type: "connect", user: chat.opts.user, room: chat.opts.room}));
        },
        onClose: function(e) {
            setTimeout(chat.connect,5000);
        },
        onlineUsers: function(msg) {
            if(typeof msg == "object") {
                chat.elements.users.innerHTML = "";
                var line = document.createElement("li");
                line.innerHTML = "Online";
                chat.elements.users.appendChild(line);
                if(msg.users != null) {
                    for(var i =0;i<msg.users.length;i++) {
                        var line = document.createElement("li");
                        line.innerHTML = msg.users[i];
                        chat.elements.users.appendChild(line);
                        line.addEventListener("click",function(e){
                            chat.openChat(e.target.innerHTML);
                        }, false);
                    }
                }
            } else {
                chat.ws.send(JSON.stringify({type:"command", command: "users"}));
            }
        },
        connect: function() {
            var ws ;
            if(typeof MozWebSocket == "function") {
                ws = new MozWebSocket("ws://"+ chat.opts.server +"/chat");
            } else if (typeof WebSocket == "function") {
                ws = new WebSocket("ws://"+ chat.opts.server +"/chat");
            }
            ws.onopen = chat.onOpen;
            ws.onmessage = chat.onMessage;
            ws.onclose = chat.onClose;
            chat.ws = ws;
        },
        toggleChat: function() {
            if(!chat.opts.open) {
                chat.elements.root.setAttribute("class",'x-fastchat-closed');
                chat.elements.hide.innerHTML = "Open";
                chat.opts.open = true;
            } else {
                chat.elements.root.removeAttribute("class");
                chat.elements.hide.innerHTML = "Close";
                chat.opts.open = false;
            }
        },
        init: function() {
            if(chat.loaded) return;

            var root = document.createElement("div");
            root.setAttribute("id","x-fastchat");
            chat.elements.root = root;

            var top = document.createElement("div");
            top.setAttribute("class","x-fastchat-top");

            var hide = document.createElement("button");
            hide.setAttribute("class","x-fastchat-hide");
            hide.addEventListener('click', chat.toggleChat,false);
            chat.elements.hide = hide;

            var ul = document.createElement("ul");
            ul.setAttribute("id","x-fastchat-users");
            chat.elements.users = ul;

            top.appendChild(hide);
            root.appendChild(top);
            root.appendChild(ul);
            document.body.appendChild(root);

            chat.connect();
            chat.toggleChat();
            chat.loaded = true;
        }
    }

    return function(opts) {
        for(var k in opts) {
            chat.opts[k] = opts[k];
        }

        var link = document.createElement("link");
        link.setAttribute("rel","stylesheet");
        link.setAttribute("href","http://"+ chat.opts.server +"/fastchat.css");
        document.head.appendChild(link);

        chat.init();
    }

})();

