(ns fastchat.core
 (:require [clj-redis.client :as redis]) 
 (:use [clojure.data.json :only (json-str read-json)]))

    (defn channels [] 
     "Create the pubsub channels"
     (redis/init)) 

    (defn online-users [db room]
     "Return users online on room"
      (redis/smembers db (str "users:" room)))

    (defn add-to-history 
      "Add msg to history"
      ([db room msg] 
       (dorun (for [user (online-users db room)] 
        (add-to-history db room (msg :from) user msg))))
      ([db room from to msg]
       (let [history (str "history:" room ":" to ":" from)]
        (redis/zadd db history (msg :timestamp) (json-str msg)) 
        (redis/expire db history (* 7 24 60 60)))))

    (defn get-history
      "Get msg history"
      ([db room to]
       (let [histories (redis/keys db (str "history:" room ":" to ":*"))
             destiny   (str "history:" room ":" to)]
        (if-not (nil? histories)
          (redis/zunionstore db destiny histories)) 
        (map read-json (redis/zrange db destiny 0 50))))
      ([db room from to]
       (map read-json (redis/zrange db 
                       (str "history:" room ":" to ":" from) 0 50 )))) 

    (defn handler [user fun ch message]
      "Handle msgs on channel"
      (let [msg (read-json message)]
        (if (= (msg :type) "leave") (.unsubscribe ch)
          (fun msg)))) 

    (defn enter [db room user fun]
     "User join room on channels, fun will be called on new messages"
      (let [ch [(str "channel:" room) (str "channel:" room ":" user)]]
        (future (redis/subscribe (redis/init) ch (partial handler user fun))))
      (redis/sadd db (str "users:" room) user)
      (dorun (for [msg (get-history db room user)] (fun msg))))

    (defn leave [db room user]
     "Makes user leave room"
     (redis/publish db (str "channel:" room ":" user) (json-str {:type "leave"})) 
     (redis/srem db (str "users:" room) user))

    (defn do-post [db room msg]
      "Raw post"
      (redis/publish db  (str "channel:" room) (json-str msg))) 

    (defn post [db room user msg]
     "Post msg from user at room in channels"
      (let [message {:from user
                     :type "message"
                     :message msg
                     :timestamp (int (/ ( System/currentTimeMillis) 1000))}] 
      (if (.startsWith msg "@")
       (let [user2 (.substring msg 1 (.indexOf msg " "))]
        (do-post db (str room ":" user) (assoc message :type "private")) 
        (add-to-history db room user user2 message)
        (do-post db (str room ":" user2) (assoc message :type "private"))
        (add-to-history db room user2 user message))
       (do (do-post db room message) (add-to-history db room message)))))

