(ns tableinout.convert
  (:require [clojure.test :refer [function?]]))

(defn- line-to-hash [header line & {:keys [mapping] :as opts}]
  (let [mfn (if (function? mapping) mapping hash-map)]
    (apply mfn (interleave header line))))

(defn convert-headerless-table [table-header table-body]
  (if (empty? table-header)
    []
    (map #(line-to-hash table-header %) table-body)))

(defn convert-table [table]
  (let [header (remove empty? (first table))
        body (rest table)]
    (if (or (nil? header) (empty? body))
      []
      (convert-headerless-table header body))))

(defn- valid-headermapping? [mapping]
  (when (sequential? mapping)
    (every? (fn [elem]
              (cond
                (map? elem)
                (= (count elem) 1)
                (string? elem)
                (> (count elem) 0)))
            mapping)))

(defn make-header-from-element [table-element]
  (if (empty? table-element)
    (list)
    (into (list) (map (fn [key]
                    {key (if (keyword? key)
                           (name key)
                           (str key))})
                  (keys table-element)))))

(defn- extract-table-body-with-headermapping-2 [elem headermapping]
  (loop [mapping headermapping
         row []]
    (let [mapinfo (first mapping)]
      (if (nil? mapinfo)
        row
        (recur (rest mapping)
               (conj row (if (map? mapinfo)
                           (get elem (first (keys mapinfo)))
                           "")))))))

(defn- extract-table-body-with-headermapping [htable headermapping]
  (loop [table htable
         body []]
    (let [elem (first table)]
      (if (nil? elem)
        body
        (recur (rest table)
               (conj body (extract-table-body-with-headermapping-2 elem
                                                                   headermapping)))))))

(defn- header-name [mapping-info]
  (cond
    (map? mapping-info)
    (first (vals mapping-info))
    (string? mapping-info)
    mapping-info))

(defn deconvert-table [htable & {:keys [headermapping] :as opts}]
  (if (empty? htable)
    []
    (let [mapping (if (valid-headermapping? headermapping)
                    headermapping
                    (make-header-from-element (first htable)))
          body (extract-table-body-with-headermapping htable mapping)]
      (if (empty? mapping)
        []
        (into [] (cons (map header-name mapping) body))))))
