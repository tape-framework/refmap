(ns tape.refmap
  (:require [integrant.core :as ig]))

(defrecord RefMap [key]
  ig/RefLike
  (ref-key [_] key)
  (ref-resolve [_ config resolvef]
    (into {} (for [[k v] (ig/find-derived config key)]
               [k (resolvef k v)]))))

(defn refmap
  "Create a map of references to all matching top-level keys in a config map."
  [key]
  {:pre [(ig/valid-config-key? key)]}
  (->RefMap key))

(defn refmap?
  "Return true if its argument is a refmap."
  [x]
  (instance? RefMap x))

#?(:clj (def readers {'tape/refmap refmap}))
