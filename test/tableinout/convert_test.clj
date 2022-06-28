(ns tableinout.convert-test
  (:require [clojure.test :refer :all]
            [tableinout.convert :refer :all]))


(deftest analyze-headerless-table-test
  (testing "Convert empty tables"
    (is (= (convert-headerless-table [] []) []))
    (is (= (convert-headerless-table ["hoge" "piyo"] []) []))
    (is (= (convert-headerless-table [] [[""]]) [])))

  (testing "Convert tables"
    (is (= (convert-headerless-table ["hoge" "piyo"] [["a" "b"]
                                                      ["c" "d"]
                                                      ["e"]
                                                      ["f" "g" "h"]])
           [{:hoge "a" :piyo "b"}
            {:hoge "c" :piyo "d"}
            {:hoge "e"}
            {:hoge "f" :piyo "g"}]))))
