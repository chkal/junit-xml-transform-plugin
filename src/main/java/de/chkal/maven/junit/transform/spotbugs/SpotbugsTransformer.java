package de.chkal.maven.junit.transform.spotbugs;

import de.chkal.maven.junit.transform.AbstractTransformer;
import de.chkal.maven.junit.transform.xml.junit.Testsuite;
import de.chkal.maven.junit.transform.xml.junit.Testsuite.Testcase;
import de.chkal.maven.junit.transform.xml.junit.Testsuite.Testcase.Failure;
import de.chkal.maven.junit.transform.xml.spotbugs.BugCollection;
import de.chkal.maven.junit.transform.xml.spotbugs.BugCollection.BugInstance;
import de.chkal.maven.junit.transform.xml.spotbugs.SourceLine;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;

public class SpotbugsTransformer extends AbstractTransformer {

  @Override
  public Testsuite buildTestsuite(File inputFile) throws JAXBException {

    Unmarshaller unmarshaller = JAXBContext.newInstance(BugCollection.class).createUnmarshaller();
    BugCollection bugCollection = (BugCollection) unmarshaller.unmarshal(inputFile);

    List<Testcase> testcases = bugCollection.getBugInstance().stream()
        .map(bugInstance -> transformBugInstance(bugCollection, bugInstance))
        .collect(Collectors.toList());

    Testsuite testsuite = new Testsuite();
    testsuite.setName("SpotBugs");
    testsuite.setTime(bugCollection.getFindBugsSummary().getClockSeconds().toString());
    testsuite.setTests(String.valueOf(testcases.size()));
    testsuite.setErrors(String.valueOf(testcases.size()));
    testsuite.setFailures("0");
    testsuite.setSkipped("0");
    testsuite.getTestcase().addAll(testcases);
    return testsuite;

  }

  private Testcase transformBugInstance(BugCollection bugCollection, BugInstance bugInstance) {

    Failure failure = new Failure();
    failure.setMessage(bugInstance.getShortMessage());
    failure.setType(bugInstance.getCategory());
    failure.setValue(getFullDescription(bugCollection, bugInstance));

    Testcase testcase = new Testcase();
    testcase.setClassname(getTestClassName(bugInstance));
    testcase.setName(getTestName(bugInstance));
    testcase.getFailure().add(failure);
    return testcase;

  }

  private String getTestClassName(BugInstance bugInstance) {

    return bugInstance.getClazzOrTypeOrMethod().stream()
        .filter(o -> o instanceof BugCollection.BugInstance.Class)
        .map(o -> ((BugCollection.BugInstance.Class) o).getClassname())
        .findFirst().orElse("Unknown class");

  }

  private String getTestName(BugInstance bugInstance) {

    SourceLine sourceLine = bugInstance.getClazzOrTypeOrMethod().stream()
        .filter(o -> o instanceof SourceLine)
        .map(o -> (SourceLine) o)
        .findFirst().orElse(null);

    if (sourceLine == null) {
      return "Unknown source line";
    }

    StringBuilder result = new StringBuilder();
    if (sourceLine.getStart() != null) {
      result.append("Line ").append(sourceLine.getStart());
      if (sourceLine.getEnd() != null && !sourceLine.getStart().equals(sourceLine.getEnd())) {
        result.append("-").append(sourceLine.getEnd());
      }
      result.append(": ");
    }
    result.append(bugInstance.getShortMessage());

    return result.toString();

  }

  private static String getFullDescription(BugCollection bugCollection, BugInstance bugInstance) {

    StringBuilder description = new StringBuilder();
    description.append(bugInstance.getLongMessage());

    bugCollection.getBugPattern().stream()
        .filter(bugPattern -> bugPattern.getAbbrev().equals(bugInstance.getAbbrev()))
        .findFirst().ifPresent(bugPattern -> {
          description.append("\n\n");
          description.append(Jsoup.parse(bugPattern.getDetails()).text());
        });

    return description.toString();

  }

}
