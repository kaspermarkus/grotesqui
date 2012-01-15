(ns grotesqui.core
 (:use [seesaw core mig dev])
  (:require [seesaw.dnd :as dnd], [grotesqui.nodes :as uinodes], [grotesqui.fakeql :as ql])
 ;(:gen-class :main true))
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Global vars to hold state
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;
; root
; The frame holding the application.
; This is created as a global var to be used on selects
(def ^:dynamic *root* nil)

;;;;;;;;;;
; modified flag
; flag telling whether anything has been modified since last save
(def ^:dynamic *modified-flag* false)

;(ql/add-listener (fn [] (do (def ^:dynamic *modified-flag* true) (println *modified-flag*)))) 

;;;;;;;;;;;
; describe
; Description panel
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
; UI Handling function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-pipe-ui
	"Updates the graphical representation of the givent pipe ref"
	[piperef] (do (println (str "PIPE: " piperef))
	(config! (select *root* [:#pipe-panel]) :items (map uinodes/node-ui @piperef))))

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
					[(grid-panel  :id :pipe-panel :background "#000") "width 200px!, grow"]
					[(label :background "#abcdef") "grow"]]))

(defn make-palette 
	"Creates a palette (list of draggable nodes).
	 The palette holds a mig panel with all the draggable nodes"
	[] 
	(mig-panel 
		;"0[][grow]" - horizontal settings - fill out, the 0 sets left padding to 0px
		;"0[]0[]" - vertical: set first padding to 0, set spacing between rows to 0
		:constraints ["", "0[grow]", "0[]3[]"]
		:items 
			[[(label :text "Input" :resource ::input-header ) "span,growx"]
			[(uinodes/node-palette "csv-in" "input" describe) "span,growx"]
			[(label :text "Transformation" :resource ::transformation-header) "span,growx"]
			[(uinodes/node-palette "drop-columns" "transformation" describe) "span,growx"]
			[(label :text "Output" :resource ::output-header) "span,growx"]
			[(uinodes/node-palette "csv-out" "output" describe) "span,growx"]]))

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
		(def ^:dynamic *root* (frame
          :title "Grotesqui 0.1",
          :on-close :hide,
          :size [800 :by 600]))
		(invoke-later (show! *root*))     ;show window
		(config! *root* :content (mig-panel   ;fill out with base content
	 		:constraints ["fill", "[fill]", "[fill]"]
			:items 
				[[(make-description-panel) "height 150px!, dock south"]
				 [(scrollable (make-palette) :hscroll :never) "dock west, width 200px!"]
	 			 [(make-canvas) "grow"]]))
		(describe) ;set the default text in the describe panel
		(ql/init-current-pipe)
		(ql/add-listener (fn [] (update-pipe-ui ql/current-pipe)))
		(ql/insert-node ql/current-pipe (ql/node {:type :dropzone}))
		(update-pipe-ui ql/current-pipe)))	
		

(defn -main [& args]
	(init))
