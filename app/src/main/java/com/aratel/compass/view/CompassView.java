package com.aratel.compass.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aratel.compass.R;

public class CompassView extends View {

    private float mBearing;

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String eastString;
    private String southString;
    private String westString;
    private int textHeight;


    public CompassView(Context context) {
        super(context);


    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // *** if we want to set styles use this constructor >>> we want to set the attribute which defined in values/attrs.xml
    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // to allow a user using a D-pad to select and focus the compass (this will allow them to receive accessibility events from the View
        setFocusable(true);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompassView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.CompassView_bearing)) {
            setmBearing(a.getFloat(R.styleable.CompassView_bearing, 0));
        }
        // You must always call recycle when you are done reading values from the TypedArray.
        a.recycle();

        Context c = this.getContext();
        Resources r = this.getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(ContextCompat.getColor(c, R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        northString = r.getString(R.string.cardinal_north);
        eastString = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(c, R.color.text_color));

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(ContextCompat.getColor(c, R.color.marker_color));

    }


    // to calculate the length of the shortest side
    // use setMeasuredDimension to set the height and width
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // the compass is a circle that fills as mush as possible
        // set the measured dimensions by figuring out the shortest boundary
        // height or width
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    // draw the compass face using the String and Paint objects you created
    @Override
    protected void onDraw(Canvas canvas) {

        //1- Find the center of the control, and store the length of the smallest side as the Compass’s radius
        int mMeasureWidth = getMeasuredWidth();
        int mMeasureHeight = getMeasuredHeight();

        int px = mMeasureWidth / 2;
        int py = mMeasureHeight / 2;

        int radius = Math.min(px, py);

        //2- Draw the outer boundary, and color the background of the Compass face using the drawCircle method
        // draw the background
        canvas.drawCircle(px, py, radius, circlePaint);

        //3- This Compass displays the current heading by rotating the face so that the current direction is always at the top of the device
        // To achieve this, rotate the canvas in the opposite direction to the current heading
        // rotate our perspective so that the top is
        // facing the current bearing
        canvas.save();
        canvas.rotate(-mBearing, px, py);

        //4- Rotate the canvas through a full rotation, drawing markings every 15 degrees and the abbreviated direction string every 45 degrees
        int textWidth = (int) textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - radius + textHeight;

        // Draw the marker every 15 degrees and test every 45
        for (int i = 0; i < 24; i++) {
            //Draw a marker
            canvas.drawLine(px, py - radius, px, py - radius + 10, markerPaint);

            canvas.save();
            canvas.translate(0, textHeight);

            //Draw the cardinal points
            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case (0): {
                        dirString = northString;
                        int arrowY = 2 * textHeight;
                        canvas.drawLine(px, arrowY, px - 5, 3 * textHeight, markerPaint);
                        canvas.drawLine(px, arrowY, px + 5, 3 * textHeight, markerPaint);

                        break;
                    }
                    case (6):
                        dirString = eastString;
                        break;
                    case (12):
                        dirString = southString;
                        break;
                    case (18):
                        dirString = westString;
                        break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
                // Draw the test every alternate 45deg
                String angle = String.valueOf(i * 15);
                float angleTextWidth = textPaint.measureText(angle);

                int angleTextX = (int) (px - angleTextWidth / 2);
                int angleTextY = py - radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();
            canvas.rotate(15, px, py);
        }
        canvas.restore();
    }

    // to use the current heading as the content value to be used for accessibility events
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        if (isShown()) {
            String bearingStr = String.valueOf(mBearing);
            event.getText().add(bearingStr);
            return true;
        } else
            return false;
    }

    private int measure(int measureSpec) {
        int result = 0;

        //first decode the measurement specifications
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    /**
     * Call invalidate in the set method to ensure that the View is repainted when the bearing changes
     */
    public void setmBearing(float bearing) {
        mBearing = bearing;
        invalidate();

        // add accessibility support
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getmBearing() {
        return mBearing;
    }



}
