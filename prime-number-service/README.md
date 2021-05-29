# PrimeNumberService

## Summary

* We use [gRPC]() for RPC communication. We have dependency with module ````GRpcContract```` which contains the contract and also the generated sources
  to be used from client and server

* To control all possible side effects in our program I use Effect system [ZIO](https://zio.dev), a pure functional programing toolkit
  which provide the features to have Pure functional programs with side effect control,
  lazy evaluation, performance improvements since the program run in Fibers(Green threads) instead in OS Threads, and also DI mechanism with ZLayers.
  
## How to Test

* I use [scalatest](https://www.scalatest.org) framework to design the unit and IT test in our system.
  the whole battery of test must be executed during the maven test phase, but in case you want to run the test
  with the IDE you can take a look to the test [here](src/test/scala)
* To run all battery test you must run

````
mvn clean install
````  

## How to run 

After compile the module, go to the target folder and run the fatjar generated invoking the main class

````
java -cp PrimerNumberService-1.0-SNAPSHOT-jar-with-dependencies.jar com.politrons.grpc.PrimerNumberServer

````