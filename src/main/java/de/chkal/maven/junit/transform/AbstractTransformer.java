package de.chkal.maven.junit.transform;

import de.chkal.maven.junit.transform.xml.junit.Testsuite;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;

public abstract class AbstractTransformer {

  public abstract Testsuite buildTestsuite(File inputFile) throws JAXBException;

  public void transform(File inputFile, File outputFile) {

    try {

      Testsuite testsuite = buildTestsuite(inputFile);

      Marshaller marshaller = JAXBContext.newInstance(Testsuite.class).createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(testsuite, outputFile);

    } catch (JAXBException e) {
      throw new IllegalStateException(e);
    }

  }

}
