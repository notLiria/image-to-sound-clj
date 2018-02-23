(ns image-to-sound.core
  (:gen-class)
  (:use [mikera.image.colours :as image-colours]
        [mikera.image.core :as image-core])
  (:require [dynne.sampled-sound :as sampled-sound]))

(def ant (load-image "/home/liria/clojure/image-to-sound-clj/resources/image-to-sound/image/samples/Ant.png"))


(defn extract-pixels-from-image
  ([image]
   (map #(image-colours/components-rgb %) (get-pixels image))))

(defn extract-red-from-image
  ([image]
   (map #(image-colours/extract-red %) (get-pixels image))))

(defn extract-green-from-image
  ([image]
   (map #(image-colours/extract-green %) (get-pixels image))))

(defn extract-blue-from-image
  ([image]
   (map #(image-colours/extract-blue %) (get-pixels image))))

(defn lin-scale
  ([x input-min input-max output-min output-max]
   (* (/ (float x) (java.lang.Math/abs (- input-min input-max)))
      (java.lang.Math/abs (- output-min output-max)))))

(defn scale-8-bit-to-piano
  "Scales an 8 bit number (0-255) to something inside the Piano frequency range (27.5-4186)"
  ([x]
   (lin-scale x 0 255 27.5 4186)))

(defn create-sinusoidal-sound
  "Takse a frequency and a time and outputs a sinusoidal sound"
  ([freq time]
   (sampled-sound/sinusoid freq time)))

(defn mean
  [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count)
      (/ sum count)
      0)))

(defn standard-deviation
  [coll]
  (let [avg (mean coll)
        squares (for [x coll]
                  (let [x-avg (- x avg)]
                    (* x-avg x-avg)))
        total (count coll)]
    (-> (/ (apply + squares)
           (- total 1))
        (Math/sqrt))))


(defn append-all
  "Concatenates multiple sounds together -- Charlie Wang"
  [sounds]
  (let [durations (pmap #(sampled-sound/duration %) sounds)]
    (reify sampled-sound/SampledSound
      (duration [this] (reduce + durations))
      (channels [this] (sampled-sound/channels (first sounds)))
      (chunks [this sample-rate]
        (doall (mapcat #(sampled-sound/chunks % sample-rate) sounds))))))

(defn create-sounds
  "Uses stddev of a pixel to determine length; uses rgb to determine frequency"
  ([image]
   (let [stddev-unscaled (map #(standard-deviation %) (extract-pixels-from-image image))]
     (let [stddev (map #(lin-scale % (apply min stddev-unscaled) (apply max stddev-unscaled) 0 1) stddev-unscaled)
           red-freqs (map #(scale-8-bit-to-piano %) (extract-red-from-image image))
           green-freqs (map #(scale-8-bit-to-piano %) (extract-green-from-image image))
           blue-freqs (map #(scale-8-bit-to-piano %) (extract-blue-from-image image))]
       (let [red-sounds (map create-sinusoidal-sound red-freqs stddev)
             blue-sounds (map create-sinusoidal-sound blue-freqs stddev)
             green-sounds (map create-sinusoidal-sound green-freqs stddev)]
         (append-all  red-sounds))))))


(defn save-sounds
  [image title]
  (sampled-sound/save (create-sounds image) title 44100)
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (save-sounds ant "ant.wav")
)
