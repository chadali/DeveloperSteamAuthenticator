package com.chadali.developersteamauthenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.crypto.spec.*;
import javax.crypto.Mac;

public class MainActivity extends ActionBarActivity {
    private static ListView list_view;
    private static List<String> accounts;
    Toast code;
    String sharedSecret;
    int timeDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accounts = new ArrayList<String>();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_key_file), Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            accounts.add(entry.getKey());
        }

        listView();
        // test
        Intent intent = getIntent();
        if (intent.hasExtra(AddAccount.INTENT_MESSAGE)) {
            String message = intent.getStringExtra(AddAccount.INTENT_MESSAGE);
            if (code != null) {
                code.cancel();
            }
            code = Toast.makeText(MainActivity.this , message, Toast.LENGTH_LONG);
            code.show();
        }

    }

    public void listView() {
        list_view = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.accounts_list, accounts);
        list_view.setAdapter(adapter);
        list_view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (code != null) {
                            code.cancel();
                        }
                        String name = (String)list_view.getItemAtPosition(position);
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_key_file), Context.MODE_PRIVATE);
                        String secret = sharedPref.getString(name, "no secret found");
                        code = Toast.makeText(MainActivity.this, "Value: " + getAuthCode(secret, 0), Toast.LENGTH_LONG);
                        code.show();
                    }
                }
        );
        list_view.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = (String)list_view.getItemAtPosition(position);
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_key_file), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove(name);
                        editor.apply();
                        if (code != null) {
                            code.cancel();
                        }
                        code = Toast.makeText(MainActivity.this, "Deleted: " + name, Toast.LENGTH_LONG);
                        code.show();
                        finish();
                        startActivity(getIntent());
                        return true;
                    }
                }
        );

    }

    public void goToAccountAddition(View view) {
        Intent intent = new Intent(this, AddAccount.class);
        startActivity(intent);
    }

    public String getAuthCode(String sharedSecret, int timeDiff) {
        long unixTime = (System.currentTimeMillis() / 1000L) + timeDiff;
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = Base64.decode(sharedSecret, Base64.DEFAULT);
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            int time = (int) (unixTime/30);
            ByteBuffer b = ByteBuffer.allocate(8);
            b.putInt(4, time);
            b.order(ByteOrder.BIG_ENDIAN);
            byte[] result = b.array();
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(result);
            byte start = (byte) (rawHmac[19] & 0x0F);

            byte[] bytes = new byte[4];
            bytes = Arrays.copyOfRange(rawHmac, start, start+4);
            ByteBuffer wrapped = ByteBuffer.wrap(bytes);
            int codeInt = wrapped.getInt();
            int fullcode = (codeInt & 0x7fffffff) & 0x00000000ffffffff;

            char[] STEAMCHARS = new char[] {
                    '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C',
                    'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
                    'R', 'T', 'V', 'W', 'X', 'Y'};
            String chars = "23456789BCDFGHJKMNPQRTVWXY";
            String code = "";
            for(int i = 0; i < 5; i++) {
                String curChar = String.valueOf(chars.charAt(fullcode % chars.length()));
                code = code + curChar;
                fullcode /= chars.length();
            }
            return code;

        } catch (Exception e) {
           return "Invalid secret!"; 
        }
    }
}
