(ns fastchat.core
 (:require [clj-redis.client :as redis]) 
 (:use [clojure.data.json :only (json-str read-json)]))

    (defn channels [] 
     "Create the pubsub channels"
     {}) 

    (defn handler [user fun ch message]
      "Handle msgs on channel"
      (let [msg (read-json message)]
        (if (= (msg :type) "leave") (.unsubscribe ch)
          (fun msg)))) 

    (defn enter [db room user fun]
     "User join room on channels, fun will be called on new messages"
      (let [ch [(str "channel:" room) (str "channel:" room ":" user)]]
        (future (redis/subscribe (redis/init db) ch (partial handler user fun))))
      (redis/sadd (redis/init db) (str "users:" room) user))

    (defn do-post [db room msg]
      "Raw post"
      (redis/publish (redis/init db)  (str "channel:" room) (json-str msg))) 

    (defn post [db room user msg]
     "Post msg from user at room in channels"
      (let [message {:from user
                     :type "message"
                     :message msg
                     :timestamp (/ ( System/currentTimeMillis) 1000)}] 
      (if (.startsWith msg "@")
       (let [user2 (.substring msg 1 (.indexOf msg " "))]
        (do-post db (str room ":" user) (assoc message :type "private")) 
        (do-post db (str room ":" user2) (assoc message :type "private")))
       (do-post db room message))))

    (defn online-users [db room]
     "Return users online on room"
      (redis/smembers (redis/init db) (str "users:" room)))

    (defn leave [db room user]
     "Makes user leave room"
     (redis/publish (redis/init db) (str "channel:" room ":" user) (json-str {:type "leave"})) 
     (redis/srem (redis/init db) (str "users:" room) user))

