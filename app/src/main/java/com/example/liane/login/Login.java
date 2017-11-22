package com.example.liane.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/*
	*Main activity of the login screen
*/
public class Login extends AppCompatActivity {

	private CameraSource camera;
	private Preview nPreview;
	private FaceOverlay nFaceOverlay;
	private static final int PLAY_SERVICES = 9001;
	private static final int CAMERA_PERM = 1;
	private static final int STORAGE_PERM = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		nPreview = findViewById(R.id.preview);
		nFaceOverlay = findViewById(R.id.faceBox);

		//Check Camera Permission & Storage permission
		int reqcam = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);


		if (reqcam == PackageManager.PERMISSION_GRANTED) {
			createCamera();
		} else {

			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM);
		}

		final Activity thisActivity = this;

		//Listener for Submit Button
		Button submitButton = findViewById(R.id.submit);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int reqStorage = ActivityCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
				if(reqStorage == PackageManager.PERMISSION_GRANTED){
					camera.takePicture(null, jpeg);
				}
				else{
					ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERM);
				}

			}
		});

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case CAMERA_PERM: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					createCamera();

				} else {

					Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
				}
				return;
			}
			case STORAGE_PERM: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					createCamera();

				} else {

					Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}



	//Restarts Camera
	@Override
	protected void onResume() {
		super.onResume();
		startCamera();
	}

	//Stops the preview
	@Override
	protected void onPause(){
		super.onPause();
		nPreview.stop();
	}

	//Destroys resources when closed and releases the camera
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(camera != null){
			camera.release();
		}
	}

	//Creates the Front Facing Camera
	private void createCamera(){

		Context appContext = getApplicationContext();
		com.google.android.gms.vision.face.FaceDetector detector = new FaceDetector.Builder(appContext)
				.setClassificationType(com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS)
				.build();

		detector.setProcessor(new MultiProcessor.Builder<>(new FaceTracker()).build());

		camera = new CameraSource.Builder(appContext, detector)
				.setRequestedPreviewSize(650,480)
				.setFacing(CameraSource.CAMERA_FACING_FRONT)
				.setRequestedFps(30.0f)
				.build();
	}

	//Starts the Camera for the Preview
	private void startCamera(){

		int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
		if (code != ConnectionResult.SUCCESS) {
			Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, PLAY_SERVICES);
			dlg.show();
		}

		if(camera != null){
			try{
				nPreview.startRequest(camera, nFaceOverlay);
			} catch (IOException e) {
				camera.release();
				camera = null;
			}
		}
	}

	//Create multiple face trackers for each detected face
	private class FaceTracker implements  MultiProcessor.Factory<Face> {
		@Override
		public Tracker<Face> create (Face face){

			return new GraphicTracker(nFaceOverlay);
		}
	}

	//Maintains graphic for each detected face
	private class GraphicTracker extends Tracker<Face> {

		private FaceOverlay graphic;
		private RenderFace rFace;

		GraphicTracker(FaceOverlay overlay){
			graphic = overlay;
			rFace = new RenderFace(overlay);
		}

		//starts tracking new face
		@Override
		public  void onNewItem(int faceId, Face item){

			graphic.setId(faceId);
		}

		//Updates Position
		@Override
		public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
			graphic.add(rFace);
			rFace.updateFace(face);
		}

		//Hide graphic when face is temporarily missing from preview
		@Override
		public void onMissing(FaceDetector.Detections<Face> detectionResults) {

			graphic.remove(rFace);
		}

		//Remove graphic when face permanently removed from preview
		@Override
		public  void onDone(){

			graphic.remove(rFace);
		}

	}

	//PictureCallBack for takePicture method
	CameraSource.PictureCallback jpeg = new CameraSource.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data) {
			new saveImage().execute(data);
			startCamera();
		}
	};

	@SuppressLint("StaticFieldLeak")
	private class saveImage extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... data) {
			FileOutputStream outStream;

			try {
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File (sdCard.getAbsolutePath() + "/login");
				dir.mkdir();

				@SuppressLint("DefaultLocale")
				String fileName = String.format("%d.jpg", System.currentTimeMillis());
				File outFile = new File(dir, fileName);

				outStream = new FileOutputStream(outFile);
				outStream.write(data[0]);
				outStream.flush();
				outStream.close();

				refreshGallery(outFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		sendBroadcast(mediaScanIntent);
	}
}
