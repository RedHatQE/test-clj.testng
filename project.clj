(defproject test_clj.testng "1.0.1-SNAPSHOT"
  :description "A clojure library to generate classes that can be run by TestNG."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :dev-dependencies [[org.testng/testng "6.0.1"]
                     [swank-clojure "1.3.0"]
                     [webui-framework "1.0.2-SNAPSHOT"]]
  :class-file-whitelist #"test_clj")
