(ns tape.refmap-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer [deftest is are]])
            [weavejester.dependency :as dep]
            [integrant.core :as ig]
            [tape.refmap :as rm]))

(def log (atom []))

(defmethod ig/prep-key ::p [_ v]
  (merge {:a (ig/ref ::a)} v))

(defmethod ig/init-key :default [k v]
  (swap! log conj [:init k v])
  [v])

(derive ::p ::pp)
(derive ::pp ::ppp)

(derive ::ap ::a)
(derive ::ap ::p)

(deftest refmap-test
  (is (rm/refmap? (rm/refmap ::foo)))
  (is (rm/refmap? (rm/refmap [::foo ::bar])))
  (is (ig/reflike? (rm/refmap ::foo)))
  (is (ig/reflike? (rm/refmap [::foo ::bar]))))

(deftest composite-keyword-test
  (is (= (ig/expand {::a (rm/refmap ::ppp), ::p 1, ::pp 2})
        {::a {::p 1 ::pp 2}, ::p 1, ::pp 2}))
  (is (= (ig/expand {::a (rm/refmap ::ppp)})
        {::a {}})))

#?(:clj
   (deftest read-string-test
     (is (= (ig/read-string {:readers rm/readers}
              "{:foo/a #tape/refmap :foo/b, :foo/b 1}")
           {:foo/a (rm/refmap :foo/b), :foo/b 1}))))

(deftest dependency-graph-test
  (let [m {::a (ig/ref ::p), ::b (rm/refmap ::ppp) ::p 1, ::pp 2}]
    (testing "graph with refmaps"
      (let [g (ig/dependency-graph m)]
        (is (dep/depends? g ::a ::p))
        (is (dep/depends? g ::b ::p))
        (is (dep/depends? g ::b ::pp))))))

(deftest init-test
  (testing "with refmaps"
    (reset! log [])
    (let [m (ig/init {::a (rm/refmap ::ppp), ::p 1, ::pp 2})]
      (is (= m {::a [{::p [1] ::pp [2]}], ::p [1], ::pp [2]}))
      (is (= @log [[:init ::p 1]
                   [:init ::pp 2]
                   [:init ::a {::p [1] ::pp [2]}]]))))

  (testing "with refmaps and keys"
    (reset! log [])
    (let [m {::a (rm/refmap ::ppp), ::p 1, ::pp 2}]
      (is (= (ig/init m [::a]) {::a [{}]}))
      (is (= (ig/init m [::a ::p]) {::a [{::p [1]}] ::p [1]}))
      (is (= (ig/init m [::a ::pp]) {::a [{::p [1] ::pp [2]}]
                                     ::p [1] ::pp [2]})))))
