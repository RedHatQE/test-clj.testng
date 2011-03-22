(ns test-clj.testng
  (:import [org.testng.annotations AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
	    BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test]))


(defn method-name [t]
  (str (:name (meta t))))

(defn test? [t]
  (some (meta t) [AfterClass AfterGroups AfterMethod AfterSuite AfterTest	 
		  BeforeClass BeforeGroups BeforeMethod BeforeSuite BeforeTest Test]))

(defn class-keys-to-symbol [m]
  (let [cm (select-keys m (filter class? (keys m)))
        rest (dissoc m cm)
        sm (zipmap (for [k (keys m)] (-> k .getName symbol))
	    (vals m))]
    (merge rest sm)))

(defmacro gen-class-testng []
  (let [publics (vals (ns-publics *ns*))
	tests (filter test? publics)
	methods (map (fn [test]
		       (let [name (method-name test)]
			 `[~(with-meta (symbol name) (class-keys-to-symbol (meta test))) [] ~'void])) tests)]
    `(gen-class :prefix "" :name ~(symbol (namespace-munge *ns*)) :methods [~@methods])))


(defmacro data-driven [myfn newmeta data]
  (let [v (resolve myfn)
        basemeta (meta v)
        basename (str (:name basemeta))
        newmeta (class-keys-to-symbol newmeta)
        defns (for [[count item] (map-indexed vector data)]
                `(defn ~(with-meta (symbol (str basename  "_" count)) 
                          (update-in newmeta [(-> Test .getName symbol) :groups]
                                     (fn [v a] (vec (conj v a))) basename))
                   [_#]
                   (~myfn ~@item)))]
    `(do ~@defns)))

(comment 

  (defmacro data-driven [myfn newmeta data]
  (let [v (resolve myfn)
        basemeta (meta v)
        basename (str (:name basemeta))
        newmeta (class-keys-to-symbol newmeta)
        addfn (fn [v a] (vec (conj v a)))
        testsym (-> Test .getName symbol) 
        defns (doall (for [[count item] (map-indexed vector data)]
                       (let []
                        `(defn ~(with-meta (symbol (str basename  "_" count)) 
                                  (-> (update-in newmeta [testsym :groups]
                                                 addfn  basename)
                                      (update-in [testsym :dependsOnMethods]
                                                 (if (> count 0) addfn identity)
                                                 ())))
                           [_#]
                           (~myfn ~@item)))))]
    `(do ~@defns)))

         (defn ^{Test {:group ["dd-group-x" "othergroup"]} :enabled false} mytest [_]
           (apply myfn item)))
