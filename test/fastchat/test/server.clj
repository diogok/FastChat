(ns fastchat.test.server
  (:use [fastchat.core :only [clear]]
        [fastchat.server :only [-main]]
        [clojure.data.json :only [read-str write-str]])
  (:use midje.sweet))

(fact "Serving FastChat"
  (let [stop (-main)]
    (clear)
    #_"???"
    (stop)))

