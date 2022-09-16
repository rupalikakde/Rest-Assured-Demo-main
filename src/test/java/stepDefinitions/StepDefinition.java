package stepDefinitions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

public class StepDefinition {
    public static Response response;
    public static Response responseText;
    public static RequestSpecification requestSpecification;
    public static JSONObject msgBody = new JSONObject();   
    public static Map<String, String> customHeaders = new HashMap();
    public static String id;
    public static String userName;

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(StepDefinition.class);

    @Given("I use {string} as base URI")
    public void iUseAsBaseUri(String baseURI) {
        RestAssured.baseURI = baseURI;
        logger.info("Base URI is loaded !!!");
    }

    @When("I make {string} call to {string}")
    public void i_make_call_to(String method, String totalPath) throws InterruptedException {
        Map<String, String> queryParams = new HashMap<String, String>();

        //Get queryParams
        if (totalPath.contains("?")) {
            String[] paramPairs = ((totalPath.split("\\?"))[1]).split("&");
            for (String pair : paramPairs) {
                String[] myPair = pair.split("=");
                if (myPair.length == 1) { //When param value is blank
                    queryParams.put(myPair[0], "");
                } else {
                    queryParams.put(myPair[0], myPair[1]);
                }
            }
        }

        //Get Path
        String path = (totalPath.split("\\?"))[0];

        RestAssured.basePath = path;
        requestSpecification = RestAssured.given().headers(customHeaders).queryParams(queryParams);
        switch (method.toUpperCase().trim()) {
            case "GET":
                logger.info("GET Method is called !!!");
                if (path.contains("user")) 
                {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(2000);
                        response = requestSpecification.given().when().get();
                        if (response.statusCode() == 200) {
                            break;
                        }
                    }                   
                } 
                else
                {
                    response = requestSpecification.given().when().get();
                }
                break;
            case "POST":
                logger.info("POST Method is called !!!");
            	response = requestSpecification.body(msgBody.toJSONString()).post();
            	 break;               
            case "DELETE":
                logger.info("Delete Method is called !!!");
            	Thread.sleep(5000);
                if (path.contains("pet") || path.contains("store")) {
                    response = requestSpecification.given().when().delete("/" + id);
                } else {
                    response = requestSpecification.given().when().delete();
                }
                break;
            case "PUT":
                logger.info("PUT Method is called !!!");
            	response = requestSpecification.body(msgBody.toJSONString()).put();              
                break;
            default:
                logger.info(" Method not implemented called !!!");
                Assert.fail("Ooops!! Method [" + method + "] is not implemented.");
                break;
        }
    }

    @When("I make the request empty")
    public void i_make_the_request_empty() {
        logger.info("Empty request is made !!!");
    	msgBody.clear();
    }

    @Then("I get response code {string}")
    public void i_get_response_code(String responseCode) {
        Assert.assertEquals(
            "Incorrect response code. Expected: " + responseCode + " Actual: " + response
                .statusCode(), Integer.parseInt(responseCode), response.statusCode());

    }

    @Then("I verify at least {string} pet details is available in the response")
    public void i_verify_at_least_pet_details_is_available_in_the_response(String count)
        throws ParseException {

        List<String> myResponse = response.jsonPath().getList("$");
        Assert.assertTrue(myResponse.size() + " pet details found.",
            myResponse.size() >= Integer.parseInt(count));


    }
    
    @Then("I verify {string} pet details is available in the response") 
    public void i_verify_pet_details_is_available_in_the_response(String count) throws ParseException
    {
    	List<String> myResponse = response.jsonPath().getList("$");
        Assert.assertTrue(myResponse.size() + " pet details found as expected",
            myResponse.size() == Integer.parseInt(count));        
        
        
    }
    
    @Then("I verify PetStoreOrders details is available in the response")
    public void i_verify_pet_store_orders_details_is_available_in_the_response() throws ParseException{
    	int avlCnt= response.jsonPath().get("available");
    	System.out.println(avlCnt);
    	 //Assert.assertTrue("the available PetStore orders count",avlCnt>=1);
    	
    }

    @Then("I read request body from {string}") 
    public void iReadRequestBodyFrom(String fileName) throws IOException, ParseException 
    {
        logger.info("Reading request from json !!!");
        msgBody.clear();

    	JSONParser parser = new JSONParser();
    	Object obj = parser.parse(new FileReader("src/test/resources/testdata/" + fileName + ".json"));
        msgBody = (JSONObject) obj;
      
            
    }

    @Given("I make REST service headers with the below fields")
    public void iMakeRestServiceHeaderWithBelowFields(DataTable headerValues)
        throws ParseException {
        customHeaders.clear();
        List<Map<String, String>> headers = headerValues.asMaps(String.class, String.class);
        Iterator myHeader = headers.iterator();

        while (myHeader.hasNext()) {
            Map<String, String> header = (Map) myHeader.next();
            customHeaders.putAll(header);
        }
    }
    
    @Then("I verify ResposeBody for Status as {string}")
    public void i_verify_respose_body_for_status_as(String stsValue) {

        logger.info("Verifing  response body  !!!");
    	Assert.assertEquals("Incorrect status value. Expected: " + stsValue + 
    			"Actual: " + response.jsonPath().get("status"), stsValue, response.jsonPath().get("status"));
    }
    
    @Then("I verify ResposeBody for message as {string}")
    public void i_verify_respose_body_for_message_as(String expectedMessage) {
    	String actualMessage = response.jsonPath().get("message").toString();
        Assert.assertEquals(expectedMessage, actualMessage);
    }

    @And("I capture the ID from response") public void iCaptureTheidFrom() {
        id = (response.jsonPath().get("id")).toString();
    }

    @And("I capture the user name from request") public void iCaptureTheUserNameFromRequest() {
        userName = msgBody.get("username").toString();
    }

    @And("I update request with below values")
    public void iUpdateRequestWithBelowValues(DataTable fieldList) {
        List<Map<String, String>> fields = fieldList.asMaps(String.class, String.class);
        for (Map<String, String> field : fields) {
            for (Map.Entry<String, String> entry : field.entrySet()) {
                msgBody.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Given("I reset API parameters") 
    public void iResetAPIParameters() {
        response = null;
        responseText = null;
        requestSpecification = null;
        msgBody.clear();
        customHeaders.clear();
        id = "";
        userName = "";
    }

    @And("I verify below values in response")
    public void iVerifyBelowValuesInResponse(DataTable fieldList) {
        List<Map<String, String>> fields = fieldList.asMaps(String.class, String.class);
        for (Map<String, String> field : fields) {
            for (Map.Entry<String, String> entry : field.entrySet()) {
                msgBody.put(entry.getKey(), entry.getValue());
                Assert.assertEquals("Field value mitmatch", entry.getValue(),
                    response.body().path(entry.getKey()));
            }
        }
    }
    
    @Then("I make POST call to {string} to upload pet image {string}")
    public void iMakePOSTCallToToUploadPetImage(String path, String imageName) {
    	RestAssured.basePath = path;
    	requestSpecification = RestAssured.given().headers(customHeaders)
    		.multiPart("file", new File("src/test/resources/testdata/" + imageName));
    	response = requestSpecification.body(msgBody.toJSONString()).post();
    }    
}
