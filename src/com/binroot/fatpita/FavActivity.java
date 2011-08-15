package com.binroot.fatpita;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * A grid that displays a set of framed photos.
 *
 */
public class FavActivity extends Activity {

	ArrayList<String> urlList;
	Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.gridfav);
		urlList = new ArrayList<String>();
		final String []urls = getIntent().getExtras().getStringArray("sites");
		Collections.addAll(urlList, urls);

		Log.d("fatpita", "urlList: "+urlList+", "+urlList.size()+", "+urlList.get(0).length());

		if(urlList.get(0).length()<=0) {
			Log.d("fatpita", "toasting");
			Toast.makeText(this, "You have no favorites.", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Go back and click the star to add some!", Toast.LENGTH_LONG).show();
		}

		GridView g = (GridView) findViewById(R.id.myGrid);
		final ImageAdapter ia = new ImageAdapter(this);
		g.setAdapter(ia);

		g.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.d("fatpita", "clicked: "+arg2+", "+ urlList.get(arg2));
				Intent favIntent = new Intent();
				favIntent.putExtra("url", urlList.get(arg2));
				setResult(100, favIntent);
				finish();
			}
		});
		
		g.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				Log.d("fatpita", "long pressed: "+arg2+", "+ urlList.get(arg2));
				// TODO: remove url from fav
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Remove image from favorites?")
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   String urlToRemove = urlList.get(arg2);
								urlList.remove(urlToRemove);
								
								String allUrls = "";
								for(int i=0; i<urlList.size(); i++) {
									allUrls += urlList.get(i)+" ";
								}
								allUrls.replace(urlToRemove, "");
								
								FileOutputStream fos = null;
								try {
									//Log.d("fatpita", "Opening file favList");
									fos = openFileOutput("favList", Context.MODE_WORLD_READABLE);
									fos.write(allUrls.getBytes());
									fos.close();
								} catch (FileNotFoundException e) {Log.d("fatpita", "Could not find file favList");} 
								catch (IOException e) {Log.d("fatpita", "Could not close file favList");}
							
								ia.notifyDataSetChanged();
								
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
		});
		
		Button backButton = (Button) findViewById(R.id.button_back_grid);
		backButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}

	Handler mHandler = new Handler();

	public class ImageAdapter extends BaseAdapter  
	{  
	    Context context;  

	    public ImageAdapter (Context c)  {  
	        context = c;  
	    }  
	  
	    public int getCount() {  
	        return urlList.size();
	    }

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return urlList.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;  
			  
	        if (convertView == null) {  // if it's not recycled, initialize some attributes  
	            imageView = new ImageView (context);  
	            imageView.setScaleType (ImageView.ScaleType.CENTER_CROP);
	            imageView.setLayoutParams(new GridView.LayoutParams(140, 140));
				imageView.setAdjustViewBounds(false);
	        } else {  
	            imageView = (ImageView)convertView;  
	        }  
	        
	        
	        BitmapManager2 bm = new BitmapManager2();
	        String thumbIds [] = new String[urlList.size()];
	        thumbIds = urlList.toArray(thumbIds);
	        
	        bm.fetchBitmapOnThread(thumbIds[position], imageView, 6);

	        return imageView; 
		}  
	  
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