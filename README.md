# PDF zusammenfuegen

`pdf-zusammenfuegen` ist eine kleine Clojure-Bibliothek mit einfacher CLI zum lokalen Zusammenfuegen mehrerer PDF-Dateien. Das Projekt richtet sich an Entwickler und Automatisierungs-Workflows, die PDFs ohne grosse Desktop-Software zu einer einzigen Datei kombinieren moechten.

## Projektseite

- Website: [pdfzus.de](https://pdfzus.de/)

## Funktionen

- mehrere lokale PDF-Dateien in einer festen Reihenfolge zusammenfuegen
- klare Validierung fuer Eingabedateien und Ausgabepfad
- kleine CLI fuer Skripte, Cronjobs und Build-Pipelines
- Bibliotheks-API fuer die direkte Einbindung in Clojure-Projekte

## Installation

```clojure
;; deps.edn
{:deps {com.github.wsgtcyx/pdf-zusammenfuegen {:mvn/version "0.1.0"}}}
```

## Bibliothek verwenden

```clojure
(require '[pdf-zusammenfuegen.core :as merge])

(merge/merge-files!
  {:inputs ["rechnung-1.pdf" "rechnung-2.pdf" "anhang.pdf"]
   :output "gesamt.pdf"})
```

## CLI verwenden

```bash
clojure -M -m pdf-zusammenfuegen.cli --output gesamt.pdf teil-1.pdf teil-2.pdf teil-3.pdf
```

Hilfe anzeigen:

```bash
clojure -M -m pdf-zusammenfuegen.cli --help
```

## Entwicklung

Tests ausfuehren:

```bash
clojure -M:test
```

Release-Artefakte bauen:

```bash
clojure -T:jar jar
```

Das Build legt JAR, POM und lokale Checksum-Dateien unter `artifacts/build/<version>/` ab.

## Deployment zu Clojars

```bash
CLOJARS_USERNAME=<username> CLOJARS_PASSWORD=<deploy-token> \
clojure -X:deploy \
  :artifact '"artifacts/build/0.1.0/pdf-zusammenfuegen-0.1.0.jar"' \
  :pom-file '"artifacts/build/0.1.0/pdf-zusammenfuegen-0.1.0.pom"'
```

## Hinweise

- Das Werkzeug arbeitet mit lokalen Dateien.
- Die eigentliche PDF-Verarbeitung basiert auf Apache PDFBox.
- Fuer eine browserbasierte Nutzung und weitere PDF-Workflows ist [pdfzus.de](https://pdfzus.de/) die passende Projektseite.

