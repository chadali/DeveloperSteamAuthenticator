package com.chadali.developersteamauthenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddAccount extends AppCompatActivity {
    public static final String INTENT_MESSAGE = "com.chadali.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
    }

    public void addAccount(View view) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_key_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        EditText username = (EditText) findViewById(R.id.nickname);
        EditText sharedSecret = (EditText) findViewById(R.id.sharedSecret);

        editor.putString(username.getText().toString(), sharedSecret.getText().toString());
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_MESSAGE, "Stored new account " + username.getText().toString());
        startActivity(intent);
    }

}
