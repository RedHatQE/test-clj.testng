(ns
    ^{:doc "Allows tests to be run under TestNG."
      :author "Jeff Weiss"}
  test-clj.testng
  (:import [org.testng.annotations AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
	    BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test]))


(defn method-name [t]
  (str (:name (meta t))))

(defn test? [t]
  (some (meta t) [AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
		  BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test]))

(defn class-keys-to-symbol [m]
  (let [m (select-keys m (filter class? (keys m)))]
    (zipmap (for [k (keys m)] (-> k .getName symbol))
	    (vals m))))

(defmacro gen-class-testng []
  (let [publics (vals (ns-publics *ns*))
	tests (filter test? publics)
	methods (map (fn [test]
		       (let [name (method-name test)]
			 `[~(with-meta (symbol name) (class-keys-to-symbol (meta test))) [] ~'void])) tests)]
    `(gen-class :prefix "" :name ~(symbol (namespace-munge *ns*)) :methods [~@methods])))
