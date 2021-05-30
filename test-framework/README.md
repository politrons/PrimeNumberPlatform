# Test Framework

## Summary 

Test framework module has dependencies with the two other modules servers, in order to make real 
Integration and performance test without mocks, and then prove that the mocks created for each module 
behave as we expect, and the SLA is what we define initially and remains unaltered.

**OBSERVATIONS**
If any integration test fail, we must reproduce the bug in the unit test, in order to determine why our
 unit test coverage it did not cover that bug.

## How to Test

* **Integration** test is also run by maven life-cycle in integration-test phase using scalstest.
in case you want to run it by IDE you can find the test [here](src/test/scala/com/politrons/it/PrimeNumberPlatformSpec.scala)

* **Performance test** Performance test are not working properly for now, since it seems the gatling version that I'm using is not
supporting http2.0 and I dont have the time to upgrade to one of the latest version, because I would have to deal 
  with clash library conflicts, specially netty.


![My image](../img/gatling.png)