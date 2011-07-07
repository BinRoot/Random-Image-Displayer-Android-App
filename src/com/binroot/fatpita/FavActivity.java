package com.binroot.fatpita;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A grid that displays a set of framed photos.
 *
 */
public class FavActivity extends Activity {

	ArrayList<String> urlList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridfav);
		urlList = new ArrayList<String>();
		String []urls = getIntent().getExtras().getStringArray("sites");
		Collections.addAll(urlList, urls);

		Log.d("fatpita", "urlList: "+urlList+", "+urlList.size()+", "+urlList.get(0).length());

		if(urlList.get(0).length()<=0) {
			Log.d("fatpita", "toasting");
			Toast.makeText(this, "You have no favorites.", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Go back and click the star to add some!", Toast.LENGTH_LONG).show();
		}

		GridView g = (GridView) findViewById(R.id.myGrid);
		g.setAdapter(new ImageAdapter(this));

		g.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.d("fatpita", "clicked: "+arg2+", "+ urlList.get(arg2));
				Intent favIntent = new Intent();
				favIntent.putExtra("url", urlList.get(arg2));
				setResult(100, favIntent);
				finish();
			}
		});
	}

	Handler mHandler = new Handler();

	public class ImageAdapter extends BaseAdapter {
		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return urlList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}
		ImageView imageView;
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(140, 140));
				imageView.setAdjustViewBounds(false);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			} else {
				imageView = (ImageView) convertView;
			}

			//imageView.setImageDrawable(loadImageFromWebOperations("http://media.smashingmagazine.com/images/rss/rss_icon.jpg"));

			DownloadImagesTask task1 = new DownloadImagesTask();
			task1.setImageView(imageView);
			task1.execute(urlList.get(position));

			return imageView;
		}


		class DownloadImagesTask extends AsyncTask<String, Integer, Bitmap> {

			private int imageViewID;
			private ImageView iv;

			protected void onPostExecute(Bitmap bitmap1) {
				iv.setImageBitmap(bitmap1);
			}

			public void setImageView(ImageView iv) {
				this.iv = iv;
			}

			protected Bitmap doInBackground(String... url) {
				Bitmap bitmap1 = loadImageFromWebOperations(url[0]);
				return bitmap1;
			}
		}

		private Bitmap loadImageFromWebOperations(String url) { 
			try { 
				InputStream is = (InputStream) new URL(url).getContent(); 
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 10;
				Bitmap preview_bitmap=BitmapFactory.decodeStream(is,null,options);

				return preview_bitmap; 
			}
			catch (Exception e) { 
				Log.d("fatpita", "exception = "+e.getMessage());
				return null; 
			} 
		}

		private Context mContext;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add("Clear All");

		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Clear All")) {
			setResult(101);
			finish();
		}
		return true;
	}
}