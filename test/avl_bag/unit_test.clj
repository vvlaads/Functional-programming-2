(ns avl-bag.unit-test
  (:require
   [avl-bag.bag :refer [Bag add add-one concat-bag count-of del del-one empty-bag equals-bag? filter-bag fold-left fold-right has? map-bag set-count total-count unique-items-count]]
   [avl-bag.core :refer [avl-bag]]
   [clojure.test :refer [deftest is]]))

; Проверка базовых операций с числами
(deftest avlbag-functional-tests
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

; Проверка работы с разными типами
(deftest avlbag-mixed-types-tests
  ; Тест 1: Хранение различных типов в одном bag
  (let [mixed-bag (-> (avl-bag)
                      (add-one 42)                     ; число
                      (add-one "hello")                ; строка  
                      (add-one :keyword)               ; ключевое слово
                      (add-one 'symbol)                ; символ
                      (add-one 3.14)                   ; float
                      (add-one true)                   ; boolean
                      (add-one nil)                    ; nil
                      (add-one [1 2 3])                ; вектор
                      (add-one {:a 1})                 ; map
                      (add-one #{:x :y}))]             ; set
    (is (= 10 (total-count mixed-bag)))
    (is (= 10 (unique-items-count mixed-bag)))
    (is (has? mixed-bag 42))
    (is (has? mixed-bag "hello"))
    (is (has? mixed-bag :keyword))
    (is (has? mixed-bag 'symbol))
    (is (has? mixed-bag true))
    (is (has? mixed-bag nil))
    (is (has? mixed-bag [1 2 3]))
    (is (has? mixed-bag {:a 1}))
    (is (has? mixed-bag #{:x :y})))

  ; Тест 2: Одинаковые значения разных типов считаются разными элементами
  (let [numeric-bag (-> (avl-bag)
                        (add "42" 2)      ; строка "42"
                        (add 42 3)        ; число 42
                        (add 42.0 1)      ; float 42.0
                        (add \4 1))]      ; символ '4'
    (is (= 7 (total-count numeric-bag)))
    (is (= 4 (unique-items-count numeric-bag)))
    (is (= 2 (count-of numeric-bag "42")))
    (is (= 3 (count-of numeric-bag 42)))
    (is (= 1 (count-of numeric-bag 42.0)))
    (is (= 1 (count-of numeric-bag \4))))

  ; Тест 3: Удаление элементов разных типов
  (let [bag (-> (avl-bag)
                (add "text" 3)
                (add :kw 2)
                (add 100 4)
                (del "text" 2)
                (del-one :kw)
                (del 100 1))]
    (is (= 1 (count-of bag "text")))
    (is (= 1 (count-of bag :kw)))
    (is (= 3 (count-of bag 100)))
    (is (= 5 (total-count bag))))

  ; Тест 4: set-count с разными типами
  (let [bag (-> (avl-bag)
                (add-one :a)
                (add-one "b")
                (set-count :a 5)
                (set-count "b" 3))]
    (is (= 5 (count-of bag :a)))
    (is (= 3 (count-of bag "b")))
    (is (= 8 (total-count bag))))

  ; Тест 5: map-bag с преобразованием типов
  (let [bag (-> (avl-bag)
                (add 1 2)
                (add "two" 1)
                (add :three 1)
                (map-bag str))] ; преобразуем все в строки
    (is (= 2 (count-of bag "1")))
    (is (= 1 (count-of bag "two")))
    (is (= 1 (count-of bag ":three")))
    (is (not (has? bag 1)))    ; оригинального числа больше нет
    (is (not (has? bag :three)))) ; оригинального ключевого слова больше нет

  ; Тест 6: filter-bag с предикатами для разных типов
  (let [bag (-> (avl-bag)
                (add 1 2)
                (add "hello" 3)
                (add :kw 1)
                (add [1] 2)
                (filter-bag #(or (number? %)
                                 (keyword? %))))]
    (is (= 3 (total-count bag))) ; только 1 (2 раза) и :kw (1 раз)
    (is (= 2 (unique-items-count bag)))
    (is (has? bag 1))
    (is (has? bag :kw))
    (is (not (has? bag "hello")))
    (is (not (has? bag [1]))))

  ; Тест 7: fold-left/fold-right с разнотипными элементами
  (let [bag (-> (avl-bag)
                (add 10 2)
                (add " items" 1))]
    ; Приводим все к строке
    (is (= " 10 10  items"
           (fold-left bag
                      (fn [acc v] (str acc " " (str v)))
                      "")))

    ; Подсчитываем только числа
    (is (= 20 (fold-left bag
                         (fn [acc v]
                           (if (number? v)
                             (+ acc v)
                             acc))
                         0))))

  ; Тест 8: concat-bag с разными типами
  (let [bag1 (-> (avl-bag)
                 (add 1 2)
                 (add "a" 1))
        bag2 (-> (avl-bag)
                 (add :x 3)
                 (add 1 1))
        combined (concat-bag bag1 bag2)]
    (is (= 7 (total-count combined)))
    (is (= 3 (unique-items-count combined)))
    (is (= 3 (count-of combined 1))) ; 2 из bag1 + 1 из bag2
    (is (= 1 (count-of combined "a")))
    (is (= 3 (count-of combined :x))))

  ; Тест 9: equals-bag? с разными типами
  (let [bag-a (-> (avl-bag)
                  (add 1 2)
                  (add "text" 1)
                  (add :kw 3))
        bag-b (-> (avl-bag)
                  (add :kw 3)
                  (add "text" 1)
                  (add 1 2))
        bag-c (-> (avl-bag)
                  (add 1 2)
                  (add "text" 1))]
    (is (equals-bag? bag-a bag-b)) ; одинаковые элементы в разном порядке
    (is (not (equals-bag? bag-a bag-c))) ; разные наборы элементов

  ; Тест 10: Сложные структуры данных
    (let [bag (-> (avl-bag)
                  (add [1 2 3] 2)
                  (add {:x 1
                        :y 2} 1)
                  (add #{:a :b :c} 1))]
      (is (= 4 (total-count bag)))
      (is (= 3 (unique-items-count bag)))
      (is (has? bag [1 2 3]))
      (is (has? bag {:x 1
                     :y 2}))
      (is (has? bag #{:a :b :c}))

    ; Проверяем, что идентичные структуры считаются одинаковыми
      (let [bag2 (-> (avl-bag)
                     (add [1 2 3] 1)
                     (add [1 2 3] 1))] ; два отдельных добавления
        (is (= 2 (count-of bag2 [1 2 3])))))

  ; Тест 11: Граничный случай - добавление/удаление nil
    (let [bag (-> (avl-bag)
                  (add nil 3)
                  (del nil 2))]
      (is (= 1 (count-of bag nil)))
      (is (= 1 (total-count bag)))
      (is (= 1 (unique-items-count bag))))))

; Проверка на соответсвию протоколу
(deftest satisfies-bag
  (is (satisfies? Bag (avl-bag))))

(deftest clojure-interfaces-tests
  ; Seqable - можно ли получить последовательность
  (let [bag (-> (avl-bag)
                (add 1 2)
                (add 2 1))]
    (is (seq? (seq bag)))
    (is (= [1 1 2] (sort (seq bag)))) ; сортируем, т.к. порядок может быть любой
    (is (= 3 (count (seq bag)))))

  ; Counted - работает ли count
  (let [bag (-> (avl-bag)
                (add 1 3)
                (add 2 2))]
    (is (= 5 (count bag))))

  ; IPersistentCollection - conj и empty
  (let [bag (-> (avl-bag)
                (add 1 2))]
    (is (= 3 (count-of (conj bag 1) 1))) ; conj должен увеличивать count
    (is (= 1 (count-of (conj bag 2) 2))) ; conj для нового элемента
    (is (empty? (empty bag)))
    (is (= 0 (count (empty bag)))))

  ; ILookup/IFn - доступ как функция и через get
  (let [bag (-> (avl-bag)
                (add :a 3)
                (add :b 1))]
    (is (= 3 (bag :a))) ; как функция
    (is (= 1 (bag :b)))
    (is (= 0 (bag :c))) ; отсутствующий элемент
    (is (= :not-found (bag :d :not-found))) ; с значением по умолчанию

    ; через get (использует ILookup)
    (is (= 3 (get bag :a)))
    (is (= 1 (get bag :b)))
    (is (= 0 (get bag :c)))
    (is (= :default (get bag :e :default))))

  ; Equiv - сравнение через =
  (let [bag1 (-> (avl-bag) (add 1 2) (add 2 1))
        bag2 (-> (avl-bag) (add 2 1) (add 1 2))
        bag3 (-> (avl-bag) (add 1 1) (add 2 2))]
    (is (= bag1 bag2)) ; одинаковые мультимножества
    (is (not= bag1 bag3)) ; разные
    (is (not= bag1 [1 1 2])))) ; сравнение с другим типом