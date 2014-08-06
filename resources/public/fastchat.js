var fastchat = (function() {

    var chat = {
        ws: null,
        loaded: false,
        connected: false,
        windows: {},
        elements: {},
        opts: {
            server: location.host,
            room: location.host,
            user: "anonymous",
            open: false 
        }
    };

    chat.reorder = function() {
        var c = 1;
        for(var user in chat.windows) {
            if(chat.windows[user].attached) {
                chat.windows[user].root.style.marginRight = ( 250*c ) + "px";
                c++;
            }
        }
    };

    chat.send = function(user) {
        var text = "@"+ user+" "+chat.windows[user].input.value.trim();
        var msg  = {type:"message", message:text};
        if(text.length >= 1) chat.ws.send(JSON.stringify(msg));
        chat.windows[user].input.value = "";
    };

    chat.toggle = function(user) {
        if(chat.windows[user].open) {
            chat.windows[user].root.setAttribute("class",'x-fastchat-window x-fastchat-closed');
            chat.windows[user].hide.innerHTML = "<i class='icon-plus-sign'></i>";
            chat.windows[user].open = false;
        } else {
            chat.windows[user].root.setAttribute("class",'x-fastchat-window');
            chat.windows[user].hide.innerHTML = "<i class='icon-minus-sign'></i>";
            chat.windows[user].open = true;
        }
    };

    chat.closeChat = function(user) {
        document.body.removeChild(chat.windows[user].root);
        chat.windows[user].attached = false;
        chat.reorder();
    };

    chat.createChat = function(user) {
        var window = {open: true, attached: true};

        var root = document.createElement("div");
        root.setAttribute("class","x-fastchat-window");
        window.root = root;

        var top = document.createElement("div");
        top.innerHTML = user;
        top.setAttribute("class","x-fastchat-top");

        var close = document.createElement("button");
        close.setAttribute("class","x-fastchat-close");
        close.addEventListener('click', function() {chat.closeChat(user);} ,false);
        close.innerHTML = "<i class='icon-remove'></i>";

        var hide = document.createElement("button");
        hide.setAttribute("class","x-fastchat-hide");
        hide.addEventListener('click', function() {chat.toggle(user);} ,false);
        hide.innerHTML = "<i class='icon-minus-sign'></i>";
        window.hide = hide;

        var clear = document.createElement("button");
        clear.setAttribute("class","x-fastchat-clear");
        clear.addEventListener('click', function() {chat.clear(user);} ,false);
        clear.innerHTML = "<i class='icon-refresh'></i>";

        var ul = document.createElement("ul") ;
        ul.setAttribute("class","x-fastchat-chat");
        window.messages = ul;

        var input = document.createElement("input");
        input.onkeyup = function(e) {
            if(e.keyCode == 13) chat.send(user);
            else if(e.keyCode == 27) chat.closeChat(user);
        }
        input.setAttribute("placeholder","type here...")
        window.input = input;

        top.appendChild(close);
        top.appendChild(hide);
        top.appendChild(clear);
        root.appendChild(top);
        root.appendChild(ul);
        root.appendChild(input);
        document.body.appendChild(root);
        return window;
    };

    chat.openChat = function(user){
        if(typeof chat.windows[user] != "object")  chat.windows[user] = chat.createChat(user);
        if(!chat.windows[user].attached) {
            document.body.appendChild(chat.windows[user].root);
            chat.windows[user].attached = true;
        }
        if(!chat.windows[user].open) chat.toggle(user);
        chat.reorder();
    };

    chat.showMessage = function(user,msg) {
        var line = document.createElement("li");
        line.innerHTML = "<span class='x-fastchat-from'>"+msg.from+"</span>: "+ msg.message.substring(msg.to.length +2);
        if(typeof chat.windows[user] != "object") chat.openChat(user);
        chat.windows[user].messages.appendChild(line);
        chat.scroll(user);
    };

    chat.clear = function(user) {
        chat.windows[user].messages.innerHTML = "";
        chat.ws.send(JSON.stringify({type:"clear", from: user}));
    };

    chat.scroll = function(user){
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
    };

    chat.onMessage= function(e) {
        var msg = JSON.parse(e.data);
        console.log(msg);
        if(msg.type == 'welcome') {
            if(!chat.connected) {
                chat.onlineUsers();
                setInterval(chat.onlineUsers,5000);
                chat.connected = true;
            }
        } else if(msg.type == 'bye') {
        } else if(msg.type == "message" || msg.type == "private") {
            if(msg.from == chat.opts.user) {
                chat.showMessage(msg.to,msg);
            } else {
                chat.showMessage(msg.from,msg);
            }
        } else if(msg.type == "users") {
            chat.onlineUsers(msg);
        }
    };

    chat.onOpen = function(e) {
        chat.ws.send(JSON.stringify({type: "connect", user: chat.opts.user, room: chat.opts.room}));
    };

    chat.onClose = function(e) {
        setTimeout(chat.connect,5000);
    };

    chat.onlineUsers = function(msg) {
        if(typeof msg == "object") {
            chat.elements.users.innerHTML = "";
            if(msg.users != null) {
                for(var i =0;i<msg.users.length;i++) {
                    if(msg.users[i] == chat.opts.user) continue;
                    var line = document.createElement("li");
                    line.innerHTML = "<i class='icon-user' rel='"+msg.users[i]+"'></i> "+msg.users[i];
                    line.setAttribute("rel",msg.users[i]);
                    chat.elements.users.appendChild(line);
                    line.addEventListener("click",function(e){
                        chat.openChat(e.target.getAttribute("rel"));
                    }, false);
                }
            }
        } else {
            chat.ws.send(JSON.stringify({type:"users"}));
        }
    };

    chat.connect = function() {
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
    };

    chat.toggleChat = function() {
        if(!chat.opts.open) {
            chat.elements.root.setAttribute("class",'x-fastchat-closed');
            chat.elements.hide.innerHTML = "<i class='icon-plus-sign'></i>";
            chat.opts.open = true;
        } else {
            chat.elements.root.removeAttribute("class");
            chat.elements.hide.innerHTML = "<i class='icon-minus-sign'></i>";
            chat.opts.open = false;
        }
    };

    chat.init = function() {
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
    };

    return function(opts) {
        for(var k in opts) {
            chat.opts[k] = opts[k];
        }

        var link = document.createElement("link");
        link.setAttribute("rel","stylesheet");
        link.setAttribute("href","http://"+ chat.opts.server +"/fastchat.css");
        document.head.appendChild(link);

        chat.init();
    };

})();

