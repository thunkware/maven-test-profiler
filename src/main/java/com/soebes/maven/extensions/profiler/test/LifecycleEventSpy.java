package com.soebes.maven.extensions.profiler.test;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.MavenExecutionResult;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public LifecycleEventSpy() {
        logger.debug("LifeCycleProfiler ctor called.");
    }

    @Override
    public void init(Context context) throws Exception {
        logger.info("Maven Test Profiler 0.1.0 started.");
    }

    @Override
    public void onEvent(Object event) throws Exception {
        try {
            if (event instanceof MavenExecutionResult) {
                executionResultEventHandler((MavenExecutionResult) event);
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
    }

    @Override
    public void close() {
        logger.debug("Profiler: done.");
    }

    private List<ReportTestSuite> getAllTestReports(File reportDirectory) throws MavenReportException {
        SurefireReportParser report = new SurefireReportParser();
        report.setLocale(Locale.ENGLISH);
        report.setReportsDirectory(reportDirectory);
        List<ReportTestSuite> parseXMLReportFiles = report.parseXMLReportFiles();
        return parseXMLReportFiles;
    }

    private void executionResultEventHandler(MavenExecutionResult event) throws MavenReportException {
        List<ReportTestSuite> unitTestsResults = new ArrayList<ReportTestSuite>();

        for (MavenProject project : event.getTopologicallySortedProjects()) {
            // TODO: Check if we need to make this configurable? But how?
            // TODO: Can we somehow get the configuration of maven-surefire-plugin if the
            // location has been changed?
            File reportDirectory = new File(project.getBuild().getDirectory(), "surefire-reports");
            if (!reportDirectory.exists()) {
                continue;
            }

            unitTestsResults.addAll(getAllTestReports(reportDirectory));
        }

        if (!unitTestsResults.isEmpty()) {
            unitTestSummary(unitTestsResults);
        }

    }

    private void unitTestSummary(List<ReportTestSuite> unitTestsResults) {
        logger.info("UNIT TEST SUMMARY");

        printResult(unitTestsResults);

        printSummary(unitTestsResults);

        sortLongestTestTimeFirst(unitTestsResults);

        List<ReportTestSuite> worstUnitTests = unitTestsResults.subList(0, Math.min(unitTestsResults.size(), 5));

        logger.info("------------------------------------------------------------------------");
        logger.info("SLOWEST UNIT TEST SUMMARY");
        printResult(worstUnitTests);

        // Failure summary ...if some...
        List<ReportTestCase> testCases = new ArrayList<ReportTestCase>();
        for (ReportTestSuite reportTestSuite : unitTestsResults) {
            testCases.addAll(reportTestSuite.getTestCases());
        }

        for (ReportTestCase reportTestCase : testCases) {
            if ((reportTestCase.getFailure() != null) && !reportTestCase.getFailure().isEmpty()) {
                Map<String, Object> failure = reportTestCase.getFailure();
                String message = (String) failure.get("message");
                String type = (String) failure.get("type");
                // FIXME: Currently i can't access the stack trace output which is in the xml file!!
                logger.warn("Failed Test case: {}({})", reportTestCase.getName(), reportTestCase.getFullClassName());
                logger.warn("       {} {}", message, type);
            }
        }
    }

    public static Comparator<ReportTestSuite> ELAPSED_TIME_LARGEST_FIRST = new Comparator<ReportTestSuite>() {
        @Override
        public int compare(ReportTestSuite o1, ReportTestSuite o2) {
            if (o1.getTimeElapsed() < o2.getTimeElapsed()) {
                return +1;
            } else if (o1.getTimeElapsed() > o2.getTimeElapsed()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private void sortLongestTestTimeFirst(List<ReportTestSuite> unitTestsResults) {
        Collections.sort(unitTestsResults, ELAPSED_TIME_LARGEST_FIRST);
    }

    private void printSummary(List<ReportTestSuite> unitTestsResults) {
        SurefireReportParser report = new SurefireReportParser();
        report.setLocale(Locale.ENGLISH);
        Map<String, String> summary = report.getSummary(unitTestsResults);

        logger.info("--------- -------- ------ ------- ----------");

        String totalTests = summary.get("totalTests");
        String totalErrors = summary.get("totalErrors");
        String totalSkipped = summary.get("totalSkipped");
        String totalFailures = summary.get("totalFailures");
        Float totalElapsedTime = Float.parseFloat(summary.get("totalElapsedTime"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%9s", totalTests));
        sb.append(" ");
        sb.append(String.format("%8s", totalFailures));
        sb.append(" ");
        sb.append(String.format("%6s", totalErrors));
        sb.append(" ");
        sb.append(String.format("%7s", totalSkipped));
        sb.append(" ");
        sb.append(String.format("%10.3f", totalElapsedTime));

        logger.info(sb.toString());
        logger.info("========= ======== ====== ======= ==========");

        logger.info("");
        logger.info("Rate: {} %", summary.get("totalPercentage"));

        Float averageTimePerTest = totalElapsedTime / Float.parseFloat(totalTests);
        logger.info("Average Time per Test: {}", String.format("%6.6f", averageTimePerTest));

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
            logger.info("{}", sb.toString());
        }
    }

}
