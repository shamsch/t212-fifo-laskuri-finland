(ns t212-fifo-laskuri-finland.core
  "FIFO stock gain/loss calculator for Trading 212 exports."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; --- Data structures ---
(defrecord Transaction [date type symbol quantity price])
(defrecord Lot [quantity price date])
(defrecord Sale [symbol quantity sold-price cost-basis gain-loss date])

;; --- Utility functions ---
(defn filter-transaction
  "From a CSV row map, filter out only buy or sell transcations and add a new field :type based on the action e.g Market Sell -> sell"
  [transaction]
  (let [action (:action transaction)]
    (cond
      (str/includes? action "buy") (assoc transaction :type "buy")
      (str/includes? action "sell") (assoc transaction :type "sell")
      :else nil)))

(defn add-transaction-to-record
  "Converts parsed transaction map to Transaction record and adds to collection"
  [transactions trnx-map]
  (let [new-transaction-record (->Transaction
                                (:time trnx-map)
                                (:type trnx-map)
                                (:ticker trnx-map)
                                (:no-of-shares trnx-map)
                                (:total trnx-map))]
    (conj transactions new-transaction-record)))

(defn sanitize-header-name
  "Header can have whitespace or symobols e.g / . ( ) etc. Replace them with hyphen and remove them where possible."
  [header]
  (-> header
      str/lower-case
      (str/replace #"[(){}\\[\\]]" "")  ; remove brackets and parentheses
      (str/replace #"\s+|/|\." "-")     ; replace whitespace, /, . with hyphens
      (str/replace #"-+" "-")           ; collapse multiple hyphens to single
      keyword))

;; --- CSV Parsing ---
(defn load-transactions
  "Parse T212 CSV into `Transaction` records.
   TODO:
   - Read CSV file
   - Map headers to row values
   - Filter only :buy and :sell actions
   - Parse date, quantity, and price
   Return sorted seq of transactions."
  [file]
  (with-open [reader (io/reader file)]
    (let [csv (doall (csv/read-csv reader))
          [header & rows] csv
          keyworded-header (map sanitize-header-name header)
          csv-row-map (map #(zipmap keyworded-header %) rows)]
      csv-row-map
    )) 
)

;; --- FIFO Logic ---
(defn add-purchase
  "Add a purchase lot to the positions map for the given transaction."
  [positions txn]
  ;; TODO: implement - should add txn as a lot to positions[symbol]
  positions)


(defn consume-lots
  "Consume lots based on the quantity needed.
   Return remaining lots after fulfilling the quantity, or empty if fully consumed."
  [lots quantity-sold]
  (loop [remaining-lots lots
         quanity-left-to-be-sold quantity-sold
         result []]
    ;; when either "nothing left to consume" or "no more lots"
    (if (or (<= quanity-left-to-be-sold 0) (empty? remaining-lots))
      (into result remaining-lots)
      (let [lot (first remaining-lots)
            available-quantity (:quantity lot)]
        (cond
          ;; exact match - consume entire lot, add remaining lots to result
          (= available-quantity quanity-left-to-be-sold)
          (into result (rest remaining-lots))

          ;; partial consumption - reduce lot quantity, add it and remaining lots to result
          (> available-quantity quanity-left-to-be-sold)
          (into (conj result (update lot :quantity - quanity-left-to-be-sold))
                (rest remaining-lots))

          ;; consume entire lot, continue with rest of lot and reduce quantity left to be sold by available quantity
          ;; which just got consumed
          :else (recur (rest remaining-lots)
                       (- quanity-left-to-be-sold available-quantity)
                       result))))))

(defn process-fifo-lots
  "Process FIFO lots for a sale transaction.
   Takes current positions and a sell transaction.
   Matches sale quantity against oldest lots for the symbol.
   Returns updated positions with lots removed/reduced."
  [positions txn]
  ;; get ticker, quantity, and all the lots available for the symbol
  (let [symbol (:symbol txn)
        sale-qty (:quantity txn)
        lots (get positions symbol [])]
    (if (empty? lots)
      ;; when something is sold without no lots as in no purchase data
      (do
        (println "WARNING: incomplete data, sold " (:quantity txn) " of " (:symbol txn) " without available buying data.")
        positions)
      ;; process the sale against lots
      (let [updated-lots (consume-lots lots sale-qty)]
        (if (empty? updated-lots)
          (dissoc positions symbol)
          (assoc positions symbol updated-lots))))))


(defn calculate-sale-records
  "Update sales records based on the sold lots."
  [positions txn])

(defn process-sale
  "Take a sale transaction, update positions and sales records."
  [state txn]
  (-> state
      (assoc :positions (process-fifo-lots (:positions state) txn))
      (update :sales into (calculate-sale-records (:positions state) txn))))

(defn calculate-fifo
  "Process transactions chronologically to calculate FIFO gains/losses."
  [transactions]
  (reduce
   (fn [state txn]
     (case (:type txn)
       "buy"  (update state :positions add-purchase txn)
       "sell" (process-sale state txn)
       state))
   {:positions {} :sales []}
   transactions))

;; --- Reporting ---
(defn print-summary
  "Print results: total gain/loss per symbol and remaining positions.
   TODO: group and sum sales, calculate avg cost for positions."
  [results]
  ;; TODO: implement
  (println "Not yet implemented."))

;; --- CLI entry point ---
(defn -main
  "Entry point for the application."
  [& args]
  (if-let [csv-file (first args)]
    (let [transactions (load-transactions csv-file)
          results (calculate-fifo transactions)]
      (print-summary results))
    (println "Usage: clj -M -m t212-fifo-laskuri-finland.core <csv-file>")))

;; --- REPL playground ---
(comment
  ;; loading csv file
  (def sample-file "samples/2022.csv")
  (load-transactions sample-file)

  ;; testing consuming lot
  (def test-lots [{:ticker "AAPL" :quantity 30 :price 100 :date "2023-01-01"}
                  {:ticker "AAPL" :quantity 40 :price 110 :date "2023-01-02"}
                  {:ticker "AAPL" :quantity 20 :price 120 :date "2023-01-03"}])
  (consume-lots test-lots 60))
