package de.tum.socialcomp.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * This convenience class is used to handle asynchronous 
 * HTTP-POST messages to the webservice.
 * 
 * @author Niklas Klügel
 *
 */

public class HttpPoster extends AsyncTask<String, Void, String> {
	private String serverUrl = "";
	private String httpPostResult;
	
	public HttpPoster(String url) {
		this.serverUrl = url;
	}
	
	// Uses the default url specified in the Configuration class 
	public HttpPoster(){
		this(Configuration.ServerURL);
	}

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
            HttpPost post = new HttpPost(url); 
      
             httpClient.execute(post);
             result = convertInputStreamToString(post.getEntity().getContent());
                    

        } catch(Exception e) {
            /*
             * some useful exception handling should be here 
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
    	httpPostResult = result;
    }   
    
    /**
     * Returns the result of the operation if needed lateron.
     * @return
     */
    
    public String getResult(){
    	return httpPostResult;
    }
}  