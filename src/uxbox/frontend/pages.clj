;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.frontend.pages
  (:require [promesa.core :as p]
            [catacumba.http :as http]
            [uxbox.schema :as us]
            [uxbox.services :as sv]
            [uxbox.util.response :refer (rsp)]
            [uxbox.util.querystring :as qs]
            [uxbox.util.uuid :as uuid]))

(defn list-pages
  [{user :identity}]
  (let [params {:user user :type :page/list}]
    (-> (sv/query params)
        (p/then #(http/ok (rsp %))))))

(defn list-pages-by-project
  [{user :identity params :route-params}]
  (let [params {:user user
                :project (uuid/from-string (:id params))
                :type :page/list-by-project}]
    (-> (sv/query params)
        (p/then #(http/ok (rsp %))))))

(defn create-page
  [{user :identity params :data}]
  (p/alet [params (assoc params
                         :type :page/create
                         :user user)
           result (p/await (sv/novelty params))
           loc (str "/api/pages/" (:id result))]
    (http/created loc (rsp result))))

(defn update-page
  [{user :identity params :route-params data :data}]
  (let [params (merge data
                      {:id (uuid/from-string (:id params))
                       :type :page/update
                       :user user})]
    (-> (sv/novelty params)
        (p/then #(http/ok (rsp %))))))

(defn update-page-metadata
  [{user :identity params :route-params data :data}]
  (let [params (merge data
                      {:id (uuid/from-string (:id params))
                       :type :page/update-metadata
                       :user user})]
    (-> (sv/novelty params)
        (p/then #(http/ok (rsp %))))))

(defn delete-page
  [{user :identity params :route-params}]
  (let [params {:id (uuid/from-string (:id params))
                :type :page/delete
                :user user}]
    (-> (sv/novelty params)
        (p/then (fn [v] (http/no-content))))))

(defn retrieve-page-history
  [{user :identity params :route-params query :query-params}]
  (let [params (merge params
                      (qs/parse-params query)
                      {:type :page/history-list
                       :id (uuid/from-string (:id params))
                       :user user})]
    (->> (sv/query params)
         (p/map #(http/ok (rsp %))))))
