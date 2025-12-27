# Лабораторная работа №2

  * Студент: `Силинцев Владислав Витальевич`
  * Группа: `P3314`
  * ИСУ: `355273`
  * Функциональный язык: `Clojure`
  * Вариант: `avl-bag`
---

## Требования к разработанному ПО

Реализовать структуру данных **bag** (мультимножество) на основе **AVL-дерева** с выполнением следующих требований:

### Функциональные требования:
 - Добавление элемента (`add`, `add-one`);
 - Удаление элемента (`del`, `del-one`);
 - Фильтрация (`filter-bag`);
 - Отображение (`map-bag`);
 - Свёртки: левая и правая (`fold-left`, `fold-right`);
 - Сравнение двух bag (`equals-bag?`);
 - Операции моноида:
   - нейтральный элемент (`empty-bag`);
   - бинарная операция (`concat-bag`);
   - выполнение законов ассоциативности.

### Нефункциональные требования:
 - Структура данных должна быть **неизменяемой**;
 - Реализация должна быть **полиморфной**;
 - API написан в идиоматичном стиле Clojure;
 - Внутреннее представление не «протекает» наружу;
 - Unit-тесты + property-based тестирование;
 - Эффективное сравнение мультимножеств (без сортировок списков).

---

## Ключевые элементы реализации
### Интерфейс Bag:
```clojure
(ns avl-bag.bag)

(defprotocol Bag
  (add
    [this element count])
  (add-one
    [this element])
  (del
    [this element count])
  (del-one
    [this element])

  (set-count
    [this element count])
  (has?
    [this element])
  (count-of
    [this element])

  (total-count
    [this])
  (unique-items-count
    [this])

  (map-bag
    [this f])
  (filter-bag
    [this pred])

  (fold-left
    [this f init])
  (fold-right
    [this f init])

  (empty-bag
    [this])
  (concat-bag
    [this bag])
  (equals-bag?
    [this bag]))
```

### Создание узла AVL-дерева:
```clojure
; Создание узла
(defn- init-node [value count left right]
  {:value value
   :count count
   :left left
   :right right})
```

### Вращения AVL-дерева для балансировки:
```clojure
; Малое левое вращение
(defn- small-left-rotation [a]
  (let [l (:left a)
        b (:right a)
        c (:left b)
        r (:right b)]
    (init-node (:value b)
               (:count b)
               (init-node (:value a)
                          (:count a)
                          l
                          c)
               r)))

; Большое левое вращение
(defn- big-left-rotation [a]
  (let [l (:left a)
        b (:right a)
        c (:left b)
        r (:right b)
        m (:left c)
        n (:right c)]
    (init-node (:value c)
               (:count c)
               (init-node (:value a)
                          (:count a)
                          l
                          m)
               (init-node (:value b)
                          (:count b)
                          n
                          r))))

; Малое правое вращение
(defn- small-right-rotation [a]
  (let [b (:left a)
        l (:left b)
        c (:right b)
        r (:right a)]
    (init-node (:value b)
               (:count b)
               l
               (init-node (:value a)
                          (:count a)
                          c
                          r))))

; Большое правое вращение
(defn- big-right-rotation [a]
  (let [r (:right a)
        b (:left a)
        l (:left b)
        c (:right b)
        m (:left c)
        n (:right c)]
    (init-node (:value c)
               (:count c)
               (init-node (:value b)
                          (:count b)
                          l
                          m)
               (init-node (:value a)
                          (:count a)
                          n
                          r))))
```

### Балансировка AVL-дерева:
```clojure
; Высота дерева с корнем в выбранном узле
(defn- height [node]
  (if (nil? node)
    0
    (+ 1 (max (height (:left node)) (height (:right node))))))

; Балансировка AVL-дерева
(defn- balance [node]
  (let [left-height (-> node :left height)
        right-height (-> node :right height)]
    (cond (> (- right-height left-height) 1)
          (if (<= (-> node :right :left height)
                  (-> node :right :right height))
            (small-left-rotation node)
            (big-left-rotation node))

          (> (- left-height right-height) 1)
          (if (<= (height (:right (:left node))) (height (:left (:left node))))
            (small-right-rotation node)
            (big-right-rotation node))

          :else node)))
```

### Разработанный тип данных и функция-конструктор:
```clojure
; Тип данных AVL-BAG
(deftype AvlBag [root]
  Bag
  (add
    [_ element count]
    (if (> count 0)
      (AvlBag. (add-value root element count))
      (AvlBag. root)))

  (add-one
    [_ element]
    (AvlBag. (add-value root element 1)))

  (del
    [_ element count]
    (AvlBag. (del-value root element count)))

  (del-one
    [_ element]
    (AvlBag. (del-value root element 1)))

  (set-count
    [_ element count]
    (if (> count 0)
      (AvlBag. (set-count-for-value root element count))
      (AvlBag. root)))

  (has?
    [_ element]
    (has-value root element))

  (count-of
    [_ element]
    (count-of-value root element))

  (total-count
    [_]
    (count-of-elements root))

  (unique-items-count
    [_]
    (count-of-unique-elements root))

  (map-bag
    [_ f]
    (AvlBag. (map-node-into-root root nil f)))

  (filter-bag
    [_ pred]
    (AvlBag. (filter-node-into-root root nil pred)))

  (fold-left
    [_ f init]
    (fold-left-node root f init))

  (fold-right
    [_ f init]
    (fold-right-node root f init))

  (empty-bag
    [_]
    (AvlBag. nil))

  (concat-bag
    [this bag]
    (.fold-left bag
                (fn [acc v] (.add acc v 1))
                this))

  (equals-bag?
    [this other-bag]
    (and (= (.total-count this) (.total-count other-bag))
         (= (.unique-items-count this) (.unique-items-count other-bag))
         (.fold-left this
                     (fn [acc v]
                       (and acc
                            (= (.count-of this v)
                               (.count-of other-bag v))))
                     true)))

  clojure.lang.Seqable
  (seq [this]
    (let [elements (.fold-left this conj [])]
      (seq elements)))

  clojure.lang.Counted
  (count [this]
    (.total-count this))

  clojure.lang.IPersistentCollection
  (cons [this element]
    (.add-one this element))
  (empty [_]
    (AvlBag. nil))
  (equiv [this other]
    (cond
      (identical? this other) true
      (instance? AvlBag other) (.equals-bag? this other)
      :else false))

  clojure.lang.ILookup
  (valAt [this key]
    (.count-of this key))
  (valAt [this key not-found]
    (let [cnt (.count-of this key)]
      (if (zero? cnt) not-found cnt)))

  clojure.lang.IFn
  (invoke [this key]
    (.count-of this key))
  (invoke [this key not-found]
    (let [cnt (.count-of this key)]
      (if (zero? cnt) not-found cnt))))

; Функция-конструктор
(defn avl-bag
  ([] (AvlBag. nil)))
```

## Тестирование
### Unit-тесты
Создание мультимножества:
```clojure
  ; Создание и пустой bag
  (let [empty (avl-bag)]
    (is (= 0 (total-count empty)))
    (is (= 0 (unique-items-count empty)))
    (is (false? (has? empty 1))))
```

Добавление элемента:
```clojure
  ; add и add-one
  (let [bag1 (-> (avl-bag)
                 (add-one 10)
                 (add 20 3))]
    (is (= 4 (total-count bag1)))         ; 1 + 3
    (is (= 2 (unique-items-count bag1)))
    (is (has? bag1 10))
    (is (= 1 (count-of bag1 10)))
    (is (= 3 (count-of bag1 20))))
```

Отображение мультимножества:
```clojure
  ; map-bag
  (let [bag4 (-> (avl-bag)
                 (add 1 2)
                 (add-one 3)
                 (map-bag #(* % 10)))]
    (is (= 2 (count-of bag4 10)))
    (is (= 1 (count-of bag4 30)))
    (is (has? bag4 10))
    (is (has? bag4 30)))
```

Использование стандартных протоколов коллекции:
```clojure
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
```
---

### Property-based тестирование
Свойство нейтрального элемента:
```clojure
; Свойство 1: concat с empty-bag — нейтральный элемент
(defspec empty-bag-left-neutral 100
  (prop/for-all [b bag-gen]
                (equals-bag? (concat-bag (avl-bag) b) b)))
```

Свойство ассоциативности:
```clojure
; Свойство 2: concat ассоциативно
(defspec concat-associative 100
  (prop/for-all [a bag-gen
                 b bag-gen
                 c bag-gen]
                (equals-bag?
                 (concat-bag (concat-bag a b) c)
                 (concat-bag a (concat-bag b c)))))
```

Свойство увеличения общего числа элементов, при добавлении:
```clojure
; Свойство 3: add-one увеличивает total-count
(defspec add-one-increases-count 100
  (prop/for-all [b bag-gen
                 v value-gen]
                (> (total-count (add-one b v))
                   (total-count b))))
```
---

### Отчет инструмента тестирования:
```
Running tests in #{"test"}

Testing avl-bag.property-based-test
{:result true, :num-tests 100, :seed 1766815415627, :time-elapsed-ms 1160, :test-var "concat-associative"}
{:result true, :num-tests 100, :seed 1766815416817, :time-elapsed-ms 80, :test-var "add-one-increases-count"}
{:result true, :num-tests 100, :seed 1766815416903, :time-elapsed-ms 143, :test-var "empty-bag-left-neutral"}

Testing avl-bag.unit-test

Ran 7 tests containing 113 assertions.
0 failures, 0 errors.
```
---

### Метрики:
- **Основные операции (`add`/`del`/`has?`):** O(log n) - сбалансированное AVL-дерево
- **Сравнение (`equals-bag?`):** O(n) - прямое сравнение без сортировки
- **Конкатенация (`concat-bag`):** O(m log n) - добавление элементов одного bag в другой

## Выводы
В ходе выполнения лабораторной работы были изучены и применены:
- Принципы неизменяемых структур данных;
- Рекурсивные алгоритмы балансировки AVL-дерева;
- Построение полиморфного API на Clojure;
- Реализация моноидальных структур;
- Тестирование на основе свойств (property-based);
- Отделение интерфейса от реализации.