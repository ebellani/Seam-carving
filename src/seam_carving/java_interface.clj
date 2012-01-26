(ns seam-carving.java-interface
  "Used as an interface from a JAVA program to call the interesting
functions of the seam carving project."
  (:use [seam-carving.operations :only (sobel-operator)]
        [seam-carving.seams      :only (generate-seams)])
  (:import [java.awt Point]
           [seam_carving.seams Seam])
  (:gen-class
   :name seams.Operations
   :methods [#^{:static true} [getSeams [java.awt.image.BufferedImage]
                               "[Lseam_carving.seams.Seam;"]
             #^{:static true} [applySobelOperator [java.awt.image.BufferedImage]
                               java.awt.image.BufferedImage]]))

(defn -getSeams [bimg]
  "Wrapper to be called from JAVA to obtain all the seams, sorted by
the least energetic. It will apply the sobel operator to get the
seams."
  (into-array Seam (generate-seams bimg)))

(defn -applySobelOperator [bimg]
  "Wrapper to be called from JAVA to obtain a new BufferedImage representing the sobel operator applied to the image passed."
  (sobel-operator bimg))

