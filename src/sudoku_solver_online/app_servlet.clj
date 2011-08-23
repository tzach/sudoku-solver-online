(ns sudoku_solver_online.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use sudoku_solver_online.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method sudoku_solver_online_app) this request response))
