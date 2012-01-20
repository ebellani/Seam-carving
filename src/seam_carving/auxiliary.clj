(ns seam-carving.auxiliary
  "Auxiliary functions. Things like I/O, displaying images and so
  forth."
  (:use    [clojure.java.io :only (file)]
           [clojure.reflect]
           [clojure.pprint])
  (:import [java.awt       Point]
           [java.awt.image BufferedImage]
           [javax.swing    ImageIcon JOptionPane JLabel]
           [javax.imageio  ImageIO]))

(defn see-buffered-image [bimg]
  {:pre  [(= (class bimg) java.awt.image.BufferedImage)]}
  "Pops up a dialog showing the BufferedImaged bimg."
  (let [icon (ImageIcon. bimg)]
    (JOptionPane/showMessageDialog nil (JLabel. icon))))

(defn load-buffered-image [filepath]
  {:pre  [(string? filepath)]
   :post [(= (class %) java.awt.image.BufferedImage)]}
  "Returns a BufferedImage from a given filepath."
  (ImageIO/read (file filepath)))

(defn print-dissected-java-object [obj]
  (pprint (reflect obj)))

(defn divide-workload [workload between]
  "Creates the indexes to be able iterate in the X axis of an image
    in parallel."
  (let [divided-workload (int (Math/floor (/ workload between)))
        remainder        (mod workload divided-workload)]
    (loop [min-index 0
           indexes []
           number-of-pairs between]
      (if (= 1 number-of-pairs)
        (conj indexes [min-index (+ remainder
                                    min-index
                                    divided-workload)])
        (recur (+ min-index divided-workload)
               (conj indexes [min-index (+ min-index divided-workload)])
               (dec number-of-pairs))))))

(defn get-raster-pixel-value
  ^{:doc
    "Returns the integer value of a greyscaled raster image at a point."}
  ([raster point]
     (get-raster-pixel-value raster
                             (int (. point getX))
                             (int (. point getY))))
  ([raster x y]
     (.getSample raster x y 0)))

(defn clone-buffered-image [bimg]
  "Provides a way to clone a buffered image so we can still have
referential transparency."
  (BufferedImage. (.getColorModel        bimg)
                  (.copyData bimg nil)
                  (.isAlphaPremultiplied bimg)
                  nil))


(defmacro with-tst-imgs [& body]
  "exposes 3 identifiers to use for testing purposes."
  `(let [~'large-img  (load-buffered-image "data/anatel-5.jpg")
         ~'medium-img (load-buffered-image "data/anatel-5.jpg")
         ~'small-img (load-buffered-image "data/cropped-anatel.jpg")]
     ~@body))
