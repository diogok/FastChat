(ns fastchat.server
  (:use [compojure.core :only [defroutes GET]]
        [compojure.route :only [resources]]
        [compojure.handler :only [site]]
        [ring.util.response :only [redirect]]
        [clojure.data.json :only (read-str write-str)]
        [org.httpkit.server :only [run-server send! with-channel on-close on-receive]])
  (:require [fastchat.core :as chat])
  (:gen-class))

(def users (ref {}))

(defmulti message (fn [_ msg]  (keyword (:type msg) )) :default :echo)

(defmethod message :connect [channel message]
  (let [user (:user message)
        room (:room message)]
    (dosync 
      (commute users 
        (fn [users] (assoc users channel {:user user :room room}))))
    (chat/enter room user
      (fn [msg] (send! channel (write-str msg))))
    (send! channel (write-str {:type "welcome"} ))))

(defmethod message :message [channel message]
 (let [user (get-in @users [channel :user])
       room (get-in @users [channel :room])] 
  (chat/post room user (:message message )))) 

(defmethod message :users [channel message]
 (let [room (get-in @users [channel :room])] 
   (send! channel (write-str {:type "users" :users (chat/online-users room)} ))))

(defmethod message :clear [channel message]
 (let [user (get-in @users [channel :user])
       room (get-in @users [channel :room])]
   (if-not (nil? (message :from))
     (chat/clear-history room (message :from) user) 
     (chat/clear-history room user))))

(defmethod message :leave [channel message]
 (let [user (get-in @users [channel :user])
       room (get-in @users [channel :room])]
   (chat/leave room user)
   (send! channel (write-str {:type "bye"}))))

(defmethod message :echo [channel message]
  (println "echo: " message)
  (send! channel (write-str message)))

(defn handler [request]
  (with-channel request channel
    (on-close channel (fn [_] (message channel {:type "leave"})))
    (on-receive channel (fn [data] (message channel (read-str data :key-fn keyword))))))

(defroutes app 
  (GET "/" [] (redirect "index.html"))
  (GET "/chat" [] handler)
  (resources "/"))

(defn -main [& args] 
  (let [handler (site #'app)
        port    (or (System/getProperty "PORT") (System/getenv "PORT") "9090")]
    (println "running on" port)
    (chat/connect)
    (run-server handler {:port (Integer/parseInt port)})))

