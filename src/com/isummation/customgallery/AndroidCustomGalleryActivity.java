package com.isummation.customgallery;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;



import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class AndroidCustomGalleryActivity extends Activity {
	public final static String TAG = AndroidCustomGalleryActivity.class.getSimpleName();
	
	public ImageAdapter imageAdapter;
	private final static int TAKE_IMAGE = 1;
	private final static int UPLOAD_IMAGES = 2;
	private Uri imageUri;
	private MediaScannerConnection mScanner;
	public GridView imagegrid;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		setContentView(R.layout.main);
		
		imageAdapter = new ImageAdapter(this);
		imageAdapter.initialize();
		imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		imagegrid.setAdapter(imageAdapter);

		final Button selectBtn = (Button) findViewById(R.id.selectBtn);
		selectBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final int len = imageAdapter.images.size();
				int cnt = 0;
				String selectImages = "";
				for (int i = 0; i < len; i++) {
					if (imageAdapter.images.get(i).selection) {
						cnt++;
						selectImages = selectImages + imageAdapter.images.get(i).id + ",";
					}
				}
				if (cnt == 0) {
					Toast.makeText(getApplicationContext(),
							"Please select at least one image",
							Toast.LENGTH_LONG).show();
				} else {
					selectImages = selectImages.substring(0,selectImages.lastIndexOf(","));
					Intent intent = new Intent(AndroidCustomGalleryActivity.this,
							UploadQueue.class);
					intent.putExtra("Ids", selectImages);
					intent.putExtra("photoId", "1234"); //it's just a value that required in my database, you can ingore it
					startActivityForResult(intent, UPLOAD_IMAGES);
				}
			}
		});
		final Button captureBtn = (Button) findViewById(R.id.captureBtn);
		captureBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String fileName = "IMG_" + sdf.format(new Date()) + ".jpg";
				File myDirectory = new File(Environment.getExternalStorageDirectory() + "/REOAllegiance/");
				myDirectory.mkdirs();
				File file = new File(myDirectory, fileName);
				imageUri = Uri.fromFile(file);
				Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

				// startActivity(intent);
				startActivityForResult(intent, TAKE_IMAGE);
			}
		});
	}


    protected void onStart(){
		Log.i(TAG, "onStart");
    	super.onStart();
    }
    
    protected void onRestart(){
		Log.i(TAG, "onRestart");
    	super.onRestart();
    	
    }

    protected void onResume(){
		Log.i(TAG, "onResume");
    	super.onResume();
    	
    }

    protected void onPause(){
		Log.i(TAG, "onPause");
    	super.onPause();
    	
    }

    protected void onStop(){
		Log.i(TAG, "onStop");
    	super.onStop();
    	
    }

    protected void onDestroy(){
		Log.i(TAG, "onDestroy");
    	super.onDestroy();
    	
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.i(TAG, String.format("onActivityResult : %d", resultCode));
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case TAKE_IMAGE:
			try {
				if (resultCode == RESULT_OK) {
					// we need to update the gallery by starting MediaSanner service.
					mScanner = new MediaScannerConnection(
							AndroidCustomGalleryActivity.this,
							new MediaScannerConnection.MediaScannerConnectionClient() {
								public void onMediaScannerConnected() {
									mScanner.scanFile(imageUri.getPath(), null /* mimeType */);
								}
	
								public void onScanCompleted(String path, Uri uri) {
									//we can use the uri, to get the newly added image, but it will return path to full sized image
									//e.g. content://media/external/images/media/7
									//we can also update this path by replacing media by thumbnail to get the thumbnail
									//because thumbnail path would be like content://media/external/images/thumbnail/7
									//But the thumbnail is created after some delay by Android OS
									//So you may not get the thumbnail. This is why I started new UI thread
									//and it'll only run after the current thread completed.
									if (path.equals(imageUri.getPath())) {
										mScanner.disconnect();
										//we need to create new UI thread because, we can't update our mail thread from here
										//Both the thread will run one by one, see documentation of android  
										AndroidCustomGalleryActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														updateUI();
													}
												});
									}
								}
							});
					mScanner.connect();
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case UPLOAD_IMAGES:
			if (resultCode == RESULT_OK){
				//do some code where you integrate this project
			}
			break;
		}
	}

	public void updateUI() {
		imageAdapter.checkForNewImages();
	}


}