## README

STATUS: Pre-alpha, in design and prototyping phase.

#### About

`tape.refmap`

- see [this](https://github.com/weavejester/integrant/pull/64) Integrant PR and associated issues.
- aims to support both Clojure & ClojureScript

The refmap construct is similar to refset, but resolves to a map from
keys to resolved refs, rather than a set of refs; it is useful when the
keys are meaningful to the logic beyond just being a slot in the config
map, and are required in further logic.

#### Usage

```clojure
(ns my-ns
  (:require [tape.refmap :as rm]))

;; Use the refmap dependecy spec in config maps
{:foo/a (rm/refmap :foo/b), :foo/b 1}

;; When reading edn configs, use the `#tape/refmap` tag symbol
;; and extra `rm/readers` reader tags.
(ig/read-string {:readers rm/readers}
                "{:foo/a #tape/refmap :foo/b, :foo/b 1}")
```

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.