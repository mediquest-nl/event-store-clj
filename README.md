# event-store-clj

A small clojure project to support the proof of Concept of event-stores. This project was used to evaluate different options for implementing a small service that stores event. Implementations have been made in:
- Clojure: this repository contains a small server
- Go-lang: the repository event-store-go  (both for standalone use and as a Google Cloud function.

The experiment involved writing the complete requests including headers to an Eventstore, resulting in approximately 500 bytes stored per incoming event. The results amazed me in the sense that 1000 http-requests were generated in less than 100 ms and also were handled and stored in this time-frame to the file-system including flushing of all bufferes.

## Usage

Open a clojure repl and start the server with the command
(require '[event-store.server :as es])
(es/-main)

Start a second repl and run a burst of tests:

(require '[event-store.test-client :as tc])

To launch a series of 1000 asynchronous http-requests in a single burst use:
(tc/fire-tests 1000)

To lauch 10 series of 100 http-requests, while waiting for all requests in a burst to be finished before running the firing the next series use:

(tc/fire-bursts 10 100)

The url currently is a hard-coded parameter in the test-program; convenient for testing, but more elegant solutions could be considered ;-)

## Test results
The tests-results have been described in presentation (MQ-internal)
https://docs.google.com/presentation/d/1REgtXiJ2EWSZOceBeDyJZu457WneJP7kRjjL3nLiZXs/edit#slide=id.g6ae1ba247c_0_0

Further details are found in:
 https://docs.google.com/document/d/1Zegi34-jVTPl47X-OyB5G7AEXn921LXI8PH68Xxjf0c/edit#heading=h.xd5cjg8zy86m

## License

Copyright © 2019 C.van Kemenade/Mediquest

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
