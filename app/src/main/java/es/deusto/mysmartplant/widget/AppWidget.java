package es.deusto.mysmartplant.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.util.ArrayList;

import es.deusto.mysmartplant.R;
import es.deusto.mysmartplant.activities.SmartPlantEditActivity;
import es.deusto.mysmartplant.activities.SmartPlantInfoActivity;
import es.deusto.mysmartplant.activities.SmartPlantListActivity;
import es.deusto.mysmartplant.entities.SmartPlant;
import es.deusto.mysmartplant.utils.SmartPlantGattProtocol;
import es.deusto.mysmartplant.utils.SmartPlantGattSuscriber;
import es.deusto.mysmartplant.utils.SmartPlantListener;
import es.deusto.mysmartplant.utils.SmartPlantProtocol;

public class AppWidget extends AppWidgetProvider implements SmartPlantListener, SmartPlantGattSuscriber {

    private Context                 context;
    private AppWidgetManager        appWidgetManager;
    private int[]                   appWidgetIds;
    private SmartPlantProtocol      _spp;

    private Bitmap                  bm;
    private ArrayList<SmartPlant>   _smartPlants;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        if (appWidgetIds.length > 0) {

            _smartPlants = SmartPlant.obtainAllSmartPlants(context);

            if(_smartPlants.size() == 0) {
                showNoPlantsRegistered();
            } else {
                _spp = SmartPlantProtocol.getInstance(context);
                if(!SmartPlantProtocol.configureBluetoothAdapter(context)) {
                    Log.d("APP", "Entra");
                }else{
                    _spp.setListener(this);
                    if(_spp.isScanning())
                        _spp.scanSmartPlants(false);
                    _spp.scanSmartPlants(true);
                }
            }
        }
    }

    public void showNoPlantsRegistered() {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int widgetId = appWidgetIds[i];
            try {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setTextViewText(R.id.lblPlantName, "No hay macetas registradas");
                if (bm != null) {
                    bm.recycle();
                    bm = null;
                }

                views.setViewVisibility(R.id.smart_plant_data, View.GONE);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                views.setImageViewResource(R.id.imgSmartPlantWidget, R.mipmap.ic_launcher);

                Intent intent = new Intent(context, SmartPlantEditActivity.class);
                intent.putExtra("smartPlant", new SmartPlant());
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

                views.setOnClickPendingIntent(R.id.layWidget, pIntent);
                appWidgetManager.updateAppWidget(widgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showNoPlantsFound() {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int widgetId = appWidgetIds[i];
            try {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setTextViewText(R.id.lblPlantName, "No se ha encontrado ninguna maceta");
                if (bm != null) {
                    bm.recycle();
                    bm = null;
                }

                views.setViewVisibility(R.id.smart_plant_data, View.GONE);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                views.setImageViewResource(R.id.imgSmartPlantWidget, R.mipmap.ic_launcher);

                Intent intent = new Intent(context, SmartPlantListActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

                views.setOnClickPendingIntent(R.id.layWidget, pIntent);
                appWidgetManager.updateAppWidget(widgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showPlantInfoFound(SmartPlant smartPlant) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int widgetId = appWidgetIds[i];
            try {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                views.setTextViewText(R.id.lblPlantName, smartPlant.get_name());
                if (bm != null) {
                    bm.recycle();
                    bm = null;
                }

                views.setViewVisibility(R.id.smart_plant_data, View.VISIBLE);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                if (smartPlant.get_imagePath() != null && !smartPlant.get_imagePath().isEmpty() && new File(smartPlant.get_imagePath()).exists()) {
                    bm = BitmapFactory.decodeFile(smartPlant.get_imagePath(), options);
                    views.setImageViewBitmap(R.id.imgSmartPlantWidget, getRoundedShape(bm));
                } else {
                    views.setImageViewResource(R.id.imgSmartPlantWidget, R.mipmap.ic_launcher);
                }
                views.setTextViewText(R.id.lblPlantName, smartPlant.get_name().toUpperCase());

                views.setTextViewText(R.id.lblPlantTemp, context.getApplicationContext().getResources().getString(R.string.temperature) + smartPlant.get_temperature() + "ºC");
                views.setTextViewText(R.id.lblPlantHum, context.getApplicationContext().getResources().getString(R.string.humidity) + smartPlant.get_humidity() + "%");
                views.setTextViewText(R.id.lblPlantLight, context.getApplicationContext().getResources().getString(R.string.luminosity) + smartPlant.get_light() + "%");

                if(smartPlant.get_temperature() < smartPlant.get_temperatureMin() || smartPlant.get_temperature() > smartPlant.get_temperatureMax()) {
                    views.setTextColor(R.id.lblPlantTemp, Color.RED);
                } else {
                    views.setTextColor(R.id.lblPlantTemp, Color.BLACK);
                }

                if(smartPlant.get_humidity() < smartPlant.get_humidityMin() || smartPlant.get_humidity() > smartPlant.get_humidityMax()) {
                    views.setTextColor(R.id.lblPlantHum, Color.RED);
                } else {
                    views.setTextColor(R.id.lblPlantHum, Color.BLACK);
                }

                if(smartPlant.get_light() < smartPlant.get_lightMin() || smartPlant.get_light() > smartPlant.get_lightMax()) {
                    views.setTextColor(R.id.lblPlantLight, Color.RED);
                } else {
                    views.setTextColor(R.id.lblPlantLight, Color.BLACK);
                }

                Intent intent = new Intent(context, SmartPlantInfoActivity.class);
                intent.putExtra("smartPlant", smartPlant);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

                views.setOnClickPendingIntent(R.id.layWidget, pIntent);
                appWidgetManager.updateAppWidget(widgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        // TODO Auto-generated method stub
        int targetWidth = 200;
        int targetHeight = 200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth,
                        targetHeight), null);
        return targetBitmap;
    }


    @Override
    public void smartPlantFound(SmartPlant smartPlant) {

    }

    @Override
    public void searchState(int state) {
        if(state == SmartPlantProtocol.SEARCH_STARTED){

        } else if (state == SmartPlantProtocol.SEARCH_END_SUCCESS){
            Log.d("APP", "Finaliza la busqueda");
            double minValue = Double.MAX_VALUE;
            SmartPlant nearestPlant = null;
            for(SmartPlant foundSmartPlant : _spp.getSmartPlants()){
                for(SmartPlant sp : _smartPlants) {
                    if(foundSmartPlant.get_serialNumber().equals(sp.get_serialNumber())) {
                        if(foundSmartPlant.get_distance() < minValue) {
                            minValue = foundSmartPlant.get_distance();
                            nearestPlant = sp;
                        }
                        break;
                    }
                }
            }
            if(nearestPlant != null) {
                SmartPlantGattProtocol.getInstance(context).connect(nearestPlant, this);
            }
        } else if (state == SmartPlantProtocol.SEARCH_END_EMPTY){
            showNoPlantsFound();
        }
    }

    @Override
    public void operationError(int status) {

    }

    @Override
    public void characteristicsRead(SmartPlant sp) {
        Log.d("APP", "Encontrada y actualizada: " + sp.toString());
        showPlantInfoFound(sp);
        SmartPlantGattProtocol.getInstance(context).disconnect();
    }

    @Override
    public void characteristicWrite(SmartPlant sp) {

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
}