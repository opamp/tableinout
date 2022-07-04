# tableinout

## 概要

csv/xlsx形式のファイルから表データを読み書きするだけのライブラリ。

## 読み込み

### load-csv-from-file!

``` clojure
load-csv-from-file! [file-path  & {:keys [table-header] :as opts}]
```

- file-path: CSVファイルのパス
- table-header: 読み込む表のヘッダー情報

table-headerが指定されない場合、CSVファイルの1行目のデータをヘッダーとみなす。
table-headerは以下のように、文字列の配列で指定する。

``` clojure
["Header 1" "Header 2" ... ]
```

### load-csv-from-str 

``` clojure
load-csv-from-str [csv-str & {:keys [table-header] :as opts}]
```

- csv-str: CSVの文字列を指定する
- table-header: 読み込む表のヘッダー情報

table-headerが指定されない場合、CSV文字列の1行目のデータをヘッダーとみなす。

### 例

```clojure
(require '[tableinout.core :as tableinout])

(tableinout/load-csv-from-file! "sample.csv") 
;; => {:header [{"Header1" "Header1"} {"Header2" "Header2"} ... ] :body [{"Header1" "data1" "Header2" "data2"} ... ]}
```

## データ操作

load-csv-from-file!などの読み込み関数で読み込んだデータは以下の形式になる。

``` clojure
{:header HEADER-DATA
 :body BODY-DATA}
```

HEADER-DATAは1つのキーと値のペアを持つハッシュの配列となる。このデータは書き込み時にヘッダーの順番を指定するのに使用される。
BODY-DATAはハッシュテーブルの配列であり、各ハッシュテーブルは1行分の情報を持っている。

``` clojure
;; HEADER-DATAの例
[{"Header1" "H1"} {"Header2" "2"} ... ]

;; BODY-DATAの例
[{"Header1" "data1" "Header2" "data2"} ... ]
```

HEADER-DATA配列のハッシュのキーの文字列はBODY-DATAのキーと対応している。HEADER-DATA配列のハッシュの値は書き込まれる際のヘッダー名を表している。
上の例の場合、"Header1"のデータは書き込み時は"H1"列に属するようになり、"Header2"は"2"列に属するようになる。
また、書き込み時はHEADER-DATA配列のハッシュの順番で列は並ぶ。つまり、上の例の場合は書き込まれたCSVは「H1,2」のような順番となる。

BODY-DATAは表のヘッダーを除いた部分を表す。ハッシュの配列であり、1つのハッシュが1行分のデータを含んでいる。
また、配列の順番が行数と一致しており、元データの1行目のデータは0番目の要素、2行目のデータは1番目の要素のように順番に並ぶ。

このデータを編集して表データを編集する。

## 書き込み

### save-table-to-csv-str

``` clojure
save-table-to-csv-str [table-info]
```

- table-info: 表情報

読み込んだ表データをtable-infoに渡すことで、csvの文字列に変換する。

### save-table-to-csv-file!

``` clojure
save-table-to-csv-file! [table-info out-file]
```

- table-info: 表情報
- out-file: 出力ファイル

読み込んだ表データをtable-infoに渡すことで、csvの文字列に変換し、out-fileに出力する。
