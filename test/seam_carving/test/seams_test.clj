(ns seam-carving.seams-test
  (:use [clojure.test]
        [seam-carving.seams]
        [seam-carving.auxiliary]
        [seam-carving.operations])
  (:import [java.awt Point Color]))

;; (deftest steps-test
;;   (is (= (next-vertical-steps 2 2 4 4)
;;          #{(Point. 1 3) (Point. 2 3) (Point. 3 3)}))
;;   (is (= (next-vertical-steps 0 0 4 4)
;;          #{(Point. 0 1) (Point. 1 1)}))
;;   (is (= (next-vertical-steps 2 1 3 3)
;;          #{(Point. 1 2) (Point. 2 2)}))
;;   (is (= (next-vertical-steps 2 2 3 3)
;;          #{}))

;;   (is (= (next-horizontal-steps 0 1 3 3)
;;          #{(Point. 1 0) (Point. 1 1) (Point. 1 2)}))
;;   (is (= (next-horizontal-steps 1 2 3 3)
;;          #{(Point. 2 1) (Point. 2 2)}))
;;   (is (= (next-horizontal-steps 2 1 3 3)
;;          #{})))



(deftest seams-test
  (with-tst-imgs
    (let [sobel-bimg       (sobel-operator small-img)
          seams            (generate-seams sobel-bimg)
          ;; painted-sobel    (paint-seams sobel-bimg seams 15)
          painted-original (paint-seams small-img seams 15)
          ]
      (println seams)
      ;; (doseq [seam seams]
      ;;   (println (:energy seam)))
      ;; (println (last seams))
      ;; (pcalls #(see-buffered-image painted-original)
      ;;         #(see-buffered-image painted-sobel)
      ;;         #(see-buffered-image sobel-bimg))
      ;; (is (= (see-buffered-image bimg)
      ;;        nil))
      (see-buffered-image painted-original)
      ;; (see-buffered-image painted-sobel)
      ))
  )



(deftest next-nodes-test
  (is (= (next-nodes (Point. 3 3) 4 4) #{}))
  (is (= (next-nodes (Point. 0 2) 4 4)
         #{(Point. 0 3) (Point. 1 3)}))
  (is (= (next-nodes (Point. 3 1) 4 4)
         #{(Point. 2 2) (Point. 3 2)}))
  (is (= (next-nodes (Point. 2 1) 4 4)
         #{(Point. 1 2) (Point. 2 2) (Point. 3 2)})))
