(ns tableinout.write
  (:require [clojure.string :as str]
            [clojure.set :as cset]
            [clojure.test :refer [function?]])
  (:import [org.apache.poi.xssf.usermodel XSSFWorkbook]
           [org.apache.poi.ss.usermodel
            IndexedColors
            FillPatternType
            BorderStyle
            HorizontalAlignment
            VerticalAlignment]
           [org.apache.poi.ss.util WorkbookUtil]
           [java.io FileOutputStream]))

(defn- create-new-xlsx-workbook []
  (new XSSFWorkbook))

(defn- create-sheet! [wb name]
  (. wb createSheet (WorkbookUtil/createSafeSheetName name)))

(defn- table-size [table]
  {:row (count table)
   :cell (count (first table))})

(defn- foreach-cell! [sheet table-range afn]
  (let [start-point (first table-range)
        end-point (second table-range)]
    (doseq [r (range (:row start-point) (:row end-point))
            c (range (:cell start-point) (:cell end-point))]
      (let [row (if-let [existrow (. sheet getRow r)] existrow (. sheet createRow r))
            cell (if-let [existcell (. row getCell c)] existcell (. row createCell c))]
        (afn r c cell)))))

(defn- write-table-to-sheet! [sheet table & {:keys [cellfn] :as opts}]
  (let [tsize (table-size table)]
    (foreach-cell!
     sheet
     [{:row 0 :cell 0}
      {:row (:row tsize) :cell (:cell tsize)}]
     (fn [r c cell]
       (let [original-celldata (nth (nth table r) c)
             celldatastr (if (keyword? original-celldata)
                           (name original-celldata)
                           (str original-celldata))]
         (. cell setCellValue (if (function? cellfn)
                                (cellfn r c celldatastr)
                                celldatastr)))))
    sheet))

(defn- set-width-of-cells! [sheet sizelst]
  (dotimes [n (count sizelst)]
    (. sheet setColumnWidth n (* 256 (nth sizelst n)))))

(defn- write-workbook-to-file! [workbook outfile]
  (let [outstream (new FileOutputStream outfile)]
    (. workbook write outstream)
    (. outstream close)))

(defn write-xlsx! [table outfile outsheet]
  (let [wb (create-new-xlsx-workbook)
        sheet (create-sheet! wb outsheet)]
    (write-table-to-sheet! sheet table)
    (write-workbook-to-file! wb outfile)))

(defn write-csv! [table outfile region-char]
  (let [str-table (map (fn [line]
                         (str/join ","
                                   (map (fn [content]
                                          (let [esc-content (str/replace content
                                                                         (str region-char)
                                                                         (str region-char region-char))]
                                            (if (str/includes? esc-content ",")
                                              (str region-char esc-content region-char)
                                              esc-content))) line)))
                       table)]
    (spit outfile (str/join "\n" str-table))))
