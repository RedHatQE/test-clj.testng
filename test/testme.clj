(ns testme
  (:use [test-clj.testng] :reload)
  (:use [clojure.test])
  (:import [org.testng.annotations Test DataProvider]))

(deftest replace-me ;; FIXME: write
  (is false "No tests have been written."))

(defn ^{Test {:dataProvider "mydata"}}
  myfn [_ onearg]
  (println (str "called with " onearg)))

(defn ^{DataProvider {:name "mydata"}} mydp [_]
  (to-array-2d [[1] ["hi"] [3] [nil]]))

(gen-class-testng)
(comment (gen-class :prefix ""
            :name testme
            :methods [[^{Test {:dataProvider "mydata"}} myfn [Object] void]
                      [^{DataProvider {:name "mydata"}} mydp [] "[[Ljava.lang.Object;"] ]))

