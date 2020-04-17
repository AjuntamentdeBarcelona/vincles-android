package cat.bcn.vincles.mobile.Client.Requests;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Model.ServerTime;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class GetServerTimeRequest  extends AsyncTask<String, Void, String> {


        private OnServerTimeDoneListener onServerTimeDoneListener;
        private String urlStr = "";

    public GetServerTimeRequest(OnServerTimeDoneListener onTaskDoneListener) {

            this.urlStr = Environment.getApiBaseUrl()+"/t/vincles-bcn.cat/vincles-services/1.0/public/time/current";
            this.onServerTimeDoneListener = onTaskDoneListener;
        }

        @Override
        protected String doInBackground(String... params) {



            try {

                URL mUrl = new URL(urlStr);
                HttpURLConnection httpConnection = (HttpURLConnection) mUrl.openConnection();
                if (httpConnection instanceof HttpsURLConnection){
                    HttpsURLConnection conHttps = (HttpsURLConnection) httpConnection;
                    // Set up a Trust all manager
                    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
                    {

                        public java.security.cert.X509Certificate[] getAcceptedIssuers()
                        {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType)
                        {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType)
                        {
                        }
                    } };

                    // Get a new SSL context
                    SSLContext sc = SSLContext.getInstance("TLSv1.2");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    // Set our connection to use this SSL context, with the "Trust all" manager in place.
                    conHttps.setSSLSocketFactory(sc.getSocketFactory());
                    // Also force it to trust all hosts
                    HostnameVerifier allHostsValid = new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return hostname.equals(OtherUtils.getBaseHostName());
                        }
                    };
                    // and set the hostname verifier.
                    conHttps.setHostnameVerifier(allHostsValid);
                }
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Content-length", "0");
                httpConnection.setUseCaches(false);
                httpConnection.setAllowUserInteraction(false);
                httpConnection.setConnectTimeout(100000);
                httpConnection.setReadTimeout(100000);

                httpConnection.connect();

                int responseCode = httpConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    return sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            long currentTime = 0;
            try {

                JsonParser parser = new JsonParser();
                JsonElement mJson =  parser.parse(s);
                Gson gson = new Gson();
                ServerTime serverTime = gson.fromJson(mJson, ServerTime.class);
                currentTime = serverTime.getCurrentTime();

            } catch (Throwable t) {
                Log.e("GetServerTime", "Could not parse malformed JSON: \"" + s + "\"");
            }

            if (onServerTimeDoneListener != null && currentTime != 0) {
                onServerTimeDoneListener.onServerTimeDone(currentTime);
            } else if (onServerTimeDoneListener != null) {
                onServerTimeDoneListener.onServerTimeError();
            }
        }

        public interface OnServerTimeDoneListener {
            void onServerTimeDone(long responseData);

            void onServerTimeError();
        }

    }
