# Maven Test Profiler

This maven extension outputs summary of all tests ran by
maven-surefire-plugin and maven-failsafe-plugin.

Every test suite is listed separately with their elapsed time
in order of slowest to faster.

This is a fork of https://github.com/khmarbaise/maven-test-profiler with minor enhancements/fixes.
Credit to [@khmarbaise](https://github.com/khmarbaise) for creating the project.

## Usage

Define the following `.mvn/extensions.xml` file:

``` xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId>io.github.thunkware</groupId>
    <artifactId>test-profiler</artifactId>
    <version>0.1.2</version>
  </extension>
</extensions>
```

Here's an example of what the output will look like:

```
mvn clean install
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 50.828s
[INFO] Finished at: Mon Feb 16 21:08:08 CET 2015
[INFO] Final Memory: 56M/654M
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] SUREFIRE UNIT TEST SUMMARY
[INFO] Tests run Failures Errors Skipped Elapsed Time ClassName
[INFO]                                          (sec)
[INFO] --------- -------- ------ ------- ------------
[INFO]       168        1      0       0        0.528
[INFO] ========= ======== ====== ======= ============
[INFO]
[INFO] Rate: 99.405 %
[INFO] Average Time per Test: 0.024000
[INFO] ------------------------------------------------------------------------
[INFO] SUREFIRE SLOWEST UNIT TEST SUMMARY
[INFO] Tests run Failures Errors Skipped Elapsed Time ClassName
[INFO]                                          (sec)
[INFO]        22        0      0       0        0.129 com.soebes.supose.cli.SuposeCLITest
[INFO]        38        0      0       0        0.115 com.soebes.supose.core.scan.SearchRepositoryGetResultTest
[INFO]        28        0      0       0        0.086 com.soebes.supose.core.scan.SearchRepositoryGetQueryTest
[INFO]         1        0      0       0        0.035 com.soebes.supose.core.recognition.TagBranchRecognitionTest
[INFO]         1        0      0       0        0.028 com.soebes.supose.core.parse.java.JavaParserTest
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] FAILSAFE UNIT TEST SUMMARY
[INFO] Tests run Failures Errors Skipped Elapsed Time ClassName
[INFO]                                          (sec)
[INFO] --------- -------- ------ ------- ------------
[INFO]       123        1      0       0        0.528
[INFO] ========= ======== ====== ======= ============
[INFO]
[INFO] Rate: 98.405 %
[INFO] Average Time per Test: 0.023000
[INFO] ------------------------------------------------------------------------
[INFO] FAILSAFE SLOWEST UNIT TEST SUMMARY
[INFO] Tests run Failures Errors Skipped Elapsed Time ClassName
[INFO]                                          (sec)
       ...
[INFO] ------------------------------------------------------------------------
```

Prerequisites: Maven 3.6.x+ and Java 1.8+ as runtime.

## Configuration

The extension can be configured with a `.mvn/jvm.config` file. For example, to customize it, the content could be:

```
-Dtest-profiler.maxSlowestSurefireResults=20
-Dtest-profiler.maxSlowestFailsafeResults=30
```

The following properties are available:

- test-profiler.enabled
  - enables or disables the extension
  - default: true

- test-profiler.surefireReportsDirectory
  - location of surefire reports directory
  - default: surefire-reports

- test-profiler.failsafeReportsDirectory
  - location of failsafe reports directory
  - default: failsafe-reports

- test-profiler.maxSurefireResults
  - max number of surefire results to show
  - default: 0

- test-profiler.maxFailsafeResults
  - max number of failsafe results to show
  - default: 0

- test-profiler.maxSlowestSurefireResults
  - max number of slowest surefire results to show
  - default: INT_MAX

- test-profiler.maxSlowestFailsafeResults
  - max number of slowest failsafe results to show
  - default: INT_MAX

- test-profiler.maxFailures
  - max number of failures to show
  - default: 0
