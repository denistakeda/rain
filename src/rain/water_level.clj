(ns rain.water-level)

(def infinity 10000000)

(declare get-shape-at-point
         get-min-water-amount)

(defn get-border-indexes
  "Returns vector [left-border-index right-border-index] where left-border-index and right-border-index
  are indexes of first not equal in height columns"
  [landscape index]
  (let [current (get landscape index)
        left-border-index  (loop [i (dec index)]
                             (cond
                               (nil? (get landscape i)) nil
                               (= current (get landscape i)) (recur (dec i))
                               :otherwise i))
        righ-border-index (loop [i (inc index)]
                            (cond
                              (>= i (count landscape)) nil
                              (= current (get landscape i)) (recur (inc i))
                              :otherwise i))]
    [left-border-index righ-border-index]))

(defn get-shape-at-point [landscape index]
  (let [current (get landscape index)
        [left-border-index right-border-index] (get-border-indexes landscape index)
        left-border (get landscape left-border-index)
        right-border (get landscape right-border-index)]
    (cond
      ;; if both left and right borders are higher than the current,
      ;; then this is the "glass" shape
      (and (or (nil? left-border)
               (> left-border current))
           (or (nil? right-border)
               (> right-border current)))
      (let [left-border  (or (get landscape left-border-index) infinity)
            right-border (or (get landscape right-border-index) infinity)
            height       (min (- left-border current) (- right-border current))
            width        (- (or right-border-index (count landscape)) (or left-border-index -1) 1)]
        {:shape              :glass
         :left-border-index  left-border-index
         :right-border-index right-border-index
         :height             height
         :width              width})

      ;; if both left and right borders are lower than current,
      ;; then this is the "hill" shape
      (and (not (nil? left-border))
           (not (nil? right-border))
           (< left-border current)
           (< right-border current))
      (let [left-shape (get-shape-at-point landscape left-border-index)
            right-shape (get-shape-at-point landscape right-border-index)]
        {:shape              :hill
         :left-border-index  left-border-index
         :right-border-index right-border-index
         :min-water-amount   (min (get-min-water-amount left-shape landscape left-border-index)
                                  (get-min-water-amount right-shape landscape right-border-index))})

      ;; in all other cases (if one border is higher and one border is lower than current),
      ;; this is "slope" shape
      :otherwise
      {:shape       :slope
       :slope-index (if (> (or left-border infinity) (or right-border infinity))
                      right-border-index
                      left-border-index)})))

;; get-min-water-amount
(defmulti get-min-water-amount
  "calculate the amount of water required to change the shape"
  (fn [shape & _] (:shape shape)))

(defmethod get-min-water-amount :glass [shape landscape index]
  (let [{:keys [height width]} shape]
    (* height width)))

(defmethod get-min-water-amount :slope [shape landscape index]
  (let [{:keys [slope-index]} shape]
    (get-min-water-amount (get-shape-at-point landscape slope-index) landscape slope-index)))

(defmethod get-min-water-amount :hill [shape landscape index]
  (let [{:keys [left-border-index right-border-index]} shape
        left-shape (get-shape-at-point landscape left-border-index)
        right-shape (get-shape-at-point landscape right-border-index)]
    (min (get-min-water-amount left-shape landscape left-border-index)
         (get-min-water-amount right-shape landscape right-border-index))))

;; pour-water-at-point
(defn pour-water-at-point
  "Pour water at point. Returns new landscape"
  [landscape index amount]
  (loop [landscape landscape
         amount amount]
    (let [shape            (get-shape-at-point landscape index)
          shape-type       (:shape shape)
          min-water-amount (get-min-water-amount shape landscape index)]
      (cond
        (= shape-type :glass)
        (let [poured-water (min amount min-water-amount)
              water-per-column (/ poured-water (:width shape))
              left-border-index (or (:left-border-index shape) -1)
              right-border-index (or (:right-border-index shape) (count landscape))
              rest-water (- amount poured-water)
              new-landscape (into []
                                  (map-indexed (fn [idx v] (if (< left-border-index idx right-border-index) (+ v water-per-column)                                                                                                                         v)) landscape))]
          (if (= rest-water 0)
            new-landscape
            (recur new-landscape rest-water)))

        (= shape-type :slope)
        (let [poured-water (min amount min-water-amount)
              rest-water (- amount poured-water)
              new-landscape (pour-water-at-point landscape (:slope-index shape) poured-water)]
          (if (= rest-water 0)
            new-landscape
            (recur new-landscape rest-water)))

        (= shape-type :hill)
        (let [poured-water (min (/ amount 2) min-water-amount)
              rest-water (- amount (* poured-water 2))
              new-landscape (-> landscape
                                (pour-water-at-point (:left-border-index shape) poured-water)
                                (pour-water-at-point (:right-border-index shape) poured-water))]
          (if (= rest-water 0)
            new-landscape
            (recur new-landscape rest-water)))))))

(defn spread-water [landscape hours]
  (loop [landscape landscape
         index 0]
    (if (= index (count landscape))
      landscape
      (recur (pour-water-at-point landscape index hours) (inc index)))))

(defn calculate-water-level [landscape hours]
  (into [] (map - (spread-water landscape hours) landscape)))
