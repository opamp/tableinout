(ns tableinout.convert-test
  (:require [clojure.test :refer :all]
            [tableinout.convert :refer :all]))

(deftest convert-headerless-table-test
  (testing "Convert empty tables"
    (is (= (convert-headerless-table [] []) []))
    (is (= (convert-headerless-table ["AYO" "HOOO"] []) []))
    (is (= (convert-headerless-table [] [[""]]) [])))

  (testing "Convert tables"
    (is (= (convert-headerless-table ["hoge" "piyo"] [["a" "b"]
                                                      ["c" "d"]
                                                      ["e"]
                                                      ["f" "g" "h"]])
           [{"hoge" "a" "piyo" "b"}
            {"hoge" "c" "piyo" "d"}
            {"hoge" "e"}
            {"hoge" "f" "piyo" "g"}]))))

(deftest convert-table-test
  (testing "Convert empty tables"
    (is (= (convert-table []) []))
    (is (= (convert-table [["aaa" "bbb"]]) [])))
  
  (testing "Convert tables"
    (is (= (convert-table [["aaa" "bbb" ""]
                           ["a" "b" "c"]
                           ["d" "e" "f"]])
           [{"aaa" "a" "bbb" "b"}
            {"aaa" "d" "bbb" "e"}]))))

(deftest deconvert-table-test
  (testing "Decomvert empty table"
    (is (= (deconvert-table []) []))
    (is (= (deconvert-table [{}]) []))
    )

  (testing "Deconvert tables"
    (let [result (deconvert-table [{"a" "aaa" "b" "bbb"}])]
      (is (or (= result [["a" "b"] ["aaa" "bbb"]])
              (= result [["b" "a"] ["bbb" "aaa"]]))))

    (is (= (deconvert-table [{"a" "aa" "b" "bb"}
                             {"a" "cc" "b" "dd"}
                             {"a" "ee" "b" "ff" "c" "ABC"}]
                            :headermapping [{"a" "A"}
                                            {"b" "B"}
                                            "c"])
           [["A" "B" "c"]
            ["aa" "bb" ""]
            ["cc" "dd" ""]
            ["ee" "ff" ""]]))))

