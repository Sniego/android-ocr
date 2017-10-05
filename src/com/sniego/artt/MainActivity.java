package com.sniego.artt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.memetix.mst.detect.Detect;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	Language[] languages = Language.values();
	
	Spinner jezyk1,jezyk2 ;
	Button ok ,ocr,artt;
	TextView jezyk11,jezyk22,tekstJ11,tekstJ22;
	EditText tekst;
	ProgressBar pas;
	String wykryty = "";
	
	public static final String sciezkadanych = Environment.getExternalStorageDirectory().toString() + "/ARTT/";
	public static final String TAG = "ARTT.java";
	
	protected String sciezka;
	protected boolean zrobione;
	static String zrobinezdiecie = "zrobione_zdiecie";
	public int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
	public static final String jezyk = "eng"; 
	
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		String[] sciezki = new String[] { sciezkadanych, sciezkadanych + "tessdata/" };

		for (String sciezka : sciezki) {
			File dir = new File(sciezka);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG,"Blad: Operacja utworzenia plikow w " + sciezka + " na karcie pamieci niepowidla sie");
					return;
				} else {
					Log.v(TAG, "Utworzono pliki " + sciezka + " na karcie pamieci");
				}
			}
		}
		
		
		if (!(new File(sciezkadanych + "tessdata/" + jezyk + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + jezyk + ".traineddata");
				OutputStream out = new FileOutputStream(sciezkadanych	+ "tessdata/" + jezyk + ".traineddata");

				byte[] buf = new byte[1024];
				int len;
				
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				
				out.close();
				
				Log.v(TAG, "Skopiowano " + jezyk + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Blad kopiowania " + jezyk + " traineddata " + e.toString());
			}
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		jezyk1 = (Spinner) findViewById(R.id.jezyk1);
		jezyk2 = (Spinner) findViewById(R.id.jezyk2);
		
		jezyk1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, jezyki()));
		jezyk2.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, jezyki()));
		jezyk2.setSelection(1);

		ok = (Button) findViewById(R.id.ok);
		ocr = (Button) findViewById(R.id.ocr);

			
		jezyk11 = (TextView) findViewById(R.id.jezyk11);
		tekstJ11 = (TextView) findViewById(R.id.tekstJ11);
		jezyk22 = (TextView) findViewById(R.id.jezyk22);
		tekstJ22 = (TextView) findViewById(R.id.tekstJ22);
		
		tekst = (EditText) findViewById(R.id.tekst);
		
		jezyk11.setVisibility(TextView.INVISIBLE);
		tekstJ11.setVisibility(TextView.INVISIBLE);
		jezyk22.setVisibility(TextView.INVISIBLE);
		tekstJ22.setVisibility(TextView.INVISIBLE);
		
		pas = (ProgressBar) findViewById(R.id.pas);
		pas.setVisibility(ProgressBar.INVISIBLE);

		sciezka = sciezkadanych + "/ocr.jpg";
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
					class translator extends AsyncTask<Void, Void, Void>{
						
						String tlumaczeniebing = "";
				
						@Override
						protected void onPreExecute() {
							
							pas.setVisibility(ProgressBar.VISIBLE);
							super.onPreExecute();
						}

						@Override
						protected Void doInBackground(Void... params) {
							
							try {
								tlumaczeniebing = tlumaczeniebing();
							} catch (Exception e) {
								
								tlumaczeniebing = "blad translacji (sprawdz internet)";
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							
							jezyk11.setVisibility(TextView.VISIBLE);
							tekstJ11.setVisibility(TextView.VISIBLE);
							jezyk22.setVisibility(TextView.VISIBLE);
							tekstJ22.setVisibility(TextView.VISIBLE);							
							
							tekstJ11.setText(tekst.getText().toString());
							tekstJ22.setText(tlumaczeniebing);
							
							jezyk11.setText(wykryty);
							jezyk22.setText(languages[jezyk2.getSelectedItemPosition()].name());
							pas.setVisibility(ProgressBar.INVISIBLE);
							super.onPostExecute(result);
						}						
					}
					new translator().execute();
			}
		});
		
		ocr.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				kamera();			
			}
		});	
		
			
		Locale loc = new Locale("en");
		Log.i("-------------",Arrays.toString(loc.getAvailableLocales()));
	}
		
	public String[] jezyki(){
		String jezyk[] = new String[languages.length];
		for(int i = 0; i < languages.length; i++){
			jezyk[i] = languages[i].name();
		}
		return jezyk;
	}
	
	public String tlumaczeniebing() throws Exception{
		
	       Translate.setClientId("ARTT");
	       Translate.setClientSecret("6d1Fo7Fqyzxyp62TS94akXhQ9EarZm/uwOk2I28+J1o="); 
	       
	       String tlumaczeniebing = Translate.execute(tekst.getText().toString(),languages[jezyk1.getSelectedItemPosition()], languages[jezyk2.getSelectedItemPosition()]);
		   
	       Language detectedLanguage = Detect.execute(tekst.getText().toString());
	       this.wykryty = detectedLanguage.getName(Language.ENGLISH);
	       
		return tlumaczeniebing;
	}
	
	protected void kamera() {
		File plik = new File(sciezka);
		Uri outputFileUri = Uri.fromFile(plik);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "kodwyniku: " + resultCode);

		if (resultCode == -1) {
			onPhotoTaken();
		} else {
			Log.v(TAG, "Urzytkownik anulowal");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(MainActivity.zrobinezdiecie,  zrobione);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(MainActivity.zrobinezdiecie)) {
			onPhotoTaken();
		}
	}
	
	protected void onPhotoTaken() {
		zrobione = true;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(sciezka, options);

		try {
			ExifInterface exif = new ExifInterface(sciezka);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orientacja: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotacja: " + rotate);

			if (rotate != 0) {		
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();			
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);			
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}	
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);			
		} catch (IOException e) {
			Log.e(TAG, "Blad orientacji: " + e.toString());
		}
	
		Log.v(TAG, "Przed baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(sciezkadanych, jezyk, ocrEngineMode);
		baseApi.setImage(bitmap);		
		String recognizedText = baseApi.getUTF8Text();		
		baseApi.end();
		Log.v(TAG, "Przetworzony tekst: " + recognizedText);

		if ( jezyk.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		recognizedText = recognizedText.trim();

		if ( recognizedText.length() != 0 ) {
			tekst.setText(tekst.getText().toString().length() == 0 ? recognizedText : tekst.getText() + " " + recognizedText);
			tekst.setSelection(tekst.getText().toString().length());
		}
			
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
