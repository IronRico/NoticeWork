package com.example.liane.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

/*
	* Sets up preview window.
 */

public class Preview extends ViewGroup {

	private SurfaceView nSurfaceView;
	private boolean isCamRequested = false;
	private boolean isSurfaceAvailable = false;
	private CameraSource camera;
	private FaceOverlay graphic;


	public Preview(Context context, AttributeSet att){
		super(context, att);

		nSurfaceView = new SurfaceView(context);
		nSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				isSurfaceAvailable = true;
				try {
					startPreview();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				isSurfaceAvailable = false;
			}
		});

		addView(nSurfaceView);

	}

	//Starts camera if it's available and starts the preview
	public void start(CameraSource cam) throws IOException {

		if(cam == null){
			stop();
		}

		camera = cam;

		if(camera != null){
			isCamRequested = true;
			startPreview();
		}
	}

	//Assigns the Graphic from the Login Class and calls the start method to start preview
	public void startRequest (CameraSource cam, FaceOverlay overlay) throws IOException{
		graphic = overlay;
		start(cam);
	}

	//Starts the Preview
	@SuppressLint("MissingPermission")
	private void startPreview() throws IOException {
		if(isSurfaceAvailable && isCamRequested) {

			camera.start(nSurfaceView.getHolder());

			if(graphic != null) {
				Size size = camera.getPreviewSize();
				int min = Math.min(size.getWidth(), size.getHeight());
				int max = Math.max(size.getWidth(), size.getHeight());

				graphic.setCameraInfo(min, max, camera.getCameraFacing());
			}

		}
	}

	//Sets the layout of the Preview
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

		int width = 320;
		int height = 240;

		if (camera != null) {
			Size size = camera.getPreviewSize();
			if (size != null) {
				//Preview is flipped 90 degrees. Default view is in Landscape, app is in portrait.
				width = size.getHeight();
				height = size.getWidth();
			}
		}

		final int layoutWidth = right - left;
		final int layoutHeight = bottom - top;

		// Computes height and width for potentially doing fit width.
		int childWidth = layoutWidth;
		int childHeight = (int)(((float) layoutWidth / (float) width) * height);

		// If height is too tall using fit width, does fit height instead.
		if (childHeight > layoutHeight) {
			childHeight = layoutHeight;
			childWidth = (int)(((float) layoutHeight / (float) height) * width);
		}

		for (int i = 0; i < getChildCount(); ++i) {
			getChildAt(i).layout(0, 0, childWidth, childHeight);
		}

		try {
			startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Stops the Camera
	public void stop(){
		if(camera != null){
			camera.stop();
		}
	}
}
