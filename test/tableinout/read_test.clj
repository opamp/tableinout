(ns tableinout.read-test
  (:require [clojure.test :refer :all]
            [tableinout.read :refer :all]
            [clojure.string :as str]))

(deftest read-csv-test
  (testing "Empty CSV read"
    (is (= (read-csv "") [[""]])))

  (testing "Region chars"
    (is (= (read-csv "hoge,Apiyo,botA,a" :region-chars #{\A}) [["hoge" "piyo,bot" "a"]]))
    (is (= (read-csv "hoge,Apiyo,botA,Blist,listB" :region-chars #{\A \B}) [["hoge" "piyo,bot" "list,list"]])))

  (testing "Map values"
    (is (= (read-csv "hoge, piyo ,bot ") [["hoge" " piyo " "bot "]]))
    (is (= (read-csv "hoge, piyo ,bot " :map-fn (fn [x] (str/trim x))) [["hoge" "piyo" "bot"]]))
    (is (= (read-csv "hoge,Apiyo,botA   ,a" :map-fn (fn [x] (str/trim x)) :region-chars #{\A}) [["hoge" "piyo,bot" "a"]]))
    )
  
  (testing "One line CSV"
    (is (= (read-csv "hoge,piyo,bot") [["hoge" "piyo" "bot"]]))
    (is (= (read-csv "hoge,\"piyo,bot\",a" :region-chars #{\"}) [["hoge" "piyo,bot" "a"]]))
    (is (= (read-csv "hoge,\"piy\"\"o,bot\",a" :region-chars #{\"}) [["hoge" "piy\"o,bot" "a"]]))
    (is (= (read-csv "hoge,piy\"\"o,bot" :region-chars #{\"}) [["hoge" "piy\"o" "bot"]])))
  
  (testing "More lines CSV"
    (is (= (read-csv "hoge,piyo,bot\nhoge,piyo,bot\naaa,bbb,ccc") [["hoge" "piyo" "bot"]
                                                                   ["hoge" "piyo" "bot"]
                                                                   ["aaa" "bbb" "ccc"]]))))
