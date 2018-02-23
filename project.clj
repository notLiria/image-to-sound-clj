(defproject image-to-sound "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.mikera/imagez "0.12.0"]
                 [org.craigandera/dynne "0.4.1"]
                 [overtone "0.10.3"]
                 ]

  :resource-paths ["src/test/resources"]
  :main ^:skip-aot image-to-sound.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
