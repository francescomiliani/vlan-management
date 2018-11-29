package application;

import java.net.*;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
/**
 * This class performs the HTTP request and receive the HTTP response returns by the Restful server.
 * The HTTP requests are of 2 types: GET or POST. The POST are contain parameters, while the GET one NO.
**/
public class HTTPManager {
	private StringBuffer content = null;
	private HttpURLConnection con = null;//Connection
	private URL url = null;
	private String request_url;

	public static final String GET_REQUEST = "GET";
	public static final String POST_REQUEST = "POST";

	public HTTPManager( String url ) {
		this.request_url = url;
	}

	/**
	 * This method simply invoke the specific http request
	**/
	public String doHTTPRequest( String type, Map<String, String> parameters ) {
		String resp = "";

		switch( type ) {
			case GET_REQUEST:
				resp = sendGet( );
				break;
			case POST_REQUEST:
				resp = sendPost( parameters );
				break;
		}
		return resp;
	}

	// HTTP POST request
	private String sendGet( ) {

		try {
			url = new URL( request_url );
			//Create the connection
			con = (HttpURLConnection) url.openConnection();

			// Setting Request Headers
			con.setRequestMethod( GET_REQUEST );
			con.setRequestProperty("Content-Type", "application/json");
			con.setUseCaches(false);
			con.setDoOutput(true);


			//Obtain a response
			int status = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + status);
			//Finally, let’s read the response of the request and place it in a content String:

			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
			String inputLine;
			content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append( inputLine + "\n" );
			}
			in.close();
			//To close the connection, we can use the disconnect() method:
		}  catch (ConnectException e) {
			// TODO Auto-generated catch block
			System.err.println("Connection refused");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    if (con != null)
		        con.disconnect();
		}

		System.out.println( "Content: " + content.toString() );
		return content.toString();
	}

	// HTTP POST request
	private String sendPost( Map<String, String> parameters ) {
		try {
			url = new URL( request_url );
			con = (HttpURLConnection) url.openConnection();

			//add request header
			con.setRequestMethod( POST_REQUEST );
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			con.setUseCaches(false);
			
			String s = getJsonString( parameters );

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes( s );
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + s );
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			content = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine + "\n");
			}
			in.close();
		} catch( ConnectException e ) {
			System.err.println("Connection refused");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    if (con != null)
		        con.disconnect();
		}
		//print result
		System.out.println( "Content: " + content.toString() );
		return content.toString();

	}
	
	/**
	 * This method is used for construct a formatted string from a set of parameters
	**/
	private String getJsonString(Map<String, String> params) {
        String result = "{";

        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        Map.Entry<String, String> entry = null;
        while( it.hasNext()  ) {
        	entry = it.next();
        	result += "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
        	if( it.hasNext() ==  true )
        		result += ", ";
        }
        return result += "}";
    }
}