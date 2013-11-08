(ns fastchat.core
 (:require [taoensso.carmine :as redis])
 (:use [clojure.data.json :only (read-str write-str)]))

(def conn (atom {:pool {} :spec {:host "127.0.0.1" :port 6379} :prefix "fastchat"}))
(def listeners (ref {}))

(defn connect
  "To start the chat we need to connect to proper redis database"
  ([host] (connect host 6379))
  ([host port] (connect host port "fastchat"))
  ([host port prefix] (swap! conn (fn [_] {:pool {} :spec {:host host :port port} :prefix prefix}))))

(defmacro db 
  "Run the commands in current connection"
  [& body] 
  `(redis/wcar conn ~@body))

(defn read-json 
  "Create data from a json string"
  [json] (read-str json :key-fn keyword))

(defn write-json
  "Create a json string from data"
  [obj] (write-str obj))

(defn mkey
  "Creates the proper redis key, including prefix"
  [& args] (apply str (:prefix @conn) ":" (interpose ":" args)))

(defn clear
  ""
  [] (let [keys (db (redis/keys (mkey "*")))]
      (dorun (for[key keys]
       (db (redis/del key))))))

(defn online-users [room]
 "Return users online on room"
  (db (redis/smembers (mkey "users" room))))

(defn add-to-history 
  "Add msg to history"
  ([room msg] 
   (dorun 
     (for [user (online-users room)] 
      (add-to-history room (msg :from) user msg))))
  ([room from to msg]
   (let [history (mkey "history" room to from)]
    (redis/zadd history (msg :timestamp) (write-json msg)))))

(defn get-history
  "Get msg history"
  ([room to]
   (let [histories (db (redis/keys (mkey "history" room to "*")))
         destiny   (mkey "history" room to)]
    (if (empty? histories)
      (db (redis/del destiny))
      (db (redis/zunionstore* destiny histories)))
    (map read-json (db (redis/zrange destiny 0 50)))))
  ([room from to]
   (map read-json (db (redis/zrange (mkey "history" room to from) 0 50)))))

(defn clear-history
  "Clear chat history"
  ([room to]
   (let [histories (db (redis/keys (mkey "history" room to "*")))]
     (if-not (nil? histories) (db (redis/del histories)))))
  ([room from to] 
   (db (redis/del [(mkey "history:" room to from)]))))

(defn handler
  ""
  [fun]
  (fn [[type channel message]]
   (if (= type "message")
    (let [msg (read-json message)]
     (fun msg)))))

(defn- listen
  ""
  [room user fun]
   (redis/with-new-pubsub-listener (:spec @conn)
     {(mkey "channel" room) (handler fun)
      (mkey "channel" room user) (handler fun)}
     (redis/subscribe (mkey "channel" room))
     (redis/subscribe (mkey "channel" room user))))

(defn enter
 "The user enter a room, and start the chat"
  [room user fun]
  (let [listener (listen room user fun)]
   (dosync
    (commute listeners (fn [listeners] (assoc listeners (str room user) listener))))
    (db (redis/sadd (mkey "users" room) user))
    (dorun (for [msg (db (get-history room user))] (fun msg)))))

(defn leave [room user]
 "Makes user leave room"
 (redis/with-open-listener (get @listeners (str room user))
   (redis/unsubscribe (mkey "channel" room))
   (redis/unsubscribe (mkey "channel" room user)))
 (redis/close-listener (get @listeners (str room user )))
 (db (redis/srem (mkey "users" room) user))
 (dosync 
   (commute listeners (fn [listeners] (dissoc listeners (str room user))))))

(defn do-post
  "Raw post"
  ([room msg] (redis/publish (mkey "channel" room) (write-json msg)))
  ([room user msg] (redis/publish (mkey "channel" room user) (write-json msg))))

(defn post 
 "Post msg from user at room in channels"
 [room user msg]
  (let [message {:from user
                 :type "message"
                 :message msg
                 :timestamp (int (/ ( System/currentTimeMillis) 1000))}] 
  (if (.startsWith msg "@")
   (let [user2   (.substring msg 1 (.indexOf msg " "))
         message (assoc message :type "private" :to user2)]
    (db
      (do-post room user message) 
      (add-to-history room user user2 message)
      (do-post room user2 message)
      (add-to-history room user2 user message)))
    (db
      (do-post room message)
      (add-to-history room message)))))

