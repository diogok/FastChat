(ns fastchat.test.core
 (:use [fastchat.core])
 (:use [clojure.test]))

    (deftest corecore
     (let [channels (channels) , room "123"
           user0 "diogok"
           msgs0 (atom [])
           user1 "gislene"
           msgs1 (atom [])
           user2 "girlaine"
           msgs2 (atom []) ]
      (enter channels room user0
       (fn [msg] (swap! msgs0 conj (msg :message))))
      (enter channels room user1
       (fn [msg] (swap! msgs1 conj (msg :message))))
      (enter channels room user2
       (fn [msg] (swap! msgs2 conj (msg :message))))
      (post channels room user0 "hello") 
      (Thread/sleep 250) 
      (is (= ["hello"] @msgs1) ) 
      (is (= ["hello"] @msgs0 ) ) 
      (post channels room user1 "@diogok hello you!") 
      (Thread/sleep 250) 
      (is (= ["hello" "@diogok hello you!"] @msgs0 ) ) 
      (is (= ["hello" "@diogok hello you!"] @msgs1 ) ) 
      (is (= ["hello"] @msgs2)) 
      (leave channels room user1) 
      (post channels room user0 "Yoh!") 
      (Thread/sleep 250) 
      (is (= ["hello" "@diogok hello you!" "Yoh!"] @msgs0)) 
      (is (= ["hello" "@diogok hello you!"] @msgs1)) 
      (is (= ["hello" "Yoh!"] @msgs2 ))
      (is (= (list "girlaine" "diogok") (online-users channels room))))) 

