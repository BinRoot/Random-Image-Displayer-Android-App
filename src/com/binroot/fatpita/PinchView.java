package com.binroot.fatpita;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

public class PinchView extends WebView {

    Context context;
    GestureDetector gd;

    public PinchView(Context context) {
        super(context);
        this.context = context;
        gd = new GestureDetector(context, sogl);
    }

    public PinchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        gd = new GestureDetector(context, sogl);

    }

    public PinchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        gd = new GestureDetector(context, sogl);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gd.onTouchEvent(event);
        return super.onTouchEvent(event);

    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        gd.setOnDoubleTapListener(onDoubleTapListener);
    }

    private GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener();

}
