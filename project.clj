(defproject sudoku-solver-online "1.1.0-SNAPSHOT"
  :description "Online sudoku game and solver"
  :namespaces [sudoku_solver_online.app_servlet]
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [compojure "0.5.1"]
		 [hiccup "0.3.0"]
		 ]
  :dev-dependencies [[appengine-magic "0.3.0-SNAPSHOT"]
		     [swank-clojure "1.2.1"]]
  )

;; http://github.com/gcv/appengine-magic

;; run localy
;; lein appengine-clean
;; lein appengine-prepare


;; 3. d:/code/appengine-java-sdk-1.3.7/bin/dev_appserver.cmd resources/

;; deploy on GAE
;; d:/code/appengine-java-sdk-1.3.7/bin/appcfg.cmd update resources/

