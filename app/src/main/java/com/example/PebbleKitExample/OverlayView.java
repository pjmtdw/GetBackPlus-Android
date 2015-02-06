package com.example.PebbleKitExample;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.view.View;

public class OverlayView extends View {
  private int offsetY;

  public void setOffsetY(int y){
    offsetY = y;
  }

  public OverlayView(Context c,AttributeSet a){
    super(c,a);
    offsetY = 0;
  }
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float height = (float)canvas.getHeight();
    float width = (float)canvas.getWidth();
    Paint paint = new Paint();
    paint.setARGB(160, 36, 36, 36);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(4.5f);
    paint.setPathEffect(new DashPathEffect(new float[]{4.5f, 4.5f}, 0));
    Path path = new Path();
    path.moveTo(width/4,offsetY+height/2);
    path.lineTo(width*0.75f,offsetY+height/2);
    path.moveTo(width/2,offsetY+height/4);
    path.lineTo(width/2,offsetY+height*0.75f);
    canvas.drawPath(path,paint);
  }
}
