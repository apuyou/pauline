package fr.utc.assos.payutc;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;
import fr.utc.assos.payutc.soap.AdditionalKeyStoresSSLSocketFactory;
import fr.utc.assos.payutc.soap.PBuy;

/**
 * Demande au seller de badger
 * @author thomas
 *
 */
public class PaulineActivity extends BaseActivity {
	public static final String LOG_TAG			= "PaulineActivity";
	
	public static final int ASKSELLERPASSWORD	= 0;
	
	public final static int ID_POI				= 2;
	public final static int ID_FUNDATION		= 2;
	public static final int MEAN_OF_LOGIN		= 5; 
	
	public static final String ID_TRECOUVR			= "5B1BF88B";
	
	public static final PBuy PBUY = new PBuy();

	public static final int CASWEBVIEW	= 0;
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate PaulineActivity");
        setContentView(R.layout.main);
        HttpsURLConnection.setDefaultSSLSocketFactory(createAdditionalCertsSSLSocketFactory());
        HostnameVerifier v = new HostnameVerifier() {
        	public boolean verify(String hostname, SSLSession session) {
        		return true;
        	}
        };
        HttpsURLConnection.setDefaultHostnameVerifier(v);
        // @todo virer ce vieux hack
    	//startAskSellerPasswordActivity(ID_TRECOUVR); 
        /*SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", createAdditionalCertsSSLSocketFactory(), 443));
        */
        
        
    }
    
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(LOG_TAG, "requestCode:"+requestCode+" ,resultCode:"+resultCode);
		switch (requestCode) {
		case CASWEBVIEW:
			if (resultCode == RESULT_OK) {
				String ticket = data.getStringExtra("ticket");
				Log.i(LOG_TAG, "ticket : "+ticket);
			}
		}
    }
	

    public void onLogin(View view) {
    	Log.d(LOG_TAG,"startCasWebView");
    	Intent intent = new Intent(this, fr.utc.assos.payutc.CasWebView.class);
    	startActivityForResult(intent, CASWEBVIEW);
    }
    
    
    
    public void startHomeActivity() {
    	Log.d(LOG_TAG,"startHomeActivity");
    	Intent intent = new Intent(this, fr.utc.assos.payutc.HomeActivity.class);
    	startActivity(intent);
    }
    protected SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            final InputStream in = getApplicationContext().getResources().openRawResource( R.raw.mystore);  
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, getApplicationContext().getString( R.string.mystore_password ).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }
    

    private class LoadSellerTask extends AsyncTask<Integer, Integer, Integer> {
    	private String mIdSeller, mPass;
    	private int mMeanOfLogin;
    	private ProgressDialog mProgressDialog;
    	
    	public LoadSellerTask(String idSeller, int meanOfLogin, String pass) {
    		mIdSeller = idSeller;
    		mMeanOfLogin = meanOfLogin;
    		mPass = pass;
    	}

        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        	mProgressDialog = ProgressDialog.show(PaulineActivity.this, 
        			"Identification", 
        			"Connection au serveur en cour...",
        			true,
        			false
        	);
        }
        
        @Override
        protected Integer doInBackground(Integer... args) {
        	int r = PaulineActivity.PBUY.loadSeller(mIdSeller, mMeanOfLogin, mPass, PaulineActivity.ID_POI);
        	return r;
        }

        @Override
        protected void onPostExecute(Integer r) {
        	mProgressDialog.dismiss();
        	if (r==1) {
            	stop(true);
        	}
        	else {
        		Toast.makeText(PaulineActivity.this, "Echec de l'identification", Toast.LENGTH_SHORT).show();
        	}
        }
    }

}