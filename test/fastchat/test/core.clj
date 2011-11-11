(ns fastchat.test.core
 (:require [clj-redis.client :as redis]) 
 (:use [fastchat.core])
 (:use [lazytest.deftest]))

    (deftest corecore
     (let [channels (channels)
           room "123"
           user0 "diogok"
           msgs0 (atom [])
           user1 "gislene"
           msgs1 (atom [])
           user2 "girlaine"
           msgs2 (atom []) ]
      (redis/flush-all  channels ) 
      (enter channels room user0 (fn [msg] (swap! msgs0 conj (msg :message))))
      (enter channels room user1 (fn [msg] (swap! msgs1 conj (msg :message))))
      (enter channels room user2 (fn [msg] (swap! msgs2 conj (msg :message))))
      (Thread/sleep 500) 
      (post channels room user0 "hello") 
      (Thread/sleep 500) 
      (is (= ["hello"] @msgs1) ) 
      (is (= ["hello"] @msgs0 ) ) 
      (post channels room user1 "@diogok hello you!") 
      (Thread/sleep 1000) 
      (is (= ["hello" "@diogok hello you!"] @msgs0 ) ) 
      (is (= ["hello" "@diogok hello you!"] @msgs1 ) ) 
      (is (= ["hello"] @msgs2)) 
      (leave channels room user1) 
      (post channels room user0 "Yoh!") 
      (Thread/sleep 500) 
      (is (= ["hello" "@diogok hello you!" "Yoh!"] @msgs0)) 
      (is (= ["hello" "@diogok hello you!"] @msgs1)) 
      (is (= ["hello" "Yoh!"] @msgs2 ))
      (is (= (list "girlaine" "diogok") (online-users channels room)))
      (is (= (list "hello" "@diogok hello you!" "Yoh!") (map :message (get-history channels room user0)))) 
      (is (= (list "hello" "@diogok hello you!") (map :message (get-history channels room user0 user1)))) 
      (is (= (list "hello" "Yoh!") (map :message (get-history channels room user2)))) 
      (is (= (list ) (map :message (get-history channels room user1 user2)))) 
      (clear-history channels room user2) 
      (is (= (list ) (map :message (get-history channels room user2)))) 
      (clear-history channels room user1 user0) 
      (is (= (list "hello" "Yoh!") (map :message (get-history channels room user0)))) 
       )) 

