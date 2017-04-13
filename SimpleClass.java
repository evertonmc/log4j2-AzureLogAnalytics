import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import java.io.DataOutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

public class SimpleClass {

	public static void main(String[] args) {

		// Update the customer ID to your Operations Management Suite workspace ID
		String customer_id = "workspaceid";

		// For the shared key, use either the primary or the secondary Connected Sources client authentication key
		String shared_key = "workspacekey";

		// The log type is the name of the event that is being submitted
		String log_type = "CustomApplication";
		
		// Input to OMS Log Analytics
		String json = new String("{\"LinuxCurlId\":\"001\",\"LinuxCurlValue1\":\"value1\"}");
		
		String Signature = "";
		String encodedHash = "";
		String url = "";

		// Date object
		Date date = new Date();

		// Todays date input for OMS Log Analytics
		String timeNow = String.format("%ta, %<td %<tb %<tY %<tT GMT", date );
   
		// String for signing the key
		String stringToSign="POST\n" + json.length() + "\napplication/json\nx-ms-date:"+timeNow+"\n/api/logs";


		try {
			byte[] decodedBytes = Base64.decodeBase64(shared_key);

			Mac hasher = Mac.getInstance("HmacSHA256");
			hasher.init(new SecretKeySpec(decodedBytes, "HmacSHA256"));
			byte[] hash = hasher.doFinal(stringToSign.getBytes());
		    
			encodedHash = DatatypeConverter.printBase64Binary(hash);
			Signature = "SharedKey " + customer_id + ":" + encodedHash;
	    
			url = "https://" + customer_id + ".ods.opinsights.azure.com/api/logs?api-version=2016-04-01";	    
			URL objUrl = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) objUrl.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Log-Type",log_type);
			con.setRequestProperty("x-ms-date", timeNow);
			con.setRequestProperty("Authorization", Signature);
	        
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(json);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + json);
			System.out.println("Response Code : " + responseCode);
		}
		catch (Exception e) {
			System.out.println("Catch statement: " + e);
		}
	}
}
