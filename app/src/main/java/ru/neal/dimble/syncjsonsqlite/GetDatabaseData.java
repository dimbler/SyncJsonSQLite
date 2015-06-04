package ru.neal.dimble.syncjsonsqlite;

/**
 * Created by dimbler on 04.06.2015.
 */

import org.json.JSONObject;
        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.UnsupportedEncodingException;
import java.security.Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.http.HttpResponse;
        import org.apache.http.HttpVersion;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.conn.ClientConnectionManager;
        import org.apache.http.conn.scheme.PlainSocketFactory;
        import org.apache.http.conn.scheme.Scheme;
        import org.apache.http.conn.scheme.SchemeRegistry;
        import org.apache.http.conn.ssl.SSLSocketFactory;
        import org.apache.http.entity.StringEntity;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
        import org.apache.http.params.BasicHttpParams;
        import org.apache.http.params.HttpConnectionParams;
        import org.apache.http.params.HttpParams;
        import org.apache.http.params.HttpProtocolParams;
        import org.apache.http.protocol.HTTP;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
        import android.util.Log;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.HttpEntity;
        import org.apache.http.util.EntityUtils;
        import android.widget.Toast;


public class GetDatabaseData extends AsyncTask<Void, Void, JSONObject> {
    private final String DeviceUrl = "https://propusk.neal.ru/compbase.json";

    private Context context;

    public GetDatabaseData (Context context){
        this.context = context;
    }

    public boolean isNetworkConnectedOrConnecting(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    protected JSONObject doInBackground(Void... paramms) {

        if (isNetworkConnectedOrConnecting(context)) {

            KeyStore keystore = null;
            try {
                keystore = KeyStore.getInstance("PKCS12");
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            try {
                keystore.load(context.getResources().openRawResource(R.raw.client2), "zaq12WSX".toCharArray());

                InputStream caInput = context.getResources().openRawResource(R.raw.ca);

                try {
                    CertificateFactory cfa = CertificateFactory.getInstance("X.509");
                    java.security.cert.Certificate ca;
                    // generate a certificate
                    ca = cfa.generateCertificate(caInput);
                    Log.d("CA", "ca=" + ((X509Certificate) ca).getSubjectDN());

                    keystore.setCertificateEntry("ca", ca);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    caInput.close();
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }

            SSLSocketFactory sslSocketFactory = null;
            try {
                sslSocketFactory = new AdditionalKeyStoresSSLSocketFactory(keystore);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpProtocolParams.setUseExpectContinue(params, true);

            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(params, timeoutSocket);

            final SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sslSocketFactory, 443));

            ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
            DefaultHttpClient httpclient = new DefaultHttpClient(manager, params);

            /*

            HttpParams hparams = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(hparams, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(hparams, timeoutSocket);

            HttpProtocolParams.setVersion(hparams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(hparams, HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(hparams, true);

            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

            HttpClient client = new DefaultHttpClient(conMgr, params);
            */
            HttpGet GetRequest = new HttpGet(DeviceUrl);
            String response;

            try {
                HttpResponse responce = httpclient.execute(GetRequest);
                HttpEntity httpEntity = responce.getEntity();

                response = EntityUtils.toString(httpEntity);

                //Log.d("response is", response);
                return new JSONObject(response);


            } catch (UnsupportedEncodingException e) {
                Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("AsyncOperationFailed", e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    @Override
    protected void onPostExecute(JSONObject result)
    {
        super.onPostExecute(result);
        if(result != null)
        {
            try
            {
                //Log.d("JSON RESULT", result.toString());

                String value = result.getString("version");

                //String status = jobj.getString("status");
                //if(status.equals("true"))
                //{
                //   JSONArray array = jobj.getJSONArray("data");
                //}
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            //Toast.makeText(this.context, "Ошибка соединения с сервером", Toast.LENGTH_SHORT).show();
        }
    }
}

