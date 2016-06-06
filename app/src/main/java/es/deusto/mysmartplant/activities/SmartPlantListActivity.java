package es.deusto.mysmartplant.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import es.deusto.mysmartplant.R;
import es.deusto.mysmartplant.adapters.SmartPlantsArrayAdapter;
import es.deusto.mysmartplant.entities.SmartPlant;
import es.deusto.mysmartplant.utils.SmartPlantListener;
import es.deusto.mysmartplant.utils.SmartPlantProtocol;

/**
 * Created by Jordan on 16/5/16.
 */

public class SmartPlantListActivity extends AppCompatActivity implements SmartPlantListener {

    public static final int                 REQUEST_BLUETOOTH_ENABLE = 1;

    private SmartPlantProtocol              _spp;
    private ListView                        _listSmartPlants;
    private SmartPlantsArrayAdapter         _arrayAdapter;
    private ActionMode                      _mActionMode;

    private Toolbar                         _toolbar;
    private ArrayList<SmartPlant>           _smartPlants;
    private ProgressBar                     _barSearching;
    private FloatingActionButton            _fab;

    private int                             _selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_plant_list);

        _toolbar = (Toolbar) this.findViewById(R.id.main_toolbar);
        setSupportActionBar(_toolbar);

        checkBluetoothLeSupport();

        _barSearching = (ProgressBar) findViewById(R.id.barSearch);

        _listSmartPlants = (ListView) findViewById(R.id.smartPlantsListView);
        _listSmartPlants.setDivider(null);
        _listSmartPlants.setDividerHeight(0);
        _listSmartPlants.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                SmartPlant smartPlant = _smartPlants.get(position);

                if(smartPlant.is_found()) {
                    Log.i("APP", smartPlant.toString());

                    _spp = SmartPlantProtocol.getInstance(SmartPlantListActivity.this);
                    _spp.scanSmartPlants(false);

                    Intent i = new Intent(SmartPlantListActivity.this, SmartPlantInfoActivity.class);
                    i.putExtra("smartPlant", smartPlant);
                    startActivity(i);
                    overridePendingTransition(R.anim.flip_in_to_left, R.anim.flip_out_to_left);
                } else {
                    Toast.makeText(SmartPlantListActivity.this, "No se ha encontrado la maceta inteligente", Toast.LENGTH_LONG).show();
                }
            }
        });

        _listSmartPlants.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // Called when the user long-clicks an item on the list
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View row, int position, long rowid) {
                if (_mActionMode != null) {
                    return false;
                }

                _selectedIndex = position;

                _listSmartPlants.setItemChecked(position, true);
                _arrayAdapter.setSelectedIndex(position);

                _mActionMode = SmartPlantListActivity.this.startActionMode(mActionModeCallback);
                return true;
            }
        });

        _fab = (FloatingActionButton) findViewById(R.id.fab);
        _fab.attachToListView(_listSmartPlants);
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartPlantListActivity.this, SmartPlantEditActivity.class);
                SmartPlant smartPlant = new SmartPlant();
                smartPlant.set_temperatureMin(10);
                smartPlant.set_temperatureMax(20);
                smartPlant.set_humidityMin(25);
                smartPlant.set_humidityMax(75);
                smartPlant.set_lightMin(25);
                smartPlant.set_lightMax(75);
                intent.putExtra("smartPlant", smartPlant);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        if(SmartPlant.obtainAllSmartPlants(getApplicationContext()).size() != 0) {
            _listSmartPlants.setVisibility(View.VISIBLE);
            scanSmartPlants();
        } else {

            _listSmartPlants.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            if(SmartPlant.obtainAllSmartPlants(getApplicationContext()).size() != 0) {
                scanSmartPlants();
            } else {
                Toast.makeText(this, R.string.not_any_smart_plant, Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkBluetoothLeSupport(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.msg_bluetooth_le_required, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void scanSmartPlants(){

        _smartPlants = SmartPlant.obtainAllSmartPlants(getApplicationContext());
        _arrayAdapter = new SmartPlantsArrayAdapter(this, _smartPlants);
        _listSmartPlants.setAdapter(_arrayAdapter);

        _spp = SmartPlantProtocol.getInstance(this);
        if(!SmartPlantProtocol.configureBluetoothAdapter(this)){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
        }else{
            _spp.setListener(this);
            _arrayAdapter.notifyDataSetChanged();
            if(_spp.isScanning())
                _spp.scanSmartPlants(false);
            _spp.scanSmartPlants(true);
        }
    }

    private void refreshSmartPlants(){
        for(SmartPlant foundSmartPlant : _spp.getSmartPlants()){
            for(SmartPlant sp : _smartPlants) {
                if(foundSmartPlant.get_serialNumber().equals(sp.get_serialNumber())) {
                    sp.set_found(true);
                    sp.set_distance(foundSmartPlant.get_distance());
                    sp.set_macAddress(foundSmartPlant.get_macAddress());
                    break;
                }
            }
        }
    }

    @Override
    public void smartPlantFound(SmartPlant smartPlant) {
        //Log.i("APP", "Smart Plant found");
    }

    @Override
    public void searchState(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if(state == SmartPlantProtocol.SEARCH_STARTED){
                _barSearching.setVisibility(View.VISIBLE);
            }else if (state == SmartPlantProtocol.SEARCH_END_SUCCESS){
                _barSearching.setVisibility(View.GONE);
                // Cargar las smart plants
                refreshSmartPlants();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _arrayAdapter.notifyDataSetChanged();
                    }
                });
            }else if (state == SmartPlantProtocol.SEARCH_END_EMPTY){
                _barSearching.setVisibility(View.GONE);
                _arrayAdapter.notifyDataSetChanged();
            }
            }
        });
    }

    @Override
    public void operationError(int status) {
        // Reset Bluetooth adapter on UI Thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SmartPlantListActivity.this, "Reseteamos el bluetooth", Toast.LENGTH_LONG).show();

            }
        });

        // Disable Bluetooth Adapter
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        // Enable Bluetooth Adapter 1 sec. later
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.enable();
            }
        }, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_list_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            _listSmartPlants.setEnabled(false);
            _fab.setEnabled(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    if(_smartPlants.get(_selectedIndex).remove(getApplicationContext())) {
                        _smartPlants.remove(_selectedIndex);
                        _arrayAdapter.notifyDataSetChanged();
                        mode.finish();
                        Toast.makeText(getApplicationContext(), "La maceta inteligente ha sido eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case R.id.action_edit:
                    Intent intent = new Intent(SmartPlantListActivity.this, SmartPlantEditActivity.class);
                    intent.putExtra("smartPlant", _smartPlants.get(_selectedIndex));
                    mode.finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            _listSmartPlants.setItemChecked(_selectedIndex, false);
            _listSmartPlants.setEnabled(true);
            _fab.setEnabled(true);
            _arrayAdapter.setSelectedIndex(-1);
            _mActionMode = null;
        }
    };
}