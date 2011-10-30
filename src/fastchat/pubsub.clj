(ns fastchat.pubsub)

    (defn mkchannels []
     "Create channels ref" 
     (ref {}))

    (defn notify! [listeners message] 
     "Notifies listeners of message"
     (dorun (for [listener listeners]
             (future (listener message))))) 

    (defn send! [channels channel message]
     "Send a message to channel at channels"
     (if (nil? (get @channels channel))
       (dosync (commute channels assoc channel (ref []))))
     (notify! @(get @channels channel) message))

    (defn listen! [channels channel listener]
     "Add a listener on channel of channels"
     (dosync 
      (if (nil? (get @channels channel))
       (commute channels assoc channel (ref [])))
      (commute (get @channels channel) conj listener))) 

    (defn leave! [channels channel listener]
     "Unregister listener from channel in channels"
     (if-not (nil? (get @channels channel))
      (dosync (commute (get @channels channel)
               (fn [chs] (filter #(not (= %1 listener)) chs)))))) 

