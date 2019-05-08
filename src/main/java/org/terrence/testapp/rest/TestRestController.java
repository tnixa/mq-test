package org.terrence.testapp.rest;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

  @Autowired
  protected VisualRecognition visualRecognition;

  // Create the input stream from the jpg file

  public static InputStream createInputStream() throws Exception {
    InputStream inputStream = TestRestController.class.getResourceAsStream("/640px-IBM_VGA_90X8941_on_PS55.jpg");
    return inputStream;
  }

  // Test the Visual Recognizer by analyzing a test image

  @RequestMapping(value = "/test", produces = "text/plain")
  public String runTest() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    try {
      pw.println("Beginning test...");

      ClassifyOptions classifyOptions = new ClassifyOptions.Builder().imagesFile(createInputStream())
          .imagesFilename("640px-IBM_VGA_90X8941_on_PS55.jpg").build();

      ClassifiedImages result = visualRecognition.classify(classifyOptions).execute().getResult();
      System.out.println(result);

      // check to see if query exists in the results
      String expectedKeyword = "computer circuit";
      if (result.toString().toLowerCase().contains(expectedKeyword.toLowerCase())) {
        pw.println("PASS: Visual Recognizer results contain expected keyword: " + expectedKeyword);
      } else {
        pw.println("FAIL: Visual Recognizer results do not contain expected keyword: " + expectedKeyword);
      }

    } catch (Exception e) {
      pw.println("FAIL: Unexpected error during test.");
      e.printStackTrace();
    }
    pw.flush();
    return sw.toString();
  }
}