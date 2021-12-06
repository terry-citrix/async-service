package com.terrydu.asyncservice.api;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.springframework.stereotype.Service;

@Service
public class HttpService {

  public Single<HttpResponse> callHttp(String tenantName, String httpsUrl) {
    return callHttpPrivate(tenantName, httpsUrl);
  }

  private Single<HttpResponse> callHttpPrivate(String tenantName, String httpsUrl) {
    return Single.create(singleSubscriber -> {
      System.out.println("Calling Terry URL, tenant: " + tenantName + "' on thread " + Thread.currentThread().getName());
      String response = "<ERROR>";

      try {
        response = responseFromHttpCall(httpsUrl);
      } catch (IOException e) {
        e.printStackTrace();
        singleSubscriber.onError(new RuntimeException("rrrrr", e));
      }
      HttpResponse t = new HttpResponse(response, tenantName);
      singleSubscriber.onSuccess(t);

    });
  }

  private String responseFromHttpCall(String httpsUrl) throws IOException {
    URL myUrl = new URL(httpsUrl);
    HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
    conn.setConnectTimeout(120000);
    conn.setReadTimeout(120000);
    InputStream is = conn.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    String inputLine;

    StringBuilder stringBuilder = new StringBuilder();

    while ((inputLine = br.readLine()) != null) {
      stringBuilder.append(inputLine);
    }

    return stringBuilder.toString();
  }

  /**
   * Deletes the entity having supplied id.
   *
   * @param inId Id of entity to delete.
   * @return Observable that will receive completion, or exception if error occurs.
   */
  public Observable<HttpResponse> callJersey(String tenantName, String httpsUrl) {
    return Observable.create(inSource -> {
      System.out.println("Calling Terry URL, tenant: " + tenantName + "' on thread " + Thread.currentThread().getName());
      String response = "<ERROR>";
      try {
        response = responseFromHttpCall(httpsUrl);
      } catch (IOException e) {
        e.printStackTrace();
        inSource.onError(new RuntimeException("rrrrr", e));
      }
      HttpResponse value = new HttpResponse(response, tenantName);
      inSource.onNext(value);
      inSource.onComplete();
    });
  }

}
