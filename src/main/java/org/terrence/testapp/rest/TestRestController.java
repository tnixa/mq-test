package org.terrence.testapp.rest;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.CreateClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.GetClassifierOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

  @Autowired
  protected NaturalLanguageClassifier naturalLanguageClassifier;

  // Test the classifier by analyzing test text

  private String classifierId = null;

  @RequestMapping(value = "/test", produces = "text/plain")
  public String runTest() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    try {

      // if the classifierId is null then a classifier has not been created so create
      // one
      // else a classifier exists and we need to check if it is done training
      // if it is done training then classify the test text
      // else it is still training so return in progress message

      if (classifierId == null) {
        // crete classifier
        System.out.println("classifierId is null");

        // training and metadata files stored in the jar in the 'resources' folder
        InputStream trainStream = this.getClass().getResourceAsStream("/weather_data_train.csv");
        InputStream metadataStream = this.getClass().getResourceAsStream("/metadata.json");

        CreateClassifierOptions createOptions = new CreateClassifierOptions.Builder().metadata(metadataStream)
            .trainingData(trainStream).build();

        Classifier classifier = naturalLanguageClassifier.createClassifier(createOptions).execute();
        classifierId = classifier.getClassifierId();

        return "Classifier created";

      } else {
        // return status
        System.out.println("classifierId is not null: " + classifierId);

        GetClassifierOptions getOptions = new GetClassifierOptions.Builder().classifierId(classifierId).build();

        Classifier classifierNew = naturalLanguageClassifier.getClassifier(getOptions).execute();

        System.out.println("ClassifierNew status: " + classifierNew.getStatus());

        if (classifierNew.getStatus().equalsIgnoreCase("Available")) {
          // training is ready so do the test
          pw.println("Beginning test...");
          String testText = "How hot will it be today?";
          String expectedClassText = "temperature";
          pw.println("testText is: " + testText);

          ClassifyOptions classifyOptions = new ClassifyOptions.Builder().classifierId(classifierId).text(testText)
              .build();

          Classification classification = naturalLanguageClassifier.classify(classifyOptions).execute();

          System.out.println(classification);

          if (classification.toString().contains(expectedClassText)) {
            // check classifier output
            pw.println("PASS: Classification results contain keyword: '" + expectedClassText + "'");
            pw.flush();
            return sw.toString();
          } else {
            pw.println("FAIL: Classification results do not contain keyword: '" + expectedClassText + "'");
            pw.flush();
            return sw.toString();
          }
        } else {
          // training is not ready
          return "Training in progress";
        }
      }

    } catch (Exception e) {
      pw.println("FAIL: Unexpected error during test.");
      e.printStackTrace();
    }
    pw.flush();
    return sw.toString();
  }
}