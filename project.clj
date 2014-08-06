(defproject fastchat "2.0.0"
  :main fastchat.server
  :description "Websocket fast chat and messenger"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"] 
                 [com.taoensso/carmine "2.6.2"]
                 [http-kit "2.1.13"]
                 [ring/ring-core "1.3.0"]
                 [compojure "1.1.6"]
                 [javax.servlet/servlet-api "2.5"]]
  :jvm-opts ["-Xmx512m" "-server"]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
