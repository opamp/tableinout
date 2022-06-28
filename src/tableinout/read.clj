(ns tableinout.read
  (:require [clojure.string :as str]
            [clojure.set :as cset])
  (:import [org.apache.poi.ss.usermodel WorkbookFactory]))

(defn- slice-csv-str [csv-str region-chars ignore-region val]
  (if (or (= csv-str "") (and (not ignore-region) (contains? #{\, \newline} (nth csv-str 0))))
    {:splitedby (if (empty? csv-str) nil (nth csv-str 0))
     :first val
     :rest (if (empty? csv-str) "" (subs csv-str 1))}
    (let [first-char (nth csv-str 0)
          second-char (if (>= (count csv-str) 2) (nth csv-str 1) "")
          rest-str (subs csv-str 1)]
      (cond
        (and (contains? region-chars first-char) (= first-char second-char))
        (recur (subs rest-str 1) region-chars ignore-region (str val first-char))
        (and (contains? region-chars first-char) (not= first-char (first val)))
        (recur rest-str region-chars (not ignore-region) val)
        :else
        (recur rest-str
               region-chars
               ignore-region
               (str val first-char))))))

(defn- build-line [csv-str region-chars map-fn]
  (loop [result (slice-csv-str csv-str region-chars false "")
         line-array []]
    (if (or (nil? (:splitedby result)) (= (:splitedby result) \newline))
      {:line (conj line-array (map-fn (:first result)))
       :rest (:rest result)}
      (recur (slice-csv-str (:rest result) region-chars false "")
             (conj line-array (map-fn (:first result)))))))

(defn- build-table [csv-str region-chars map-fn]
  (loop [result (build-line csv-str region-chars map-fn)
         table []]
    (if (empty? (:rest result))
      (conj table (:line result))
      (recur (build-line (:rest result) region-chars map-fn)
             (conj table (:line result))))))

(defn read-csv [csv-str & {:keys [region-chars map-fn] :as opts}]
  (build-table csv-str
               region-chars
               (if map-fn map-fn (fn [x] x))))

(defn read-csv-file [csv-file & {:keys [region-chars map-fn encoding] :as opts}]
  (read-csv (slurp csv-file :encoding encoding) :region-chars region-chars :map-fn map-fn))

(defn create-workbook [xlsx-file password]
  (if (string? password)
    (WorkbookFactory/create xlsx-file password)
    (WorkbookFactory/create xlsx-file)))

(defn read-xlsx-file [xlsx-file & {:keys [password] :as opts}]
  (let [wb (create-workbook xlsx-file password)]
    wb ;; temporary return
    ))
