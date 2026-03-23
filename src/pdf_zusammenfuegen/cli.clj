(ns pdf-zusammenfuegen.cli
  (:require [clojure.string :as str]
            [pdf-zusammenfuegen.core :as merge]))

(def ^:private usage-text
  (str/join
   \newline
   ["PDF zusammenfuegen"
    ""
    "Verwendung:"
    "  clojure -M -m pdf-zusammenfuegen.cli --output gesamt.pdf teil-1.pdf teil-2.pdf"
    ""
    "Optionen:"
    "  --output <datei>   Zielpfad fuer die zusammengefuehrte PDF"
    "  --help             Diese Hilfe anzeigen"]))

(defn- parse-args [args]
  (loop [[current & more] args
         state {:inputs []}]
    (cond
      (nil? current)
      state

      (= current "--help")
      (assoc state :help? true)

      (= current "--output")
      (if-let [target (first more)]
        (recur (rest more) (assoc state :output target))
        (assoc state :error "Nach --output fehlt ein Dateipfad."))

      (str/starts-with? current "--")
      (assoc state :error (str "Unbekannte Option: " current))

      :else
      (recur more (update state :inputs conj current)))))

(defn run-cli [args]
  (let [{:keys [help? error output inputs]} (parse-args args)]
    (cond
      help?
      {:status :help
       :exit-code 0
       :message usage-text}

      error
      {:status :error
       :exit-code 1
       :message (str error "\n\n" usage-text)}

      :else
      (let [{:keys [output input-count]} (merge/merge-files! {:inputs inputs
                                                              :output output})]
        {:status :ok
         :exit-code 0
         :message (format "PDF erfolgreich erstellt: %s (%d Dateien)"
                          output
                          input-count)}))))

(defn -main [& args]
  (let [{:keys [exit-code status message]} (try
                                             (run-cli args)
                                             (catch clojure.lang.ExceptionInfo ex
                                               {:status :error
                                                :exit-code 1
                                                :message (ex-message ex)})
                                             (catch Exception ex
                                               {:status :error
                                                :exit-code 1
                                                :message (str "Unerwarteter Fehler: " (.getMessage ex))}))]
    (binding [*out* (if (= status :error) *err* *out*)]
      (println message))
    (System/exit exit-code)))

