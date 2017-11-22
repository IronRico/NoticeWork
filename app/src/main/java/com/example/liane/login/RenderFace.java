package com.example.liane.login;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;

/*
	*Class detects face and draws a square around the face.
*/

public class RenderFace extends FaceOverlay.Graphic{

	private static final float BOX_STROKE_WIDTH = 5.0f;

	private Paint square;

	private volatile Face face;

	RenderFace(FaceOverlay overlay) {
		super(overlay);

		square = new Paint();
		square.setColor(Color.parseColor("#0000ff"));
		square.setStyle(Paint.Style.STROKE);
		square.setStrokeWidth(BOX_STROKE_WIDTH);
	}

	//Updates face position
	void updateFace(Face face) {
		this.face = face;
		postInvalidate();
	}

	//Draws square around face
	@Override
	public void draw(Canvas canvas) {
		Face face = this.face;
		if (face == null) {
			return;
		}

		//Center between eyes
		float x = translateX(face.getPosition().x + face.getWidth() / 2);
		float y = translateY(face.getPosition().y + face.getHeight() / 2);

		// Draws square around the face
		float xOffset = scaleX(face.getWidth() / 2.0f);
		float yOffset = scaleY(face.getHeight() / 2.0f);
		float top = y - yOffset;
		float bottom = y + yOffset;
		float left = x - xOffset;
		float right = x + xOffset;

		canvas.drawRect(left, top, right, bottom, square);
	}
}
