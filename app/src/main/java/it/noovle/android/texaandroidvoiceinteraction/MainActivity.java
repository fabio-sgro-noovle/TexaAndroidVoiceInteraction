package it.noovle.android.texaandroidvoiceinteraction;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static MainActivity mIstance;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private RequestQueue mRequestQueue;
    private ImageButton btnSpeak;

    private ArrayList<Risultato> risultati;
    private EditText editText;
    private ListView lista;
    private Button btnCerca;

    public static synchronized MainActivity getInstance() {
        return mIstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIstance = this;
        setContentView(R.layout.activity_main);


        editText = (EditText) findViewById(R.id.editText);
        lista = (ListView) findViewById(R.id.listView);

        btnSpeak = (ImageButton) findViewById(R.id.buttonInteraction);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        btnCerca = (Button) findViewById(R.id.buttonCerca);
        btnCerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testo = editText.getText().toString();
                eseguiRicercaGsa(testo);
            }
        });

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "PARLA");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "TTS NON SUPPORTATO",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String risultato = result.get(0);

                    verificaComando(risultato);

                }
            }
            break;
        }
    }


    private void verificaComando(String comando) {
        if (comando.startsWith("cerca")) {
            try {
                comando = comando.substring(6);
                editText.setText(comando);
                eseguiRicercaGsa(comando);
            } catch (Exception e) {
                Log.e("TEXA", "errore nello split del comando");
            }
        } else if (comando.startsWith("apri risultato")) {
            comando = comando.substring(15);
            apriRisultato(comando);
        } else {
            editText.setText(comando);
        }
    }

    private void apriRisultato(String numString) {
        try {
            Integer num = Integer.valueOf(numString);
            Risultato risultato
                    = risultati.get(num);
            String url = risultato.getUrl();
            apriUrl(url);
        } catch (Exception e) {
            Log.e("TEXA", "Errore nella apertura " + numString);
        }
    }

    private void eseguiRicercaGsa(String comando) {
        try {
            comando = URLEncoder.encode(comando, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String tag_json_arry = "json_obj_req";

        String url = "http://gsa-fi.noovle.it/search?q=" + comando + "&client=texa&site=texa&proxystylesheet=texa";

        JsonObjectRequest req;
        req = new JsonObjectRequest(Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        processa(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Accept-Language", "it");
                return params;
            }
        };

// Adding request to request queue
        MainActivity.getInstance().addToRequestQueue(req, tag_json_arry);
    }

    private void processa(JSONObject resp) {
        risultati = new ArrayList<Risultato>();
        try {
            JSONObject risult = resp.getJSONObject("GSP").getJSONObject("RES");
            JSONArray ris = risult.getJSONArray("R");
            for (int i = 0; i < ris.length(); i++) {
                JSONObject risultato = ris.getJSONObject(i);
                String numero = risultato.getString("N");

                String titolo = risultato.getString("T");
                titolo = StringEscapeUtils.unescapeHtml4(titolo);
                String url = risultato.getString("U");
                Risultato risultatoObject = new Risultato(numero, titolo, url);
                risultati.add(risultatoObject);
            }

            //spelling suggestion
            JSONObject sugger = resp.getJSONObject("GSP").getJSONObject("Spelling");
            JSONArray arr = sugger.getJSONArray("Suggestion");
            JSONObject sugg = arr.getJSONObject(0);
            final String suggerimento = sugg.getString("q");

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            verificaComando("cerca " + suggerimento);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Forse cercavi " + suggerimento + " ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();


        } catch (Exception e) {
            VolleyLog.d(TAG, "processa Error: " + e.getMessage());
        }

        RisultatiAdapter itemsAdapter;
        if (risultati.size() == 0) {
            Toast.makeText(this, "Nessun risultato trovato", Toast.LENGTH_LONG).show();
        }
        itemsAdapter = new RisultatiAdapter(this, risultati);
        lista.setAdapter(itemsAdapter);

        AdapterView.OnItemClickListener listenerClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvUrl = (TextView) view.findViewById(R.id.textviewUrl);
                String url = tvUrl.getText().toString();
                Log.i("TEXA", "url --> " + url);
                apriUrl(url);
            }
        };
        lista.setOnItemClickListener(listenerClick);
    }

    private void apriUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
