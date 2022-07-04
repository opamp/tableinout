(ns tableinout.read
  (:require [clojure.string :as str]
            [clojure.set :as cset])
  (:import [java.io File]
           [org.apache.poi.ss.usermodel WorkbookFactory CellType]))

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

(defn- read-cell-data [cell]
  (let [cell-type (. cell getCellType)]
    (cond
      (= cell-type CellType/STRING)
      (. cell getStringCellValue)
      (= cell-type CellType/NUMERIC)
      (. cell getNumericCellValue)
      (= cell-type CellType/BOOLEAN)
      (. cell getBooleanCellValue)
      (= cell-type CellType/FORMULA)
      (str (. cell getCellFormula))
      :else nil)))

(defn- read-row [row start-cellnum initdata]
  (if-let [cell (. row getCell start-cellnum)]
    (if-let [cell-data (read-cell-data cell)]
      (recur row (+ start-cellnum 1) (conj initdata cell-data))
      initdata)
    initdata))

(defn- read-sheet-table [sheet start-rownum initdata]
  (if-let [row (. sheet getRow start-rownum)]
    (recur sheet (+ start-rownum 1) (conj initdata (read-row row 0 [])))
    initdata))

(defn read-xlsx-file! [xlsx-file sheet-name]
  (let [wb (WorkbookFactory/create (new File xlsx-file))
        sheet (if (string? sheet-name) (. wb getSheet sheet-name) (. wb getSheetAt 0))]
    (read-sheet-table sheet 0 [])))

