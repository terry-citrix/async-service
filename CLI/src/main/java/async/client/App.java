package async.client;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private String baseUrl = null;
    private int threadCount = 0;
    private int repeatPeriod = 0;

    public static void main(String[] args) {
        App app = new App();
        app.parseInputs(args);
        app.start();
    }

    public void parseInputs(String[] args) {
        for (int i=0; i < args.length; i++) {
            if ("-url".equalsIgnoreCase(args[i])) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing input after -url");
                    System.exit(1);
                }
                baseUrl = args[i];

            } else if ("-count".equalsIgnoreCase(args[i])) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing input after -count");
                    System.exit(1);
                }
                try {
                    threadCount = Integer.parseInt(args[i]);
                } catch (NumberFormatException ex) {
                    System.err.println("ERROR - the argument after '-count' is not a number!");
                    System.exit(1);
                }
            } else if ("-repeat".equalsIgnoreCase(args[i])) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing input after -repeat");
                    System.exit(1);
                }
                try {
                    repeatPeriod = Integer.parseInt(args[i]);
                    System.out.println("Repeat Period is: " + repeatPeriod);
                } catch (NumberFormatException ex) {
                    System.err.println("ERROR - the argument after '-repeat' is not a number!");
                    System.exit(1);
                }
            }
        }

        if (baseUrl == null || threadCount == 0) {
            System.err.println("Error - missing inputs");
            System.exit(1);
        }

        if (!baseUrl.toLowerCase().startsWith("http")) {
            System.err.println("Error - the url is invalid");
            System.exit(1);
        }
    }

    public void start() {
        ExecutorService executor= Executors.newFixedThreadPool(1000);
        try {
            int tenantCount = 0;
            do {
                for (int i = 0; i < threadCount; i++) {
                    tenantCount++;
                    String tenantName = "Customer" + tenantCount;
                    executor.execute(new ApiCaller(baseUrl + tenantName));
                }

                if (repeatPeriod == 0) {
                    System.out.println("We're not repeating, so stop here.");
                    break;
                } else {
                    Thread.sleep(repeatPeriod * 1000L);
                    System.out.println("Starting next batch after the repeat period.");
                }
            } while (true);
        } catch (Exception err){
            err.printStackTrace();
        }
        executor.shutdown(); // once you are done with ExecutorService
    }

}

class ApiCaller implements Runnable {
    String url;

    public ApiCaller(String url) {
        this.url = url;
    }

    public void run() {
        System.out.println("Calling " + url);
        final HttpUriRequest request = new HttpGet(url);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse httpResponse = httpclient.execute(request)) {
                int responseCode = httpResponse.getCode();
                if (responseCode != HttpStatus.SC_OK) {
                    System.err.println("Got a bad response after calling " + url);
                } else {
                    HttpEntity entity = httpResponse.getEntity();
                    try {
                        String response = EntityUtils.toString(entity);
                        System.out.println("Got response after calling " + url + ": " + response);
                    } catch (ParseException ex) {
                        System.err.println("Error parsing response after calling " + url);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("ERROR in ApiCaller calling " + url + ". Details: " + ex.getMessage());
        }
    }

}
