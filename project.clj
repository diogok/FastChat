(defproject fastchat "1.0beta1"
  :main fastchat.server
  :description "Websocket fastchat"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.3"] 
                 [com.taoensso/carmine "2.3.1"]
                 [http-kit "2.1.13"]
                 [ring/ring-core "1.1.8"]
                 [compojure "1.1.6"]]
  :jvm-opts ["-Xmx512m" "-server" "-XX:+UseConcMarkSweepGC"]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.1.1"]]}})
