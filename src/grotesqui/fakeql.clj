(ns grotesqui.fakeql)

;;;;;;;;;;;
; list of listeners. Get called on every time pipe is updated
(def listeners (ref '()))
(defn add-listener [ func ] (dosync (alter listeners concat [func])))
(defn alert-listeners [] (apply (fn [f] (f)) @listeners))

;;;;;;;;;;
; current-pipe
; Should always be a reference to the pipe currently displayed in the UI.
(def current-pipe nil) 
(defn init-current-pipe [] (def current-pipe (ref '())))

(defn genkw [s] (keyword (gensym s)))

(defmulti node :type)

(defmethod node :dropzone [props]
  { :type :dropzone
    :id (genkw "dropzone")})

(defmethod node :csv-in [props] 
	{ :type :csv-in, 
		:category :input, 
		:id (genkw "csv-in"),
		:options { :filename nil, :header true, :separator ";"}})

(defmethod node :csv-out [props] 
	{ :type :csv-out, 
		:category :output, 
		:id (genkw "csv-out")
		:options { :filename nil, :header true, :separator ";"}})

(defmethod node :drop-columns [props] { :type :drop-columns, :category :transformation, :id (genkw "drop-columns")})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Pipe manipulation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn replace-node
  "Inserts the node into the given pipe-ref."
  ([pipe new-node replace-id]
    (let 
      [category (get new-node :category)
       splitlist  (split-with (fn [node] (not (= replace-id (get node :id)))) pipe)
       prelist (first splitlist)
       postlist (rest (second splitlist))]
			 (cond
					(= category :input) (concat [new-node (node {:type :dropzone})] postlist)
					(= category :output) (concat prelist [(node {:type :dropzone}) new-node])
					(= category :transformation) 
						(concat prelist [(node {:type :dropzone}) new-node (node {:type :dropzone})] postlist)
					:else (println "ERRORRRRRRRRRRRRRRRRRRRRRRRRRR")))))

(defn insert-node
	"Inserts the node into the given pipe-ref."
	([piperef node] (dosync (alter piperef concat [node])))
	([piperef node replace-id] 
			(do
				(dosync (alter piperef replace-node node replace-id))
				(alert-listeners))))

(defn update-node
	"Updates a node in the given pipe-ref."
	([piperef node]
		(do 
    	(dosync (alter piperef (fn [pipe] 
				(let 
      		[id (get node :id)
       	 	 splitlist  (split-with (fn [tstnode] (not (= id (get tstnode :id)))) pipe)
      		 prelist (first splitlist)
      		 postlist (rest (second splitlist))]
					(concat prelist (list node) postlist)))))
			(alert-listeners))))

