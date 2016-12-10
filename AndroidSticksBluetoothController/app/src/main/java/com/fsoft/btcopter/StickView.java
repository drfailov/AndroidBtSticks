package com.fsoft.btcopter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Dr. Failov on 09.12.2016.
 */
public class StickView extends View {
    float positionV = 0.5f;
    float positionH = 0.5f;
    float fingerDownPosX = -1; //left = 0  right = 1
    float fingerDownPosY = -1; //top = 1   bottom = 0
    Paint paint = null;

    public StickView(Context context) {
        super(context);
    }

    public StickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(paint == null){
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(3);
            paint.setTextSize(20);
        }
        int h = getHeight();
        int w = getWidth();
        float cx = w*positionH;
        if(fingerDownPosX != -1)
            cx = fingerDownPosX + cx-w/2;
        float cy = h*(1-positionV);
        if(fingerDownPosY != -1)
            cy = fingerDownPosY + cy-h/2;
        float rad = ((float)h)*0.1f;


        //----draw colors
        if(fingerDownPosX != -1) {
            if (positionV > 0.5) {
                paint.setColor(Color.argb(150, 255, 0, 0));//red
                canvas.drawRect(0, cy, w - 1, fingerDownPosY, paint);
            } else {
                paint.setColor(Color.argb(150, 0, 0, 255));//blue
                canvas.drawRect(0, fingerDownPosY, w - 1, cy, paint);
            }
            if (positionH > 0.5) {
                paint.setColor(Color.argb(150, 255, 255, 0));//cyan
                canvas.drawRect(fingerDownPosX, 0, cx, h - 1, paint);
            } else {
                paint.setColor(Color.argb(150, 0, 255, 255));//yellow
                canvas.drawRect(cx, 0, fingerDownPosX, h - 1, paint);
            }
        }

        //draw white
        paint.setColor(Color.WHITE);
        if(fingerDownPosX != -1) {
            canvas.drawRect(
                    Math.min(fingerDownPosX, cx),
                    Math.min(fingerDownPosY, cy),
                    Math.max(fingerDownPosX, cx),
                    Math.max(fingerDownPosY, cy),
                    paint
            );
            canvas.drawCircle(fingerDownPosX, fingerDownPosY, rad / 3f, paint);
        }
        canvas.drawCircle(cx, cy, rad, paint);
        canvas.drawLine(0, cy, w-1, cy, paint);
        canvas.drawLine(cx, 0, cx, h-1, paint);
        canvas.drawText("H = " + positionH, 10, 25, paint);
        canvas.drawText("V = " + positionV, 10, 50, paint);


        paint.setColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            fingerDownPosX = event.getX();
            fingerDownPosY = event.getY();
            positionH = 0.5f;
            positionV = 0.5f;
        }
        if(action == MotionEvent.ACTION_MOVE){
            float dx = event.getX()-fingerDownPosX;
            float dy = event.getY()-fingerDownPosY;
            float h = getHeight();
            float w = getWidth();
            positionH = dx/w + 0.5f;
            if(positionH < 0) positionH = 0;
            if(positionH > 1) positionH = 1;
            positionV = - (dy/h - 0.5f);
            if(positionV < 0) positionV = 0;
            if(positionV > 1) positionV = 1;
        }
        if(action == MotionEvent.ACTION_UP){
            fingerDownPosX = -1;
            fingerDownPosY = -1;
            positionH = 0.5f;
            positionV = 0.5f;
        }
        invalidate();
        return true;
    }
}
