(ns pdf-zusammenfuegen.test-runner
  (:require [clojure.test :as test]
            [pdf-zusammenfuegen.core-test]))

(defn -main [& _]
  (let [{:keys [fail error]} (test/run-tests 'pdf-zusammenfuegen.core-test)]
    (System/exit (if (zero? (+ fail error)) 0 1))))
