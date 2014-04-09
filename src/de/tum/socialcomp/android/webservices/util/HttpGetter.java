package de.tum.socialcomp.android.webservices.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import de.tum.socialcomp.android.Configuration;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * This convenience class is used to handle asynchronous 
 * HTTP-GET messages to the webservice.
 * 
 * @author Niklas Klügel
 *
 */

public class HttpGetter extends AsyncTask<String, Void, String> {
	private String serverUrl = "";
	
	public HttpGetter(String url) {
		this.serverUrl = url;
	}
	
	// Uses the default url specified in the Configuration class 
	public HttpGetter(){
		this(Configuration.ServerURL);
	}
	
	private String httpGetResult = "";

    @Override
    protected String doInBackground(String... params)   
    {           
        BufferedReader inBuffer = null;
        String url = this.serverUrl;
        String result = "fail";
        
        for(String parameter: params) {
        	url = url + "/" + parameter;
        }                
        
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet getter = new HttpGet(url); 
           
            HttpResponse httpResponse =  httpClient.execute(getter);
            result = convertInputStreamToString(httpResponse.getEntity().getContent());
                    

        } catch(Exception e) {
            /*
             * some exception handling should take place here
             */
        	
        } finally {
            if (inBuffer != null) {
                try {
                    inBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  result;
    }
    
    private String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }

    protected void onPostExecute(String result) {       
    	// this is used to access the result of the HTTP GET lateron
    	httpGetResult = result;
    }   
    
    /**
     * Returns the result of the operation if needed lateron.
     * @return
     */
    public String getResult(){
    	return httpGetResult;
    }
    
}  