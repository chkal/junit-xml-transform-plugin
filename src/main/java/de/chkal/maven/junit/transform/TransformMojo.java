package de.chkal.maven.junit.transform;

import de.chkal.maven.junit.transform.spotbugs.SpotbugsTransformer;
import java.io.File;
import java.text.MessageFormat;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "transform", defaultPhase = LifecyclePhase.VERIFY)
public class TransformMojo extends AbstractMojo {

  @Parameter(defaultValue = "true")
  public boolean spotbugsEnabled;

  @Parameter(defaultValue = "${project.build.directory}/spotbugsXml.xml")
  public File spotbugsInputFile;

  @Parameter(defaultValue = "${project.build.directory}/spotbugsXml-junit.xml")
  public File spotbugsOutputFile;

  public void execute() throws MojoExecutionException, MojoFailureException {

    if (spotbugsEnabled) {
      if (spotbugsInputFile.canRead()) {
        logInfo("SpotBugs native XML report found: {0}", spotbugsInputFile);
        new SpotbugsTransformer().transform(spotbugsInputFile, spotbugsOutputFile);
        logInfo("SpotBugs JUnit XML report created: {0}", spotbugsOutputFile);
      } else {
        getLog().info(String.format("SpotBugs native XML report not found: %s", spotbugsInputFile));
      }
    } else {
      logInfo("SpotBug transformation disabled.");
    }

  }

  private void logInfo(String pattern, Object... arguments) {
    getLog().info(MessageFormat.format(pattern, arguments));
  }


}
