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

* **Performance test** TODO
![My image](../img/gatling.png)