package jp.ac.titech.itpro.sdl.trackballemulator;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by onuki on 2017/07/07.
 */

/* android.hardware.Camera を取得できるようにした */
public class MyJavaCameraView extends JavaCameraView {
    public MyJavaCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public MyJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera getCamera() {
        return mCamera;
    }
}
