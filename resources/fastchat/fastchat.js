var fastchat = (function() {
    var chat = {
        ws: null,
        loaded: false,
        connected: false,
        elements: {
            root: null,
            input: null,
            close: null,
            users: null,
            messages: null
        },
        opts: {
            server: "67.23.230.58:8081",
            room: location.host,
            user: "anonymous",
            open: false 
        },
        send: function() {
            var text = chat.elements.input.value.trim();
            var msg  = {type:"message", message: text};
            if(text.length >= 1) chat.ws.send(JSON.stringify(msg));
            chat.elements.input.value = "";
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
                var line = document.createElement("li");
                line.innerHTML = "<span class='x-fastchat-from'>"+msg.from+"</span>: "+ msg.message;
                chat.elements.messages.appendChild(line);
                chat.scroll();
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
                            chat.elements.input.value="@"+ e.target.innerHTML+" ";
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
        toggle: function() {
            if(!chat.opts.open) {
                chat.elements.root.setAttribute("class",'x-fastchat-closed');
                chat.elements.close.innerHTML = "Chat /\\";
                chat.opts.open = true;
            } else {
                chat.elements.root.removeAttribute("class");
                chat.elements.close.innerHTML = "\\/";
                chat.elements.messages.scrollTop = chat.elements.messages.clientHeight + 50;
                chat.opts.open = false;
            }
        },
        scroll: function(){
            var ul = chat.elements.messages;
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
        init: function() {
            if(chat.loaded) return;

            var root = document.createElement("div");
            root.setAttribute("id","x-fastchat");
            chat.elements.root = root;

            var top = document.createElement("div");
            top.setAttribute("id","x-fastchat-top");

            var button = document.createElement("button");
            button.addEventListener('click', chat.toggle,false);
            chat.elements.close = button;

            var ul = document.createElement("ul");
            ul.setAttribute("id","x-fastchat-chat");
            chat.elements.messages = ul;

            var ul2 = document.createElement("ul");
            ul2.setAttribute("id","x-fastchat-users");
            chat.elements.users = ul2;

            var input = document.createElement("input");
            input.setAttribute("type","textfield");
            input.onkeypress = function(e) { if(e.keyCode == 13) chat.send(); }
            chat.elements.input = input;

            top.appendChild(button);
            root.appendChild(top);
            root.appendChild(ul);
            root.appendChild(ul2);
            root.appendChild(input);
            document.body.appendChild(root);

            chat.connect();
            chat.toggle();
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

