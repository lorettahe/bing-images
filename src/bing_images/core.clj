(ns bing-images.core
  (:require [clj-http.client :as http]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]])
  (:import [java.io ByteArrayInputStream]))

(def local-image-path
  "/tmp/bing-image-of-today.jpg")

(defn zip-str
  [s]
  (zip/xml-zip
    (xml/parse (ByteArrayInputStream. (.getBytes s)))))

(defn fetch-current-bing-archive
  []
  (:body (http/get "http://www.bing.com/HPImageArchive.aspx?format=xml&idx=0&n=1&mkt=en-GB")))

(defn fetch-current-image-url
  []
  (let [bing-archive (fetch-current-bing-archive)
        url-regex #".*<url>([^<]+)<\/url>.*"
        image-url (second (re-matches url-regex bing-archive))]
    (str "http://www.bing.com" image-url)))

(defn -main
  [& args]
  (let [image-url (fetch-current-image-url)
        _ (println image-url)]
    (do
      (with-open [in (io/input-stream image-url)
                  out (io/output-stream local-image-path)]
        (io/copy in out))
      (sh "gsettings" "set" "org.gnome.desktop.background" "picture-uri" (str "file://" local-image-path))))
  (shutdown-agents))
