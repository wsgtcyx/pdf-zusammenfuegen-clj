(ns pdf-zusammenfuegen.core-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [pdf-zusammenfuegen.core :as merge])
  (:import (java.nio.file Files)
           (org.apache.pdfbox.pdmodel PDDocument PDPage)))

(defn- create-temp-dir []
  (.toFile (Files/createTempDirectory "pdf-zusammenfuegen-test" (make-array java.nio.file.attribute.FileAttribute 0))))

(defn- delete-recursively! [file]
  (when (.exists file)
    (when (.isDirectory file)
      (doseq [child (or (.listFiles file) [])]
        (delete-recursively! child)))
    (.delete file)))

(defn- create-pdf! [path pages]
  (with-open [document (PDDocument.)]
    (dotimes [_ pages]
      (.addPage document (PDPage.)))
    (.save document path))
  path)

(defn- page-count [path]
  (with-open [document (PDDocument/load (io/file path))]
    (.getNumberOfPages document)))

(deftest merge-files-creates-output
  (let [tmp-dir (create-temp-dir)]
    (try
      (let [input-a (create-pdf! (str (io/file tmp-dir "a.pdf")) 1)
            input-b (create-pdf! (str (io/file tmp-dir "b.pdf")) 2)
            output (str (io/file tmp-dir "merged.pdf"))
            result (merge/merge-files! {:inputs [input-a input-b]
                                        :output output})]
        (is (.exists (io/file output)))
        (is (= 3 (page-count output)))
        (is (= output (:output result)))
        (is (= 2 (:input-count result))))
      (finally
        (delete-recursively! tmp-dir)))))

(deftest merge-files-requires-at-least-two-inputs
  (let [tmp-dir (create-temp-dir)]
    (try
      (let [input-a (create-pdf! (str (io/file tmp-dir "single.pdf")) 1)]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"Mindestens zwei PDF-Dateien"
             (merge/merge-files! {:inputs [input-a]
                                  :output (str (io/file tmp-dir "merged.pdf"))}))))
      (finally
        (delete-recursively! tmp-dir)))))

(deftest merge-files-rejects-missing-inputs
  (let [tmp-dir (create-temp-dir)]
    (try
      (let [missing (str (io/file tmp-dir "missing.pdf"))
            valid (create-pdf! (str (io/file tmp-dir "valid.pdf")) 1)]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"Eingabedatei nicht gefunden"
             (merge/merge-files! {:inputs [valid missing]
                                  :output (str (io/file tmp-dir "merged.pdf"))}))))
      (finally
        (delete-recursively! tmp-dir)))))

(deftest merge-files-rejects-non-pdf-output
  (let [tmp-dir (create-temp-dir)]
    (try
      (let [input-a (create-pdf! (str (io/file tmp-dir "a.pdf")) 1)
            input-b (create-pdf! (str (io/file tmp-dir "b.pdf")) 1)]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"muss auf \.pdf enden"
             (merge/merge-files! {:inputs [input-a input-b]
                                  :output (str (io/file tmp-dir "merged.txt"))}))))
      (finally
        (delete-recursively! tmp-dir)))))

