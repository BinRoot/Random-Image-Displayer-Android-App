package com.binroot.fatpita;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class FatPitaPlusActivity extends Activity {

	ApplicationStart appState;
	ImageView iv;
	private static ProgressDialog Dialog;
	Button mainButton;
	Button backButton;
	Button favButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		iv = (ImageView) findViewById(R.id.img);
		favButton = (Button) findViewById(R.id.button_fav);
		backButton = (Button) findViewById(R.id.button_back);
		mHandler = new Handler();
		appState = ((ApplicationStart)getApplicationContext());

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
						mainButton.setBackgroundResource(R.drawable.clicked2);
						updateImage();
					}
				});
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
			Log.d("fatpita", "Opening file favList");
			fis = openFileInput("favList");
			fis.close();
		} catch (FileNotFoundException e) {
			Log.d("fatpita", "Could not find favList");

			FileOutputStream fos = null;
			try {
				Log.d("fatpita", "Creating favList");
				fos = openFileOutput("favList", Context.MODE_PRIVATE);
				fos.close();

			} catch (FileNotFoundException e2) {
				Log.d("fatpita", "Could not create favList");
			} catch (IOException e3) {
				Log.d("fatpita", "Could not close fos");
			}
		} catch (IOException e) {
			Log.d("fatpita", "Could not close fis");
		}
	}

	/**
	 * Add the current URL to the list of favorite images
	 * @param url - Full URL of image
	 */
	private void addToFav(String url) {
		FileOutputStream fos = null;
		try {
			Log.d("fatpita", "Opening file favList");
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
		String url = randomURL();
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


	/**
	 * Menu display
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add("View Favorites");
		return result;   
	}

	Handler mHandler;
	/**
	 * Menu event listener
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("View Favorites")) {
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
		return true;
	}

	public void onActivityResult(int reqCode, int resCode, Intent data) {
		if(reqCode == 100) {
			// Returning form FavActivity
			
			if(resCode == 100) {
				String url = data.getExtras().getString("url");
				Log.d("fatpita", "User picked "+url);

				iv.setImageBitmap(loadImage(url));


				appState.getHistory().add(url);
				appState.setBackCursor(appState.getHistory().size()-1);
				appState.setURL(url);

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
					Log.d("fatpita", "Opening file favList");
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
			iv.setImageBitmap(loadImage(appState.getHistory().get(appState.getBackCursor())));
			appState.setURL(appState.getHistory().get(appState.getBackCursor()));

			if(!appState.getFavList().contains(appState.getURL())) {
				favButton.setBackgroundResource(android.R.drawable.btn_star);
			}
			else {
				favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
			}
		}
	}

	/**
	 * Safe url loading. Does not record history.
	 * @param url - The URL of an image
	 * @return A Bitmap image from url
	 */
	private Bitmap loadImage(String url) {
		Bitmap b = loadBitmapFromWebOperations(url);
		while(b == null) {
			url = randomURL();
			b = loadBitmapFromWebOperations(url);
		}
		return b;
	}

	/**
	 * Safe url loading. Records history.
	 * @param url - The URL of an image
	 * @return A Bitmap image from url
	 */
	private Bitmap loadNewImage(String url) {
		Bitmap b = loadBitmapFromWebOperations(url);
		while(b == null) {
			url = randomURL();
			b = loadBitmapFromWebOperations(url);
		}
		
		appState.getHistory().add(url);
		appState.setBackCursor(appState.getHistory().size()-1);
		appState.setURL(url);

		if(!appState.getFavList().contains(appState.getURL())) {
			favButton.setBackgroundResource(android.R.drawable.btn_star);
		}
		else {
			favButton.setBackgroundResource(android.R.drawable.btn_star_big_on);
		}

		return b;
	}

	/**
	 * Picks a random URL
	 * @return a random URL
	 */
	private String randomURL() {
		int pick = (int)(Math.random()*3)+1;

		String url = "";

		if(pick==1) {
			url = loadURLFrom("fatpita");
		}
		else if (pick==2) {
			url = loadURLFrom("fukung");
		}
		else if (pick==3) {
			url = loadURLFrom("eatliver");
		}
		return url;
	}

	/**
	 * Used by randomURL() to get a url from a website
	 * @param website - the image distributing website
	 * @return a random URL from that website
	 */
	private String loadURLFrom(String website) {
		String url = null;

		if(website.equals("fatpita")) {
			int fatpitaRand = (int) (Math.random()*10000);
			url = "http://fatpita.net/images/image%20(" +
			fatpitaRand + ").jpg";
			Log.d("fatpita", "image from fatpita: "+fatpitaRand);
		}
		else if(website.equals("fukung")) {
			int fukungRand = (int) (Math.random()*41420) +1;
			url = "http://media.fukung.net/images/" +
			fukungRand +
			".jpg";
			Log.d("fatpita", "image from fukung: "+fukungRand);
		}
		else if(website.equals("eatliver")) {
			// 2005: 1..780
			// 2006: 781..1656
			// 2007: 1657..2688
			// 2008: 2689..3840
			// 2009: 3841..5196
			// 2010: 5197..6588
			// 2011: 6589..7452
			int year = 0;
			int eatLiverRand = (int) (Math.random()*7452) +1;
			if(eatLiverRand>=1 && eatLiverRand<=780) {
				year = 2005;
			}
			else if(eatLiverRand>=781 && eatLiverRand<=1656) {
				year = 2006;
			}
			else if(eatLiverRand>=1657 && eatLiverRand<=2688) {
				year = 2007;
			}
			else if(eatLiverRand>=2689 && eatLiverRand<=3840) {
				year = 2008;
			}
			else if(eatLiverRand>=3841 && eatLiverRand<=5196) {
				year = 2009;
			}
			else if(eatLiverRand>=5197 && eatLiverRand<=6588) {
				year = 2010;
			}
			else if(eatLiverRand>=6589 && eatLiverRand<=7452) {
				year = 2011;
			}

			url = "http://www.eatliver.com/img/" +
			year +
			"/" +
			eatLiverRand +
			".jpg";
			Log.d("fatpita", "image from eatliver: "+year+"/"+eatLiverRand);
		}

		return url;
	}

	/**
	 * AsyncTask to download images
	 */
	private class DownloadImageTask extends AsyncTask<Void, Void, Void> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() */
		protected Void doInBackground(Void... arg0) {
			try {
				Log.d("fatpita", "* Loading now!");
				mHandler.post(new Runnable() {

					public void run() {
						iv.setImageBitmap(loadNewImage(appState.getURL()));
					}
				});

			} catch (Exception e) { }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mHandler.post(new Runnable() {
				public void run() {
					mainButton.setBackgroundResource(R.drawable.clear);
				}
			});
			Log.d("fatpita","* PostExecute");
			Dialog.dismiss();
		}

		@Override
		protected void onPreExecute(){
			Log.d("Async","Dialog shown!");
			String rand = getRandomString();
			Dialog = new ProgressDialog(FatPitaPlusActivity.this);
			Dialog.setMax(100);
			Dialog.setMessage(rand);
			Dialog.show();
			//Dialog = ProgressDialog.show(FatPitaPlusActivity.this, "", rand, true);
		}
	}

	/**
	 * Randomly generates a String for the loading screen
	 * @return a random string
	 */
	private String getRandomString() {
		String retStr = "Loading";
		String [] strList = {
				"Loading...",
				"Trying...",
				"Fetching...",
				"Thinking...",
				"Looking...",
				"Updating...",
				"Gathering...",
				"Searching...",
				"Getting...",
				"Computing...",
				"Calculating...",
				"Tasking...",
				"Downloading...",
				"This one's good...",
				"Gonna love this...",
				"Here it comes...",
				"Almost here...",
				"LOL...",
				"OMG...",
				"WTF..."
		};
		int randIndex = (int) (Math.random()*strList.length);
		retStr = strList[randIndex];
		return retStr;
	}

	/**
	 * Generates a Bitmap from an image online
	 * @param url - the URL of an image online
	 * @return a Bitmap of the image from the URL
	 */
	private Bitmap loadBitmapFromWebOperations(String url) { 
		try { 
			InputStream is = (InputStream) new URL(url).getContent(); 
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap preview_bitmap=BitmapFactory.decodeStream(is,null,options);
			
			Log.d("fatpita", "loading image "+preview_bitmap.getWidth()+"x"+preview_bitmap.getHeight());
			// Ignore large images
			if(preview_bitmap.getWidth()>1000 || preview_bitmap.getHeight()>1000) {
				return null; 
			}
			return preview_bitmap; 
		}
		catch (Exception e) { 
			Log.d("fatpita", "exception = "+e.getMessage());
			return null; 
		} 
	}
}