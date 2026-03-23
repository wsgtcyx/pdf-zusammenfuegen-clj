(ns pdf-zusammenfuegen.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (org.apache.pdfbox.io MemoryUsageSetting)
           (org.apache.pdfbox.multipdf PDFMergerUtility)))

(defn- normalize-path [value]
  (when (some? value)
    (.getPath (io/file value))))

(defn- pdf-file? [path]
  (str/ends-with? (str/lower-case path) ".pdf"))

(defn- validate-inputs [inputs]
  (when-not (sequential? inputs)
    (throw (ex-info "Die Option :inputs muss eine Sequenz von PDF-Dateipfaden sein."
                    {:type ::invalid-inputs})))
  (when (< (count inputs) 2)
    (throw (ex-info "Mindestens zwei PDF-Dateien werden benoetigt."
                    {:type ::not-enough-inputs
                     :inputs (count inputs)})))
  (doseq [input inputs]
    (let [path (normalize-path input)
          file (io/file path)]
      (when-not (pdf-file? path)
        (throw (ex-info (str "Nur PDF-Dateien sind erlaubt: " path)
                        {:type ::invalid-extension
                         :path path})))
      (when-not (.exists file)
        (throw (ex-info (str "Eingabedatei nicht gefunden: " path)
                        {:type ::missing-input
                         :path path})))
      (when-not (.isFile file)
        (throw (ex-info (str "Eingabepfad ist keine Datei: " path)
                        {:type ::invalid-input-path
                         :path path})))))
  (mapv normalize-path inputs))

(defn- validate-output [output]
  (let [path (normalize-path output)
        file (io/file path)
        parent (.getParentFile file)]
    (when (str/blank? path)
      (throw (ex-info "Die Option :output ist erforderlich."
                      {:type ::missing-output})))
    (when-not (pdf-file? path)
      (throw (ex-info (str "Die Ausgabedatei muss auf .pdf enden: " path)
                      {:type ::invalid-output-extension
                       :path path})))
    (when (and parent (not (.exists parent)))
      (.mkdirs parent))
    path))

(defn merge-files!
  [{:keys [inputs output]}]
  (let [valid-inputs (validate-inputs inputs)
        valid-output (validate-output output)
        merger (PDFMergerUtility.)]
    (doseq [input valid-inputs]
      (.addSource merger (io/file input)))
    (.setDestinationFileName merger valid-output)
    (.mergeDocuments merger (MemoryUsageSetting/setupMainMemoryOnly))
    {:inputs valid-inputs
     :output valid-output
     :input-count (count valid-inputs)}))

