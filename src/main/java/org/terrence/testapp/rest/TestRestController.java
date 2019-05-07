package org.terrence.testapp.rest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.stream.JsonReader;

import com.ibm.watson.developer_cloud.personality_insights.v3.PersonalityInsights;
import com.ibm.watson.developer_cloud.personality_insights.v3.model.Content;
import com.ibm.watson.developer_cloud.personality_insights.v3.model.Profile;
import com.ibm.watson.developer_cloud.personality_insights.v3.model.ProfileOptions;
import com.ibm.watson.developer_cloud.util.GsonSingleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

  @Autowired
  protected PersonalityInsights personalityInsights;

  // Test the Insights by analyzing a test json file

  @RequestMapping(value = "/test", produces = "text/plain")
  public String runTest() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    try {
      pw.println("Beginning test...");
      InputStream profileStream = this.getClass().getResourceAsStream("/profile.json");
      InputStreamReader inputStreamReader = new InputStreamReader(profileStream);
      JsonReader jsonReader = new JsonReader(inputStreamReader);
      Content content = GsonSingleton.getGson().fromJson(jsonReader, Content.class);

      ProfileOptions profileOptions = new ProfileOptions.Builder().content(content).consumptionPreferences(true)
          .rawScores(true).build();
      pw.println("Creating Personality Insights from profile.json file");
      Profile profile = personalityInsights.profile(profileOptions).execute();
      System.out.println("Profile Results: " + profile);

      // check to see if query exists in the results
      String expectedKeyword = "Agreeableness";
      if (profile.toString().toLowerCase().contains(expectedKeyword.toLowerCase())) {
        pw.println("PASS: Personality Insight results contain expected keyword: " + expectedKeyword);
      } else {
        pw.println("FAIL: Personality Insight results do not contain expected keyword: " + expectedKeyword);
      }

    } catch (Exception e) {
      pw.println("FAIL: Unexpected error during test.");
      e.printStackTrace();
    }
    pw.flush();
    return sw.toString();
  }
}