package com.isummation.customgallery;

import java.io.File;
import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

class ImageCursorLoader extends CursorLoader {

	public ImageCursorLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
	}
	
}
public class ImageAdapter extends BaseAdapter {
	public final static int VIEW_IMAGE = 3;
	
	private LayoutInflater mInflater;
	private final Activity activity ;
	private long lastId;
	public ArrayList<ImageItem> images = new ArrayList<ImageItem>();

	class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
	}

	class ImageItem {
		boolean selection;
		int id;
		Bitmap img;
	}
	
	public ImageAdapter(Activity activity) {
		this.activity = activity; 
	    mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void initialize() {
		images.clear();
		final String[] columns = { MediaStore.Images.Thumbnails._ID };
		final String orderBy = MediaStore.Images.Media._ID;
		CursorLoader cursorloader = new CursorLoader(activity.getApplicationContext(), 
				                                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
				                                     columns, 
				                                     null, 
				                                     null, 
				                                     orderBy) ;
		Cursor imagecursor = cursorloader.loadInBackground() ;
		if(imagecursor != null){
			int image_column_index = imagecursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int count = imagecursor.getCount();
			for (int i = 0; i < count; i++) {
				imagecursor.moveToPosition(i);
				int id = imagecursor.getInt(image_column_index);
				ImageItem imageItem = new ImageItem();
				imageItem.id = id;
				lastId = id;
				imageItem.img = MediaStore.Images.Thumbnails.getThumbnail(
						this.activity.getApplicationContext().getContentResolver(), id,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				images.add(imageItem);
			}
			imagecursor.close();
		}
		notifyDataSetChanged();
	}
	
	public void checkForNewImages(){
		//Here we'll only check for newer images
		final String[] columns = { MediaStore.Images.Thumbnails._ID };
		final String orderBy = MediaStore.Images.Media._ID;
		CursorLoader cursorloader = new CursorLoader(activity.getApplicationContext(), 
				                                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
				                                     columns, 
				                                     MediaStore.Images.Media._ID + " > " + lastId , 
				                                     null, 
				                                     orderBy) ;
		Cursor imagecursor = cursorloader.loadInBackground() ;
		
		int image_column_index = imagecursor
				.getColumnIndex(MediaStore.Images.Media._ID);
		int count = imagecursor.getCount();
		for (int i = 0; i < count; i++) {
			imagecursor.moveToPosition(i);
			int id = imagecursor.getInt(image_column_index);
			ImageItem imageItem = new ImageItem();
			imageItem.id = id;
			lastId = id;
			imageItem.img = MediaStore.Images.Thumbnails.getThumbnail(
					this.activity.getApplicationContext().getContentResolver(), id,
					MediaStore.Images.Thumbnails.MICRO_KIND, null);
			imageItem.selection = true; //newly added item will be selected by default
			images.add(imageItem);
		}
		imagecursor.close();
		notifyDataSetChanged();
	}

	public int getCount() {
		return images.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.galleryitem, null);
			holder.imageview = (ImageView) convertView
					.findViewById(R.id.thumbImage);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.itemCheckBox);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ImageItem item = images.get(position);
		holder.checkbox.setId(position);
		holder.imageview.setId(position);
		holder.checkbox.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				int id = cb.getId();
				if (images.get(id).selection) {
					cb.setChecked(false);
					images.get(id).selection = false;
				} else {
					cb.setChecked(true);
					images.get(id).selection = true;
				}
			}
		});
		holder.imageview.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int id = v.getId();
				ImageItem item = images.get(id);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				final String[] columns = { MediaStore.Images.Media.DATA };

				CursorLoader cursorloader = new CursorLoader(activity.getApplicationContext(), 
						                                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
						                                     columns, 
						                                     MediaStore.Images.Media._ID + " = " + item.id, 
						                                     null, 
						                                     MediaStore.Images.Media._ID) ;
				Cursor imagecursor = cursorloader.loadInBackground() ;
				if (imagecursor != null && imagecursor.getCount() > 0){
					imagecursor.moveToPosition(0);
					String path = imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					File file = new File(path);
					imagecursor.close();
					intent.setDataAndType(
							Uri.fromFile(file),
							"image/*");
					activity.startActivityForResult(intent, VIEW_IMAGE);
				}
			}
		});
		holder.imageview.setImageBitmap(item.img);
		holder.checkbox.setChecked(item.selection);
		return convertView;
	}
}