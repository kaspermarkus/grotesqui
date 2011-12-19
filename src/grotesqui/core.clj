(ns grotesqui.core
 (:use [seesaw core mig])
  (:require [seesaw.dnd :as dnd], [grotesqui.nodes :as uinodes])
 (:gen-class :main true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Global vars to hold state
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;
; root
; The frame holding the application.
; This is created as a global var to be used on selects
(def *root* (frame
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; UI Handling function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-pipe-ui
	"Updates the graphical representation of the givent pipe ref"
	[piperef]
	(config! (select *root* [:#pipe-panel]) :items (map uinodes/node-ui @piperef)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Pipe manipulation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn insert-node
	"Inserts the node into the given pipe-ref."
	[piperef node]
	(dosync (alter piperef concat [node])))
	

;;;;;;;;;;;;;;;;;;;;;;;;;;
;TRASH:
;;;;;;;;;;;;;;;;;;;;;;;;;
;(add-to-pipe current-pipe (create-dropzone) :none)

(defn describe 
	([text] (invoke-later (config! (select *root* [:#description-panel]) :text text)))
	([] (invoke-later (config! (select *root* [:#description-panel]) :text (str
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
	[]
	(mig-panel
	:constraints ["fill,wrap 3", "0[grow]0[]0[grow]0", "0[grow]0[]0[grow]0"]
	:items [[(label :background "#abcdef") "span 1 3, grow"]
					[(label :background  "#abcdef") "grow"]
					[(label :background "#abcdef") "span 1 3, grow"]
					[(vertical-panel  :id :pipe-panel) ""]
					[(label :background "#abcdef") "grow"]]))

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
	  	 [(uinodes/mysql-in-palette describe) "span,growx"]
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
		(invoke-later (show! *root*))     ;show window
		(config! *root* :content (mig-panel   ;fill out with base content
	 		:constraints ["fill", "[fill]", "[fill]"]
			:items 
				[[(make-description-panel) "height 150px!, dock south"]
				 [(scrollable (make-palette) :hscroll :never) "dock west, width 200px!"]
	 			 [(make-canvas) "grow"]]))
		(describe) ;set the default text in the describe panel
		(def current-pipe (ref '()))
		(insert-node current-pipe (uinodes/dropzone))
		(update-pipe-ui current-pipe)))	
		

(defn -main [& args]
	(init))
