(ns fastchat.server
 (:import [org.webbitserver WebServer WebServers WebSocketHandler]
          [org.webbitserver.handler StaticFileHandler EmbeddedResourceHandler]) 
 (:require [fastchat.core :as chat]) 
 (:use [clojure.data.json :only (json-str read-json)]) 
 (:gen-class))

    (def mkchannels chat/channels) 

    (defn connect [channels users room user conn]
     "Connect user to room at channels using connection"
     (swap! users assoc conn {:user user :room room})
     (chat/enter channels room user (fn [msg] (.send conn (json-str msg))))
     (.send conn "ok"))

    (defn post [channels users conn message]
     "Post message from user to room at channels"
     (let [user (get-in @users [conn :user])
           room (get-in @users [conn :room])] 
      (chat/post channels room user message))) 

    (defn command [channels users conn message]
     "Ansewer a command"
     (if (= (message :command) "users")
     (let [user (get-in @users [conn :user])
           room (get-in @users [conn :room])] 
      (chat/do-post channels (str room ":" user)
       {:type "users" :users (chat/online-users channels room)}))))

    (defn leave [channels users conn]
     "User for conn leaves the room"
     (let [user (get-in @users [conn :user])
           room (get-in @users [conn :room])]
       (chat/leave channels room user)
       (.send conn "bye")))

    (defn message [channels users conn j]
     "Parse message j and delegates"
      (let [msg (read-json j)]
        (if (= (msg :type) "connect") 
         (connect channels users (msg :room) (msg :user) conn)) 
        (if (= (msg :type) "message")
         (post channels users conn (msg :message)))
        (if (= (msg :type) "command")
         (command channels users conn msg))))

    (defn handler [channels users] 
     "Create proper Websocket handler for channels"
       (proxy [WebSocketHandler] []
        (onOpen [c] nil)
        (onClose [c] (leave channels users c))
        (onMessage [c j] (message channels users c j))))

    (defn -main [& args]
     "Start websocket server"
     (let [server (WebServers/createWebServer (Integer/parseInt (first args)))]
      (.add server "/chat" (handler (mkchannels) (atom {}))) 
      (.add server (EmbeddedResourceHandler. "fastchat")) 
      (println (.getUri (.start server))) server))

