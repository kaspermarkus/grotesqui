(ns grotesqui.core
 (:use [seesaw core mig chooser dev border font])
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

;;;;;;;;;;;
; describe
; Description panel
(defn describe 
	([text] (invoke-later (config! (select *root* [:#description-panel]) :text text)))
	([] (invoke-later (config! (select *root* [:#description-panel]) :resource ::description-pane))))

(defn save-as-action [e]
  (do 
    (if-let 
      [f (choose-file 
           :type "Save"
           :selection-mode :files-only)]
      (ql/save-pipes (str f)))))

(defn save-action [e]
  (if (= ql/*last-save-file* nil) (save-as-action e) (ql/save-pipes ql/*last-save-file*)))

(defn check-unsaved-changes
  [callback]
  (let [empty-pipe? (= (count (ql/get-current-pipe)) 1)
        last-saved (if (= ql/*last-save-file* nil) nil (load-file ql/*last-save-file*))]
    (if (and (not empty-pipe?) (not (= last-saved (ql/get-current-pipe))))
      (do 
        (show! (pack! (dialog 
                        :type :warning 
                        :option-type :yes-no-cancel 
                        :content "You have unsaved changes!\n\nDo you want to save the current program before continuing?"
                        :success-fn (fn [p] (do (save-action nil) (callback)))
                        :no-fn (fn [p] (callback))))))
      (callback))))

(defn load-action [e]
  (check-unsaved-changes 
    (fn [] (if-let 
             [f (choose-file 
                  :type "Load"
                  :selection-mode :files-only)]
      (ql/load-pipes (str f))))))

(defn new-action [e]
  (check-unsaved-changes ql/new-pipes))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; UI Handling function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-pipe-ui
	"Updates the graphical representation of the givent pipe ref"
	[] (do (config! (select *root* [:#pipe-panel]) :items (map uinodes/node-ui (ql/get-current-pipe)))))

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
	(label :text "Description" :id :description-panel :font (font :style :plain) :border (line-border :color "#999" :thickness 1) :background "#fff" :v-text-position :top :valign :top))



(defn mbar 
  []
  (let [new-item (menu-item :text "New")
        save-item (menu-item :text "Save")
        save-as-item (menu-item :text "Save As..")
        load-item (menu-item :text "Load")
        quit-item (menu-item :text "Quit")
        run-item (menu-item :text "Run")
        test-item (menu-item :text "Test")] 
    (do (listen save-as-item :action save-as-action)
        (listen save-item :action save-action)
        (listen load-item :action load-action)
        (listen new-item :action new-action)
        (menubar :items 
           [(menu :text "File" :items [ new-item load-item save-item save-as-item quit-item ])
            (menu :text "Actions" :items [ run-item test-item ])]))))           
           
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
          :menubar (mbar)
          :size [800 :by 600]))
		(invoke-later (show! *root*))     ;show window
		(config! *root* :content (mig-panel   ;fill out with base content
	 		:constraints ["fill", "[fill]", "[fill]"]
			:items 
				[[(make-description-panel) "gap 10px 6px 10px 6px, height 150px!, dock south"]
				 [(scrollable (make-palette) :hscroll :never) "dock west, width 200px!"]
	 			 [(make-canvas) "grow"]]))
		(describe) ;set the default text in the describe panel
		(ql/add-listener (fn [] (update-pipe-ui)))
		(ql/new-pipes)))

(defn -main [& args]
	(init))
