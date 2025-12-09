(ns avl-bag.unit-test
  (:require [clojure.test :refer :all]
            [avl-bag.core :refer :all]
            [avl-bag.bag :refer :all]))

(deftest avlbag-full-tests
  ; Создание и пустой bag
  (let [empty (avl-bag)]
    (is (= 0 (total-count empty)))
    (is (= 0 (unique-items-count empty)))
    (is (false? (has? empty 1))))

  ; add и add-one
  (let [bag1 (-> (avl-bag)
                 (add-one 10)
                 (add 20 3))]
    (is (= 4 (total-count bag1)))         ; 1 + 3
    (is (= 2 (unique-items-count bag1)))
    (is (has? bag1 10))
    (is (= 1 (count-of bag1 10)))
    (is (= 3 (count-of bag1 20))))

  ; del и del-one
  (let [bag2 (-> (avl-bag)
                 (add 5 3)
                 (add-one 10)
                 (del-one 5)
                 (del 10 1))]
    (is (= 2 (count-of bag2 5)))
    (is (= 0 (count-of bag2 10)))
    (is (not (has? bag2 10)))
    (is (has? bag2 5))
    (is (= 2 (total-count bag2)))
    (is (= 1 (unique-items-count bag2))))

  ; set-count
  (let [bag3 (-> (avl-bag)
                 (add-one 1)
                 (set-count 1 5)
                 (set-count 2 0))] ; count 0 не добавляется
    (is (= 5 (count-of bag3 1)))
    (is (= 1 (unique-items-count bag3)))
    (is (not (has? bag3 2))))

  ; map-bag
  (let [bag4 (-> (avl-bag)
                 (add 1 2)
                 (add-one 3)
                 (map-bag #(* % 10)))]
    (is (= 2 (count-of bag4 10)))
    (is (= 1 (count-of bag4 30)))
    (is (has? bag4 10))
    (is (has? bag4 30)))

  ; filter-bag
  (let [bag5 (-> (avl-bag)
                 (add 1 2)
                 (add-one 3)
                 (add-one 4)
                 (filter-bag even?))]
    (is (= 1 (total-count bag5)))
    (is (= 1 (unique-items-count bag5)))
    (is (has? bag5 4))
    (is (not (has? bag5 1)))
    (is (not (has? bag5 3))))

  ; fold-left и fold-right
  (let [bag6 (-> (avl-bag)
                 (add 1 2)
                 (add-one 3))]
    (is (= 5 (fold-left bag6 + 0)))   ; 1+1+3
    (is (= 5 (fold-right bag6 + 0))))

  ; empty-bag
  (let [bag7 (-> (avl-bag)
                 (add 1 2)
                 (add-one 2)
                 (empty-bag))]
    (is (= 0 (total-count bag7)))
    (is (= 0 (unique-items-count bag7))))

  ; concat-bag
  (let [bag8a (-> (avl-bag)
                  (add-one 1)
                  (add 2 2))
        bag8b (-> (avl-bag)
                  (add 2 3)
                  (add-one 3))
        bag8c (concat-bag bag8a bag8b)]
    (is (= 7 (total-count bag8c))) ; 1 + (2+3) + 1
    (is (= 3 (unique-items-count bag8c)))
    (is (= 1 (count-of bag8c 1)))
    (is (= 5 (count-of bag8c 2)))
    (is (= 1 (count-of bag8c 3))))

  ; equals-bag?
  (let [bag9a (-> (avl-bag)
                  (add 1 2)
                  (add 2 3))
        bag9b (-> (avl-bag)
                  (add 2 3)
                  (add 1 2))
        bag9c (-> (avl-bag)
                  (add 1 2)
                  (add 2 2))]
    (is (equals-bag? bag9a bag9b))
    (is (not (equals-bag? bag9a bag9c)))))

(deftest satisfies-bag
  (is (satisfies? Bag (avl-bag)))
  (is (satisfies? Bag (->AvlBag nil))))
