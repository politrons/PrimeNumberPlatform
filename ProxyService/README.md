# Proxy Server

# Summary  

* For the Rest API layer I use [Finagle](https://twitter.github.io/finagle/) which provide the possibility 
  to use **Reader** and **Writable** to open a continuously streams between client and server.
  
* I use [gRPC](https://grpc.io) for RPC communication between ```ProxyServer``` and ```PrimeNumberServer```.
  I have dependency with module ````GRpcContract```` which contains the contract and also the generated sources to be used from ```PrimerNumberClient```.
  
* To control all possible side effects in our program I use Effect system [ZIO](https://zio.dev), a pure functional programing toolkit
which provide the features of have Pure functional programs with side effect control,
  lazy evaluation, performance improvements since the program run in Fibers(Green threads) instead in OS Threads, and also DI mechanism with ZLayers.

# How to Test

* I use [scalatest](https://www.scalatest.org) framework to design the unit and IT test in our system.
the whole batery of test must be executed during the maven test phase, but in case you want to run the test
  with the IDE you can take a look to the test [here](src/test/scala)