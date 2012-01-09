(ns seam-carving.kernels
  "A collection of possible kernels to use in
    transformations."
  (:import [java.awt.image Kernel]))

(def vertical-sobel-format
  ^{:private true}
  [1 2 1
   0 0 0
   -1 -2 -1])

(def horizontal-sobel-format
  ^{:private true}
  [-1 0 1 
   -2 0 2 
   -1 0 1])

(def vertical-sobel-kernel
  ^{:doc "Detects vertical changes. See
  http://homepages.inf.ed.ac.uk/rbf/HIPR2/sobel.htm and
  http://en.wikipedia.org/wiki/Sobel_operator#Formulation"}
  (Kernel. 3 3 (float-array vertical-sobel-format)))

(def horizontal-sobel-kernel
  ^{:doc "Detects horizontal changes. See
  http://homepages.inf.ed.ac.uk/rbf/HIPR2/sobel.htm and
  http://en.wikipedia.org/wiki/Sobel_operator#Formulation"}
  (Kernel. 3 3 (float-array horizontal-sobel-format)))

