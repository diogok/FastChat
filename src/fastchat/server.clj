(ns fastchat.server
  (:use [ring.middleware.reload :only [wrap-reload]]
        [compojure.core :only [defroutes GET]]
        [compojure.route :only [resources]]
        [compojure.handler :only [site]]
        [ring.util.response :only [redirect]]
        [clojure.data.json :only (read-str write-str)]
        [org.httpkit.server :only [run-server send! with-channel on-close on-receive]])
  (:require [fastchat.core :as chat]))

(def users (ref {}))

(defmulti message (fn [_ msg] (:type msg)))

(defmethod message :connect [channel message]
  (let [user (:user message)
        room (:room message)]
    (dosync 
      (commute users 
        (fn [users] (assoc users channel {:user user :room room}))))
    (chat/enter room user
      (fn [msg] (send! channel (write-str msg))))
    (send! channel {:type "welcome"})))

(defmethod message :message [channel message]
 (let [user (get-in @users [channel :user])
       room (get-in @users [channel :room])] 
  (chat/post room user message))) 

(defmethod message :users [channel message]
 (let [room (get-in @users [channel :room])] 
   (send! channel {:type "users" :users (chat/online-users room)})))

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

(defn handler [request]
  (with-channel request channel
    (on-close channel (fn [_] (message channel {:type "leave"})))
    (on-receive channel (fn [data] (message channel (read-str data :key-fn keyword))))))

(defroutes app 
  (GET "/" [] (redirect "index.html"))
  (GET "/chat" [] handler)
  (resources "/"))

(defn -main [& args] 
  (let [handler (wrap-reload (site #'app))
        port    (or (System/getenv "PORT") "9090")]
    (println "running on" port)
    (run-server handler {:port (Integer/parseInt port)})))

