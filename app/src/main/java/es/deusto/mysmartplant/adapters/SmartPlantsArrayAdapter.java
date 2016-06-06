package es.deusto.mysmartplant.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import es.deusto.mysmartplant.R;
import es.deusto.mysmartplant.entities.SmartPlant;

/**
 * Created by Jordan on 16/5/16.
 */

public class SmartPlantsArrayAdapter extends ArrayAdapter<SmartPlant> {

	private Context 				context;
	private ArrayList<SmartPlant> 	values;
	private LayoutInflater 			inflater;
	private int         			selectedIndex;
	
	public class CustomListItem {
		LinearLayout 	laySmartPlantItem;
		TextView 		lblNameList;
		TextView 		lblState;
		TextView 		lblDistance;
		ImageView		imgSmartPlant;
	}
	
	public SmartPlantsArrayAdapter(Context context, ArrayList<SmartPlant> commandsList) {
		super(context, R.layout.smart_plant_list_item, commandsList);
		this.context = context;
		values = new ArrayList<SmartPlant>();
		values.addAll(commandsList);
		inflater = LayoutInflater.from(this.context);
	}

	public void setSelectedIndex(int ind) {
		selectedIndex = ind;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		CustomListItem myListItem;

		final SmartPlant smartPlant = getItem(position);
		
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.smart_plant_list_item, parent, false);
			myListItem = new CustomListItem();

			myListItem.laySmartPlantItem = (LinearLayout) convertView.findViewById(R.id.laySmartPlantItem);
			myListItem.lblNameList = (TextView) convertView.findViewById(R.id.lblNameList);
			myListItem.lblState = (TextView) convertView.findViewById(R.id.lblState);
			myListItem.lblDistance = (TextView) convertView.findViewById(R.id.lblDistance);
			myListItem.imgSmartPlant = (ImageView) convertView.findViewById(R.id.imageList);
			convertView.setTag(myListItem);
		} else {
			myListItem = (CustomListItem) convertView.getTag();
		}

		myListItem.lblNameList.setText(smartPlant.get_name());

		if(smartPlant.is_found()) {
			myListItem.lblState.setTextColor(Color.parseColor("#008800"));
			myListItem.lblState.setText("Encontrado");
			myListItem.laySmartPlantItem.setBackground(context.getResources().getDrawable(R.drawable.background_found));
		} else {
			myListItem.lblState.setTextColor(Color.parseColor("#FF0000"));
			myListItem.lblState.setText("No encontrado");
			myListItem.laySmartPlantItem.setBackground(context.getResources().getDrawable(R.drawable.background_not_found));
		}

		if(smartPlant.get_distance() == 0.0) {
			myListItem.lblDistance.setVisibility(View.GONE);
		} else {
			myListItem.lblDistance.setVisibility(View.VISIBLE);
			myListItem.lblDistance.setText(Double.toString(smartPlant.get_distance()) + " m.");
		}

		if(smartPlant.get_imagePath() == null || smartPlant.get_imagePath().isEmpty() || ! new File(smartPlant.get_imagePath()).exists()) {
			myListItem.imgSmartPlant.setImageResource(R.drawable.flower_icon);
			myListItem.imgSmartPlant.setBackgroundResource(R.drawable.image_background);
			myListItem.imgSmartPlant.setPadding(25,25,25,25);
			myListItem.imgSmartPlant.setMaxHeight(10);
			myListItem.imgSmartPlant.setMaxWidth(10);
		} else {
			Bitmap thumbnail = (BitmapFactory.decodeFile(smartPlant.get_imagePath()));
			myListItem.imgSmartPlant.setImageBitmap(getRoundedShape(thumbnail));
			myListItem.imgSmartPlant.setBackgroundResource(R.drawable.image_background);
			myListItem.imgSmartPlant.setPadding(0,0,0,0);
			myListItem.imgSmartPlant.setMaxHeight(10);
			myListItem.imgSmartPlant.setMaxWidth(10);
		}

		myListItem.imgSmartPlant.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + smartPlant.get_imagePath()), "image/*");
				v.getContext().startActivity(intent);
			}
		});

		return convertView;
	}

	public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
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

}
