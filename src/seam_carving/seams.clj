(ns seam-carving.seams
  "A seam is a connected path of low energy pixels in an image. A seam
is an optimal 8-connected path of pixels on a single image from top to
bottom, or left to right, where optimality is defined by an image
energy function. Uses a best first search to figure out the seamn
path."
  (:use [seam-carving.auxiliary ;; :only (get-raster-pixel-value
                                ;;        clone-buffered-image)
         ]
        [seam-carving.operations]
        [clojure.set])
  (:import [java.awt Point Color Graphics2D]
           [java.awt.image Raster BufferedImage]))

(defrecord SeamMap
    ^{:doc "A map linking all the possible ways inside an image, and a sorted set of entry points."}
  [path-map entry-points])

(defn build-seam-map [raster h w]
  "Read http://en.wikipedia.org/wiki/Seam_carving#Dynamic_Programming"

  (defn compare-points [p1 p2]
    (> (get-raster-pixel-value raster p1)
       (get-raster-pixel-value raster p2)))
  
  (defn least-energy-neighbor [x y]
    "assumes a vertical seam. always moves 'up' within the
bounderies passed and verifies each node in the neighborhood and
returns the one with the least amount of energy."
    (first (union (sorted-set-by compare-points)
                  (cond (== y 0) nil
                        (== x 0)
                        #{(Point. x       (dec y))
                          (Point. (inc x) (dec y))}
                        (== x (dec w))
                        #{(Point. x       (dec y))
                          (Point. (dec x) (dec y))}
                        :else              
                        #{(Point. x       (dec y))
                          (Point. (inc x) (dec y))
                          (Point. (dec x) (dec y))}))))

  (defn accumulate-seam-map-row [x y path-map row]
    "Builds a part of the seam map."
    (if (>= x w)
      [path-map row]
      (let [current-point (Point. x y)]
        (recur (inc x) y
               (assoc path-map
                 current-point (least-energy-neighbor x y))
               (conj row current-point)))))
  
  (loop [seam-map {}
         row      (sorted-set-by compare-points)
         y        0]
    (if (>= y h)
      (SeamMap. seam-map row)
      (let [[new-seam-map new-row]
            (accumulate-seam-map-row 0 y seam-map row)]
        (recur new-seam-map new-row (inc y))))))

(defn reconstruct-path [path-map entry-point]
  "walks back from the goal node to the initial node, that is marked
by having a nil parent."
  (loop [path           [entry-point]
         current-parent (path-map entry-point)]
    (if (nil? current-parent)
      (reverse path)
      (recur (conj path current-parent)
             (path-map current-parent)))))

(defn- all-seams [raster width heigth]
  "Auxiliary accumulative function to generate all seams. Sorts the
seams by the lowest energy value."
  (let [seam-map (build-seam-map raster heigth width)
        the-map  (:path-map seam-map)
        entry-points (:entry-points seam-map)]
    (for [entry-point entry-points]
      (reconstruct-path the-map entry-point))))


;; (defn generate-informed-seam [raster initial-node goal-node heigth width]
;;   "Uses A* to search for the best path to transverse a raster.
;; http://en.wikipedia.org/wiki/A*_search_algorithm#Pseudocode
;; http://nakkaya.com/2010/06/01/path-finding-using-astar-in-clojure/"
;;   (letfn
;;       [(compare-nodes [a-node another-node path-map]
;;          "compares nodes using the path map. Used in the open set to
;; figure out the next step to select from."
;;          (println (.distance a-node)
;;                   (.distance another-node))
;;          (< (+ (.distance a-node)
;;                (get-raster-pixel-value raster a-node))
;;             (+ (.distance another-node)
;;                (get-raster-pixel-value raster another-node))))

;;        (explore-path [closed-set open-set path-map]
;;          "removes a node from the open set, adds it to the closed set,
;;        figures out who the next nodes are from this current node, and
;;        collects these neighbors."
;;          (cond (empty? open-set)          false
;;                (get closed-set goal-node) path-map
;;                :else
;;                (let [current-node (first open-set)]
;;                  #(collects-neighbors (conj closed-set current-node)
;;                                       (rest open-set)
;;                                       path-map
;;                                       (filter
;;                                        (fn [a-node] (not (get closed-set a-node)))
;;                                        (next-nodes current-node width heigth))
;;                                       current-node))))

;;        (collects-neighbors [closed-set open-set path-map neighbors current-node]
;;          "Checks each neighbor, verifies if there is already a path to
;; it. If the cost of the path is less than the path of the current node
;; to it, changes the neighbor parent to this current node. If there is
;; no path to it, add the current node as parent too."
;;          (if (empty? neighbors)
;;            #(explore-path closed-set open-set path-map)
;;            #(let [neighbor          (first neighbors)
;;                   rest-of-neighbors (rest neighbors)]
;;               (if (get open-set neighbor)
;;                 ;; compares the current node with this neighbor parent
;;                 (if (compare-nodes neighbor
;;                                    (path-map current-node)
;;                                    path-map)
;;                   (collects-neighbors closed-set
;;                                       open-set
;;                                       path-map
;;                                       rest-of-neighbors
;;                                       current-node)
;;                   ;; swaps the parent for the current node
;;                   (collects-neighbors closed-set
;;                                       open-set
;;                                       (assoc path-map neighbor current-node)
;;                                       rest-of-neighbors
;;                                       current-node))
;;                 ;; adds the neighbor to the open set 
;;                 (collects-neighbors closed-set
;;                                     (conj open-set neighbor)
;;                                     (assoc path-map neighbor current-node)
;;                                     rest-of-neighbors
;;                                     current-node)))))]
    
;;     (let [initial-path-map {initial-node nil}
;;           result-path-map (trampoline explore-path
;;                                       #{}
;;                                       (sorted-set-by #(compare-nodes
;;                                                        %1 %2 initial-path-map)
;;                                                      initial-node)
;;                                       initial-path-map)]
;;       (if result-path-map
;;         (reconstruct-path result-path-map goal-node raster)
;;         false))))



(defn generate-seams [bimg]
  ^{:doc "A friendly wrapper for the all-seams function."}
  (let [raster (.getData   bimg)
        width  (.getWidth  bimg)
        heigth (.getHeight bimg)]
    (all-seams raster width heigth)))

(defn- paint-seam [bimg points color]
  "Paints a seam in some color on a clone of the image."
  (let [img-graphics (.createGraphics bimg)]
    (.setColor img-graphics color)
    (doseq [point points]
      (.drawLine img-graphics
                 (.getX point)
                 (.getY point)
                 (.getX point)
                 (.getY point)))
    bimg))

(defn paint-seams
  ([bimg seams]
     (paint-seams bimg seams (count seams) Color/WHITE))
  ([bimg seams how-many]
     (paint-seams bimg seams how-many      Color/WHITE))
  ([bimg seams how-many color]
     (let [cloned-bimg  (clone-buffered-image bimg)]
       (doseq [seam (take how-many seams)]
         (paint-seam cloned-bimg seam color))
       cloned-bimg)))

