(ns seam-carving.seams
  "A seam is a connected path of low energy pixels in an image. A seam
is an optimal 8-connected path of pixels on a single image from top to
bottom, or left to right, where optimality is defined by an image
energy function. Uses a best first search to figure out the seamn
path."
  (:use [seam-carving.auxiliary :only (get-raster-pixel-value
                                       clone-buffered-image)]
        [seam-carving.operations]
        [clojure.set])
  (:import [java.awt Point Color Graphics2D]
           [java.awt.image Raster BufferedImage]))

(defrecord SeamMap
    ^{:doc "A map linking all the possible ways inside an image, and a sorted set of entry points."}
  [path-map entry-points])

(defprotocol SeamJavaInterface
  ^{:doc "This protocol exists because the Seam record is expected to
  be exposed to JAVA."}
  (getPoints [seam])
  (getEnergy [seam]))

(defrecord Seam
    ^{:doc "A collection of Points and an energy value."}
  [points energy]
  SeamJavaInterface
  (getPoints [seam]
    (into-array Point (:points seam)))
  (getEnergy [seam]
    (:energy seam)))

(defn build-seam-map [raster h w]
  "Read http://en.wikipedia.org/wiki/Seam_carving#Dynamic_Programming"

  (defn compare-points [p1 p2]
    (let [value1 (get-raster-pixel-value raster p1)
          value2 (get-raster-pixel-value raster p2)]
      ())
    (> (get-raster-pixel-value raster p1)
       (get-raster-pixel-value raster p2)))
  
  (defn least-energy-neighbor [x y]
    "assumes a vertical seam. always moves 'up' within the
bounderies passed and verifies each node in the neighborhood and
returns the one with the least amount of energy."
    (first (sort compare-points
                 (cond (== y 0) nil
                       (== x 0)
                       [(Point. x       (dec y))
                        (Point. (inc x) (dec y))]
                       (== x (dec w))
                       [(Point. x       (dec y))
                        (Point. (dec x) (dec y))]
                       :else              
                       [(Point. x       (dec y))
                        (Point. (inc x) (dec y))
                        (Point. (dec x) (dec y))]))))

  (defn accumulate-seam-map-row [x y path-map row]
    "Builds a part of the seam map."
    (if (>= x w)
      [path-map row]
      (let [current-point (Point. x y)
            next-neighbor (least-energy-neighbor x y)]
        (recur (inc x) y
               (assoc path-map current-point next-neighbor)
               (conj row current-point)))))
  
  (loop [seam-map {}
         row      #{}
         y        0]
    (if (>= y h)
      (SeamMap. seam-map (sort compare-points row))
      (let [[new-seam-map new-row]
            (accumulate-seam-map-row 0 y seam-map #{})]
        (recur new-seam-map new-row (inc y))))))

(defn builds-seam-from-path [path-map entry-point raster]
  "walks back from the goal node to the initial node, that is marked
by having a nil parent. "
  (loop [path           [entry-point]
         current-parent (path-map entry-point)
         energy         (get-raster-pixel-value raster entry-point)]
    (if (nil? current-parent)
      (Seam. (reverse path) energy)
      (recur (conj path current-parent)
             (path-map current-parent)
             (+ energy
                (get-raster-pixel-value raster current-parent))))))

(defn- all-seams [raster width heigth]
  "Auxiliary accumulative function to generate all seams. Sorts the
seams by the lowest energy value."
  (let [seam-map     (build-seam-map raster heigth width)
        the-map      (:path-map seam-map)
        entry-points (:entry-points seam-map)]
    (sort #(< (:energy %1)
              (:energy %2))
          (for [entry-point entry-points]
            (builds-seam-from-path the-map entry-point raster)))))

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
  ([bimg seams] (paint-seams bimg seams Color/WHITE))
  ([bimg seams color]
     "Creates a new image like the buffered image passed and paints
all seams passed in it with a given color."
     (let [cloned-bimg  (clone-buffered-image bimg)]
       (doseq [seam seams]
         (paint-seam cloned-bimg (:points seam) color))
       cloned-bimg)))


