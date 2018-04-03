package edu.cnm.deepdive.mytestsigninvkv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.Arrays;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

  private String idToken;
  private PassphraseService service;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Button request = findViewById(R.id.request_passphrase);
    request.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new RequestPassphraseTask().execute();
      }
    });
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(getString(R.string.service_url))
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    service = retrofit.create(PassphraseService.class);
    idToken = getTestApplication().getGoogleAccount().getIdToken();
  }

  private TestApplication getTestApplication() {
    return (TestApplication) getApplication();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_logout:
        getTestApplication().getSignInClient().signOut()
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                getTestApplication().setGoogleAccount(null);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
              }
            });
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }

  }

  private class RequestPassphraseTask extends AsyncTask<Void, Void, String[]> {


    @Override
    protected String[] doInBackground(Void... voids) {
      try {
        return service.get(getString(R.string.authorization_header, idToken)).execute().body();
      } catch (IOException e) {
        cancel(true);
        return null;
      }
    }

    @Override
    protected void onPostExecute(String[] response) {
      EditText passphrase = findViewById(R.id.passphrase);
      String concatResponse = Arrays.toString(response)
          .replaceAll(getString(R.string.comma_regex), " ");
      passphrase.setText(concatResponse.substring(1, concatResponse.length() - 1));
    }

    // TODO on canceled
  }

}
