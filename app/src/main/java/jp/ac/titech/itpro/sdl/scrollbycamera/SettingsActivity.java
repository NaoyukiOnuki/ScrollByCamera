package jp.ac.titech.itpro.sdl.scrollbycamera;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by onuki on 2017/06/20.
 */

public class SettingsActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    String TAG = "SettingsActivity";

    WebView webView;
    int scrollVertical = 0;
    int scrollHorizontal = 0;
    float alpha, threshold, scale;

    boolean buttonPressed = false;
    double[] fingerColor = new double[3];
    TextView textView;

    CameraBridgeViewBase mCameraView;

    // カメラ画像
    private Mat image, image_small;

    // オプティカルフロー用
    private Mat image_prev, image_next;
    private MatOfPoint2f pts_prev, pts_next;

    // OpenCVライブラリのロード
    private BaseLoaderCallback mCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    pts_prev = new MatOfPoint2f();
                    pts_next = new MatOfPoint2f();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.alpha, outValue, true);
        alpha = outValue.getFloat();
        getResources().getValue(R.dimen.threshold, outValue, true);
        threshold = outValue.getFloat();
        getResources().getValue(R.dimen.scale, outValue, true);
        scale = outValue.getFloat();
        Log.d(TAG, "alpha = " + alpha);


        textView = (TextView) findViewById(R.id.textView);

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        image = new Mat(height, width, CvType.CV_8UC3);
        image_small = new Mat(height/8, width/8, CvType.CV_8UC3);
        image_prev = new Mat(image_small.rows(), image_small.cols(), image_small.type());
        image_next = new Mat(image_small.rows(), image_small.cols(), image_small.type());
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //Mat mat = new Mat();
        //Imgproc.threshold(inputFrame.gray(), mat, 20.0, 255.0,
        //        Imgproc.THRESH_BINARY);
        //return mat;

        ArrayList<Double> verticalDiff = new ArrayList<>();
        ArrayList<Double> horizontalDiff = new ArrayList<>();

        // 縮小
        image = inputFrame.rgba();
        Imgproc.resize(image, image_small, image_small.size(), 0, 0, Imgproc.INTER_NEAREST);

        // グレースケール
        Mat gray = new Mat(image_small.rows(), image_small.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(image_small, gray, Imgproc.COLOR_RGB2GRAY);

        // 閾値
        //Imgproc.threshold(gray, gray, 20.0, 255.0,
        //        Imgproc.THRESH_BINARY);
        //Imgproc.threshold(inputFrame.gray(), image, 20.0, 255.0,
        //        Imgproc.THRESH_BINARY);

        int blackCount = 0;
        int whiteCount = 0;
        int redCount = 0;
        int greenCount = 0;
        int blueCount = 0;
        Size size = image.size();
        int rough = 100;
        for (int i = 0; i < size.height / rough; i++) {
            for (int j = 0; j < size.width / rough; j++) {
                double[] data = image.get(i * rough, j * rough);
                if (data[0] > 240 && data[1] > 240 && data[2] > 240) whiteCount++;
                else blackCount++;

                if (data[1] < 32 && data[2] < 32) redCount++;

                if (buttonPressed) {
                    fingerColor[0] += data[0];
                    fingerColor[1] += data[1];
                    fingerColor[2] += data[2];
                    Log.d(TAG, "color is " + data[0] + " " + data[1] + " " + data[2]);
                }
            }
        }

        if (buttonPressed) {
            fingerColor[0] /= (whiteCount + blackCount);
            fingerColor[1] /= (whiteCount + blackCount);
            fingerColor[2] /= (whiteCount + blackCount);
            Log.d(TAG, "finger is " + fingerColor[0] + " " + fingerColor[1] + " " + fingerColor[2]);
            buttonPressed = false;
        }

        //Log.d(TAG, "white / (white+black) = " +  whiteCount/(double)(whiteCount+blackCount));

        if (redCount < 0.5 * (whiteCount + blackCount) && whiteCount < 0.7 * (whiteCount + blackCount) ) {
            scrollVertical = (int) (alpha * scrollVertical);
            scrollHorizontal = (int) (alpha * scrollHorizontal);
        } else {

            // 特徴点抽出
            MatOfPoint features = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(gray, features, 10, 0.01, 10);

            // 特徴点が見つかった
            if (features.total() > 0) {
                // 過去のデータが存在する
                if (pts_prev.total() > 0) {
                    // 現在のデータ
                    gray.copyTo(image_next);
                    pts_next = new MatOfPoint2f(features.toArray());

                    // オプティカルフロー算出
                    MatOfByte status = new MatOfByte();
                    MatOfFloat err = new MatOfFloat();
                    Video.calcOpticalFlowPyrLK(image_prev, image_next, pts_prev, pts_next, status, err);

                    // 表示
                    long flow_num = status.total();
                    if (flow_num > 0) {
                        List<Byte> list_status = status.toList();
                        List<Point> list_features_prev = pts_prev.toList();
                        List<Point> list_features_next = pts_next.toList();
                        double scale_x = image.cols() / image_small.cols();
                        double scale_y = image.rows() / image_small.rows();
                        for (int i = 0; i < flow_num; i++) {
                            if (list_status.get(i) == 1) {
                                Point p1 = new Point();
                                p1.x = list_features_prev.get(i).x * scale_x;
                                p1.y = list_features_prev.get(i).y * scale_y;
                                //Imgproc.circle(image, p1, 3, new Scalar(255,0,0), -1, 8, 0 );
                                Point p2 = new Point();
                                p2.x = list_features_next.get(i).x * scale_x;
                                p2.y = list_features_next.get(i).y * scale_y;
                                //Imgproc.circle(image, p2, 3, new Scalar(255,255,0), -1, 8, 0 );

                                if (threshold < Math.hypot(p2.x - p1.x, p2.y - p1.y)) {
                                    horizontalDiff.add(p2.x - p1.x);
                                    verticalDiff.add(p2.y - p1.y);

                                    // フロー描画
                                    int thickness = 5;
                                    Imgproc.line(image, p1, p2, new Scalar(0, 255, 0), thickness);
                                }

                            }
                        }
                    }
                }

                // 過去のデータ
                gray.copyTo(image_prev);
                pts_prev = new MatOfPoint2f(features.toArray());
            }

            double verticalAverage = 0;
            double horizontalAverage = 0;
            for (double d : verticalDiff) {
                verticalAverage += d / verticalDiff.size();
            }
            for (double d : horizontalDiff) {
                horizontalAverage += d / horizontalDiff.size();
            }
            verticalAverage *= 5;
            horizontalAverage *= 5;
            //Log.d(TAG, "average is " + average);
            scrollVertical = (int) (alpha * scrollVertical + (1 - alpha) * ((int) verticalAverage));
            scrollHorizontal = (int) (alpha * scrollHorizontal + (1 - alpha) * ((int) horizontalAverage));
            Log.d(TAG, "update scroll value");
        }

        if (buttonPressed) {
            fingerColor[0] = fingerColor[1] = fingerColor[2] = 0;
        }

        webView.scrollBy((int) (scrollVertical * scale), (int) (scrollHorizontal * scale));
        //Log.d(TAG, "scroll = " + scrollVertical + " " + scrollHorizontal);

        return image;
    }
    /*
        private static class OpenCVLoaderCallback extends BaseLoaderCallback {
            private final CameraBridgeViewBase mCameraView;
            private OpenCVLoaderCallback(Context context, CameraBridgeViewBase cameraView) {
                super(context);
                mCameraView = cameraView;
            }

            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        mCameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        }
    */
    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null) mCameraView.disableView();
    }
}
