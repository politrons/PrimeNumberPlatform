# Prime Number Platform 
![My image](img/prime.jpg)

The Prime number platform is a Microservice platform formed by two JVM services, and one gRPC library which contain the
contract of the communication between them.

The communication between them are made by RPC(Remote Produce Call) by [gRPC](https://grpc.io)

The documentation in detail of each service, it can be found here:

* [Proxy service](ProxyService/README.md)
* [Prime number service](PrimerNumberService/README.md)
* [gRPC contract](GRpcContract/README.md)

## Stack

![My image](img/ZIO.png) ![My image](img/finagle.png) ![My image](img/grpc.png)
