(ns grotesqui.nodes
 (:use [seesaw core mig dev chooser], [clojure inspector])
	(:require [seesaw.dnd :as dnd], [grotesqui.fakeql :as ql])
	(:import javax.swing.TransferHandler))

;(show! (frame :content () :size [640 :by 480]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; General UI node handling:
(defmulti node-ui (fn [node] (get node :type)))

;(defmulti node :type)

(defn pipe-ui
	"Creates a graphical representation of the pipe"
	[piperef]
	(grid-panel :columns 1 :items (map node-ui @piperef)))	

;Creates a graphical representation of the node passed to the function and returns it
(defmethod node-ui :dropzone
	[node]
	(let [ id (get node :id)] 
		(label
			:resource ::dropzone
			:id id
			:transfer-handler [:import 
				[dnd/string-flavor (fn [{:keys [target data]}] 
					(do 
						(println (str "Dropping new node on " id)) 
						(ql/insert-node ql/current-pipe (ql/node {:type (keyword data)}) id)))]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIX THE BELOW
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod node-ui :drop-columns
	[node]
    	(label
      	:text "Drop Columns"
      	:id (get node :id)
      	:resource ::transformation))

(defmethod node-ui :csv-out
	[node]
    	(label
      	:text ".CSV File"
      	:id (get node :id)
      	:resource ::output))

(defn csv-in-show-properties [node]
  (let
    [options (get node :options)
		 filename-field (label :text (if (= (get options :filename) nil) "No file selected" (get options :filename)))
     file-button (button :text "Browse")
     header-checkbox (checkbox :selected? (get options :header))
     separator (text :text (get options :separator))
     dialog (dialog 
          :option-type :ok-cancel 
          :content 
          (mig-panel
            :constraints ["", "", ""]
            :items 
            [["file:" ""]
             [filename-field "" ]
             [file-button "wrap"]
             ["Separator:" ""]
             [separator "wrap"]
             ["Column Names:" ""]
             [header-checkbox ""]]))]
    (do 
			(println node)
      (listen file-button :action 
              (fn [e] 
                    (if-let 
                  [f (choose-file 
                       :type "Select"
                       :selection-mode :files-only
                       :filters [["CSV files" ["csv"]]
                          (file-filter "All Files" (constantly true))])]
        (text! filename-field (str f)))))
      (show! (pack! dialog)))))

(defmethod node-ui :csv-in
  	[node]
  	  (let 
       	[
    		uinode (label
      		:text ".CSV File"
      		:id (get node :id)
      		:resource ::input)]
			(do (listen uinode
				:mouse-entered (fn [e] (println "some description"))
				:mouse-clicked (fn [e] (csv-in-show-properties node)))
				uinode)))

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
