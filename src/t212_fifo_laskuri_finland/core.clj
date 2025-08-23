(ns t212-fifo-laskuri-finland.core
  "FIFO stock gain/loss calculator for Trading 212 exports."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; --- Data structures ---
(defrecord Transaction [date type symbol quantity price])
(defrecord Lot [quantity price date])
(defrecord Sale [symbol quantity sold-price date cost-basis gain-loss])

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
                                (Double/parseDouble (:no-of-shares trnx-map))
                                (Double/parseDouble (:total trnx-map)))]
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
          csv-row-map (map #(zipmap keyworded-header %) rows)
          filtered-transactions (keep filter-transaction csv-row-map)
          transaction-record (reduce add-transaction-to-record [] filtered-transactions)]
      transaction-record)))

;; --- FIFO Logic ---
(defn add-purchase
  "Add a purchase lot to the positions map for the given transaction."
  [positions txn]
  ;; get all the info out of txn 
  (let [symbol (:symbol txn)
          new-lot (->Lot
                    (:quantity txn)
                    (:price txn)
                    (:date txn))
        exisiting-lots (get positions symbol [])]
    (assoc positions symbol (conj exisiting-lots new-lot))))


(defn consume-lots
  "Consume lots based on the quantity needed.
   Return map with :remaining-lots and :consumed-lots."
  [lots quantity-sold]
  (loop [remaining-lots lots
         quanity-left-to-be-sold quantity-sold
         consumed []]
    ;; when either "nothing left to consume" or "no more lots remaining to be sold"
    (if (or (<= quanity-left-to-be-sold 0) (empty? remaining-lots))
      {:remaining-lots remaining-lots
       :consumed-lots consumed}
      (let [lot (first remaining-lots)
            available-quantity (:quantity lot)]
        (cond
          ;; exact match - consume entire lot
          (= available-quantity quanity-left-to-be-sold)
          {:remaining-lots (rest remaining-lots)
           :consumed-lots (conj consumed lot)}

          ;; partial consumption - split the lot
          (> available-quantity quanity-left-to-be-sold)
          (let [consumed-portion (assoc lot :quantity quanity-left-to-be-sold)
                remaining-portion (update lot :quantity - quanity-left-to-be-sold)]
            {:remaining-lots (cons remaining-portion (rest remaining-lots))
             :consumed-lots (conj consumed consumed-portion)})

          ;; consume entire lot, continue with rest
          :else (recur (rest remaining-lots)
                       (- quanity-left-to-be-sold available-quantity)
                       (conj consumed lot)))))))

(defn process-fifo-lots
  "Process FIFO lots for a sale transaction.
   Takes current positions and a sell transaction.
   Matches sale quantity against oldest lots for the symbol.
   Returns map with :positions (updated) and :consumed-lots."
  [positions txn]
  ;; get ticker, quantity, and all the lots available for the symbol
  (let [symbol (:symbol txn)
        sale-qty (:quantity txn)
        lots (get positions symbol [])]
    (if (empty? lots)
      ;; when something is sold without no lots as in no purchase data
      (do
        (println "WARNING: incomplete data, sold " (:quantity txn) " of " (:symbol txn) " without available buying data.")
        {:positions positions :consumed-lots []})
      ;; process the sale against lots
      (let [{:keys [remaining-lots consumed-lots]} (consume-lots lots sale-qty)
            updated-positions (if (empty? remaining-lots)
                                (dissoc positions symbol)
                                (assoc positions symbol remaining-lots))]
        {:positions updated-positions :consumed-lots consumed-lots}))))


(defn calculate-sale-records
  "Update sales records based on the sold lots and calculate gain/loss."
  [txn consumed-lots]
  (let [total-cost-basis (reduce + (map :price consumed-lots))
        gain-loss (- (:price txn) total-cost-basis)]
    [(->Sale (:symbol txn) (:quantity txn) (:price txn) (:date txn) total-cost-basis gain-loss)]))

(defn process-sale
  "Take a sale transaction, update positions and sales records."
  [state txn]
  (let [{:keys [positions consumed-lots]} (process-fifo-lots (:positions state) txn)]
    (-> state
        (assoc :positions positions)
        (update :sales into (calculate-sale-records txn consumed-lots)))))

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
  (def sample-file "samples/test.csv")
  (def transactions (load-transactions sample-file))
  
  ;; test complete FIFO calculation
  (def fifo-results (calculate-fifo transactions))
  (println "Positions:" (:positions fifo-results))
  (println "Sales:" (:sales fifo-results))
  
  ;; testing consuming lot
  (def test-lots [{:ticker "AAPL" :quantity 30 :price 100 :date "2023-01-01"}
                  {:ticker "AAPL" :quantity 40 :price 110 :date "2023-01-02"}
                  {:ticker "AAPL" :quantity 20 :price 120 :date "2023-01-03"}])
  (consume-lots test-lots 60))
