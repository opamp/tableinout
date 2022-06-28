(ns tableinout.convert)

(defn- line-to-hash [header line]
  (apply array-map (interleave (map #(keyword %) header) line)))

(defn convert-headerless-table [table-header table-body]
  (if (empty? table-header)
    []
    (map #(line-to-hash table-header %) table-body)))

(defn convert-table [table]
  (let [header (first table)
        body (rest table)]
    (convert-headerless-table header body)))
