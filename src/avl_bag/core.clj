(ns avl-bag.core
  (:require [avl-bag.bag :refer [Bag]]))

; Создание узла
(defn- init-node [value count left right]
  {:value value
   :count count
   :left left
   :right right})

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

; Добавление значения value count раз
(defn- add-value [node value count]
  (cond (nil? node)
        (init-node value count nil nil)

        (= (:value node) value)
        (init-node (:value node)
                   (+ (:count node) count)
                   (:left node)
                   (:right node))

        (< (:value node) value)
        (balance (init-node (:value node)
                            (:count node)
                            (:left node)
                            (add-value (:right node) value count)))

        :else (balance (init-node (:value node)
                                  (:count node)
                                  (add-value (:left node) value count)
                                  (:right node)))))

; Поиск минимального значения
(defn- find-min-node [node]
  (if (nil? (:left node))
    {:value (:value node)
     :count (:count node)}

    (find-min-node (:left node))))

; Удаление минимального узла
(defn- remove-min-node [node]
  (if (nil? (:left node))
    (if (nil? (:right node))
      nil
      (balance (let [r (:right node)]
                 (init-node (:value r)
                            (:count r)
                            (:left r)
                            (:right r)))))

    (balance (init-node (:value node)
                        (:count node)
                        (remove-min-node (:left node))
                        (:right node)))))

; Замена удалённого узла
(defn- replace-removed-node [node]
  (cond (nil? (:left node))
        (:right node)

        (nil? (:right node))
        (:left node)

        :else (let [min-value (find-min-node (:right node))]
                (init-node (:value min-value)
                           (:count min-value)
                           (:left node)
                           (remove-min-node (:right node))))))

; Удаление value count раз
(defn- del-value [node value count]
  (cond (nil? node)
        nil

        (= (:value node) value)
        (if (> (- (:count node) count) 0)
          (init-node value
                     (- (:count node) count)
                     (:left node)
                     (:right node))
          (replace-removed-node node))

        (< (:value node) value)
        (balance (init-node (:value node)
                            (:count node)
                            (:left node)
                            (del-value (:right node) value count)))

        :else (balance (init-node (:value node)
                                  (:count node)
                                  (del-value (:left node) value count)
                                  (:right node)))))

; Вернуть дерево с установленным количеством повторов для выбранного значения
(defn- set-count-for-value [node value count]
  (cond (= (:value node) value)
        (init-node value
                   count
                   (:left node)
                   (:right node))

        (< (:value node) value)
        (init-node (:value node)
                   (:count node)
                   (:left node)
                   (set-count-for-value (:right node) value count))

        :else (init-node (:value node)
                         (:count node)
                         (set-count-for-value (:left node) value count)
                         (:right node))))

; Найти значение в текущем дереве
(defn- has-value [node value]
  (cond (nil? node) false
        (= (:value node) value) true
        (< (:value node) value) (has-value (:right node) value)
        :else (has-value (:left node) value)))

; Количество повторов значения в дереве
(defn- count-of-value [node value]
  (cond (nil? node) 0
        (= (:value node) value) (:count node)
        (< (:value node) value) (count-of-value (:right node) value)
        :else (count-of-value (:left node) value)))

; Количество элементов в дереве, учитывая повторы
(defn- count-of-elements [node]
  (if (nil? node)
    0
    (+ (:count node)
       (count-of-elements (:right node))
       (count-of-elements (:left node)))))

; Количество элементов в дереве, не учитывая повторы
(defn- count-of-unique-elements [node]
  (if (nil? node)
    0
    (inc (+ (count-of-unique-elements (:right node)) (count-of-unique-elements (:left node))))))

; Map: применение f к каждому значению и вставка в новое дерево
(defn- map-node-into-root [node acc-root f]
  (if (nil? node)
    acc-root
    (let [acc-left (map-node-into-root (:left node) acc-root f)
          acc-with-node (add-value acc-left (f (:value node)) (:count node))]
      (map-node-into-root (:right node) acc-with-node f))))

; Filter: вставка только удовлетворяющих pred
(defn- filter-node-into-root [node acc-root pred]
  (if (nil? node)
    acc-root
    (let [acc-left (filter-node-into-root (:left node) acc-root pred)
          acc-with-node (if (pred (:value node))
                          (add-value acc-left (:value node) (:count node))
                          acc-left)]
      (filter-node-into-root (:right node) acc-with-node pred))))

; Обход слева направо и применение операции f, с сохранением в acc
(defn- fold-left-node [node f acc]
  (if (nil? node)
    acc
    (let [acc-left (fold-left-node (:left node) f acc)
          acc-node (loop [i 0 acc acc-left]
                     (if (< i (:count node))
                       (recur (inc i) (f acc (:value node)))
                       acc))]
      (fold-left-node (:right node) f acc-node))))

; Обход справа налево и применение операции f, с сохранением в acc
(defn- fold-right-node [node f acc]
  (if (nil? node)
    acc
    (let [acc-right (fold-right-node (:right node) f acc)
          ;; применяем функцию count раз
          acc-node (loop [i 0 acc acc-right]
                     (if (< i (:count node))
                       (recur (inc i) (f acc (:value node)))
                       acc))]
      (fold-right-node (:left node) f acc-node))))

; Объединение деревьев
(defn- merge-into-root [node acc-root]
  (if (nil? node)
    acc-root
    (let [acc-left (merge-into-root (:left node) acc-root)
          acc-with-node (add-value acc-left (:value node) (:count node))]
      (merge-into-root (:right node) acc-with-node))))

; Тип данных AVL-BAG
(defrecord AvlBag [root]
  Bag
  (add
    [this element count]
    (if (> count 0)
      (->AvlBag (add-value root element count))
      (->AvlBag root)))

  (add-one
    [this element]
    (->AvlBag (add-value root element 1)))

  (del
    [this element count]
    (->AvlBag (del-value root element count)))

  (del-one
    [this element]
    (->AvlBag (del-value root element 1)))

  (set-count
    [this element count]
    (if (> count 0)
      (->AvlBag (set-count-for-value root element count))
      (->AvlBag root)))

  (has?
    [this element]
    (has-value root element))

  (count-of
    [this element]
    (count-of-value root element))

  (total-count
    [this]
    (count-of-elements root))

  (unique-items-count
    [this]
    (count-of-unique-elements root))

  (map-bag
    [this f]
    (->AvlBag (map-node-into-root root nil f)))

  (filter-bag
    [this pred]
    (->AvlBag (filter-node-into-root root nil pred)))

  (fold-left
    [this f init]
    (fold-left-node root f init))

  (fold-right
    [this f init]
    (fold-right-node root f init))

  (empty-bag
    [this]
    (->AvlBag nil))

  (concat-bag
    [this bag]
    (let [other-root (.-root bag)]
      (->AvlBag (merge-into-root other-root root))))

  (equals-bag?
    [this bag]
    (let [other-root (.-root bag)]
      (if (and (= (.total-count this) (.total-count bag))
               (= (.unique-items-count this) (.unique-items-count bag)))
        (letfn [(eq-node? [node]
                  (if (nil? node)
                    true
                    (let [v (:value node)
                          c (:count node)
                          other-c (count-of-value other-root v)]
                      (and (= c other-c)
                           (eq-node? (:left node))
                           (eq-node? (:right node))))))]
          (eq-node? root))
        false))))

; Функция-конструктор
(defn avl-bag
  ([] (->AvlBag nil))
  ([root] (->AvlBag root)))
