(ns seam-carving.auxiliary-test
  (:use [clojure.test]
        [seam-carving.auxiliary])
  (:import [java.awt.image BufferedImage]))

(deftest check-image-size
  "Checks to see if the image loaded from the data directory has the
  correct size, just in case."
  (is (= (-> (load-buffered-image "data/BGE.jpg") .getHeight) 50))
  (is (= (-> (load-buffered-image "data/BGE.jpg") .getWidth) 200)))

(deftest see-an-image
  "Check an image of the data directory. You will have to use your
eyes."
  (is (= (see-buffered-image (load-buffered-image "data/BGE.jpg")) nil)))

(deftest dividing-workload-tests
  (is (= (divide-workload 250 4)
         [[0 62] [62 124] [124 186] [186 250]])))
