(ns test-clj.testng
  (:use [clojure.contrib.map-utils :only [deep-merge-with]])
  (:import [org.testng.annotations AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
	    BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test DataProvider]))


(defn method-name [t]
  (str (:name (meta t))))

(defn test? [t]
  (some (meta t) [AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
		  BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test]))

(defn dataprovider? [t]
  (some (meta t) [DataProvider]))

(defn convert-keys "Takes a map m and passes its keys that match pred through function f."
  [m pred f]
  (zipmap (for [k (keys m)]
            (if (pred k)
              (f k)
              k))
          (vals m)))

(defn class-keys-to-symbol [m]
  (convert-keys m class? (fn [k] (-> k .getName symbol))))

(defn symbol-keys-to-class [m]
  (convert-keys m symbol? (fn [k] (let [r (resolve k)]
                             (if (class? r) r k)))))

(defn num-args [t]
  (- (apply max (map count (:arglists (meta t))))
     1))

(defmacro gen-class-testng
  "Generates an ahead-of-time compiled java class from whatever
  namespace it's called from.  Any functions in the namespace with
  metadata containing TestNG annotations will be turned into TestNG
  test methods."
  []
  (let [publics (vals (ns-publics *ns*))
        methods (for [test (filter test? publics)] 
                  `[~(with-meta (symbol (method-name test))
                       (class-keys-to-symbol (meta test)))
                    ~(vec (repeat (num-args test) Object)) ~'void])
        dps (for [dp (filter dataprovider? publics)] 
                  `[~(with-meta (symbol (method-name dp))
                       (class-keys-to-symbol (meta dp))) [] "[[Ljava.lang.Object;"])]
    `(gen-class :prefix "" :name ~(symbol (namespace-munge *ns*)) :methods [~@methods ~@dps])))


(defmacro data-driven
  "Generates data driven tests.  One TestNG test method will be
generated for each item in 'data' (which should be a list of argument
lists).  Each generated TestNG method will execute fn 'myfn' but with
a different argument list.  The TestNG annotations for each method
will come from 'newmeta', a map.  Note TestNG annotation classes must
be fully qualified.  Finally, each item in 'data' can have its own
metadata, which will be merged with 'newmeta'. "

 [myfn newmeta data]
  (let [basename (-> myfn resolve meta :name str) 
        newmeta (symbol-keys-to-class newmeta)
        merge-fn (fn [a b] (if (coll? a) (vec (concat a b)) b))
        defns (for [[count item] (map-indexed vector (eval data))]
                `(defn ~(with-meta (symbol (str basename  "_" count)) 
                          (deep-merge-with merge-fn
                                           (update-in newmeta [Test :groups]
                                                      (fn [v a] (vec (conj v a))) basename)
                                           (or (meta item) {})))
                   [_#]
                   (~myfn ~@item)))]
    `(do ~@defns)))


