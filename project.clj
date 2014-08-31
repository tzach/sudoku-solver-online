(defproject sudoku-solver-online "1.1.0-SNAPSHOT"
  :description "Online sudoku game and solver"  
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		              [compojure "0.6.4"]
		              [hiccup "0.3.0"]]
  :dev-dependencies [[lein-ring "0.4.5"]
	             	     [swank-clojure "1.2.1"]]
   :ring {:handler sudoku_solver_online.core/app})


;; run localy
;; lein run server

;; generate a standard war
;; lein ring war|uberwar

;; eg deploy on cloudbees
;; bees app:deploy


;; heroku...

;; GAE
