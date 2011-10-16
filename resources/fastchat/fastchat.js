var fastchat = (function() {
    var chat = {
        ws: null,
        root: null,
        loaded: false,
        connected: false,
        opts: {
            server: "67.23.230.58:8081",
            room: location.host,
            user: "anonymous",
            open: false 
        },
        send: function() {
            var field = chat.root.lastChild;
            var text = field.value.trim();
            var msg  = {type:"message", message: text};
            if(text.length >= 1) chat.ws.send(JSON.stringify(msg));
            field.value = "";
        },
        onMessage: function(e) {
            if(e.data == "ok" || e.data == "bye") return ;
            var msg = JSON.parse(e.data);
            if(msg.type == "message" || msg.type == "private") {
                var line = document.createElement("li");
                line.innerHTML = "<span class='x-fastchat-from'>"+msg.from+"</span>: "+ msg.message;
                chat.root.childNodes[1].appendChild(line);
                chat.scroll();
            } else if(msg.type == "users") {
                chat.onlineUsers(msg);
            }
            if(!chat.connected) {
                chat.onlineUsers();
                setInterval(chat.onlineUsers,5000);
                chat.connected = true;
            }
        },
        onOpen: function(e) {
            var line = document.createElement("li");
            line.innerHTML = "<span class='x-fastchat-from'>system</span>: ONLINE.";
            chat.root.childNodes[1].appendChild(line);
            chat.ws.send(JSON.stringify({type: "connect", user: chat.opts.user, room: chat.opts.room}));
        },
        onClose: function(e) {
            var line = document.createElement("li");
            line.innerHTML = "<span class='x-fastchat-from'>system</span>: OFFLINE.";
            chat.root.childNodes[1].appendChild(line);
            setTimeout(chat.connect,5000);
        },
        onlineUsers: function(msg) {
            if(typeof msg == "object") {
                chat.root.childNodes[2].innerHTML = "";
                var line = document.createElement("li");
                line.innerHTML = "Online";
                chat.root.childNodes[2].appendChild(line);
                for(var i =0;i<msg.users.length;i++) {
                    var line = document.createElement("li");
                    line.innerHTML = msg.users[i];
                    chat.root.childNodes[2].appendChild(line);
                }
            } else {
                chat.ws.send(JSON.stringify({type:"command", command: "users"}));
            }
        },
        connect: function() {
            var ws = new WebSocket("ws://"+ chat.opts.server +"/chat");
            ws.onopen = chat.onOpen;
            ws.onmessage = chat.onMessage;
            ws.onclose = chat.onClose;
            chat.ws = ws;
        },
        toggle: function() {
            if(!chat.opts.open) {
                chat.root.setAttribute("class",'x-fastchat-closed');
                chat.root.childNodes[0].childNodes[0].innerHTML = "Chat /\\";
                chat.opts.open = true;
            } else {
                chat.root.removeAttribute("class");
                chat.root.childNodes[0].childNodes[0].innerHTML = "\\/";
                chat.root.childNodes[1].scrollTop = chat.root.childNodes[1].clientHeight + 50;
                chat.opts.open = false;
            }
        },
        scroll: function(){
            var ul = chat.root.childNodes[1];
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
            root.innerHTML = "<div id='x-fastchat-top'><button>\\/</button></div><ul id='x-fastchat-chat'></ul><ul id='x-fastchat-users'></ul><input type='textfield' />";
            document.body.appendChild(root);
            chat.root = root;

            chat.root.lastChild.onkeypress = function(e) {
                if(e.keyCode == 13) chat.send();
            }

            root.childNodes[0].childNodes[0].addEventListener('click', chat.toggle,false);

            chat.connect();
            chat.toggle();
            chat.loaded = true;
        }
    }

    /*
    if(window.addEventListener) {
        window.addEventListener('DOMContentLoaded', chat.init,false);
    } else if(window.attachEvent) {
        window.attachEvent('onreadystatechange', chat.init);
    }
    */

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
