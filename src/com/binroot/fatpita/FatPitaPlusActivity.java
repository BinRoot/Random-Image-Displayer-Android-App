package com.binroot.fatpita;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

	ImageView iv;
	ApplicationStart appState;
	private static ProgressDialog Dialog;
	Button mainButton;
	Button favButton;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		iv = (ImageView) findViewById(R.id.img);
		mHandler = new Handler();
		favButton = (Button) findViewById(R.id.button_fav);

		Toast.makeText(this, "Tap anywhere for another pic!", Toast.LENGTH_SHORT).show();;
		
		appState = ((ApplicationStart)getApplicationContext());


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
						// TODO: dialog box starts here
						mainButton.setBackgroundResource(R.drawable.clicked);
						updateImage();
					}
				});
			}
		});

		Button backButton = (Button) findViewById(R.id.button_back);
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

	private void updateImage() {
		String url = randomURL();
		updateImage(url);
	}

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
		//menu.add("Go Back");
		//menu.add("Save Pic");
		//menu.add("Get Saved Pic");
		//menu.add("Share With Friends");
		menu.add("View Favorites");
		return result;   
	}

	Handler mHandler;
	/**
	 * Menu event listener
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Go Back")) {
			mHandler.post(new Runnable() {
				public void run() {
					goBack();
				}
			});
		}
		else if(item.getTitle().equals("Save Pic")) {
			String FILENAME = "favpic";

			FileOutputStream fos = null;
			try {
				fos = openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			}

			InputStream is = null;
			try {
				is = (InputStream) new URL(appState.getURL()).getContent();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Drawable d = Drawable.createFromStream(is, "src name"); 
			Bitmap b = ((BitmapDrawable)d ).getBitmap();

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] imageInByte = stream.toByteArray();

			try {
				fos.write(imageInByte);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(item.getTitle().equals("Get Saved Pic")) {
			FileInputStream fis = null;
			try {
				fis = openFileInput("favpic");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Bitmap b = BitmapFactory.decodeStream(fis);
			iv.setImageBitmap(b);

			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(item.getTitle().equals("Share With Friends")) {
			final CharSequence[] items = {"Email", "SMS"};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Share with...");
			builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if(items[item].equals("Email")) {



						final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

						emailIntent .setType("plain/text");
						emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
						emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this pic!");
						emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, "I found this on my fatpita Plus app: "+appState.getURL());
						startActivity(Intent.createChooser(emailIntent, "Send mail..."));

					}
					else if(items[item].equals("SMS")) {
						Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);
						picMessageIntent.setType("image/jpeg");

						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						InputStream is = null;
						try {
							is = (InputStream) new URL(appState.getURL()).getContent();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Drawable d = Drawable.createFromStream(is, "src name"); 
						Bitmap b = ((BitmapDrawable)d ).getBitmap();
						ContentValues values = new ContentValues();
						values.put("address", "703");
						values.put("body", "hi");
						getContentResolver().insert(Uri.parse("content://sms/sent"), values);

					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		else if(item.getTitle().equals("View Favorites")) {
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

			//Toast.makeText(this, sites, Toast.LENGTH_LONG).show();


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

				iv.setImageDrawable(loadImage(url));


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
			else if(resCode == 101) {
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

	public void goBack() {
		if(appState.getBackCursor()!=0) {
			appState.setBackCursor(appState.getBackCursor()-1);
			iv.setImageDrawable(loadImage(appState.getHistory().get(appState.getBackCursor())));
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
	 * Save url loading
	 * @param url - The url of an image
	 * @return A Drawable image from url
	 */
	private Drawable loadImage(String url) {
		Drawable d = loadImageFromWebOperations(url);
		while(d == null) {
			url = randomURL();
			d = loadImageFromWebOperations(url);
		}
		return d;
	}

	private Drawable loadNewImage(String url) {
		Drawable d = loadImageFromWebOperations(url);
		while(d == null) {
			url = randomURL();
			d = loadImageFromWebOperations(url);
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

		return d;
	}

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

	private class DownloadImageTask extends AsyncTask<Void, Void, Void> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() */
		protected Void doInBackground(Void... arg0) {
			try {
				Log.d("fatpita", "* Loading now!");
				mHandler.post(new Runnable() {

					public void run() {
						iv.setImageDrawable(loadNewImage(appState.getURL()));
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

	private Drawable loadImageFromWebOperations(String url) 
	{ 
		try { 
			InputStream is = (InputStream) new URL(url).getContent(); 
			Drawable d = Drawable.createFromStream(is, "src name"); 

			Bitmap b = ((BitmapDrawable)d ).getBitmap();
			Log.d("fatpita", "loading image "+b.getWidth()+"x"+b.getHeight());
			// Ignore large images
			if(b.getWidth()>1000 || b.getHeight()>1000) {
				return null; 
			}
			return d; 
		}
		catch (Exception e) { 
			Log.d("fatpita", "exception = "+e.getMessage());
			return null; 
		} 
	}
}