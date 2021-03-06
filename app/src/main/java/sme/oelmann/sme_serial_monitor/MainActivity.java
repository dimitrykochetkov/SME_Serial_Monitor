package sme.oelmann.sme_serial_monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import sme.oelmann.sme_serial_monitor.helpers.Converter;
import sme.oelmann.sme_serial_monitor.helpers.PortUtil;
import sme.oelmann.sme_serial_monitor.helpers.SMEAnimator;
import sme.oelmann.sme_serial_monitor.helpers.VersionHelper;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private EditText etIn;
    private AutoCompleteTextView etBlackOut;
    private Switch swOn;

    public static boolean firstLoad = true;
    private boolean portIsOpened = false;
    private int textSize = 0;
    private long back_pressed;

    private PortUtil portUtil;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // loading custom toolbar
        View view = View.inflate(this, R.layout.actionbar, null);
        TextView tv = view.findViewById(R.id.tvTitle);
        tv.setText(getString(R.string.app_name));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
            view.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        }

        // loading last viewed activity
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        // declare interface
        Button bClear = findViewById(R.id.bClear);
        Button bSend = findViewById(R.id.bSend);
        etIn = findViewById(R.id.etIn);
        etBlackOut = findViewById(R.id.etBlackOut);
        swOn = findViewById(R.id.swOn);
        TextView tvVersion = findViewById(R.id.tvVersion);

        // setting on touch and on click listeners for some buttons
        bClear.setOnTouchListener(this);
        bSend.setOnTouchListener(this);

        bClear.setOnClickListener(this);
        bSend.setOnClickListener(this);

        swOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refresh();
            }
        });

        // adding app version in a lower panel
        String version = "v." + new VersionHelper(this, BuildConfig.BUILD_DATE).getVersion();
        tvVersion.setText(version);

        portUtil = new PortUtil(this); // declaring an object to work with a port

        // checking if device has default port
        if (portUtil.getDefaultPort().equals(getString(R.string.no_ports))) dialogAlert();
        else swOn.setEnabled(true);

        // saving default port to shared preferences
        SharedPreferences.Editor spEdit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        spEdit.putString(SettingsActivity.kPORTS, portUtil.getDefaultPort());
        spEdit.apply();

        // register receiver for terminal bytes
        LocalBroadcastManager.getInstance(this).registerReceiver(brHEX, new IntentFilter("HEX"));
    }

    @Override
    protected void onResume(){
        super.onResume();
        firstLoad = false;
        if (portUtil.getOpenedPort().equals("")) swOn.setText(getString(R.string.off));
        else swOn.setTextOn(portUtil.getOpenedPort() + " " + getString(R.string.on));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(brHEX != null) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(brHEX);
                brHEX = null;
            } catch (Exception e) { e.getMessage(); }
        }
        if (portUtil != null) portUtil.closePort();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.bClear:
                    SMEAnimator.animation(v, ContextCompat.getColor(this, R.color.colorgrey400), ContextCompat.getColor(this, R.color.colorgrey50));
                    break;
                case R.id.bSend:
                    SMEAnimator.animation(v, ContextCompat.getColor(this, R.color.colorred400), ContextCompat.getColor(this, R.color.colorred100));
                    break;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (SettingsActivity.performClick) v.performClick();
            switch (v.getId()) {
                case R.id.bClear:
                    SMEAnimator.animation(v, ContextCompat.getColor(this, R.color.colorgrey50), ContextCompat.getColor(this, R.color.colorgrey400));
                    break;
                case R.id.bSend:
                    SMEAnimator.animation(v, ContextCompat.getColor(this, R.color.colorred100), ContextCompat.getColor(this, R.color.colorred400));
                    break;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bClear:
                clear();
                break;
            case R.id.bSend:
                send();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mSettings:
                startNewWindow(SettingsActivity.class);
                return true;
            case R.id.mQuit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                back();
                break;
        }
        return true;
    }

    private BroadcastReceiver brHEX = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String rawString = intent.getStringExtra("HEX");
            String convertedString = Converter.convertHEXStringToASCII(rawString);
            convertedString = convertedString.replace("\r\n\r\n", "\r\n").replace("\r\n", "\r\n" + " > ");
            if (convertedString.startsWith("\r\n")) convertedString = convertedString.substring("\r\n".length());
            etIn.append(convertedString + '\n');
            textSize += convertedString.length();
            if (textSize > 65535) clear();
        }
    };

    private void refresh(){
        if (!portIsOpened) {
            if (portUtil.getPorts().length > 1) {
                // get port path and baudrate from memory
                String portPath = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.kPORTS, portUtil.getDefaultPort());
                int baudrate = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.kBAUDRATE, "38400"));
                // open port
                if (portUtil.openPort(portPath, baudrate)) {
                    portIsOpened = true;
                    String openedPort = portUtil.getOpenedPort() + " " + getString(R.string.on);
                    swOn.setText(openedPort);
                    clear();
                } else {
                    swOn.setText(R.string.off);
                    swOn.setChecked(false);
                    portIsOpened = false;
                }
            } else {
                swOn.setText(getString(R.string.off));
                swOn.setChecked(false);
                portIsOpened = false;
            }
        } else {
            portUtil.closePort();
            swOn.setText(getString(R.string.off));
            swOn.setChecked(false);
            portIsOpened = false;
        }
    }



    private void send(){
        String out = etBlackOut.getText().toString();
        if (out.length() > 0) {
            out += '\n';
            byte[] data = out.getBytes();
            portUtil.sendBytes(data);
            out = " < " + out;
            etIn.append(out);
            textSize += out.length();
            if (textSize > 65535) clear();
        }
    }

    private void dialogAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.attention));
        builder.setMessage(getString(R.string.access_unavailable));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void clear(){
        etIn.setText("");
        etBlackOut.setText("");
        textSize = 0;
    }

    private void startNewWindow(Class cl){
        Intent intent = new Intent(this, cl);
        startActivity(intent);
    }

    private void back(){
        if (back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(this, R.string.press_again, Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }
}
