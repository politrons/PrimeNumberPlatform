# gRPC contract

* For the communication between services I choose [gRPC](https://grpc.io) since my previous experience with `````Thrift vs gRPC````` taught me that
  for streaming ````Thrift```` is not so mature and has less features than ```gRPC``` of course this it might not be true anymore.
* Since I use ```gRPC``` maven plugin, I have to execute the ```protobuf```
  task, it will generate all the code specify in the definition file ````prime_number.proto````
  Once the class ````PrimeNumberService```` has been generated, client and server can use ```PrimeNumberServiceGrpc``` and ```PrimeNumberServiceStub``` 
  to communicate between each other.
  