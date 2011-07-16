package com.binroot.fatpita;

import android.util.Log;

public abstract class URLFinder {
	
	public static String randomURL() {
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
	
	public static String loadURLFrom(String website) {
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
}
