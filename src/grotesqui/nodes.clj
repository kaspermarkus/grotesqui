(ns grotesqui.nodes
 (:use [seesaw core mig dev chooser border], [clojure inspector])
	(:require [seesaw.dnd :as dnd], [grotesqui.fakeql :as ql])
	(:import javax.swing.TransferHandler))

;(show! (frame :content () :size [640 :by 480]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; General UI node handling:
(defmulti node-ui (fn [node] (get node :type)))

;Creates a graphical representation of the node passed to the function and returns it
(defmethod node-ui :dropzone
	[node]
	(let [ id (get node :id)] 
		(label
			:resource ::dropzone
			:halign :center
			:h-text-position :center
			:background :aliceblue
			:border (line-border :color "#000")
			:id id
			:transfer-handler [:import 
				[dnd/string-flavor (fn [{:keys [target data]}] 
					(do 
						(println (str "Dropping new node on " id)) 
						(ql/insert-node (ql/node {:type (keyword data)}) id)))]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIX THE BELOW
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod node-ui :drop-columns
	[node]
    	(label
				:text "Drop Columns"
				:id (get node :id)
				:h-text-position :center
				:halign :center
				:border (line-border :color "#000")
				:resource ::transformation))

(defn csv-out-show-properties [node]
	  (let
    [options (get node :options)
     filename-field (text :id :filename :text (if (= (get options :filename) nil) "" (get options :filename)))
     file-button (button :text "Browse")
     header-checkbox (checkbox :id :header :selected? (get options :header)) 
     separator (text :id :separator :text (get options :separator))
     dialog (dialog 
          :option-type :ok-cancel 
          :success-fn (fn [e] (let [updated-node (assoc node :options (value e))] (do (println updated-node) (ql/update-node updated-node))))
          :content
          (mig-panel
            :constraints ["", "", ""]
            :items
            [["file:" ""]
             [filename-field "width 200px!"]
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
               :filters [["CSV files" ["csv"]]
                  (file-filter "All Files" (constantly true))])]
              (text! filename-field (str f)))))
      (show! (pack! dialog)))))

(defmethod node-ui :csv-out
	[node]
    (let 
			[uinode 
				(label
					:text ".CSV File"
					:h-text-position :center
					:halign :center
					:border (line-border :color "#000")
					:id (get node :id)
					:resource ::output)]
			(do (listen uinode
				:mouse-entered (fn [e] (println "some description"))
        :mouse-clicked (fn [e] (csv-out-show-properties node)))
        uinode)))

(defn csv-in-show-properties [node]
  (let
    [options (get node :options)
		 filename-field (label :id :filename :text (if (= (get options :filename) nil) "No file selected" (get options :filename)))
     file-button (button :text "Browse")
     header-checkbox (checkbox :id :header :selected? (get options :header))
     separator (text :id :separator :text (get options :separator))
     dialog (dialog 
          :option-type :ok-cancel 
	:success-fn (fn [e] (let [updated-node (assoc node :options (value e))] (do (println updated-node) (ql/update-node updated-node))))
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
       	[uinode 
					(label
						:h-text-position :center
						:halign :center
						:border (line-border :color "#000")
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
