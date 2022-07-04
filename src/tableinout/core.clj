(ns tableinout.core
  (:require [clojure.string :as str]
            [tableinout.read :as r]
            [tableinout.convert :as c]
            [tableinout.write :as w]))

(defn- original-table-header [header]
  (map (fn [elem]
         (hash-map elem (str elem))) header))

(defn- make-header-body-hash [table table-header]
  (if (nil? table-header)
    {:header (original-table-header (first table))
     :body (c/convert-table table)}
    {:header (original-table-header table-header)
     :body (c/convert-headerless-table table-header table)}))

(defn load-csv-from-str [csv-str & {:keys [table-header] :as opts}]
  (let [table (r/read-csv csv-str :region-chars #{\"} :map-fn #(str/trim %))]
    (make-header-body-hash table table-header)))

(defn load-csv-from-file! [file-path & {:keys [table-header] :as opts}]
  (let [csv-str (slurp file-path)]
    (load-csv-from-str csv-str :table-header table-header)))

(defn load-xlsx-from-file! [file-path sheet-name & {:keys [table-header] :as opts}]
  (let [table (r/read-xlsx-file! file-path sheet-name)]
    (make-header-body-hash table table-header)))

(defn save-table-to-csv-str [table-info]
  (let [header (:header table-info)
        body (:body table-info)]
    (w/write-csv! (if (nil? header)
                  (c/deconvert-table body)
                  (c/deconvert-table body :headermapping header))
                  \")))

(defn save-table-to-csv-file! [table-info out-file]
  (let [csv-str (save-table-to-csv-str table-info)]
    (spit out-file csv-str)))

(defn save-table-to-xlsx-file! [table-info out-file out-sheet]
  (let [header (:header table-info)
        body (:body table-info)]
    (w/write-xlsx! (if (nil? header)
                     (c/deconvert-table body)
                     (c/deconvert-table body :headermapping header))
                   out-file
                   out-sheet)))
