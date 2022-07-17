package com.soebes.maven.extensions.profiler.test;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.log.api.ConsoleLoggerDecorator;
import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Karl Heinz Marbaise <khmarbaise@apache.org>
 */
@Named
@Singleton
public class LifecycleEventSpy extends AbstractEventSpy {
    private static final String VERSION = "0.1.2";

    private static final boolean DEFAULT_ENABLED = true;

    private static final String DEFAULT_SUREFIRE_REPORTS_DIRECTORY = "surefire-reports";
    private static final String DEFAULT_FAILSAFE_REPORTS_DIRECTORY = "failsafe-reports";

    private static final int DEFAULT_MAX_SUREFIRE_RESULTS = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_FAILSAFE_RESULTS = Integer.MAX_VALUE;

    private static final int DEFAULT_MAX_SLOWEST_SUREFIRE_RESULTS = 5;
    private static final int DEFAULT_MAX_SLOWEST_FAILSAFE_RESULTS = 5;

    private static final boolean DEFAULT_SHOW_FAILURES = false;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean enabled = DEFAULT_ENABLED;
    
    private String surefireReportsDirectory = DEFAULT_SUREFIRE_REPORTS_DIRECTORY;
    private String failsafeReportsDirectory = DEFAULT_FAILSAFE_REPORTS_DIRECTORY;
    
    private int maxSurefireResults = DEFAULT_MAX_SUREFIRE_RESULTS;
    private int maxFailsafeResults = DEFAULT_MAX_FAILSAFE_RESULTS;

    private int maxSlowestSurefireResults = DEFAULT_MAX_SLOWEST_SUREFIRE_RESULTS;
    private int maxSlowestFailsafeResults = DEFAULT_MAX_SLOWEST_FAILSAFE_RESULTS;
    
    private boolean showFailures = DEFAULT_SHOW_FAILURES;

    public LifecycleEventSpy() {
        logger.debug("LifeCycleProfiler ctor called.");
    }

    @Override
    public void init(Context context) throws Exception {
        enabled = Boolean.parseBoolean(getProperty(context, "test-profiler.enabled", DEFAULT_ENABLED + ""));
        if (!enabled) {
            return;
        }

        logger.info("Maven Test Profiler " + VERSION + " started.");

        surefireReportsDirectory = getProperty(context, "test-profiler.surefireReportsDirectory", DEFAULT_SUREFIRE_REPORTS_DIRECTORY);
        failsafeReportsDirectory = getProperty(context, "test-profiler.failsafeReportsDirectory", DEFAULT_FAILSAFE_REPORTS_DIRECTORY);

        maxSurefireResults = Integer.parseInt(getProperty(context, "test-profiler.maxSurefireResults", DEFAULT_MAX_SUREFIRE_RESULTS + ""));
        maxFailsafeResults = Integer.parseInt(getProperty(context, "test-profiler.maxFailsafeResults", DEFAULT_MAX_FAILSAFE_RESULTS + ""));
        
        maxSlowestSurefireResults = Integer.parseInt(getProperty(context, "test-profiler.maxSlowestSurefireResults", DEFAULT_MAX_SLOWEST_SUREFIRE_RESULTS + ""));
        maxSlowestFailsafeResults = Integer.parseInt(getProperty(context, "test-profiler.maxSlowestFailsafeResults", DEFAULT_MAX_SLOWEST_FAILSAFE_RESULTS + ""));

        showFailures = Boolean.parseBoolean(getProperty(context, "test-profiler.showFailures", DEFAULT_SHOW_FAILURES + ""));
    }

    private String getProperty(Context context, String key, String defaultValue) {
        @SuppressWarnings("unchecked")
        Map<String, String> systemProperties = (Map<String, String>) context.getData().get("systemProperties");
        String value = System.getProperty(key, defaultValue);
        if (systemProperties != null) {
            return systemProperties.getOrDefault(key, value);
        }
        return value;
    }

    @Override
    public void onEvent(Object event) throws Exception {
        if (!enabled) {
            return;
        }

        try {
            if (event instanceof MavenExecutionResult) {
                executionResultEventHandler((MavenExecutionResult) event);
            }
        } catch (Exception e) {
            logger.warn("Cannot profile test results", e);
        }
    }

    @Override
    public void close() {
        logger.debug("Profiler: done.");
    }

    private List<ReportTestSuite> getAllTestReports(File reportDirectory) throws MavenReportException {
        List<File> reportsDirectories = Collections.singletonList(reportDirectory);
        ConsoleLogger consoleLogger = new ConsoleLoggerDecorator(logger);
        SurefireReportParser report = new SurefireReportParser(reportsDirectories, Locale.ENGLISH, consoleLogger);
        return report.parseXMLReportFiles();
    }

    private void executionResultEventHandler(MavenExecutionResult event)
            throws MavenReportException {
        List<ReportTestSuite> surefireTestsResults = new ArrayList<>();
        List<ReportTestSuite> failsafeTestsResults = new ArrayList<>();

        for (MavenProject project : event.getTopologicallySortedProjects()) {
            collectTestsResults(surefireTestsResults, project, surefireReportsDirectory, maxSurefireResults);
            collectTestsResults(failsafeTestsResults, project, failsafeReportsDirectory, maxFailsafeResults);
        }

        printTestSummary(surefireTestsResults, "SUREFIRE TEST SUMMARY", maxSlowestSurefireResults);
        printTestSummary(failsafeTestsResults, "FAILSAFE TEST SUMMARY", maxSlowestFailsafeResults);
    }

    private void collectTestsResults(List<ReportTestSuite> testsResults, MavenProject project, String dir, int max) throws MavenReportException {
        if (testsResults.size() == max) {
            return;
        }

        File reportDirectory = new File(project.getBuild().getDirectory(), dir);
        if (!reportDirectory.exists()) {
            return;
        }

        testsResults.addAll(getAllTestReports(reportDirectory));
    }

    private void printTestSummary(List<ReportTestSuite> testsResults, String label, int maxWorst) {
        if (testsResults.isEmpty()) {
            return;
        }

        logger.info("");
        logger.info("");
        logger.info(label);

        printResult(testsResults);

        printSummary(testsResults);

        sortLongestTestTimeFirst(testsResults);

        List<ReportTestSuite> worstUnitTests = testsResults.subList(0, Math.min(testsResults.size(), maxWorst));

        logger.info("------------------------------------------------------------------------");
        logger.info("SLOWEST {}", label);
        printResult(worstUnitTests);

        // Failure summary ...if some...
        List<ReportTestCase> testCases = new ArrayList<>();
        for (ReportTestSuite reportTestSuite : testsResults) {
            testCases.addAll(reportTestSuite.getTestCases());
        }

        for (ReportTestCase reportTestCase : testCases) {
            if (reportTestCase.hasFailure() && showFailures) {
                String message = reportTestCase.getFailureMessage();
                String type = reportTestCase.getFailureType();
                // FIXME: Currently i can't access the stack trace output which is in the xml file!!
                logger.warn("Failed Test case: {}({})", reportTestCase.getName(), reportTestCase.getFullClassName());
                logger.warn("       {} {}", message, type);
            }
        }

        logger.info("------------------------------------------------------------------------");
    }

    public static final Comparator<ReportTestSuite> ELAPSED_TIME_LARGEST_FIRST = Comparator.comparing(ReportTestSuite::getTimeElapsed);

    private void sortLongestTestTimeFirst(List<ReportTestSuite> unitTestsResults) {
        Collections.sort(unitTestsResults, ELAPSED_TIME_LARGEST_FIRST);
    }

    private void printSummary(List<ReportTestSuite> unitTestsResults) {
        List<File> reportsDirectories = Collections.emptyList();
        SurefireReportParser report = new SurefireReportParser(reportsDirectories, Locale.ENGLISH, new ConsoleLoggerDecorator(logger));
        Map<String, String> summary = report.getSummary(unitTestsResults);

        logger.info("--------- -------- ------ ------- ----------");

        String totalTests = summary.get("totalTests");
        String totalErrors = summary.get("totalErrors");
        String totalSkipped = summary.get("totalSkipped");
        String totalFailures = summary.get("totalFailures");
        String totalElapsedTime = summary.get("totalElapsedTime");
        float totalElapsedTimeNumber = parseNumber(report, totalElapsedTime);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%9s", totalTests));
        sb.append(" ");
        sb.append(String.format("%8s", totalFailures));
        sb.append(" ");
        sb.append(String.format("%6s", totalErrors));
        sb.append(" ");
        sb.append(String.format("%7s", totalSkipped));
        sb.append(" ");
        sb.append(String.format("%10.3f", totalElapsedTimeNumber));

        logger.info("{}", sb);
        logger.info("========= ======== ====== ======= ==========");

        logger.info("");
        logger.info("Rate: {} %", summary.get("totalPercentage"));

        float totalTestsNumber = Float.parseFloat(totalTests);
        float averageTimePerTest = totalTestsNumber == 0
                ? 0
                : totalElapsedTimeNumber / totalTestsNumber;
        if (logger.isInfoEnabled()) {
            logger.info("Average Time per Test: {}", String.format("%6.6f", averageTimePerTest));
        }

    }

    private float parseNumber(SurefireReportParser report, String totalElapsedTime) {
        try {
            return report.getNumberFormat().parse(totalElapsedTime).floatValue();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse totalElapsedTime: " + totalElapsedTime, e);
        }
    }

    private void printResult(List<ReportTestSuite> unitTestsResults) {
        logger.info("Tests run Failures Errors Skipped Elapsed    ClassName");
        logger.info("                                  Time (sec)");
        for (ReportTestSuite testSuite : unitTestsResults) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%9d", testSuite.getNumberOfTests()));
            sb.append(" ");
            sb.append(String.format("%8d", testSuite.getNumberOfFailures()));
            sb.append(" ");
            sb.append(String.format("%6d", testSuite.getNumberOfErrors()));
            sb.append(" ");
            sb.append(String.format("%7d", testSuite.getNumberOfSkipped()));
            sb.append(" ");
            sb.append(String.format("%10.3f", testSuite.getTimeElapsed()));

            sb.append(" ");
            sb.append(testSuite.getFullClassName());
            logger.info("{}", sb);
        }
    }

}
