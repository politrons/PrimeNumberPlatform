# Proxy Server

# How to build 

* We use ```Apache Thirft``` for RPC communication. We define our contract in the [folder](src/main/scala/com/politrons/thrift/idl)
* Since we use ```Finagle scrooge``` maven plugin, we have to execute the ```scrooge:compile```
  task, it will generate all the code specify in the definition file ````prime_number_service.thrift````
  Once the class ````PrimeNumberService```` has been generated, we can compile our RPC client implementation ```PrimerNumberClient```

# How to Test

* We use [scalatest](https://www.scalatest.org) framework to design the unit and IT test in our system.
the whole batery of test must be executed during the maven test phase, but in case you want to run the test
  with the IDE you can take a look to the test [here](src/test/scala)