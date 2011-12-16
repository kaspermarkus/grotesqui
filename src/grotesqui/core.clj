(ns grotesqui.core
 (:use [seesaw core mig])
  (:require [seesaw.dnd :as dnd], [grotesqui.nodes :as uinodes])
 (:gen-class :main true))

;initializes an empty pipe and returns a reference to it
;(defn add-pipe [pipes] (let [ piperef (ref '())] (do 
;	(dosync (alter pipes assoc (keyword (gensym "pipe")) piperef))
;	 piperef)))

;creates a drop zone and assigns it a unique id (to be used for ref. in GUI)
;(defn create-dropzone [] (list "dropzone" {:type :dropzone, :text "Drop Zone", :id (keyword (gensym "dropzone"))}))

;(defn create-dropzone-ui [dropzone] (let [props (second dropzone)] (label
;	:text (get props :text)
;	:id (get props :id)
;	:background "#DDDDDD"
;	:transfer-handler [:import [dnd/string-flavor (fn [{:keys [target data]}] (config! target :text data))]])))
;
;(defn create-node-ui [node] (let [props (second node)] (cond
;	(= :dropzone (get props :type)) (create-dropzone-ui node)
;	:else (println (str "NO MATCH FOR: " (get props :type))))))
;
;(defn create-pipe-ui [pipe returnlist] 
;	(if (= '() pipe) 
;		(apply vector returnlist)
;		(recur (rest pipe) (cons (vector (create-node-ui (first pipe)) "growx") returnlist))))
;
; 
;;add single dropzone to pipe:
;(defn add-to-pipe [pipe node replace-id] (cond
;	(= replace-id :none) (dosync (alter pipe concat [node]))))
;
;
;(defn update-graph [piperef] (mig-panel
;	:constraints ["wrap 1", "", ""]
;	:items (create-pipe-ui @piperef '())))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Pipe manipulation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn insert-node
	"Inserts the node into the given pipe-ref."
	[piperef node]
	(dosync (alter piperef concat [node])))
	

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Global vars to hold state
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;
; root
; The frame holding the application.
; This is created as a global var to be used on selects
(def root (frame
          :title "Grotesqui 0.1",
          :on-close :hide,
          :size [800 :by 600]))

;;;;;;;;;;
; pipes
; ref to map. Holds all the pipes of the programs as map of key/pipe-ref pairs
; TBD: implement multi pipe functionality. The idea is that we keep a list of 
; references to pipes in a map: key/pipe-ref
;(def pipes (ref {}))

;;;;;;;;;;
; current-pipe
; Should always be a reference to the pipe currently displayed in the UI.
(def current-pipe nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;
;TRASH:
;;;;;;;;;;;;;;;;;;;;;;;;;
;(add-to-pipe current-pipe (create-dropzone) :none)

(defn describe 
	([text] (invoke-later (config! (select root [:#description-panel]) :text text)))
	([] (invoke-later (config! (select root [:#description-panel]) :text (str
		"<html><body>"
		"<h1>Help:</h1>"
		"<b>New nodes:</b> Drag nodes from left to right<br />"
		"<b>Info:</b> Mouse over anything to get information about it<br />"
		"<b>Edit:</b> Double click to edit properites<br />"
		"</body></html>")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Base layout stuff:
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn make-canvas 
	"Creates the canvas, that is the part of the screen that will hold the graph."
	[] 
	(mig-panel
		:constraints ["fill", "[grow][center][grow]", ""]
		:items 
			[[(label :text "" :background "#abcdef") "grow"]
			 ;[(update-graph current-pipe) "w 100, h 40"]
			 [(border-panel :id :pipe-panel :center "CENTER") "w 100, h 40"] 
		 	 [(label :text "" :background "#abcdef") "grow"]]))

(defn make-palette 
	"Creates a palette (list of draggable nodes).
	 The palette holds a mig panel with all the draggable nodes"
	[] 
	(mig-panel 
		;"0[][grow]" - horizontal settings - fill out, the 0 sets left padding to 0px
		;"0[]0[]" - vertical: set first padding to 0, set spacing between rows to 0
		:constraints ["", "0[grow]", "0[]0[]"]
		:items 
			[[(label :text "Input" :background "#BBBBFF") "span,growx"]
	  	 [(uinodes/mysql-in describe) "span,growx"]
	  	 [(label :text "Transformations" :background "#BBFFBB") "span,growx"]
       [(label :text "Drop Column" :background "#DDFFDD") "span,growx"]]))

(defn make-description-panel 
	"Creates an empty description panel with the id :description-panel
	 This will hold error messages when relevant and help text when mousing over things"
	[] 
	(label :text "Description" :id :description-panel))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Initialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init
	"Initializes the application. This involves:
	 - initialize the description panel via: make-description-pane
	 - initialize the palette in scrollable container via: make-palette
	 - initialize the canvas via: make-canvas
	 - show the root container"
	[]
	(do 
		(invoke-later (show! root))     ;show window
		(config! root :content (mig-panel   ;fill out with base content
	 		:constraints ["fill", "[fill]", "[fill]"]
			:items 
				[[(make-description-panel) "height 150px!, dock south"]
				 [(scrollable (make-palette) :hscroll :never) "dock west, width 200px!"]
	 			 [(make-canvas) "grow"]]))
		(describe) ;set the default text in the describe panel
		(def current-pipe (ref '()))
		(insert-node current-pipe (uinodes/dropzone))))

(defn -main [& args]
	(init))
