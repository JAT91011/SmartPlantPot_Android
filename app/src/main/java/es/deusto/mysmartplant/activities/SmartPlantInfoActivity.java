package es.deusto.mysmartplant.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import es.deusto.mysmartplant.R;
import es.deusto.mysmartplant.entities.SmartPlant;
import es.deusto.mysmartplant.utils.SmartPlantGattProtocol;
import es.deusto.mysmartplant.utils.SmartPlantGattSuscriber;
import es.deusto.mysmartplant.utils.SmartPlantProtocol;

/**
 * Created by Jordan on 16/5/16.
 */

public class SmartPlantInfoActivity extends AppCompatActivity implements SmartPlantGattSuscriber {

    public static final int REQUEST_BLUETOOTH_ENABLE = 1;
    public static final int EDIT_SMART_PLANT_DATA = 0;
    public static final String EDITED_SMART_PLANT_DATA = "editedSmartPlant";

    private SmartPlant          _smartPlant;
    private Toolbar             _toolbar;

    private Handler             timerHandler = null;
    private Runnable            timerRunnable = null;

    private ProgressBar         _barLoading;
    private TextView            _txtLoading;

    private ImageView           _imgSmartPlant;
    private TextView            _txtTemperature;
    private TextView            _txtHumidity;
    private TextView            _txtLight;
    private TextView            _txtBattery;

    private boolean             _readingCharacteristics;
    private Timer               _timer;
    private ShareActionProvider _mShareActionProvider;
    private boolean             _firstTime;
    private TimerUpdateData     _timerUpdateData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_plant_info);

        _toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);

        _barLoading = (ProgressBar) this.findViewById(R.id.barLoading);
        _txtLoading = (TextView) this.findViewById(R.id.txtLoading);

        _toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _readingCharacteristics = false;
                cancelTimer();
                _timer.cancel();
                SmartPlantGattProtocol.getInstance(SmartPlantInfoActivity.this).disconnect();
                SmartPlantInfoActivity.this.finish();
            }
        });

        _smartPlant = (SmartPlant) getIntent().getExtras().get("smartPlant");
        setTitle(_smartPlant.get_name());

        _txtTemperature = (TextView) findViewById(R.id.lblTemperature);
        _txtHumidity = (TextView) findViewById(R.id.lblHumidity);
        _txtLight = (TextView) findViewById(R.id.lblLight);
        _txtBattery = (TextView) findViewById(R.id.lblBattery);

        _firstTime = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        _imgSmartPlant = (ImageView) this.findViewById(R.id.imgSmartPlant);
        if(_smartPlant.get_imagePath() != null && !_smartPlant.get_imagePath().isEmpty()
                && new File(_smartPlant.get_imagePath()).exists()) {

            Bitmap thumbnail = (BitmapFactory.decodeFile(_smartPlant.get_imagePath()));
            _imgSmartPlant.setImageBitmap(thumbnail);
            _imgSmartPlant.setVisibility(View.VISIBLE);

            _imgSmartPlant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + _smartPlant.get_imagePath()), "image/*");
                    v.getContext().startActivity(intent);
                }
            });
        } else {

            Bitmap thumbnail = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.image_not_found);
            _imgSmartPlant.setImageBitmap(thumbnail);
            _imgSmartPlant.setVisibility(View.VISIBLE);
        }

        if(!_readingCharacteristics && _firstTime) {
            _timerUpdateData = new TimerUpdateData(this);
            loadSmartPlantData();
            _readingCharacteristics = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _toolbar.inflateMenu(R.menu.menu_info);

        MenuItem mnuShare = menu.findItem(R.id.action_share);

        _mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mnuShare);
        _mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_edit) {
            _readingCharacteristics = false;
            cancelTimer();
            _timer.cancel();
            Intent intent = new Intent(SmartPlantInfoActivity.this, SmartPlantEditActivity.class);
            intent.putExtra("smartPlant", _smartPlant);
            intent.putExtra("mode", true);
            startActivityForResult(intent, EDIT_SMART_PLANT_DATA);
            overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_SMART_PLANT_DATA) {
            if (resultCode == RESULT_CANCELED) {
                // SIGUE ACTUALIZANDO LOS CAMPOS
                reScheduleTimer();
            } else if (resultCode == RESULT_OK) {
                // OBTENEMOS LA PLANTA EDITADA Y LA GUARDAMOS
                _smartPlant = (SmartPlant) data.getSerializableExtra(EDITED_SMART_PLANT_DATA);
                setTitle(_smartPlant.get_name());
                Log.d("APP", "Editado: " + _smartPlant.toString());
                SmartPlantGattProtocol.getInstance(this).writeAllCharacteristics(_smartPlant);
            }
        }
    }

    private void loadSmartPlantData(){
        startTimer();
        findViewById(R.id.layLoading).setVisibility(View.VISIBLE);
        if(!SmartPlantProtocol.configureBluetoothAdapter(this)){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
        }else{
            SmartPlantGattProtocol.getInstance(this).connect(_smartPlant, this);
        }
    }

    private void startTimer(){
        if(timerHandler != null)
            timerHandler.removeCallbacks(timerRunnable);
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SmartPlantInfoActivity.this, "No se han podido obtener los datos", Toast.LENGTH_LONG).show();
                displayRetry();
            }
        };
        timerHandler.postDelayed(timerRunnable, 10000);
    }

    private void cancelTimer(){
        if(timerHandler != null)
            timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void characteristicsRead(final SmartPlant sp) {

        if(_firstTime) {
            _firstTime = false;
            _smartPlant.save(getApplicationContext());
            Log.d("APP", "GUARDA: " + _smartPlant.toString());
            reScheduleTimer();
        }
        _smartPlant = sp;

        cancelTimer();
        updateData();
    }

    private void updateData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.layLoading).setVisibility(View.GONE);
                _txtTemperature.setText(Integer.toString(_smartPlant.get_temperature()) + " ºC");
                _txtHumidity.setText(Integer.toString(_smartPlant.get_humidity()) + " %");
                _txtLight.setText(Integer.toString(_smartPlant.get_light()) + " %");
                _txtBattery.setText(String.valueOf(_smartPlant.get_battery()) + "%");

                String _shareMessage = "La información de mi planta '" + _smartPlant.get_name() + "' es la siguiente:" +
                        "\n - Temperatura: " + _smartPlant.get_temperature() + " ºC" +
                        "\n - Humedad: " + _smartPlant.get_humidity() + "%" +
                        "\n - Luminosidad: " + _smartPlant.get_light() + "%";
                findViewById(R.id.layData).setVisibility(View.VISIBLE);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, _shareMessage);
                _mShareActionProvider.setShareIntent(intent);
            }
        });
    }

    @Override
    public void characteristicWrite(SmartPlant sp) {
        reScheduleTimer();
    }

    @Override
    public void operationError(SmartPlant sp, int status) {

    }

    @Override
    public void gattConnected(SmartPlant sp) {

    }

    @Override
    public void gattDisconnected(SmartPlant sp) {

    }

    private void displayRetry(){
        _barLoading.setVisibility(View.INVISIBLE);
        _txtLoading.setText("Pulsa para volver a intentar");
        _txtLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _barLoading.setVisibility(View.VISIBLE);
                _txtLoading.setText(R.string.msg_retrieving_data);
                loadSmartPlantData();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _readingCharacteristics = false;
            cancelTimer();
            _timer.cancel();
            SmartPlantGattProtocol.getInstance(this).disconnect();
            finish();
            overridePendingTransition(R.anim.flip_in_to_right, R.anim.flip_out_to_right);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void reScheduleTimer() {
        _timer = new Timer("alertTimer",true);
        _timerUpdateData = new TimerUpdateData(this);
        _timer.schedule(_timerUpdateData, 1000L, 1000L);
    }
}

class TimerUpdateData extends TimerTask {

    private Activity        activity;

    public TimerUpdateData(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        SmartPlantGattProtocol.getInstance(activity).readAllCharacteristics();
    }
}