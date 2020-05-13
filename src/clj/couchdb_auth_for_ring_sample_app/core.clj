(ns couchdb-auth-for-ring-sample-app.core
  (:require
   [couchdb-auth-for-ring-sample-app.auth :as auth]
   [ring.adapter.jetty :refer [run-jetty]]))

(defn hello [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn secret [req username roles]
  (println "secret: " req)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "Hello " username ", only logged-in users can see this.")})

(defn app [req]
  (case (:uri req)
    "/" (hello req)
    ;; (auth/wrap-cookie-auth secret) will return a new function that takes in req
    ;; The handler that you pass into wrap-cookie-auth needs to take in three parameters:
    ;; - req -- the Ring request
    ;; - username -- the username looked up from CouchDB. This value comes from CouchDB, not the client.
    ;; - roles -- the roles array looked up from CouchDB. This value comes from CouchDB, not the client. 
    "/secret" ((auth/wrap-cookie-auth secret) req)
    "login" (auth/login-handler req)
    "logout" ((auth/wrap-cookie-auth auth/logout-handler) req)
    {:status 404 :headers {"Content-Type" "text/html"} :body "Not found."}))
