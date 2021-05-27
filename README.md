# Prime Number Platform

The Prime number platform is a Microservice platform formed by two JVM services. 
The communication between them are made by RPC(Remote Produce Call) by [Apache Thrift](https://github.com/apache/thrift)
 and [Finagle scrooge](https://twitter.github.io/scrooge/Finagle.html)

The documentation of each service and how to build and run, it can be found here:
* [Proxy service](ProxyService/README.md)
* [Prime number service](PrimerNumberService/README.md)
