(ns uxbox.services.auth
  (:require [mount.core :as mount :refer (defstate)]
            [suricatta.core :as sc]
            [buddy.hashers :as hashers]
            [buddy.sign.jwe :as jwe]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :as codecs]
            [uxbox.config :as ucfg]
            [uxbox.schema :as us]
            [uxbox.persistence :as up]
            [uxbox.services.core :as usc]))

(def ^:const +auth-opts+
  {:alg :a256kw :enc :a256cbc-hs512})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- initialize-auth-secret
  []
  (let [main-secret (:secret ucfg/config)]
    (when-not main-secret
      (throw (ex-info "Missing `:secret` key in config." {})))
    (hash/blake2b-256 main-secret)))

(defstate secret
  :start (initialize-auth-secret))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def +user-schema+
  {:username [us/required us/string]
   :email [us/required us/email]
   :password [us/required us/string]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Repository
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-user
  [conn {:keys [username password email] :as data}]
  (let [sql (str "INSERT INTO users (username, email, password, photo)"
                 " VALUES (?, ?, ?, ?) RETURNING *;")
        password (hashers/encrypt password)]
    (sc/fetch-one conn [sql username email password ""])))

(defn find-user-by-username-or-email
  [conn username]
  (let [sql (str "SELECT * FROM users WHERE username=? OR email=?")
        sqlv [sql username username]]
    (sc/fetch-one conn sqlv)))

(defn check-user-password
  [user password]
  (hashers/check password (:password user)))

(defn generate-token
  [user]
  (let [data {:id (:id user)}]
    (jwe/encrypt data secret +auth-opts+)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Service
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod usc/-novelty :auth/login
  [conn {:keys [username password scope]}]
  (let [user (find-user-by-username-or-email conn username)]
    (when-not user (throw (ex-info "Invalid credentials" {})))
    (if (check-user-password user password)
      {:token (generate-token user)}
      (throw (ex-info "Invalid credentials" {})))))
