// In Class Assignment 04 HomeWork
// MainActivity.java
// Akarsh Gupta     - 800969888
// Ahmet Gencoglu   - 800982227
//
package com.example.akarsh.inclass04homework3;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    // global declaration
    Handler handler;
    private int passwordCount= 1, passwordLength = 8;
    private ArrayList<String> passwords;
    private TextView textPassword;
    private TextView textCount;
    private TextView textLength;
    private SeekBar seekLength;
    private SeekBar seekCount;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find UI variables
        seekCount = (SeekBar) findViewById(R.id.seekCount);
        seekLength = (SeekBar) findViewById(R.id.seekLength);
        seekCount.setOnSeekBarChangeListener(this);
        seekLength.setOnSeekBarChangeListener(this);

        textCount = (TextView) findViewById(R.id.textCount);
        textLength = (TextView) findViewById(R.id.textLength);
        textPassword = (TextView) findViewById(R.id.textPassword);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.progressLabel));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        // Setup handler for threading
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    // Start generating Passwords
                    case GeneratePasswords.STATUS_START:
                        progressDialog.show();
                        progressDialog.setProgress(0);
                        break;
                    // Update progress
                    case GeneratePasswords.STATUS_PROGRESS:
                        progressDialog.setProgress((int) msg.obj);
                        break;
                    // End generating passwords
                    case GeneratePasswords.STATUS_DONE:
                        passwords = msg.getData().getStringArrayList(getResources().getString(R.string.messagePasswordBundleKey));
                        progressDialog.dismiss();
                        showPasswords();
                        break;
                }
                return false;
            }
        });
    }

    // Binding Generate Passwords Using Threads Button Onclick Event
    public void generatePasswordsUsingThreads(View v)
    {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(new GeneratePasswords());
    }
    // Binding Generate Passwords Using AsyncTask Button Onclick Event
    public void generatePasswordsUsingAsyncTask(View v)
    {
        new GeneratePasswordsAsync().execute(passwordLength,passwordCount);
    }

    // Show passwords after generate password process is finished
    public void showPasswords()
    {
        // Create Alert Dialog to show after Progress Bar is dismissed
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getString(R.string.alertLabel));

        // Convert ArrayList to CharSerquence Array
        CharSequence[] psw = passwords.toArray(new CharSequence[passwords.size()]);
        alertDialog.setItems(psw, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textPassword.setText(passwords.get(which));
            }
        });
        // Show alert
        alertDialog.show();
    }

    // Seekbar Change Event bind
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // changing text on seekbar change
        if (seekBar.getId() == R.id.seekCount) {
            passwordCount = progress + 1;
            textCount.setText(Integer.toString(passwordCount));
        } else if (seekBar.getId() == R.id.seekLength) {
            passwordLength = progress + 8;
            textLength.setText(Integer.toString(passwordLength));
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // nothing to perform
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // nothing to perform
    }

    class GeneratePasswords implements Runnable
    {
        ArrayList<String> passwords = new ArrayList<String>();
        Message message = new Message();
        Bundle messageBundle = new Bundle();

        // Message Codes
        final static int STATUS_START = 0x00;
        final static int STATUS_PROGRESS = 0x03;
        final static int STATUS_DONE = 0x01;

        // Run in backcground thread
        @Override
        public void run() {

            // Send start message
            message = new Message();
            message.what = STATUS_START;
            handler.sendMessage(message);
            for(int passwordIndex=1;passwordIndex<=passwordCount;passwordIndex++)
            {
                // send progress message
                passwords.add(Util.getPassword(passwordLength));
                message = new Message();
                message.what = STATUS_PROGRESS;
                int progress = 100/passwordCount;
                message.obj = passwordIndex * progress;
                handler.sendMessage(message);
            }
            // send complete message
            message = new Message();
            messageBundle.putStringArrayList(getResources().getString(R.string.messagePasswordBundleKey),passwords);
            message.what = STATUS_DONE;
            message.setData(messageBundle);
            handler.sendMessage(message);

        }
    }
    // Extendss Generates passwords with AsyncTask
    class GeneratePasswordsAsync extends AsyncTask<Integer,Integer,ArrayList<String>>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            progressDialog.setProgress(0);
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... params) {

            // password list
            ArrayList<String> passwords = new ArrayList<>();
            int length = params[0];
            int count = params[1];
            for (int index = 1; index <= count; index++){
                passwords.add(Util.getPassword(length));
                int progress = 100 / count;
                publishProgress(index * progress);
            }
            return passwords;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
            passwords = strings;
            showPasswords();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }


    }

}
