(defproject test_clj.testng "1.1.0-SNAPSHOT"
  :description "A clojure library to generate classes that can be run by TestNG."
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.testng/testng "6.8"]]
  :class-file-whitelist #"test_clj")
