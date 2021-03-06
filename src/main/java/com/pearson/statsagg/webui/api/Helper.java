package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Helper {
    
    private static final Logger logger = LoggerFactory.getLogger(Helper.class.getName());

    protected final static String ERROR_UNKNOWN_JSON = "{\"Error\":\"An unexpected error has occurred\"}";
    protected final static String ERROR_NOTFOUND_JSON = "{\"Error\":\"Not Found\"}";

    /*
    Creates a simple JSON string in the format of {"Message":"myMessage"}
    where 'myMessage' is the 'message' parameter
    */
    public static String createSimpleJsonResponse(String message) {
        return "{\"Message\":\"" + StringEscapeUtils.escapeJson(message) + "\"}";
    }
    
    /*
    Creates a simple JSON string in the format of {"Message":"myError"}
    where 'myMessage' is the 'myError' parameter
    */
    public static String createSimpleJsonErrorResponse(String errorMessage) {
        return "{\"Error\":\"" + StringEscapeUtils.escapeJson(errorMessage) + "\"}";
    }
    
    public static JsonObject getJsonObjectFromRequestBody(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        BufferedReader bufferedReader = null;
        JsonObject jsonObject = null;
        
        try {
            StringBuilder requestData = new StringBuilder();
            bufferedReader = request.getReader();
            String currentLine = null;
            while ((currentLine = bufferedReader.readLine()) != null) requestData.append(currentLine);
            
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(requestData.toString());
            jsonObject = new Gson().toJsonTree(jsonElement).getAsJsonObject();
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return jsonObject;
    }
    
}
