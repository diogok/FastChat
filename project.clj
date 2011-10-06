(defproject fastchat "0.1"
  :main fastchat.server
  :description "Websocket fastchat"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.1"] 
                 [org.webbitserver/webbit "0.2.1"]]
  :jvm-opts ["-Xmx512m"
             "-server"
             "-XX:+UseConcMarkSweepGC"] 
  )
