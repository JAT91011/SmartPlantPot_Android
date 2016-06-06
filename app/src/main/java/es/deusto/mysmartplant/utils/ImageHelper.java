package es.deusto.mysmartplant.utils;

/**
 * Created by Jordan on 5/4/16.
 */

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageHelper {

    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/SmartPlantsPot");
        if(!storageDir.exists())
            storageDir.mkdir();
        return new File(storageDir, imageFileName);
    }
}