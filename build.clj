(ns build
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.build.api :as b])
  (:import (java.io File FileInputStream)
           (java.security MessageDigest)))

(def lib 'com.github.wsgtcyx/pdf-zusammenfuegen)
(def version "0.1.0")
(def artifact-id "pdf-zusammenfuegen")
(def class-dir "target/classes")
(def src-dirs ["src"])
(def github-repo "https://github.com/wsgtcyx/pdf-zusammenfuegen-clj")

(defn- artifact-dir [version]
  (format "artifacts/build/%s" version))

(defn- pom-path-in-classes []
  (format "%s/META-INF/maven/com.github.wsgtcyx/%s/pom.xml" class-dir artifact-id))

(defn- digest-file [^File file algorithm]
  (with-open [input (FileInputStream. file)]
    (let [digest (MessageDigest/getInstance algorithm)
          buffer (byte-array 8192)]
      (loop []
        (let [read (.read input buffer)]
          (when (pos? read)
            (.update digest buffer 0 read)
            (recur))))
      (apply str (map #(format "%02x" (bit-and % 0xff)) (.digest digest))))))

(defn- checksum-extension [algorithm]
  (-> algorithm
      str/lower-case
      (str/replace "-" "")))

(defn- write-checksums! [path]
  (let [file (io/file path)]
    (doseq [algorithm ["MD5" "SHA-1" "SHA-256"]]
      (spit (str path "." (checksum-extension algorithm))
            (str (digest-file file algorithm) "\n")))))

(defn- normalize-pom! [path]
  (let [content (slurp path)
        cleaned (str/replace content #"\s*<name>pdf-zusammenfuegen</name>\n" "\n")]
    (spit path cleaned)))

(defn clean [_]
  (b/delete {:path "target"})
  nil)

(defn jar
  [{:keys [version]
    :or {version version}}]
  (let [basis (b/create-basis {:project "deps.edn"})
        release-dir (artifact-dir version)
        jar-file (format "%s/%s-%s.jar" release-dir artifact-id version)
        pom-file (format "%s/%s-%s.pom" release-dir artifact-id version)]
    (b/delete {:path "target"})
    (b/delete {:path release-dir})
    (.mkdirs (io/file class-dir))
    (.mkdirs (io/file release-dir))
    (b/copy-dir {:src-dirs src-dirs :target-dir class-dir})
    (b/write-pom
     {:class-dir class-dir
      :lib lib
      :version version
      :basis basis
      :src-dirs src-dirs
      :pom-data [[:name "PDF zusammenfuegen"]
                 [:description "Lokales Clojure-Werkzeug zum Zusammenfuegen mehrerer PDF-Dateien. Projektseite: https://pdfzus.de/"]
                 [:url "https://pdfzus.de/"]
                 [:licenses
                  [:license
                   [:name "Apache-2.0"]
                   [:url "https://www.apache.org/licenses/LICENSE-2.0.txt"]]]
                 [:scm
                  [:url github-repo]
                  [:connection "scm:git:git://github.com/wsgtcyx/pdf-zusammenfuegen-clj.git"]
                  [:developerConnection "scm:git:ssh://git@github.com/wsgtcyx/pdf-zusammenfuegen-clj.git"]
                  [:tag (str "v" version)]]]})
    (b/jar {:class-dir class-dir
            :jar-file jar-file})
    (normalize-pom! (pom-path-in-classes))
    (io/copy (io/file (pom-path-in-classes))
             (io/file pom-file))
    (normalize-pom! pom-file)
    (write-checksums! jar-file)
    (write-checksums! pom-file)
    (println "Build abgeschlossen:" jar-file)
    {:jar jar-file
     :pom pom-file}))
