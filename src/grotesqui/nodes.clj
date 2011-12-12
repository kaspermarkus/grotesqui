(ns grotesqui.nodes
 (:use [seesaw core mig]))

(defn spreadsheet-in [describe] 
  (let [ node (label :text ::spreadsheet-in-label :resource ::input) ]
	(do (listen node :mouse-entered (fn [e] (describe ::spreadsheet-in-description))
			 :mouse-exited (fn [e] (describe))) 
	     node)))
