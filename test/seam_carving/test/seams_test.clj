(ns seam-carving.seams-test
  (:use [clojure.test]
        [seam-carving.seams]
        [seam-carving.auxiliary])
  (:import [java.awt Point]))

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



;; (deftest seams-test
;;   (let [bimg (load-buffered-image "data/anatel-2.jpg")
;;         seams (generate-seams bimg :vertical)
;;         painted-bimg (paint-seams bimg seams)]
;;     (is (= (see-buffered-image painted-bimg)
;;            nil))
;;     ;; (is (= (see-buffered-image bimg)
;;     ;;        nil))
;;     ))

(deftest next-nodes-test
  (is (= (next-nodes (Point. 3 3) 4 4) #{}))
  (is (= (next-nodes (Point. 0 2) 4 4)
         #{(Point. 0 3) (Point. 1 3)}))
  (is (= (next-nodes (Point. 3 1) 4 4)
         #{(Point. 2 2) (Point. 3 2)}))
  (is (= (next-nodes (Point. 2 1) 4 4)
         #{(Point. 1 2) (Point. 2 2) (Point. 3 2)})))

(deftest search-test
  (let [bimg (load-buffered-image "data/anatel-2.jpg")]
    (println (generate-informed-seam (.getData bimg)
                                   (Point. 30 0)
                                   (Point. 30 (dec (.getHeight  bimg)))
                                   (.getWidth  bimg)
                                   (.getHeight  bimg)))))