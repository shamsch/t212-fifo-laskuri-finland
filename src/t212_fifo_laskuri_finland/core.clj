(ns t212-fifo-laskuri-finland.core
  "FIFO stock gain/loss calculator for Trading 212 exports."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; --- Data structures ---
(defrecord Transaction [date type symbol quantity price])
(defrecord Lot [quantity price date])
(defrecord Sale [symbol quantity sold-price cost-basis gain-loss date])

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
  ;; TODO: implement
  [])

;; --- FIFO Logic ---
(defn add-purchase
  "Add purchase lot to positions map (FIFO queue per symbol).
   TODO: conj new lot to symbol's vector in positions."
  [positions txn]
  ;; TODO: implement
  positions)

(defn process-sale
  "Match lots FIFO to fulfill sale qty, calculate proceeds, cost basis, gain/loss.
   Return {:positions updated-positions :sales sale-records}.
   TODO: implement sale matching loop."
  [positions txn]
  ;; TODO: implement
  {:positions positions
   :sales []})

(defn calculate-fifo
  "Iterate transactions; update positions & sales.
   For :buy -> add-purchase, for :sell -> process-sale.
   TODO: implement transaction processing loop."
  [txns]
  ;; TODO: implement
  {:positions {}
   :sales []})

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
  (def sample-file "")

  (def txns (load-transactions sample-file))
  (take 5 txns)

  (def result (calculate-fifo txns))

  (print-summary result)

  (:positions result)
  (:sales result))
