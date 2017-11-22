package com.example.liane.login;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

/*
	*Manipulates the square that lays overtop of the preview
 */

public class FaceOverlay extends View{

	private final Object lock = new Object();
	private int previewWidth;
	private float widthScaleFactor = 1.0f;
	private int previewHeight;
	private float heightScaleFactor = 1.0f;
	private int front = CameraSource.CAMERA_FACING_FRONT;
	private Set<Graphic> graphics = new HashSet<>();

	public FaceOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//Adds square to Preview
	public void add(Graphic graphic) {
		synchronized (lock) {
			graphics.add(graphic);
		}
		postInvalidate();
	}

	//Removes square from Preview
	public void remove(Graphic graphic) {
		synchronized (lock) {
			graphics.remove(graphic);
		}
		postInvalidate();
	}

	//Sets Width, Height, and Camera Facing Attributes
	public void setCameraInfo(int prevWidth, int prevHeight, int facing) {
		synchronized (lock) {
			this.previewWidth = prevWidth;
			this.previewHeight = prevHeight;
			front = facing;
		}
		postInvalidate();
	}

	//Draws Square on Preview
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		synchronized (lock) {
			if ((previewWidth != 0) && (previewHeight != 0)) {
				widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
				heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
			}

			for (Graphic graphic : graphics) {
				graphic.draw(canvas);
			}
		}
	}

	public static abstract class Graphic {
		private FaceOverlay mOverlay;

		Graphic(FaceOverlay overlay) {

			mOverlay = overlay;
		}

		public abstract void draw(Canvas canvas);

		//Converts horizontal supplied value to view value
		float scaleX(float horizontal) {

			return horizontal * mOverlay.widthScaleFactor;
		}

		//Converts vertical supplied value to view value
		float scaleY(float vertical) {

			return vertical * mOverlay.heightScaleFactor;
		}

		//Converts Preview X value to View X value
		float translateX(float x) {
			if (mOverlay.front == CameraSource.CAMERA_FACING_FRONT) {
				return mOverlay.getWidth() - scaleX(x);
			} else {
				return scaleX(x);
			}
		}

		//Converts Preview Y value to View Y value
		float translateY(float y) {

			return scaleY(y);
		}

		void postInvalidate() {

			mOverlay.postInvalidate();
		}
	}
}
