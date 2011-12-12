(ns grotesqui.core
 (:use [seesaw core mig] grotesqui.nodes)
 (:gen-class :main true))

(def root (frame
          :title "Grotesqui 0.1",
          :on-close :hide,
          :size [800 :by 600]))

(defn describe 
    ([text] (invoke-later (config! (select root [:#description-panel]) :text text)))
    ([] (invoke-later (config! (select root [:#description-panel]) :text (str
	 "<html><body>"
	 "<h1>Help:</h1>"
	 "<b>New nodes:</b> Drag nodes from left to right<br />"
	 "<b>Info:</b> Mouse over anything to get information about it<br />"
	 "<b>Edit:</b> Double click to edit properites<br />"
	 "</body></html>")))))

(defn make-palette [] (mig-panel 
	;"0[][grow]" - horizontal settings - fill out, the 0 sets left padding to 0px
	;"0[]0[]" - vertical: set first padding to 0, set spacing between rows to 0
	:constraints ["", "0[grow]", "0[]0[]"]
	:items 
	[ [(label :text "Input" :background "#BBBBFF") "span,growx"]
	  [(spreadsheet-in describe) "span,growx"]
	  [(label :text "MySQL" :background "#DDDDFF") "span,growx"]
	  [(label :text "CSV" :background "#DDDDFF") "span,growx"]
	  [(label :text "Transformations" :background "#BBFFBB") "span,growx"]
          [(label :text "Search and Replace" :background "#DDFFDD") "span,growx"]
          [(label :text "Drop Column" :background "#DDFFDD") "span,growx"]
          [(label :text "Merge Columns" :background "#DDFFDD") "span,growx"]
          [(label :text "Split Column" :background "#DDFFDD") "span,growx"]
          [(label :text "All Capitals" :background "#DDFFDD") "span,growx"]
          [(label :text "Drop if empty" :background "#DDFFDD") "span,growx"]
	  [(label :text "Output" :background "#FFBBBB") "span,growx"]
          [(label :text "Spread Sheet" :background "#FFDDDD") "span,growx"]
          [(label :text "MySQL" :background "#FFDDDD") "span,growx"]
          [(label :text "CSV" :background "#FFDDDD") "span,growx"]
	]))

(defn make-canvas [] (label :text "Canvas77" :background "#333333"))

(defn make-description-panel [] (label :text "Description" :id :description-panel))

(defn setup [] 
	(do (invoke-later (show! root))
	    (config! root :content (mig-panel
	 	:constraints ["fill", "[fill]", "[fill]"]
		:items [ [(make-description-panel) "height 150px!, dock south"]
			[(scrollable (make-palette) :hscroll :never) "dock west, width 200px!"]
	 		[(make-canvas) "grow"]] 
	 		))
		(describe)
	))

(defn -main [& args]
	(setup))
