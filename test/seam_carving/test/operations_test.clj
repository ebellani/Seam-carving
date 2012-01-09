(ns seam-carving.operations-test
  (:use [clojure.test]
        [seam-carving.operations]
        [seam-carving.auxiliary]))


(deftest check-images
  (let [large-img (load-buffered-image "data/Valve_original_(1).PNG")
        small-img (load-buffered-image "data/1E2.jpg")]
    
    (= (see-buffered-image (sobel-operator small-img)) nil)
    ;; (= (see-buffered-image (second (sobel-operator small-img))) nil)
  
    ;; (= (see-buffered-image
    ;;     (sobel-operator large-img)) nil)
    ))


