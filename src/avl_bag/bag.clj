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