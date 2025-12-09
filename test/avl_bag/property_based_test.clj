(ns avl-bag.property-based-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [avl-bag.core :refer :all]
            [avl-bag.bag :refer :all]))

; Генератор случайных чисел для значений
(def value-gen (gen/choose 0 100))

; Генератор случайных bag
(def bag-gen
  (gen/fmap (fn [values]
              (reduce #(add-one %1 %2) (avl-bag) values))
            (gen/vector value-gen 0 20)))

; Свойство 1: concat с empty-bag — нейтральный элемент
(defspec empty-bag-left-neutral 100
  (prop/for-all [b bag-gen]
                (equals-bag? (concat-bag (avl-bag) b) b)))

; Свойство 2: concat ассоциативно
(defspec concat-associative 100
  (prop/for-all [a bag-gen
                 b bag-gen
                 c bag-gen]
                (equals-bag?
                 (concat-bag (concat-bag a b) c)
                 (concat-bag a (concat-bag b c)))))

; Свойство 3: add-one увеличивает total-count
(defspec add-one-increases-count 100
  (prop/for-all [b bag-gen
                 v value-gen]
                (> (total-count (add-one b v))
                   (total-count b))))
