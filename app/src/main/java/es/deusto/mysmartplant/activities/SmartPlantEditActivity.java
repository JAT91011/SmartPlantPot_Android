package es.deusto.mysmartplant.activities;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import es.deusto.mysmartplant.R;
import es.deusto.mysmartplant.entities.SmartPlant;
import es.deusto.mysmartplant.utils.ImageHelper;

/**
 * Created by Jordan on 16/5/16.
 */

public class SmartPlantEditActivity extends AppCompatActivity {

    private static int          ACTION_TAKE_PHOTO = 1;
    private static int          ACTION_PICK_PHOTO_FROM_GALLERY = 2;

    private SmartPlant          _currentSmartPlant;

    private Toolbar             _toolbar;
    private TextInputLayout     _inputLayoutName, _inputLayoutSerialNumber, _inputLayoutTempMin,
                                _inputLayoutTempMax, _inputLayoutHumidityMin, _inputLayoutHumidityMax,
                                _inputLayoutLightMin, _inputLayoutLightMax;
    private EditText            _txtName, _txtSerialNumber, _txtTempMin, _txtTempMax, _txtHumidityMin,
                                _txtHumidityMax, _txtLightMin, _txtLightMax;

    private ImageView           _imgSmartPlant, _imgButton;
    private Uri                 _mImageUri;

    private boolean             _connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_plant_edit);

        _toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);

        _connected = getIntent().getBooleanExtra("mode", false);

        _currentSmartPlant = (SmartPlant)getIntent().getExtras().getSerializable("smartPlant");
        if(_currentSmartPlant.get_id() == 0) {
            setTitle(getResources().getString(R.string.view_add_smart_plant_pot_title));
        } else {
            setTitle(getResources().getString(R.string.view_edit_smart_plant_pot_title));
        }

        _txtName = (EditText) this.findViewById(R.id.txtName);
        _inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);

        _txtSerialNumber = (EditText) this.findViewById(R.id.txtSerialNumber);
        _inputLayoutSerialNumber = (TextInputLayout) findViewById(R.id.input_layout_serial);

        _txtTempMin = (EditText) this.findViewById(R.id.txtTempMin);
        _txtTempMin.setText(Integer.toString(_currentSmartPlant.get_temperatureMin()));
        _inputLayoutTempMin = (TextInputLayout) findViewById(R.id.input_layout_temp_min);
        _txtTempMax = (EditText) this.findViewById(R.id.txtTempMax);
        _txtTempMax.setText(Integer.toString(_currentSmartPlant.get_temperatureMax()));
        _inputLayoutTempMax = (TextInputLayout) findViewById(R.id.input_layout_temp_max);

        _txtHumidityMin = (EditText) this.findViewById(R.id.txtHumidityMin);
        _txtHumidityMin.setText(Integer.toString(_currentSmartPlant.get_humidityMin()));
        _inputLayoutHumidityMin = (TextInputLayout) findViewById(R.id.input_layout_humidity_min);
        _txtHumidityMax = (EditText) this.findViewById(R.id.txtHumidityMax);
        _txtHumidityMax.setText(Integer.toString(_currentSmartPlant.get_humidityMax()));
        _inputLayoutHumidityMax = (TextInputLayout) findViewById(R.id.input_layout_humidity_max);

        _txtLightMin = (EditText) this.findViewById(R.id.txtLightMin);
        _txtLightMin.setText(Integer.toString(_currentSmartPlant.get_lightMin()));
        _inputLayoutLightMin = (TextInputLayout) findViewById(R.id.input_layout_light_min);
        _txtLightMax = (EditText) this.findViewById(R.id.txtLightMax);
        _txtLightMax.setText(Integer.toString(_currentSmartPlant.get_lightMax()));
        _inputLayoutLightMax = (TextInputLayout) findViewById(R.id.input_layout_light_max);


        _imgSmartPlant = (ImageView) this.findViewById(R.id.imgSmartPlant);
        _imgSmartPlant.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        addImage();
                    }
                }
        );

        _imgButton = (ImageView) this.findViewById(R.id.imgButton);
        _imgButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        addImage();
                    }
                }
        );

        _txtName.setText(_currentSmartPlant.get_name());
        _txtSerialNumber.setText(_currentSmartPlant.get_serialNumber());
        _txtTempMin.setText(Integer.toString(_currentSmartPlant.get_temperatureMin()));
        _txtTempMax.setText(Integer.toString(_currentSmartPlant.get_temperatureMax()));
        _txtHumidityMin.setText(Integer.toString(_currentSmartPlant.get_humidityMin()));
        _txtHumidityMax.setText(Integer.toString(_currentSmartPlant.get_humidityMax()));
        _txtLightMin.setText((Integer.toString(_currentSmartPlant.get_lightMin())));
        _txtLightMax.setText((Integer.toString(_currentSmartPlant.get_lightMax())));

        if(!_connected) {
            this.findViewById(R.id.layTemperature).setVisibility(View.GONE);
            this.findViewById(R.id.layHumidity).setVisibility(View.GONE);
            this.findViewById(R.id.layLight).setVisibility(View.GONE);
        }
    }

    protected void onResume() {
        super.onResume();
        if(_currentSmartPlant.get_imagePath() != null && !_currentSmartPlant.get_imagePath().isEmpty()) {
            Bitmap thumbnail = (BitmapFactory.decodeFile(_currentSmartPlant.get_imagePath()));
            _imgSmartPlant.setImageBitmap(thumbnail);
            _imgButton.setVisibility(View.GONE);
            _imgSmartPlant.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _toolbar.getMenu().clear();
        _toolbar.inflateMenu(R.menu.menu_edit);
        _toolbar.setNavigationIcon(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_cancel) {
            setResult(RESULT_CANCELED, getIntent());
            finish();
            overridePendingTransition(R.anim.flip_in_to_right, R.anim.flip_out_to_right);
        } else if(item.getItemId() == R.id.action_save) {
            if(validateFields()) {
                String message = "";
                if(_currentSmartPlant.get_id() != 0) {
                    message = getResources().getString(R.string.successfully_edited);
                } else {
                    message = getResources().getString(R.string.successfully_added);
                }
                if(save()) {
                    Log.d("APP", "Editado que devuelve: " + _currentSmartPlant);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Intent i = getIntent();
                    i.putExtra(SmartPlantInfoActivity.EDITED_SMART_PLANT_DATA, _currentSmartPlant);
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateFields() {

        if (!validateName()) {
            return false;
        }

        if (!validateSerialNumber()) {
            return false;
        }

        if(_connected) {
            if (!validateTemperature()) {
                return false;
            }

            if (!validateHumidity()) {
                return false;
            }

            if (!validateLight()) {
                return false;
            }
        }

        return true;
    }

    private boolean validateName() {
        if (_txtName.getText().toString().trim().isEmpty()) {
            _inputLayoutName.setError(getString(R.string.error_required_name));
            requestFocus(_txtName);
            return false;
        } else {
            _inputLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateSerialNumber() {
        if (_txtSerialNumber.getText().toString().trim().isEmpty()) {
            _inputLayoutSerialNumber.setError(getString(R.string.error_required_serial_number));
            requestFocus(_txtSerialNumber);
            return false;
        } else if(_txtSerialNumber.getText().toString().trim().length() != 12) {
            _inputLayoutSerialNumber.setError(getString(R.string.error_format_serial_number));
            return false;
        } else {
            _inputLayoutSerialNumber.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateTemperature() {
        if (_txtTempMin.getText().toString().trim().isEmpty()) {
            _inputLayoutTempMin.setError(getString(R.string.error_required_temperature_min));
            requestFocus(_txtTempMin);
            return false;
        }
        else if (_txtTempMax.getText().toString().trim().isEmpty()) {
            _inputLayoutTempMax.setError(getString(R.string.error_required_temperature_max));
            _inputLayoutTempMin.setErrorEnabled(false);
            requestFocus(_txtTempMax);
            return false;
        }
        else {
            int tempMin = Integer.parseInt(_txtTempMin.getText().toString().trim());
            int tempMax = Integer.parseInt(_txtTempMax.getText().toString().trim());

            if (tempMin < -20 || tempMin > 50) {
                _inputLayoutTempMin.setError(getString(R.string.error_temperature_range));
                requestFocus(_txtTempMin);
                return false;
            }
            else if (tempMax < -20 || tempMax > 50) {
                _inputLayoutTempMax.setError(getString(R.string.error_temperature_range));
                requestFocus(_txtTempMax);
                return false;
            } else if(tempMin >= tempMax) {
                requestFocus(_txtTempMin);
                _inputLayoutTempMin.setErrorEnabled(false);
                _inputLayoutTempMax.setErrorEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.error_temperature_min_max), Toast.LENGTH_LONG).show();
                return false;
            } else {
                _inputLayoutTempMin.setErrorEnabled(false);
                _inputLayoutTempMax.setErrorEnabled(false);
            }
        }
        return true;
    }

    private boolean validateHumidity() {
        if (_txtHumidityMin.getText().toString().trim().isEmpty()) {
            _inputLayoutHumidityMin.setError(getString(R.string.error_required_humidity_min));
            requestFocus(_txtHumidityMin);
            return false;
        }
        else if (_txtHumidityMax.getText().toString().trim().isEmpty()) {
            _inputLayoutHumidityMax.setError(getString(R.string.error_required_humidity_max));
            requestFocus(_txtHumidityMax);
            _inputLayoutHumidityMin.setErrorEnabled(false);
            return false;
        }
        else {
            int humidityMin = Integer.parseInt(_txtHumidityMin.getText().toString().trim());
            int humidityMax = Integer.parseInt(_txtHumidityMax.getText().toString().trim());

            if (humidityMin < 0 || humidityMin > 100) {
                _inputLayoutHumidityMin.setError(getString(R.string.error_humidity_range));
                requestFocus(_txtHumidityMin);
                return false;
            }
            else if (humidityMax < 0 || humidityMax > 100) {
                _inputLayoutHumidityMax.setError(getString(R.string.error_humidity_range));
                requestFocus(_txtHumidityMax);
                return false;
            } else if(humidityMin >= humidityMax) {
                requestFocus(_txtHumidityMin);
                _inputLayoutHumidityMin.setErrorEnabled(false);
                _inputLayoutHumidityMax.setErrorEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.error_humidity_min_max), Toast.LENGTH_LONG).show();
                return false;
            } else {
                _inputLayoutHumidityMin.setErrorEnabled(false);
                _inputLayoutHumidityMax.setErrorEnabled(false);
            }
        }
        return true;
    }

    private boolean validateLight() {
        if (_txtLightMin.getText().toString().trim().isEmpty()) {
            _inputLayoutLightMin.setError(getString(R.string.error_required_light_min));
            requestFocus(_txtLightMin);
            return false;
        }
        else if (_txtLightMax.getText().toString().trim().isEmpty()) {
            _inputLayoutLightMax.setError(getString(R.string.error_required_light_max));
            requestFocus(_txtLightMax);
            _inputLayoutLightMin.setErrorEnabled(false);
            return false;
        }
        else {
            int lightMin = Integer.parseInt(_txtLightMin.getText().toString().trim());
            int lightMax = Integer.parseInt(_txtLightMax.getText().toString().trim());

            if (lightMin < 0 || lightMin > 100) {
                _inputLayoutLightMin.setError(getString(R.string.error_light_range));
                requestFocus(_txtLightMin);
                return false;
            }
            else if (lightMax < 0 || lightMax > 100) {
                _inputLayoutLightMax.setError(getString(R.string.error_light_range));
                requestFocus(_txtLightMax);
                return false;
            } else if(lightMin >= lightMax) {
                requestFocus(_txtLightMin);
                _inputLayoutLightMin.setErrorEnabled(false);
                _inputLayoutLightMax.setErrorEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.error_light_min_max), Toast.LENGTH_LONG).show();
                return false;
            } else {
                _inputLayoutLightMin.setErrorEnabled(false);
                _inputLayoutLightMax.setErrorEnabled(false);
            }
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean save() {
        _currentSmartPlant.set_name(_txtName.getText().toString().trim());
        _currentSmartPlant.set_serialNumber(_txtSerialNumber.getText().toString().trim());
        _currentSmartPlant.set_temperatureMin(Integer.parseInt(_txtTempMin.getText().toString().trim()));
        _currentSmartPlant.set_temperatureMax(Integer.parseInt(_txtTempMax.getText().toString().trim()));
        _currentSmartPlant.set_humidityMin(Integer.parseInt(_txtHumidityMin.getText().toString().trim()));
        _currentSmartPlant.set_humidityMax(Integer.parseInt(_txtHumidityMax.getText().toString().trim()));
        _currentSmartPlant.set_lightMin(Integer.parseInt(_txtLightMin.getText().toString().trim()));
        _currentSmartPlant.set_lightMax(Integer.parseInt(_txtLightMax.getText().toString().trim()));
        return _currentSmartPlant.save(getApplicationContext());
    }

    private void addImage() {

        final CharSequence[] options = {getResources().getString(R.string.ctx_menu_take_photo),
                getResources().getString(R.string.ctx_menu_import_from_gallery),
                getResources().getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(SmartPlantEditActivity.this);
        builder.setTitle(getResources().getString(R.string.ctx_menu_title_image));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(options[0])) {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    try {
                        File photo = ImageHelper.createImageFile();
                        _mImageUri = Uri.fromFile(photo);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, _mImageUri);
                        startActivityForResult(intent, ACTION_TAKE_PHOTO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (options[item].equals(options[1])) {
                    Intent intent = new   Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, ACTION_PICK_PHOTO_FROM_GALLERY);
                }
                else if (options[item].equals(options[2])) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void grabImage(ImageView imageView) {
        this.getContentResolver().notifyChange(_mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, _mImageUri);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("APP", "Failed to load", e);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.d("APP", _mImageUri.getPath());
            this.grabImage(_imgSmartPlant);
            _imgSmartPlant.setVisibility(View.VISIBLE);
            _imgButton.setVisibility(View.GONE);
            _currentSmartPlant.set_imagePath(_mImageUri.getPath());

        }
        else if (requestCode == ACTION_PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            if(c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                _imgSmartPlant.setImageBitmap(thumbnail);
                _currentSmartPlant.set_imagePath(picturePath);
                _imgSmartPlant.setVisibility(View.VISIBLE);
                _imgButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.flip_in_to_right, R.anim.flip_out_to_right);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}