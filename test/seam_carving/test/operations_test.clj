(ns seam-carving.operations-test
  (:use [clojure.test]
        [seam-carving.operations]
        [seam-carving.auxiliary]))


(deftest grayscale-op-test
  (with-tst-imgs
    (let [gs (.getData (->grayscale medium-img))
          cv (filter-image sobel-vertical-operation
                           medium-img)
          sobel (sobel-operator small-img)
          ]
      (see-buffered-image sobel)
      )))


