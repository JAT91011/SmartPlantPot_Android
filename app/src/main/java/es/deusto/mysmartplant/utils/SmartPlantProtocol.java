package es.deusto.mysmartplant.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;

import es.deusto.mysmartplant.entities.SmartPlant;

public class SmartPlantProtocol {

    public static int SCANNING_PERIOD = 3000;
    public static final int ADV_PREFIX_LENGTH = 5;
    public static final int ADV_UUID_LENGTH = 16;
    public static final String SMART_PLANT_ID = "SmartPlant";
    public static final String SMART_PLANT_UUID = "6ABC61D8-17A4-11E6-B6BA-3E1D05DEFE78";

    private static final int TX_POWER = -58;

    public static final int SEARCH_STARTED =        1;
    public static final int SEARCH_END_EMPTY =      2;
    public static final int SEARCH_END_SUCCESS =    3;

    private static SmartPlantProtocol       _spp;
    private BluetoothAdapter                _bluetoothAdapter;
    private SmartPlantListener              _listener;

    private boolean                         _scanning;
    private ArrayList<SmartPlant>           _arrSmartPlants = new ArrayList<SmartPlant>();
    private Handler                         _timeoutHandler;

    private SmartPlantProtocol(){

    }

    public static SmartPlantProtocol getInstance(Context c){
        if(_spp == null){
            _spp = new SmartPlantProtocol();
        }
        return _spp;
    }

    public SmartPlantListener getListener() {
        return _listener;
    }

    public void setListener(SmartPlantListener l) {
        this._listener = l;
    }

    public ArrayList<SmartPlant> getSmartPlants(){
        return _arrSmartPlants;
    }

    public boolean isScanning(){
        return _scanning;
    }

    public static boolean configureBluetoothAdapter(Context c){
        final BluetoothManager bluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        _spp._bluetoothAdapter = bluetoothManager.getAdapter();
        if (_spp._bluetoothAdapter == null || !_spp._bluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(device.getName() != null && device.getName().equals(SMART_PLANT_ID)) {
                SmartPlant smartPlant = new SmartPlant();
                smartPlant.set_serialNumber(device.getAddress().replace(":", ""));
                smartPlant.set_macAddress(device.getAddress());
                smartPlant.set_distance(round(calculateDistance(rssi), 2));

                byte[] uuid = Arrays.copyOfRange(scanRecord, ADV_PREFIX_LENGTH, ADV_PREFIX_LENGTH + ADV_UUID_LENGTH);
                if (_arrSmartPlants.contains(smartPlant)) {
                    SmartPlant previousSmartPlantInfo = findIfExists(smartPlant);
                    previousSmartPlantInfo.set_rssi(rssi);
                    previousSmartPlantInfo.set_distance(round(calculateDistance(rssi), 2));
                    _listener.smartPlantFound(smartPlant);
                }

                if (!_arrSmartPlants.contains(smartPlant)) {
                    _arrSmartPlants.add(smartPlant);
                    _listener.smartPlantFound(smartPlant);

                    // Everytime a new beacon is found, reset the timeout
                    _timeoutHandler.removeCallbacks(timeoutTask);
                    _timeoutHandler.postDelayed(timeoutTask, SmartPlantProtocol.SCANNING_PERIOD);
                }
            }

        }
    };


    public String getUuidHexString(byte[]_uuid){
        String s = "";
        for(int i=0; i<_uuid.length; i++)
            s = String.format("%02X", _uuid[i]) + s;
        return s;
    }

    private SmartPlant findIfExists(SmartPlant sp) {
        for (int i = 0; i < _arrSmartPlants.size(); i++) {
            SmartPlant existing = _arrSmartPlants.get(i);
            if (existing.equals(sp))
                return existing;
        }
        return null;
    }

    public BluetoothDevice getDevice(String mac) {
        Log.d("APP", mac);
        return _bluetoothAdapter.getRemoteDevice(mac);
    }

    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            _scanning = false;
            _bluetoothAdapter.stopLeScan(mLeScanCallback);
            if (_arrSmartPlants.size() == 0)
                _listener.searchState(SEARCH_END_EMPTY);
            else
                _listener.searchState(SEARCH_END_SUCCESS);
        }
    };

    public void scanSmartPlants(final boolean enable) {
        if (enable) {
            _timeoutHandler = new Handler();
            _timeoutHandler.postDelayed(timeoutTask, SmartPlantProtocol.SCANNING_PERIOD);
            _scanning = true;
            _arrSmartPlants.clear();
            _bluetoothAdapter.startLeScan(mLeScanCallback);
            _listener.searchState(SEARCH_STARTED);
        } else {
            _scanning = false;
            _bluetoothAdapter.stopLeScan(mLeScanCallback);
            _listener.searchState(SEARCH_END_SUCCESS);
        }
        Log.i("APP", "The status:" + _bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT));
    }

    private double calculateDistance(double rssi) {
        if (rssi == 0) {
            return -1.0;
        }

        double ratio = rssi * 1.0 / TX_POWER;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}