(ns grotesqui.nodes
 (:use [seesaw core mig])
	(:require [seesaw.dnd :as dnd])
	(:import javax.swing.TransferHandler))

(defn mysql-in [describe] 
	(let [ node (label 
		:text ::mysql-in-label 
		:resource ::input
		:drag-enabled? true
		:transfer-handler
			(dnd/default-transfer-handler
				 :export {
         		:actions (constantly :copy)
         		:start   (fn [c] [dnd/string-flavor (config c :text)])
         		; No :finish needed
         }))]
	
			(do (listen node :mouse-entered (fn [e] (describe ::mysql-in-description))
			 				:mouse-exited (fn [e] (describe))
							:mouse-pressed (fn [evt] (let 
								[comp (.getSource evt)]
								(do (println (.toString (.getTransferHandler comp)))
										(.exportAsDrag (.getTransferHandler comp) comp evt TransferHandler/COPY)))))
;public void mousePressed(MouseEvent evt) {
;        JComponent comp = (JComponent)evt.getSource();
;        TransferHandler th = comp.getTransferHandler();
;        // Start the drag operation
;        th.exportAsDrag(comp, evt, TransferHandler.COPY);
;    }
	     		node)))

(defn spreadsheet-in [describe] 
	(let [ node (label 
			:text ::spreadsheet-in-label 
			:resource ::input)]
	
			(do (listen node :mouse-entered (fn [e] (describe ::spreadsheet-in-description))
			 				:mouse-exited (fn [e] (describe)))
	     		node)))
