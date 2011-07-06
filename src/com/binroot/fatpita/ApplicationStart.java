package com.binroot.fatpita;

import java.util.ArrayList;

import android.app.Application;

public class ApplicationStart extends Application {
	
	private String currentURL;
	private ArrayList<String> history;
	private int backCursor = 0;
	private ArrayList<String> favList;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		history = new ArrayList<String>();
		favList = new ArrayList<String>();
	}

	public String getURL(){
		return currentURL;
	}
	public void setURL(String s){
		currentURL = s;
	}
	
	public ArrayList<String> getHistory() {
		return history;
	}
	
	public void setHistory(ArrayList<String> history) {
		this.history = history;
	}
	
	public int getBackCursor() {
		return backCursor;
	}
	
	public void setBackCursor(int backCursor) {
		this.backCursor = backCursor;
	}
	
	public ArrayList<String> getFavList() {
		return favList;
	}
	
	public void addFav( String url ) {
		favList.add(url);
	}
	
	public void removeFav( String url ) {
		favList.remove(url);
	}
}