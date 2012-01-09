(ns seam-carving.seams
  "A seam is a connected path of low energy pixels in an image. A seam
is an optimal 8-connected path of pixels on a single image from top to
bottom, or left to right, where optimality is defined by an image
energy function. Uses a best first search to figure out the seamn
path."
  (:use [seam-carving.auxiliary :only (get-raster-pixel-value
                                       clone-buffered-image)]
        [clojure.set])
  (:import [java.awt Point Color Graphics2D]
           [java.awt.image Raster BufferedImage]))

(defrecord Seam
    ^{:doc "Contains the a symbol [vertical|horizontal] for the type,
    a vector of points and the total amount of energy."}
  [type points energy])

;; (defn next-steps [side-pos forward-pos
;;                   max-side max-forward
;;                   point-producer-fn]
;;   "Abstracts the next-*-steps. Needs the bounderies and a function
;; that produces Points from the step values. Returns a set of points."
;;   (cond (= forward-pos (dec max-forward)) #{}
;;         (zero? side-pos) #{(point-producer-fn (inc forward-pos)
;;                                               side-pos)
;;                            (point-producer-fn (inc forward-pos)
;;                                               (inc side-pos))}
;;         (= side-pos (dec max-side))
;;         #{(point-producer-fn (inc forward-pos) side-pos)
;;           (point-producer-fn (inc forward-pos) (dec side-pos))}
;;         :else #{(point-producer-fn (inc forward-pos) (inc side-pos))
;;                 (point-producer-fn (inc forward-pos) side-pos)
;;                 (point-producer-fn (inc forward-pos) (dec side-pos))}))

;; (defn next-vertical-steps [x y width heigth]
;;   (next-steps x y width heigth
;;               (fn [point-x point-y]
;;                 (Point. point-y point-x))))

;; (defn next-horizontal-steps [x y width heigth]
;;   (next-steps x y heigth width
;;               (fn [point-y point-x]
;;                 (Point. point-y point-x))))

;; (defn generate-seam [raster
;;                      initial-index
;;                      width
;;                      heigth
;;                      seam-type]
;;   "Generates a single seam using BFS search. The heading of the seam
;; is directed by the next steps returned by the next-steps-fn.  Returns
;; each point with the exception of the first one."
;;   (defn get-largest-point-and-energy [points]
;;     "Returns both the point and its energy."
;;     (reduce #(let [[last-point last-energy] %1
;;                    next-point               %2
;;                    next-energy (get-raster-pixel-value raster %2)]
;;                (if (or (empty? %1)
;;                        (< last-energy next-energy))
;;                  [next-point next-energy]
;;                  %1))
;;             [] points))
;;   (loop [points     [] 
;;          path-index 0
;;          energy     0]
;;     (let [next-neighbors-fun (if (= seam-type :vertical)
;;                                next-vertical-steps
;;                                next-horizontal-steps)
;;           expanded-neighbors (next-neighbors-fun initial-index
;;                                                  path-index
;;                                                  width heigth)]
;;       (if (empty? expanded-neighbors)
;;         (Seam. seam-type points energy)
;;         (let [[point point-energy] (get-largest-point-and-energy expanded-neighbors)]
;;           (recur (conj points point)
;;                  (inc path-index)
;;                  (+ energy point-energy)))))))

(defn next-nodes [current-node h w]
  "Assumes a vertical seam. Always moves 'down' within the bounderies
passed."
  (let [x (.getX current-node)
        y (.getY current-node)]
    (cond (== y (dec h)) #{}
          (== x 0)
          #{(Point. (inc x) (inc y))
            (Point. x       (inc y))}
          (== x (dec w))
          #{(Point. (dec x) (inc y))
            (Point. x       (inc y))}
          :else
          #{(Point. (inc x) (inc y))
            (Point. (dec x) (inc y))
            (Point. x       (inc y))})))

(defn generate-informed-seam [raster initial-node goal-node heigth width]
  "Uses A* to search for the best path to transverse a raster.
http://en.wikipedia.org/wiki/A*_search_algorithm#Pseudocode
http://nakkaya.com/2010/06/01/path-finding-using-astar-in-clojure/"
  (letfn
      [(distance-from-start [a-node]
         (.distance a-node initial-node))

       (heuristic-cost-estimate [a-node]
         (+ (distance-from-start a-node)       
            (.distance a-node goal-node)          
            (get-raster-pixel-value raster a-node)))

       (main-loop      [closed-set open-set path-map]
         (cond (empty? open-set)          false
               (get closed-set goal-node) path-map
               :else
               (let [lowest-energy-node-found (first open-set)]
                 (auxiliary-loop (conj closed-set lowest-energy-node-found)
                                 (rest open-set)
                                 path-map
                                 (filter #(not (get closed-set %1))
                                         (next-nodes lowest-energy-node-found
                                                     width heigth))
                                 lowest-energy-node-found))))

       (auxiliary-loop [closed-set open-set path-map
                        neighbors lowest-energy-node-found]
         (if (empty? neighbors)
           (main-loop closed-set open-set path-map)
           (let [neighbor          (first neighbors)
                 rest-of-neighbors (rest neighbors)]
             (if (get open-set neighbor)
               (if (> (distance-from-start (path-map neighbor))
                      (distance-from-start lowest-energy-node-found))
                 (recur closed-set open-set path-map rest-of-neighbors
                        lowest-energy-node-found)
                 (recur closed-set open-set
                        (assoc path-map neighbor lowest-energy-node-found)
                        rest-of-neighbors lowest-energy-node-found))
               (recur closed-set
                      (conj open-set neighbor)
                      (assoc path-map neighbor lowest-energy-node-found)
                      rest-of-neighbors lowest-energy-node-found)))))]
    (let [path-map (main-loop #{}
                              (sorted-set-by #(< (heuristic-cost-estimate %1)
                                                 (heuristic-cost-estimate %2))
                                             initial-node)
                              {initial-node nil})]
      (if path-map (reconstruct-path path-map goal-node)
          false))))

(defn reconstruct-path [path-map goal-node]
  "walks back from the goal node to the initial node, that is marked
by having a nil parent."
  (loop [path           [goal-node]
         current-parent (path-map goal-node)]
    (if (nil? current-parent)
      (reverse path)
      (recur (conj path current-parent)
             (path-map current-parent)))))

(defn- all-seams [raster up-to width heigth type]
  "Auxiliary accumulative function to generate all seams. Sorts the
seams by the lowest energy value."
  (sort-by :energy <
           (pmap #(generate-seam raster %1 width heigth type)
                 (range up-to))))

(defn generate-seams
  ^{:doc "Generate all seams for a given image. It is advisable to use
  a image that has been processed by some energy operator.  You can
  control the types of seams, :vertical or :horizontal with the type
  argument. Returns false for an invalid type."}
  ([bimg] (generate-seams bimg :vertical))
  ([bimg type]
     (let [raster (.getData   bimg)
           width  (.getWidth  bimg)
           heigth (.getHeight bimg)]
       (cond (= type :vertical)
             (all-seams raster width width heigth :vertical)
             (= type :horizontal)
             (all-seams raster heigth width heigth :horizontal)
             :else false))))

(defn- paint-seam [bimg seam color]
  "Paints a seam in some color on a clone of the image."
  ;; Graphics2D g2 = img.createGraphics();
  ;; String text = "" + codePoint;
  ;; g2.setColor(color);
  ;; Graphics.drawLine(x, y, x, y);
  (let [img-graphics (.createGraphics bimg)]
    (.setColor img-graphics color)
    (doseq [point (:points seam)]
      (.drawLine img-graphics ;;ahhhh Java
                 (.getX point)
                 (.getY point)
                 (.getX point)
                 (.getY point)))
    bimg))

(defn paint-seams
  ([bimg seams] (paint-seams bimg seams Color/WHITE))
  ([bimg seams color]
     (let [cloned-bimg  (clone-buffered-image bimg)]
       (doseq [seam (take 50 seams)]
         (paint-seam cloned-bimg seam color))
       cloned-bimg)))

