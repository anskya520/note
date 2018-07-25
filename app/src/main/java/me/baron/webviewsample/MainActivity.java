package me.baron.webviewsample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private ValueCallback<Uri> uploadMessage;
	private ValueCallback<Uri[]> uploadMessageAboveL;
	private final static int FILE_CHOOSER_RESULT_CODE = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		WebView webview = (WebView) findViewById(R.id.web_view);
		assert webview != null;
		WebSettings settings = webview.getSettings();
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		webview.setWebChromeClient(new WebChromeClient() {

			// For Android < 3.0
			public void openFileChooser(ValueCallback<Uri> valueCallback) {
				uploadMessage = valueCallback;
				openImageChooserActivity();
			}

			// For Android  >= 3.0
			public void openFileChooser(ValueCallback valueCallback, String acceptType) {
				uploadMessage = valueCallback;
				openImageChooserActivity();
			}

			//For Android  >= 4.1
			public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
				uploadMessage = valueCallback;
				openImageChooserActivity();
			}

			// For Android >= 5.0
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
				uploadMessageAboveL = filePathCallback;
				openImageChooserActivity();
				return true;
			}
		});
		String targetUrl = "file:///android_asset/a.html";
		webview.loadUrl(targetUrl);
	}

	private Uri imageUri;
	File file;
	private void openImageChooserActivity() {
/*		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("image/*");
		startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);*/

		String filePath = Environment.getExternalStorageDirectory() + File.separator
				+ Environment.DIRECTORY_PICTURES + File.separator;
		String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
		file = new File(filePath + fileName);
		imageUri = Uri.fromFile(file);

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        Intent Photo = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILE_CHOOSER_RESULT_CODE) {
			updatePhotos();
			if (null == uploadMessage && null == uploadMessageAboveL) return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			if (uploadMessageAboveL != null) {
				onActivityResultAboveL(requestCode, resultCode, data);
			} else if (uploadMessage != null) {
				Log.e("result", result + "");
				if (result == null) {
//	            		mUploadMessage.onReceiveValue(imageUri);
					uploadMessage.onReceiveValue(imageUri);
					uploadMessage = null;

					Log.e("imageUri", imageUri + "");
				} else {
					uploadMessage.onReceiveValue(result);
					uploadMessage = null;
				}


			}
		}
	}


	private void updatePhotos() {
		// 该广播即使多发（即选取照片成功时也发送）也没有关系，只是唤醒系统刷新媒体文件
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(imageUri);
		sendBroadcast(intent);
	}


	@SuppressWarnings("null")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
		if (requestCode != FILE_CHOOSER_RESULT_CODE
				|| uploadMessageAboveL == null) {
			return;
		}

		Uri[] results = null;
		if (resultCode == Activity.RESULT_OK) {
			if (data == null) {
				results = new Uri[]{imageUri};
			} else {
				String dataString = data.getDataString();
				ClipData clipData = data.getClipData();

				if (clipData != null) {
					results = new Uri[clipData.getItemCount()];
					for (int i = 0; i < clipData.getItemCount(); i++) {
						ClipData.Item item = clipData.getItemAt(i);
						results[i] = item.getUri();
					}
				}

				if (dataString != null)
					results = new Uri[]{Uri.parse(dataString)};
			}
		}
		if (results != null) {
			uploadMessageAboveL.onReceiveValue(results);
			uploadMessageAboveL = null;
		} else {
			results = new Uri[]{imageUri};
			uploadMessageAboveL.onReceiveValue(results);
			uploadMessageAboveL = null;
		}

		return;
	}
}
