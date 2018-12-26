package casestudy.InfluxRestAPI;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import static org.apache.http.protocol.HTTP.USER_AGENT;

public class WriteToInfluxDB {

        public String write (StringBuilder items, String host, String databasename, String username, String password)
        {
            StringBuffer result = new StringBuffer();

            try {
                if(items.toString() != null && items.toString() != " ") {
                    String url = host + "/write?db=" + databasename + "&u=" + username + "&p=" + password;
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpPost post = new HttpPost(url);
                    post.setHeader("User-Agent", USER_AGENT);
                    post.setEntity(new StringEntity(items.toString()));

                    HttpResponse response = client.execute(post);
                    System.out.println("Response Code : "
                            + response.getStatusLine().getStatusCode());
                    if (response.getStatusLine().getStatusCode() == 204) {
                        response.setEntity(new StringEntity("---- Data Sent Successfully"));
                    }

                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                    System.out.println("Response Message :"+response.getEntity().getContent());
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                }
                else
                {
                    System.out.println("Write API Request Body is null");
                }

            }
            catch (Exception e)
            {
                System.out.println(e.fillInStackTrace());
            }
            return result.toString();
        }
    }
