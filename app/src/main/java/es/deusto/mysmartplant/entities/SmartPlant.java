package es.deusto.mysmartplant.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import es.deusto.mysmartplant.utils.DatabaseHelper;

/**
 * Created by Jordan on 16/5/16.
 */

public class SmartPlant implements Serializable {

    private int     _id;
    private String  _name;
    private String  _serialNumber;
    private String  _imagePath;
    private int   _temperature;
    private int   _temperatureMin;
    private int   _temperatureMax;
    private int     _light;
    private int     _lightMin;
    private int     _lightMax;
    private int     _humidity;
    private int     _humidityMin;
    private int     _humidityMax;
    private int     _battery;
    private double  _distance;
    private String  _macAddress;
    private int     _rssi;
    private boolean _found;

    public SmartPlant() {

    }

    public SmartPlant(String _name, String _serialNumber, String _imagePath, int _temperature,
                      int _temperatureMin, int _temperatureMax, int _light, int _lightMin,
                      int _lightMax, int _humidity, int _humidityMin, int _humidityMax, int _battery,
                      int _rssi) {
        this._name = _name;
        this._serialNumber = _serialNumber;
        this._imagePath = _imagePath;
        this._temperature = _temperature;
        this._temperatureMin = _temperatureMin;
        this._temperatureMax = _temperatureMax;
        this._light = _light;
        this._lightMin = _lightMin;
        this._lightMax = _lightMax;
        this._humidity = _humidity;
        this._humidityMin = _humidityMin;
        this._humidityMax = _humidityMax;
        this._battery = _battery;
        this._rssi = _rssi;
        this._distance = 0;
        this._found = false;
    }

    public SmartPlant(String _name, String _serialNumber, String _imagePath, int _temperatureMin,
                      int _temperatureMax, int _lightMin, int _lightMax, int _humidityMin,
                      int _humidityMax, int _rssi) {
        this._name = _name;
        this._serialNumber = _serialNumber;
        this._imagePath = _imagePath;
        this._temperatureMin = _temperatureMin;
        this._temperatureMax = _temperatureMax;
        this._lightMin = _lightMin;
        this._lightMax = _lightMax;
        this._humidityMin = _humidityMin;
        this._humidityMax = _humidityMax;
        this._rssi = _rssi;
        this._distance = 0;
        this._found = false;
    }

    public SmartPlant(int _id, String _name, String _serialNumber, String _imagePath, int _temperatureMin,
                      int _temperatureMax, int _lightMin, int _lightMax, int _humidityMin,
                      int _humidityMax) {
        this._id = _id;
        this._name = _name;
        this._serialNumber = _serialNumber;
        this._imagePath = _imagePath;
        this._temperatureMin = _temperatureMin;
        this._temperatureMax = _temperatureMax;
        this._lightMin = _lightMin;
        this._lightMax = _lightMax;
        this._humidityMin = _humidityMin;
        this._humidityMax = _humidityMax;
        this._distance = 0;
        this._found = false;
    }

    public SmartPlant(int _id, String _name, String _serialNumber, String _imagePath, int _temperature,
                      int _temperatureMin, int _temperatureMax, int _light, int _lightMin,
                      int _lightMax, int _humidity, int _humidityMin, int _humidityMax, int _battery,
                      int _rssi) {
        this._id = _id;
        this._name = _name;
        this._serialNumber = _serialNumber;
        this._imagePath = _imagePath;
        this._temperature = _temperature;
        this._temperatureMin = _temperatureMin;
        this._temperatureMax = _temperatureMax;
        this._light = _light;
        this._lightMin = _lightMin;
        this._lightMax = _lightMax;
        this._humidity = _humidity;
        this._humidityMin = _humidityMin;
        this._humidityMax = _humidityMax;
        this._battery = _battery;
        this._rssi = _rssi;
        this._distance = 0;
        this._found = false;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_imagePath() {
        return _imagePath;
    }

    public void set_imagePath(String _imagePath) {
        this._imagePath = _imagePath;
    }

    public String get_serialNumber() {
        return _serialNumber;
    }

    public void set_serialNumber(String _macAddress) {
        this._serialNumber = _macAddress;
    }

    public int get_temperature() {
        return _temperature;
    }

    public void set_temperature(int _temperature) {
        this._temperature = _temperature;
    }

    public int get_temperatureMin() {
        return _temperatureMin;
    }

    public void set_temperatureMin(int _temperatureMin) {
        this._temperatureMin = _temperatureMin;
    }

    public int get_temperatureMax() {
        return _temperatureMax;
    }

    public void set_temperatureMax(int _temperatureMax) {
        this._temperatureMax = _temperatureMax;
    }

    public int get_light() {
        return _light;
    }

    public void set_light(int _light) {
        this._light = _light;
    }

    public int get_lightMin() {
        return _lightMin;
    }

    public void set_lightMin(int _lightMin) {
        this._lightMin = _lightMin;
    }

    public int get_lightMax() {
        return _lightMax;
    }

    public void set_lightMax(int _lightMax) {
        this._lightMax = _lightMax;
    }

    public int get_humidity() {
        return _humidity;
    }

    public void set_humidity(int _humidity) {
        this._humidity = _humidity;
    }

    public int get_humidityMin() {
        return _humidityMin;
    }

    public void set_humidityMin(int _humidityMin) {
        this._humidityMin = _humidityMin;
    }

    public int get_humidityMax() {
        return _humidityMax;
    }

    public void set_humidityMax(int _humidityMax) {
        this._humidityMax = _humidityMax;
    }

    public int get_battery() {
        return _battery;
    }

    public void set_battery(int _battery) {
        this._battery = _battery;
    }

    public int get_rssi() {
        return _rssi;
    }

    public void set_rssi(int _rssi) {
        this._rssi = _rssi;
    }

    public boolean is_found() {
        return _found;
    }

    public void set_found(boolean _found) {
        this._found = _found;
    }

    public double get_distance() {
        return _distance;
    }

    public void set_distance(double _distance) {
        this._distance = _distance;
    }

    public String get_macAddress() {
        return _macAddress;
    }

    public void set_macAddress(String _macAddress) {
        this._macAddress = _macAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmartPlant smartPlant = (SmartPlant) obj;
        if(_serialNumber.equals(smartPlant.get_serialNumber()))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "SmartPlant{" +
                "_id=" + _id +
                ", _name='" + _name + '\'' +
                ", _serialNumber='" + _serialNumber + '\'' +
                ", _imagePath='" + _imagePath + '\'' +
                ", _temperature=" + _temperature +
                ", _temperatureMin=" + _temperatureMin +
                ", _temperatureMax=" + _temperatureMax +
                ", _light=" + _light +
                ", _lightMin=" + _lightMin +
                ", _lightMax=" + _lightMax +
                ", _humidity=" + _humidity +
                ", _humidityMin=" + _humidityMin +
                ", _humidityMax=" + _humidityMax +
                ", _battery=" + _battery +
                ", _distance=" + _distance +
                ", _macAddress='" + _macAddress + '\'' +
                ", _rssi=" + _rssi +
                ", _found=" + _found +
                '}';
    }

    public synchronized static SmartPlant obtainSmartPlantById(final int idSmartPlant, final Context context) {
        final SQLiteDatabase db = (new DatabaseHelper(context)).getWritableDatabase();

        final Cursor c = db.rawQuery("SELECT * FROM SMART_PLANT WHERE id = " + idSmartPlant, null);
        SmartPlant smartPlant = null;
        if(c.moveToFirst()) {
            final int id = c.getInt(0);
            final String name = c.getString(1);
            final String serialNumber = c.getString(2);
            final String imagePath = c.getString(3);
            final int maxTemperature = c.getInt(4);
            final int minTemperature = c.getInt(5);
            final int maxHumidity = c.getInt(6);
            final int minHumidity = c.getInt(7);
            final int maxLight = c.getInt(8);
            final int minLight = c.getInt(9);

            smartPlant = new SmartPlant(id, name, serialNumber, imagePath, minTemperature, maxTemperature,
                    minLight, maxLight, minHumidity, maxHumidity);

            String aux = "";
            for(int i = 0; i < serialNumber.length(); i+=2) {
                aux += serialNumber.substring(i, i+2) + ":";
            }
            smartPlant.set_macAddress(aux.substring(0, aux.length() - 1));
        }
        c.close();
        db.close();
        return smartPlant;
    }

    public synchronized static ArrayList<SmartPlant> obtainAllSmartPlants(final Context context) {
        final SQLiteDatabase db = (new DatabaseHelper(context)).getReadableDatabase();
        final ArrayList<SmartPlant> smartPlants = new ArrayList<SmartPlant>();

        final Cursor c = db.rawQuery("SELECT * FROM SMART_PLANT", null);

        if (c.moveToFirst()) {
            do {
                final int id = c.getInt(0);
                final String name = c.getString(1);
                final String serialNumber = c.getString(2);
                final String imagePath = c.getString(3);
                final int maxTemperature = c.getInt(4);
                final int minTemperature = c.getInt(5);
                final int maxHumidity = c.getInt(6);
                final int minHumidity = c.getInt(7);
                final int maxLight = c.getInt(8);
                final int minLight = c.getInt(9);

                SmartPlant smartPlant = new SmartPlant(id, name, serialNumber, imagePath,
                        minTemperature, maxTemperature, minLight, maxLight, minHumidity, maxHumidity);

                String aux = "";
                for(int i = 0; i < serialNumber.length(); i+=2) {
                    aux += serialNumber.substring(i, i+2) + ":";
                }
                smartPlant.set_macAddress(aux.substring(0, aux.length() - 1));

                smartPlants.add(smartPlant);
            }
            while (c.moveToNext());
        }
        c.close();
        db.close();
        return smartPlants;
    }

    public boolean save(final Context context) {
        final SQLiteDatabase db = (new DatabaseHelper(context)).getWritableDatabase();
        Log.d("APP", "GUARDA EN DB: " + toString());
        final ContentValues values = new ContentValues();
        values.put("name", _name);
        values.put("serialNumber", _serialNumber);
        values.put("imagePath", _imagePath);
        values.put("maxTemperature", _temperatureMax);
        values.put("minTemperature", _temperatureMin);
        values.put("maxHumidity", _humidityMax);
        values.put("minHumidity", _humidityMin);
        values.put("maxLight", _lightMax);
        values.put("minLight", _lightMin);

        boolean result;
        if(_id == 0) {
            // Se esta añadiendo uno nuevo
            result = db.insert("SMART_PLANT", null, values) > 0;

            final Cursor c = db.rawQuery("SELECT max(id) FROM SMART_PLANT", null);
            if (c.moveToFirst()) {
                this._id = c.getInt(0);
            }
            c.close();
        } else {
            // Se esta modificando uno existente
            result = db.update("SMART_PLANT", values, "id = " + this._id, null) > 0;

        }
        db.close();
        return result;
    }

    public boolean remove(final Context context) {
        boolean result;
        final SQLiteDatabase db = (new DatabaseHelper(context)).getWritableDatabase();
        result = db.delete("SMART_PLANT", "id = " + this._id, null) > 0;
        db.close();
        return result;
    }
}