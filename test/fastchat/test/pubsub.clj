(ns fastchat.test.pubsub
  (:use [fastchat.pubsub])
  (:use [lazytest.deftest]))

    (deftest pubsub-listen
     (let [channels (mkchannels) 
           msgs0 (atom [])
           msgs1 (atom [])
           ls0   #(swap! msgs0 conj %1) 
           ls1   #(swap! msgs1 conj %1)]
      (listen! channels "foo" ls0)
      (listen! channels "bar" ls1)
      (send! channels "foo" "Hello") 
      (Thread/sleep 500) 
      (send! channels "foo" "Foo") 
      (send! channels "bar" "bar") 
      (Thread/sleep 500) 
      (is (= ["Hello" "Foo"]  @msgs0 )) 
      (is (= ["bar"]  @msgs1 )) 
      (leave! channels "bar" ls1)
      (send! channels "bar" "foo") 
      (is (= ["bar"]  @msgs1 )) 
      )
    ) 

