# PrimeNumberService


# How to build

* We use ```Apache Thirft``` for RPC communication. We define our contract in the [folder](src/main/scala/com/politrons/thrift/idl)
* Since we use ```Finagle scrooge``` maven plugin, we have to execute the ```scrooge:compile```
  task, it will generate all the code specify in the definition file ````prime_number_service.thrift````
  Once the class ````PrimeNumberService```` has been generated, we can compile our RPC [server](src/main/scala/com/politrons/thrift/PrimeNumberServer.scala) and [service](src/main/scala/com/politrons/thrift/PrimeNumberServiceImpl.scala) implementation 

  