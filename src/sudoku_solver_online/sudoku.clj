(ns sudoku_solver_online.sudoku
  (:use clojure.walk clojure.set)
  (:require [clojure.contrib.seq  :as seq]
	    [clojure.contrib.generic.collection :as col]))

;;;;;;;;;;

(defn p-9 [c] (partition 9 c))
(defn p-3 [c] (partition 3 c))

(defn print-board [board]
  "Pretty print the sudoku board"
  (print "sudoku")
  (doseq [br (->> board p-3 p-9)]
    (println)
    (doseq [nr (->> br p-3 (apply interleave) p-3)]
      (println nr))))

(def *pos* (range 0 81))
(def *r-pos* (->> *pos* p-3 p-3 (apply interleave) p-3 flatten))
(def *c-pos* (->> *pos* p-3 p-9 (map #(apply interleave %))
		  (map #(p-3 %)) (apply interleave) flatten))

(def *empty-board* (vec (repeat 81 0)))

(defn trans [m coll]
  "retrun a trasnformation base on the indexs at m"
  (for [i m] (nth coll i)))

(defn r-pos [b] (trans *r-pos* b))
(defn c-pos [b] (trans *c-pos* b))

(defn neig-vals [board pos]
  (distinct (concat
	     (nth (p-9 board) (quot pos 9))
	     (nth (p-9 (r-pos board)) (quot (first (seq/positions #(== pos %) *r-pos*))  9))
	     (nth (p-9 (c-pos board)) (quot (first (seq/positions #(== pos %) *c-pos*)) 9)))))

(defn neig-pos [pos]
  (difference 
   (set (neig-vals *pos* pos))
   #{pos}))

(def neig-pos (memoize neig-pos)) ;; huge perfromance improvmant
   
(defn valid-values [board pos]
  "return a list of values which does not violate the neighbors values."
  (if (zero? (nth board pos)) 
    (clojure.set/difference (set (range 1 10)) (set (neig-vals board pos)))
    #{}))

(defn b-valid-value [b] (vec (map #(valid-values b %) *pos*)))

(defn update-vv [vv pos v]
  "update the valid value vector, base on the new value (v) at position (pos)"
  (persistent!
   (let [nvv (assoc! (transient vv) pos #{})]
     (doseq [p (neig-pos pos)]
       (assoc! nvv p (difference (nth nvv p) #{v})))
     nvv)))

(defn complete? [b] (not (some zero? b)))

(defn next-cell [vv]
  (seq/find-first
   #(not (nil? %))
   (let [fv (seq/indexed vv)]
     (for [i (range 1 10)]
       (seq/find-first #(= (count (second %)) i) fv)))))

 
(defn lazy-all-solutions [board vv]
  "return all valid solution to a sudoku problem
if you do not have all day, use it in a lazy way"
  (lazy-seq
   (if (complete? board)
     (list board)
     (let [[pos valid-values] (next-cell vv)]
       (apply concat (for [v valid-values]
		       (lazy-all-solutions (assoc board pos v) (update-vv vv pos v))))))))

(defn legal-n? [s]
  "true is there are no duplicate in this group, 0 exluded"
  (let [g (filter pos? s)]
    (= (count (distinct g)) (count g))))
  
(defn legal? [board]
  "check the value in pos is valid"
  (and
   (every? legal-n? (p-9 board))
   (every? legal-n? (p-9 (r-pos board)))
   (every? legal-n? (p-9 (c-pos board)))))

(defn sudoku [board]
  "solve a sudoku problem"
  (when (legal? board)
    (first (lazy-all-solutions board (b-valid-value board)))))

;;;;;;;;
(def *sols* (atom {}))

(defn mem-sodoku [b]
  "my own dirty implementation of memoize"
  (if-let [f (find @*sols* b)]
    (val f)
    (let [s (sudoku b)]
      (swap! *sols* assoc b s)
      s)))
;;;;;;;;


(def *core-sudoku* (sudoku *empty-board*))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; generate new problems
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rand-board 
  "switch random two didigt in a board, repaet for num times"
  ([board num]
     (loop [cnt num, board board]
       (if (zero? cnt) board
	   (let [a (inc (rand-int 9))
		 b (inc (rand-int 9))]
	     (recur (dec cnt)
		    (if (= a b)
		      board
		      (replace { a b b a } board ) ))))))
  ([board] (rand-board board 20)))

(defn gen-problem [board num]
  "replace num of the digits in the board by zero"
  (let [nb 
	(persistent!
	 (let [tb (transient board)]
	   (dotimes [_ num] (assoc! tb (rand-int 81) 0))
	   tb))]
    (swap! *sols* assoc nb board) ;; dirty trick: cach the solution for the genrated problem
    nb))
   
(defn generate-board [hard]
;  {:pre [(and (pos? hard) (< hard 5))]}
  "creatre s new problem. Hard is a degree on the scale of 1 to 4"
  (gen-problem (rand-board *core-sudoku*) (* 10 (+ 6 hard))))


;;; profiling info:
;; (def *b* [0 0 1 0 0 0 2 0 0 0 0 0 2 0 0 0 3 0 0 8 7 9 0 0 0 0 0 3 0 9 0 0 0 8 0 0 0 2 0 0 9 0 0 0 4 0 7 4 0 1 8 0 0 2 0 0 0 0 0 2 0 5 8 0 0 0 0 0 0 0 0 0 0 0 9 0 0 0 1 0 0])
;; (time (sudoku *b*)) "Elapsed time: 11574.306296 msecs"

;; (defn foo [a b & {:keys [c] :or {c 5}}] [a b c])