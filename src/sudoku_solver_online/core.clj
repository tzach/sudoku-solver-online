(ns sudoku_solver_online.core
  (:require
	    [compojure.response :as response]
	    [compojure.route :as route]
      [compojure.handler :as handler])
  (:use sudoku_solver_online.sudoku
	compojure.core
	[ring.util.response :only [redirect]]
	[hiccup.core]
	[hiccup.page-helpers]
	[hiccup.form-helpers]
  ))


;;;; utils
(defn parse-integer [str]
  (try (Integer/parseInt str)
       (catch NumberFormatException nfe 0)))

(defn parse-digit [c]
  (if (Character/isDigit c)
    (Character/digit c 10) 0))

(defn inter [colls]
  (partition (count colls) (apply interleave colls)))

;;; board utils
(defn str-board [board]
  "convert board to string"
  (reduce str board))

(defn unstr-board [s]
  "convert string to board"
  (vec (map parse-digit s)))
  
(defn board? [b]
  "check if this is a sudoku board"
  (and (= 81 (count b))
       (every? #(and (<= % 9) (>= % 0)) b)))

;;;;
;;;; html
(defn glink-to
  [url & content]
  (link-to (str "http://" url) content))

(def *main-page* [:a {:href "/" } "Back to main page"])
(def *css* [:link {:type "text/css" :rel "stylesheet" :href "/stylesheets/main.css"}])
(def *hard* {1 "easy" 2 "not so easy" 3 "not easy at all" 4 "hard"})
(def *empty-board-str* (str "/board/" (apply str (repeat 81 0))))


(def *about*
     (html
      [:h5 "v.4b"]
      "Sudoku solver is a work in progress and an exercise in building a web service with "
      (glink-to "clojure.org" "Clojure") ", "
      (glink-to "compojure.org" "Compojure") ","
      (glink-to "github.com/weavejester/hiccup" "Hiccup") " and "
      (glink-to "https://github.com/gcv/appengine-magic" "appengine-magic")
      [:p] "The solver is an evolution of my old a Swing base Sudoku " (glink-to "code.google.com/p/sudoku-solver/" "solver")
      [:p] "If you want to use the solver API (maybe building a AJAX GUI for it?), help me with the CSS, or ask any question, just let me know."
      [:p] "tzach . livyatan at gmail ..."
      [:p][:p] (glink-to "sites.google.com/site/tzachlivyatan/Sudoku-solver-online-code" "site code")
      [:p][:p] "The following individuals have spent time and effort in reviewing and testing the site:"
      (unordered-list ["shirily bar-or"])
      [:p][:p][:p][:img {:src "http://code.google.com/appengine/images/appengine-noborder-120x30.gif" :alt "Powered by Google App Engine"}]
      ))


;;;;;
(defn b-form [b]
  [:table {:class "external"}
   (for [r1 (partition 27 b)]
     [:tr {:class "ext-tr"}
      (for [c1 (p-9 r1)]
	[:td  {:class "ext-td"}
	 [:table  {:class "internal"}
	  (for [r2 (p-3 c1)]
	    [:tr {:class "int-tr"}
	     (for [c2 r2]
	       [:td
		{:class "int-td"} [:input {:class (second c2)
						:name "board"
						:type 'text :maxlength 1 :size 1
					   :value (if (zero? (first c2)) "" (first c2))
					   }
				   ]])])
	  ]])
      ])])


(defn board-form
  "main page"
  [s b]
  (html
   (doctype :html4)
   [:head *css* ]
   [:body
    [:h1 "Sudoku solver online"]
    [:h2 s]
    (form-to [:post "/board"]
	     [:p] (b-form b)
	     [:p] [:input {:type "submit" :value "Solve" :name "submit"}]
	     [:p][:p][:p])
    [:p "generate a new Sudoku problem, on a scale of 1 to 4:"
     (unordered-list
      (for [[n h] *hard*] (link-to (str "/gen/" n) h)))
     (unordered-list [(link-to *empty-board-str* "empty board")])]
    [:hr]
    [:p] *about* 
    ]))

(defn illegal [text]
  (html (doctype :html4)
	[:body
	 (str "My friend, you have " text)
	 [:p] (link-to "javascript:history.back(-1)" "Back to Sudoku board")]))

;;;;;

(defn solve [b]
  (if (legal? b)
    (if-let [s (mem-sodoku b)]
      (board-form "Sudoku Solution"
		  (for [[x y] (inter [b s])]
		    (if (= x y) [y "cell1"] [y "cell2"])))
      (illegal "sorry, this one is impossible!"))
    (illegal "input an illegal board")))


(defroutes sudoku_solver_online_app_handler
  (GET "/" [] (redirect *empty-board-str*))
  (GET "/gen/:hard" [hard]
       (redirect (str "/board/" (str-board (generate-board (parse-integer hard))))))
  (GET  "/board/:board" [board]
	(let [b (unstr-board board)]
	  (if (board? b)
	    (board-form "Sudoku Problem" (inter [b (repeat "cell1")]))
	    (illegal "input an illegal string"))))
  (POST "/board"  {params :params}
	(let [b (vec (map parse-integer (get params "board")))]
	  "Solve" (solve b)))
  (route/not-found (html [:body "page not found" [:p] *main-page*] )))


(def app
  (handler/site sudoku_solver_online_app_handler))


