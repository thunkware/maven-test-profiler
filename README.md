# Maven Test Profiler

This maven extension outputs summary of all tests ran by
maven-surefire-plugin and maven-failsafe-plugin.

Every test suite is listed separately with their elapsed time.
Also show is a list of the slowest five test suites.

## Usage

Define the following `.mvn/extensions.xml` file:

``` xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId>com.soebes.maven.extensions.profiler.test</groupId>
    <artifactId>test-profiler</artifactId>
    <version>0.1.0</version>
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
[INFO]         9        0      0       0        0.013 com.soebes.supose.config.filter.FilterFileTest
[INFO]         1        0      0       0        0.000 com.soebes.supose.config.filter.FilterPartialTest
[INFO]         6        0      0       0        0.003 com.soebes.supose.config.filter.FilteringTest
[INFO]        17        0      0       0        0.017 com.soebes.supose.config.filter.FilteringWithExcludeDifferentRepositoryIdTest
[INFO]        15        0      0       0        0.018 com.soebes.supose.config.filter.FilteringWithExcludeTest
[INFO]         3        0      0       0        0.000 com.soebes.supose.core.config.ConfigurationRepositoriesTest
[INFO]         7        0      0       0        0.017 com.soebes.supose.core.lucene.LuceneTest
[INFO]         3        0      0       0        0.018 com.soebes.supose.core.scan.IndexMergeTest
[INFO]         1        0      0       0        0.000 com.soebes.supose.core.utility.FileExtensionPropertyTest
[INFO]         1        0      0       0        0.028 com.soebes.supose.core.parse.java.JavaParserTest
[INFO]        11        1      0       0        0.005 com.soebes.supose.core.utility.FileNameTest
[INFO]         1        0      0       0        0.020 com.soebes.supose.core.recognition.RenameRecognitionTest
[INFO]        28        0      0       0        0.086 com.soebes.supose.core.scan.SearchRepositoryGetQueryTest
[INFO]         3        0      0       0        0.021 com.soebes.supose.core.config.ini.IniTest
[INFO]         1        0      0       0        0.035 com.soebes.supose.core.recognition.TagBranchRecognitionTest
[INFO]        38        0      0       0        0.115 com.soebes.supose.core.scan.SearchRepositoryGetResultTest
[INFO]         1        0      0       0        0.003 com.soebes.supose.core.config.RepositoryJobConfigurationTest
[INFO]        22        0      0       0        0.129 com.soebes.supose.cli.SuposeCLITest
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
       ...
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

The extension can be configured with a `.mvn/jvm.config` file. For example, to customize it:

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
  - default: INT_MAX

- test-profiler.maxFailsafeResults
  - max number of failsafe results to show
  - default: INT_MAX

- test-profiler.maxSlowestSurefireResults
  - max number of slowest surefire results to show
  - default: 5

- test-profiler.maxSlowestFailsafeResults
  - max number of slowest failsafe results to show
  - default: 5

- test-profiler.showFailures
  - whether to show test failures
  - default: false
