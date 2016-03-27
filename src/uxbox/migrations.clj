;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.migrations
  (:require [mount.core :as mount :refer (defstate)]
            [migrante.core :as mg :refer (defmigration)]
            [uxbox.persistence :as up]
            [uxbox.config :as ucfg]
            [uxbox.util.template :as tmpl]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Migrations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmigration utils-0000
  "Create a initial version of txlog table."
  :up (mg/resource "migrations/0000.main.utils.up.sql"))

(defmigration txlog-0001
  "Create a initial version of txlog table."
  :up (mg/resource "migrations/0001.txlog.create.up.sql"))

(defmigration auth-0002
  "Create initial auth related tables."
  :up (mg/resource "migrations/0002.auth.tables.up.sql"))

(defmigration projects-0003
  "Create initial tables for projects and pages."
  :up (mg/resource "migrations/0003.projects.and.pages.up.sql"))

(defmigration color-collections-0004
  "Create initial tables for color collections."
  :up (mg/resource "migrations/0004.color.collections.up.sql"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry point
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def +migrations+
  {:name :uxbox-main
   :steps [[:0000 utils-0000]
           [:0001 txlog-0001]
           [:0002 auth-0002]
           [:0003 projects-0003]
           [:0004 color-collections-0004]]})

(defn- migrate
  []
  (let [options (:migrations ucfg/config {})]
    (with-open [mctx (mg/context up/datasource options)]
      (mg/migrate mctx +migrations+)
      nil)))

(defstate migrations
  :start (migrate))
