(ns fastchat.test.core
 (:use [fastchat.core]
       [midje.sweet]))

(clear)

(fact "Can connect, disconnect and list users"
  (enter "room1" "diogok" println)

  (online-users "room1") => ["diogok"]

  (enter "room1" "max" println)
  (online-users "room1") => ["diogok" "max"]

  (leave "room1" "max")
  (leave "room1" "diogok")

  (online-users "room1") => []

  (clear))

(fact "Can connect, disconnect and list users with multiple room isolation"
  (enter "room1" "diogok" println)

  (online-users "room1") => ["diogok"]
  (online-users "room2") => []

  (enter "room1" "max" println)
  (enter "room2" "max" println)
  (online-users "room1") => ["diogok" "max"]
  (online-users "room2") => ["max"]

  (leave "room1" "max")
  (leave "room2" "max")
  (online-users "room1") => ["diogok"]

  (leave "room1" "diogok")

  (online-users "room1") => []
  (online-users "room2") => []

  (clear))

(fact "Basic Messaging"
  (let [msgs (atom [])
        fun  (fn [room user msg]
               (swap! msgs (fn [msgs] (conj msgs {:to user :room room :msg (:message msg )}))))
        enter* (fn [room user] (enter room user (partial fun room user)))]
    (enter* "room1" "diogok")
    (enter* "room1" "max")

    (post "room1" "diogok" "hello!")

    @msgs => (just [{:to "diogok" :room "room1" :msg "hello!"}
              {:to "max" :room "room1" :msg "hello!"}] :in-any-order)

    (post "room1" "max" "hi!")

    (rest (rest @msgs))=> (just [{:to "max" :room "room1" :msg "hi!"}
                                {:to "diogok" :room "room1" :msg "hi!"}] :in-any-order)

    (enter* "room2" "diogok")
    (enter* "room2" "max")

    (post "room2" "max" "lol?")

    (map :room @msgs) => ["room1" "room1" "room1" "room1" "room2" "room2"]

    (leave "room1" "diogok")
    (leave "room1" "max")
    (leave "room2" "diogok")
    (leave "room2" "max")
    (clear)))

(fact "Private messaging"
  (let [msgs-max (atom [])
        msgs-diogok (atom [])
        msgs-lolcat (atom [])]
    (enter "room1" "diogok"
      (fn [msg] (swap! msgs-diogok conj msg)))
    (enter "room1" "max"
      (fn [msg] (swap! msgs-max conj msg)))
    (enter "room1" "lolcat"
      (fn [msg] (swap! msgs-lolcat conj msg)))

    (post "room1" "diogok" "hello!")
    (post "room1" "max" "hi!")
    (post "room1" "max" "@diogok i hate lolcat")

    (count @msgs-max) => 3
    (count @msgs-diogok) => 3
    (count @msgs-lolcat) => 2

    (leave "room1" "diogok")
    (leave "room1" "max")
    (leave "room1" "lolcat")

    (clear)))

(fact "Chat history"
  (clear))

