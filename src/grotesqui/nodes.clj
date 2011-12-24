(ns grotesqui.nodes
 (:use [seesaw core mig])
	(:require [seesaw.dnd :as dnd], [grotesqui.fakeql :as ql])
	(:import javax.swing.TransferHandler))

;(show! (frame :content () :size [640 :by 480]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; General UI node handling:
(defmulti node-ui (fn [node] (get (second node) :type)))

;(defmulti node :type)

(defn pipe-ui
	"Creates a graphical representation of the pipe"
	[piperef]
	(grid-panel :columns 1 :items (map node-ui @piperef)))	

;Creates a graphical representation of the node passed to the function and returns it
(defmethod node-ui :dropzone
	[node]
	(let [props (second node) 
				uiprops (get props :grotesqui)
				id (get props :id)] 
		(label
			:text (get uiprops :text)
			:id id
			:background "#DDDDDD"
			:transfer-handler [:import 
				[dnd/string-flavor (fn [{:keys [target data]}] 
					(do 
						(println (str "Dropping new node on " id)) 
						(ql/insert-node ql/current-pipe (ql/node {:type (keyword data)}) id)))]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIX THE BELOW
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(defmethod node :mysql-in [props] (list "mysql-in" { :type :mysql-in, :id (keyword (gensym "mysql-in"))}))

(defmethod node-ui :drop-columns
	[node]
	  (let [props (second node)] 
    	(label
      	:text "Drop Columns"
      	:id (get props :id)
      	:resource ::transformation)))

(defmethod node-ui :csv-out
	[node]
	  (let [props (second node)] 
    	(label
      	:text ".CSV File"
      	:id (get props :id)
      	:resource ::output)))

(defmethod node-ui :csv-in
	[node]
	  (let [props (second node)] 
    	(label
      	:text ".CSV File"
      	:id (get props :id)
      	:resource ::input)))

(defn node-palette [ name type describe ] 
	(let 
		[to-keyword  (fn [ kw ] (keyword (str (namespace ::tmp) "/" kw)))
		 node (label
	  	:text (to-keyword (str name "-palette-label")) ;::csv-in-label 
    	:resource (to-keyword type)
    	:drag-enabled? true
    	:transfer-handler
      	(dnd/default-transfer-handler
        	:export {
            :actions (constantly :copy)
            :start   (fn [c] [dnd/string-flavor name])}))]
	  (do
			(listen node
    		:mouse-entered (fn [e] (describe (to-keyword (str name "-description"))))
      	:mouse-exited (fn [e] (describe))
      	:mouse-pressed (fn [evt] (let [comp (.getSource evt)]
          (.exportAsDrag (.getTransferHandler comp) comp evt TransferHandler/COPY))))
       node)))
