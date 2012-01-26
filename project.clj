(defproject seam-carving "1.1.1"
  :description
  "Should generate seams and related functions, such as resizing and
  carving."
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[swank-clojure   "1.4.0-SNAPSHOT"]]
  :aot [seam-carving.seams
        seam-carving.java-interface])