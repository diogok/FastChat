(ns fastchat.core
 (:use fastchat.pubsub))

    (defn channels [] 
     "Create the pubsub channels"
     {:channels (mkchannels) :rooms (atom {})}) 

    (defn enter [channels room user fun]
     "User join room on channels, fun will be called on new messages"
     (if (nil? (get @(channels :rooms) room))
      (swap! (channels :rooms) assoc room (atom {}))) 
     (swap! (get @(channels :rooms) room) 
      assoc user {:fun fun :user user :room room}) 
     (listen! (channels :channels) room fun) 
     (listen! (channels :channels) (str room ":" user) fun)) 

    (defn do-post [channels room msg]
     "Raw post"
     (send! (channels :channels) room msg)) 

    (defn post [channels room user msg]
     "Post msg from user at room in channels"
      (let [message {:from user
                     :type "message"
                     :message msg
                     :timestamp (/ ( System/currentTimeMillis) 1000)}] 
      (if (.startsWith msg "@")
       (let [user2 (.substring msg 1 (.indexOf msg " "))]
        (do-post channels (str room ":" user )
            (assoc message :type "private")) 
        (do-post channels (str room ":" user2) 
            (assoc message :type "private")))
       (do-post channels  room message))))

    (defn online-users [channels room]
     "Return users online on room"
     (keys @(get @(channels :rooms) room))) 

    (defn leave [channels room user]
     "Makes user leave room"
     (let [fun (get-in @(get @(channels :rooms) room) [user :fun])] 
      (swap! (get @(channels :rooms) room) dissoc user) 
      (leave! (channels :channels) room fun) 
      (leave! (channels :channels) (str room ":" user) fun)))

