package com.binroot.fatpita;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class FatPitaPlusActivity extends Activity {

	ApplicationStart appState;
	ImageView iv;
	private static ProgressBar pBar;
	Button mainButton;
	Button backButton;
	Button favButton;
	BitmapManager bitmapManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		pBar = (ProgressBar) findViewById(R.id.progressbar);
		iv = (ImageView) findViewById(R.id.img);
		favButton = (Button) findViewById(R.id.button_fav);
		backButton = (Button) findViewById(R.id.button_back);
		mHandler = new Handler();
		appState = ((ApplicationStart)getApplicationContext());
		bitmapManager = new BitmapManager(appState, favButton);

		Toast.makeText(this, "Tap anywhere for another pic!", Toast.LENGTH_SHORT).show();;
		
		this.storageSetup();
		
		// Show initial image
		if(appState.getURL()!=null) {
			updateImage(appState.getURL());
		}
		else {
			updateImage();
		}


		mainButton = (Button) findViewById(R.id.button);
		mainButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mHandler.post(new Runnable() {
					public void run() {
						updateImage();
					}
				});
			}
		});
		
		mainButton.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(appState.getURL()));
				startActivity(i);
				return true;
			}
		});

		
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

		favButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(appState.getFavList().contains(appState.getURL())) {
					appState.removeFav(appState.getURL());
					favButton.setBackgroundResource(android.R.drawable.btn_star);
				}
				else {
					Toast.makeText(getApplicationContext(), "Click menu to see your favorites", Toast.LENGTH_LONG).show();
					appState.addFav(appState.getURL());
					favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
					String url = appState.getURL();
					addToFav(url);
				}

			}
		});
	}
	
	/**
	 * If favList file doesn't exist, create it.
	 */
	private void storageSetup() {
		FileInputStream fis = null;
		try {
			//Log.d("fatpita", "Opening file favList");
			fis = openFileInput("favList");
			fis.close();
		} catch (FileNotFoundException e) {
			//Log.d("fatpita", "Could not find favList");

			FileOutputStream fos = null;
			try {
				//Log.d("fatpita", "Creating favList");
				fos = openFileOutput("favList", Context.MODE_PRIVATE);
				fos.close();

			} catch (FileNotFoundException e2) {
				//Log.d("fatpita", "Could not create favList");
			} catch (IOException e3) {
				//Log.d("fatpita", "Could not close fos");
			}
		} catch (IOException e) {
			//Log.d("fatpita", "Could not close fis");
		}
	}

	/**
	 * Add the current URL to the list of favorite images
	 * @param url - Full URL of image
	 */
	private void addToFav(String url) {
		FileOutputStream fos = null;
		try {
			//Log.d("fatpita", "Opening file favList");
			fos = openFileOutput("favList", Context.MODE_APPEND);

			fos.write(url.getBytes());
			fos.write(" ".getBytes());

			fos.close();
		} catch (FileNotFoundException e) {Log.d("fatpita", "Could not find file favList");} 
		catch (IOException e) {Log.d("fatpita", "Could not close file favList");}
	}

	/**
	 * Pick a random URL and pass it over to updateImage(String url)
	 */
	private void updateImage() {
		String url = URLFinder.randomURL();
		updateImage(url);
	}

	/**
	 * Download the image and post it to the ImageView. 
	 * Also sets the favButton to on or off accordingly
	 * @param url - URL of image to download and display
	 */
	private void updateImage(String url) {
		appState.setURL(url);
		
		if(!appState.getFavList().contains(appState.getURL())) {
			favButton.setBackgroundResource(android.R.drawable.btn_star);
		}
		else {
			favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
		}

		new DownloadImageTask().execute();
	}


	
	Handler mHandler;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	/**
	 * Menu event listener
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
	    case R.id.menu_fav:
	    	menuFavClicked();
	        return true;
	    case R.id.menu_share:
	        menuShareClicked();
	        return true;
	    case R.id.menu_download:
	    	menuDownloadClicked();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}


	private void menuFavClicked() {
		String sites = "";
		FileInputStream fis = null;
		try {
			fis = openFileInput("favList");

			int buff;
			while((buff=fis.read()) != -1) {
				byte buffArr[] = {(byte) buff};
				sites += new String(buffArr);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		String sitesArr[] = sites.split(" ");
		Intent i = new Intent(FatPitaPlusActivity.this, FavActivity.class);
		i.putExtra("sites", sitesArr);
		startActivityForResult(i, 100);
	}

	private void menuShareClicked() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check this out!");
        intent.putExtra(Intent.EXTRA_TEXT, " \n " + "From my fatpita+ app: "+appState.getURL());

        startActivity(Intent.createChooser(intent, "Share"));
	}

	private void menuDownloadClicked() {
		downloadToSDFromUrl(appState.getURL());
	}

	public void downloadToSDFromUrl(String source) {
    	AsyncTask<String, Void, Boolean> dlTask = new AsyncTask<String, Void, Boolean>(){
    		private String path; 
    		
    		protected Boolean doInBackground(String... params) {
				String imageURL = params[0];
				Bitmap myBitmap = null;

		    	try {

	                URL myImageURL = new URL(imageURL);
	                HttpURLConnection connection = (HttpURLConnection)myImageURL.openConnection();
	                connection.setDoInput(true);
	                connection.connect();
	                BufferedInputStream input = new BufferedInputStream(connection.getInputStream());

	                myBitmap = BitmapFactory.decodeStream(input);

	                path = MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap, "fatpita+", "Downloaded from the fatpita+ android app.");

	                return true;

	            } catch (Throwable e) {
	            	if(myBitmap != null){
	            		myBitmap.recycle();
	            	}
	            	return false;
	            }
			}

    		protected void onPostExecute(Boolean result){

    			if(result){
    				Notification notification = new Notification(android.R.drawable.ic_menu_upload, "Downloaded image!", System.currentTimeMillis());

    				Intent picIntent = new Intent();
    				picIntent.setAction(android.content.Intent.ACTION_VIEW);
    				picIntent.setData(Uri.parse(path));
    				PendingIntent contentIntent = PendingIntent.getActivity(FatPitaPlusActivity.this, 0, picIntent, 0);

    				notification.setLatestEventInfo(FatPitaPlusActivity.this, "fatpita+", "Click to view downloaded image", contentIntent);
    				notification.flags |= Notification.FLAG_AUTO_CANCEL;


    				NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    				nm.cancel(9);
    				nm.notify(9, notification);
    			} else{
    				Toast.makeText(FatPitaPlusActivity.this, "Image could not be downloaded", Toast.LENGTH_SHORT).show();
    			}
    		}
    	};

    	dlTask.execute(source);
	}

	
	public void onActivityResult(int reqCode, int resCode, Intent data) {
		if(reqCode == 100) {
			// Returning form FavActivity

			if(resCode == 100) {
				String url = data.getExtras().getString("url");
				//Log.d("fatpita", "User picked "+url);

				appState.setURL(url);
				new DownloadImageTask().execute();

				mHandler.post(new Runnable() {

					public void run() {
						if(!appState.getFavList().contains(appState.getURL())) {
							favButton.setBackgroundResource(android.R.drawable.btn_star);
						}
						else {
							favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
						}
					}
				});

			}
			else if(resCode == RESULT_CANCELED) {
				// Do nothing
			}
			else if(resCode == 101) { // Clear All
				FileOutputStream fos = null;
				try {
					//Log.d("fatpita", "Opening file favList");
					fos = openFileOutput("favList", Context.MODE_PRIVATE);
					fos.write("".getBytes());
					fos.close();
				} 
				catch (FileNotFoundException e) {Log.d("fatpita", "Could not find file favList");} 
				catch (IOException e) {Log.d("fatpita", "Could not close file favList");}

			}
			Toast.makeText(this, "Tap anywhere for another pic!", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Sets image depending on cursor position
	 */
	public void goBack() {
		if(appState.getBackCursor()!=0) {
			appState.setBackCursor(appState.getBackCursor()-1);

			appState.setURL(appState.getHistory().get(appState.getBackCursor()));

			bitmapManager.fetchBitmapOnThread(appState.getURL(), iv, pBar, FatPitaPlusActivity.this, false);
			
			if(!appState.getFavList().contains(appState.getURL())) {
				favButton.setBackgroundResource(android.R.drawable.btn_star);
			}
			else {
				favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
			}
		}
	}

	

	

	/**
	 * AsyncTask to download images
	 */
	private class DownloadImageTask extends AsyncTask<Void, Void, Void> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() */
		protected Void doInBackground(Void... arg0) {
			try {
				//Log.d("fatpita", "* Loading now!");
				mHandler.post(new Runnable() {

					public void run() {
						//iv.setImageBitmap(loadNewImage(appState.getURL()));
						bitmapManager.fetchBitmapOnThread(appState.getURL(), iv, pBar, FatPitaPlusActivity.this, true);
					}
				});

			} catch (Exception e) { }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			//Log.d("fatpita","* PostExecute");
		}

		@Override
		protected void onPreExecute(){
			//Dialog = ProgressDialog.show(FatPitaPlusActivity.this, "", rand, true);
		}
	}

}