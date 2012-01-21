(ns grotesqui.description-pane
   (:use [seesaw core mig chooser dev border font]))

(def ^:private default-text (str "<html><body>"
                                 "<h4>General Information</h4>"
                                 "<b>New nodes:</b> Drag nodes from the left side palette to the pipe in the main (right) part<br />"
                                 "<b>Info:</b> Mouse over anything to get information about it<br />"
                                 "<b>Edit:</b> Click on a section in the pipe to edit properites<br />"
                                 "</body></html>"))

(defn- set-content [widget text] 
  (invoke-later (config! (select (to-root widget) [:#description-panel]) :text text)))

;;;;;;;;;;;
; describe
; Description panel
(defn update-text 
	([widget text] (set-content widget text))
	([widget] (set-content widget default-text)))
  
(defn new-description-panel 
	"Creates an empty description panel with the id :description-panel
	 This will hold error messages when relevant and help text when mousing over things"
	[] 
	(label :text "Description" :id :description-panel :font (font :style :plain) :border (line-border :color "#999" :thickness 1) :background "#fff" :v-text-position :top :valign :top))

(defn map-to-string [options]
  (map (fn [key] (str "<p><b>" (name key) ":</b> " (get options key) "</p>")) (keys options)))

(defn describe-map [widget header map]
  (update-text widget (str "<html><body><h4>" header "</h4>" (apply str (map-to-string map)) "</body></html>")))
