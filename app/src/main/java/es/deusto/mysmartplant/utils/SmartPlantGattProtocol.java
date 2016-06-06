package es.deusto.mysmartplant.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import es.deusto.mysmartplant.entities.SmartPlant;

public class SmartPlantGattProtocol {

    private static SmartPlantGattProtocol _instance;

    private BluetoothGattCharacteristic _currentWriteCharacteristic = null;
    private Context                     _context;
    private SmartPlant                  _smartPlant;
    private SmartPlantGattSuscriber     _subscriber = null;
    private BluetoothGatt               _gattManager = null;

    private static final UUID SENSORS_SERVICE = UUID.fromString("0000ccc0-0000-1000-8000-00805f9b34fb");

    private static final UUID TEMPERATURE_CHARACTERISTIC = UUID.fromString("0000ccc1-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_CHARACTERISTIC = UUID.fromString("0000ccc2-0000-1000-8000-00805f9b34fb");
    private static final UUID LIGHT_CHARACTERISTIC = UUID.fromString("0000ccc3-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_CHARACTERISTIC = UUID.fromString("0000ccc4-0000-1000-8000-00805f9b34fb");

    private static final UUID SENSORS_CONFIG_SERVICE = UUID.fromString("0000ddd0-0000-1000-8000-00805f9b34fb");
    private static final UUID TEMPERATURE_MIN_CONFIG = UUID.fromString("0000ddd1-0000-1000-8000-00805f9b34fb");
    private static final UUID TEMPERATURE_MAX_CONFIG = UUID.fromString("0000ddd2-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_MIN_CONFIG = UUID.fromString("0000ddd3-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_MAX_CONFIG = UUID.fromString("0000ddd4-0000-1000-8000-00805f9b34fb");
    private static final UUID LIGHT_MIN_CONFIG = UUID.fromString("0000ddd5-0000-1000-8000-00805f9b34fb");
    private static final UUID LIGHT_MAX_CONFIG = UUID.fromString("0000ddd6-0000-1000-8000-00805f9b34fb");

    private SmartPlantGattProtocol(){

    };

    public static SmartPlantGattProtocol getInstance(Context c) {
        if(_instance == null){
            _instance = new SmartPlantGattProtocol();
            _instance._context = c;
        }
        return _instance;
    }

    public void connect(SmartPlant sp,  SmartPlantGattSuscriber s){
        _smartPlant = sp;
        _subscriber = s;
        SmartPlantProtocol.getInstance(_context).getDevice(_smartPlant.get_macAddress())
                .connectGatt(_context, false, mGattCallBack);
    }

    public void close(){
        if(_gattManager != null) {
            _gattManager.disconnect();
            _gattManager.close();
        }
    }

    public void disconnect() {
        this.close();
    }

    private final BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
            _gattManager = gatt;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (_subscriber != null)
                    _subscriber.operationError(_smartPlant, status);
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("APP", "Connected to Smart Plant");
                if (_subscriber != null)
                    _subscriber.gattConnected(_smartPlant);
                if (_currentWriteCharacteristic == null) {
                    gatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("APP", "Disconnected from Smart Plant");
                if (_subscriber != null)
                    _subscriber.gattDisconnected(_smartPlant);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            _gattManager = gatt;
            Log.i("APP", "Status: " + status);
            Log.d("APP", "Services: " + _smartPlant.toString());
            if(status == BluetoothGatt.GATT_SUCCESS){
                gatt.readCharacteristic(gatt.getService(SENSORS_SERVICE).getCharacteristic(TEMPERATURE_CHARACTERISTIC));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i("APP", "Written");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("APP", "Write OK");
                if(characteristic.getUuid().equals(TEMPERATURE_MIN_CONFIG)){
                    writeIntCharacteristic(TEMPERATURE_MAX_CONFIG, _smartPlant.get_temperatureMax());
                    Log.i("APP", "Escribe Temperatura MAX: " + _smartPlant.get_temperatureMax());

                } else if (characteristic.getUuid().equals(TEMPERATURE_MAX_CONFIG)){
                    writeIntCharacteristic(HUMIDITY_MIN_CONFIG, _smartPlant.get_humidityMin());
                    Log.i("APP", "Escribe Humedad MIN: " + _smartPlant.get_humidityMin());

                } else if (characteristic.getUuid().equals(HUMIDITY_MIN_CONFIG)){
                    writeIntCharacteristic(HUMIDITY_MAX_CONFIG, _smartPlant.get_humidityMax());
                    Log.i("APP", "Escribe Humedad MAX: " + _smartPlant.get_humidityMax());

                } else if (characteristic.getUuid().equals(HUMIDITY_MAX_CONFIG)){
                    writeIntCharacteristic(LIGHT_MIN_CONFIG, _smartPlant.get_lightMin());
                    Log.i("APP", "Escribe Luminosidad MIN: " + _smartPlant.get_lightMin());

                } else if (characteristic.getUuid().equals(LIGHT_MIN_CONFIG)){
                    writeIntCharacteristic(LIGHT_MAX_CONFIG, _smartPlant.get_lightMax());
                    Log.i("APP", "Escribe Luminosidad MAX: " + _smartPlant.get_lightMax());
                    if(_subscriber != null)
                        _subscriber.characteristicWrite(_smartPlant);
                }
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
            _currentWriteCharacteristic = null;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status != BluetoothGatt.GATT_SUCCESS){
                Log.i("APP", "Error: " + status);
                gatt.disconnect();
                gatt.close();
                if(_subscriber != null)
                    _subscriber.operationError(_smartPlant, status);
                return;
            }
            if(characteristic.getUuid().equals(TEMPERATURE_CHARACTERISTIC)){
                _smartPlant.set_temperature(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Temperatura: " + _smartPlant.get_temperature());
                gatt.readCharacteristic(gatt.getService(SENSORS_SERVICE).getCharacteristic(LIGHT_CHARACTERISTIC));

            }else if (characteristic.getUuid().equals(LIGHT_CHARACTERISTIC)){
                _smartPlant.set_light(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Light: " + _smartPlant.get_light());
                gatt.readCharacteristic(gatt.getService(SENSORS_SERVICE).getCharacteristic(HUMIDITY_CHARACTERISTIC));

            }else if (characteristic.getUuid().equals(HUMIDITY_CHARACTERISTIC)){
                _smartPlant.set_humidity(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Humedad: " + _smartPlant.get_humidity());
                gatt.readCharacteristic(gatt.getService(SENSORS_SERVICE).getCharacteristic(BATTERY_CHARACTERISTIC));

            }else if (characteristic.getUuid().equals(BATTERY_CHARACTERISTIC)) {
                _smartPlant.set_battery(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Bateria: " + _smartPlant.get_battery());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(TEMPERATURE_MIN_CONFIG));

            }else if (characteristic.getUuid().equals(TEMPERATURE_MIN_CONFIG)) {
                _smartPlant.set_temperatureMin(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Temperatura minima: " + _smartPlant.get_temperatureMin());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(TEMPERATURE_MAX_CONFIG));

            }else if (characteristic.getUuid().equals(TEMPERATURE_MAX_CONFIG)) {
                _smartPlant.set_temperatureMax(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Temperatura maxima: " + _smartPlant.get_temperatureMax());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(HUMIDITY_MIN_CONFIG));

            }else if (characteristic.getUuid().equals(HUMIDITY_MIN_CONFIG)){
                _smartPlant.set_humidityMin(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Humedad minima: " + _smartPlant.get_humidityMin());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(HUMIDITY_MAX_CONFIG));

            }else if (characteristic.getUuid().equals(HUMIDITY_MAX_CONFIG)){
                _smartPlant.set_humidityMax(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Humedad maxima: " + _smartPlant.get_humidityMax());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(LIGHT_MIN_CONFIG));

            }else if (characteristic.getUuid().equals(LIGHT_MIN_CONFIG)){
                _smartPlant.set_lightMin(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Luminosidad minima: " + _smartPlant.get_lightMin());
                gatt.readCharacteristic(gatt.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(LIGHT_MAX_CONFIG));

            }else if (characteristic.getUuid().equals(LIGHT_MAX_CONFIG)){
                _smartPlant.set_lightMax(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                //Log.i("APP", "Luminosidad maxima: " + _smartPlant.get_lightMax());
                if(_subscriber != null)
                    _subscriber.characteristicsRead(_smartPlant);
            }
        }
    };

    public void readAllCharacteristics() {
        _gattManager.readCharacteristic(_gattManager.getService(SENSORS_SERVICE).getCharacteristic(TEMPERATURE_CHARACTERISTIC));
    }

    public void writeAllCharacteristics(SmartPlant smartPlant) {
        this._smartPlant = smartPlant;
        Log.i("APP", "Escribe Temperatura MIN: " + smartPlant.get_temperatureMin());
        writeIntCharacteristic(TEMPERATURE_MIN_CONFIG, smartPlant.get_temperatureMin());
    }

    public void writeIntCharacteristic(UUID uuidCharacteristic, int valueInt){
        BluetoothGattCharacteristic c = _gattManager.getService(SENSORS_CONFIG_SERVICE).getCharacteristic(uuidCharacteristic);
        byte[] value = new byte[1];
        value[0] = (byte) (valueInt & 0xFF);
        c.setValue(value);
        Log.i("APP", Arrays.toString(value));
        _currentWriteCharacteristic = c;
        _gattManager.writeCharacteristic(_currentWriteCharacteristic);
    }
}