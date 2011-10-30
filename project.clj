(defproject fastchat "0.1"
  :main fastchat.server
  :description "Websocket fastchat"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.1"] 
                 [org.webbitserver/webbit "0.2.1"]
                 [clj-redis "0.0.12"]]
  :dev-dependencies [[com.stuartsierra/lazytest "2.0.0-SNAPSHOT"]] 
  :repositories {"stuartsierra-snapshots" "http://stuartsierra.com/m2snapshots"}
  :jvm-opts ["-Xmx512m" "-server" "-XX:+UseConcMarkSweepGC"])
