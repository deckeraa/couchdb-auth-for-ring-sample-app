(ns couchdb-auth-for-ring-sample-app.core
  (:require
   [couchdb-auth-for-ring.core]
   [couchdb-auth-for-ring-sample-app.auth :as auth]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.adapter.jetty :refer [run-jetty]]))

(defn hello [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

    ;; (auth/wrap-cookie-auth secret) will return a new function that takes in req
    ;; The handler that you pass into wrap-cookie-auth needs to take in three parameters:
    ;; - req -- the Ring request
    ;; - username -- the username looked up from CouchDB. This value comes from CouchDB, not the client.
    ;; - roles -- the roles array looked up from CouchDB. This value comes from CouchDB, not the client. 
(defn secret [req username roles]
  (println "secret: " req)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "Hello " username ", only logged-in users can see this.")})

(defn strict-create-user-handler [req username roles]
 (if (contains? (set roles) "_admin")
   (auth/create-user-handler req username roles)
   (auth/default-not-authorized-fn req)))

(defn simple-router [req]
  (case (:uri req)
    "/hello" (hello req)
    "/secret" ((com.stronganchortech.couchdb-auth-for-ring.core/wrap-cookie-auth secret) req)
    "/login" (auth/login-handler req)
    "/refresh" (auth/cookie-check-handler req)
    "/logout" ((auth/wrap-cookie-auth auth/logout-handler) req)
    "/create-user" ((auth/wrap-cookie-auth auth/create-user-handler) req)
    "/strict-create-user" ((auth/wrap-cookie-auth strict-create-user-handler) req)
    {:status 404 :headers {"Content-Type" "text/html"} :body "Not found."}))

;; wrap-cookies is a required piece of middleware, otherwise Ring won't send up the cookie that
;; login-handler tries to set.
(def app (wrap-cookies simple-router))
