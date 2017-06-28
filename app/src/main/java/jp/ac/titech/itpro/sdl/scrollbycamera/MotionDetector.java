package jp.ac.titech.itpro.sdl.scrollbycamera;

import android.util.Log;
import android.util.TypedValue;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by onuki on 2017/06/24.
 */

public class MotionDetector {

    Boolean button = false;

    class Motion {
        float x;
        float y;
        Mat image;
        Motion(float x, float y, Mat image) {
            this.x = x;
            this.y = y;
            this.image = image;
        }
    }

    final private String TAG = "MotionDetector";

    //private ArrayList<Float> verticalDiff = new ArrayList<>();
    //private ArrayList<Float> horizontalDiff = new ArrayList<>();

    private float threshold;

    // カメラ画像
    private Mat image_small, high_image, low_image;

    // オプティカルフロー用
    private Mat image_prev, image_next;
    private MatOfPoint2f pts_prev, pts_next;

    private Mat rect;
    private Point point_prev;

    MotionDetector(int width, int height, float threshold) {
        image_small = new Mat(height/8, width/8, CvType.CV_8UC3);
        high_image = new Mat(height/8, width/8, CvType.CV_8UC1);
        low_image = new Mat(height/8, width/8, CvType.CV_8UC1);
        image_prev = new Mat(image_small.rows(), image_small.cols(), image_small.type());
        image_next = new Mat(image_small.rows(), image_small.cols(), image_small.type());
        pts_prev = new MatOfPoint2f();
        pts_next = new MatOfPoint2f();

        rect = new Mat(height/8, width/8, CvType.CV_8UC3);

        this.threshold = threshold;
    }

    Motion onCameraFrame(Mat input) {

        float horizontalAverage = 0;
        float verticalAverage = 0;
        //verticalDiff.clear();
        //horizontalDiff.clear();

        rect = new Mat(input.rows(), input.cols(), input.type());


        // 縮小
        Imgproc.resize(input, image_small, image_small.size(), 0, 0, Imgproc.INTER_NEAREST);

        // RGBからHSVに変更
        //Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(image_small, image_small, Imgproc.COLOR_RGB2HSV);

        if (button) {
            for (int y = 0; y < image_small.rows(); y++) {
                for (int x = 0; x < image_small.cols(); x++) {
                    double[] data = image_small.get(y, x);
                    if (data.length >= 3) {
                        Log.d(TAG, "color is " +data[0] + " " + data[1] + " " + data[2]);
                    }
                }
            }
            button = false;
        }

        // lowとhighの2つの範囲を取って
        Core.inRange(image_small, new Scalar(0, 224, 0), new Scalar(4, 255, 255), low_image);
        Core.inRange(image_small, new Scalar(176, 224, 0), new Scalar(180, 255, 255), high_image);
        // orで合成
        Core.bitwise_or(low_image, high_image, image_small);

        // 矩形を検出
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat(image_small.rows(), image_small.cols(), CvType.CV_8UC1);
        Imgproc.findContours(image_small, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        // 一定以上の大きさの矩形があるか
        boolean exist = false;
        int index = 0;
        double max_area = image_small.rows() * image_small.cols();
        //Log.d(TAG, "contour size is " + contours.size());
        while (!exist && index < contours.size()) {
            double area = Imgproc.contourArea(contours.get(index), false);
            if (area * 8 > max_area) exist = true;
            else index++;
        }
        /*
        if (contours.size() > 0) {
            double max = 0;
            for (int i = 0; i < contours.size(); i++) {
                double area = Imgproc.contourArea(contours.get(i), false);
                if (area * 8 > max_area) exist = true;
                //Log.d(TAG, "area is " + a);
                if (a > max) {
                    max = a;
                    index = i;
                }
            }
            double avex = 0;
            double avey = 0;
            float length = contours.get(index).rows();
            for (Point p : contours.get(index).toList()) {
                avex += p.x/length;
                avey += p.y/length;
            }
            Imgproc.drawContours(rect, contours, index, new Scalar(128, 128, 128));
            area = Imgproc.contourArea(contours.get(index), false);
        }
*/
        // 一定以上の大きさの矩形があれば
        if (exist) {

            /*
            // 特徴点抽出
            MatOfPoint features = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(image_small, features, 30, 0.01, 20);

            // 特徴点が見つかった
            if (features.total() > 0) {
                // 過去のデータが存在する
                if (pts_prev.total() > 0) {
                    // 現在のデータ
                    image_small.copyTo(image_next);
                    pts_next = new MatOfPoint2f(features.toArray());

                    // オプティカルフロー算出
                    MatOfByte status = new MatOfByte();
                    MatOfFloat err = new MatOfFloat();
                    Video.calcOpticalFlowPyrLK(image_prev, image_next, pts_prev, pts_next, status, err, new Size(5, 5), 3, new TermCriteria(TermCriteria.EPS, 30, 0.01), Video.OPTFLOW_LK_GET_MIN_EIGENVALS, 1);

                    // 表示
                    long flow_num = status.total();
                    if (flow_num > 0) {
                        List<Byte> list_status = status.toList();
                        List<Point> list_features_prev = pts_prev.toList();
                        List<Point> list_features_next = pts_next.toList();
                        for (int i = 0; i < flow_num; i++) {
                            if (list_status.get(i) == 1) {
                                Point p1 = new Point();
                                p1.x = list_features_prev.get(i).x;
                                p1.y = list_features_prev.get(i).y;
                                //Imgproc.circle(image, p1, 3, new Scalar(255,0,0), -1, 8, 0 );
                                Point p2 = new Point();
                                p2.x = list_features_next.get(i).x;
                                p2.y = list_features_next.get(i).y;
                                //Imgproc.circle(image, p2, 3, new Scalar(255,255,0), -1, 8, 0 );

                                if (threshold < Math.hypot(p2.x - p1.x, p2.y - p1.y)) {
                                    horizontalDiff.add((float) (p2.x - p1.x));
                                    verticalDiff.add((float) (p2.y - p1.y));

                                    // フロー描画
                                    int thickness = 5;
                                    Imgproc.line(image_small, p1, p2, new Scalar(128, 255, 128), thickness);
                                }

                            }
                        }
                    }
                }

                // 過去のデータ
                image_small.copyTo(image_prev);
                pts_prev = new MatOfPoint2f(features.toArray());

            }
            */

            MatOfPoint max_contour = contours.get(index);
            Moments mu = MyImgproc.contourMoments(max_contour);
            Point p = new Point(mu.get_m10()/mu.get_m00(), mu.get_m01()/mu.get_m00());
            if (point_prev != null) {
                //horizontalAverage += p.x - point_prev.x;
                //verticalAverage += p.y - point_prev.y;
                if (Math.abs(p.x - point_prev.x) > threshold) horizontalAverage += p.x - point_prev.x;
                if (Math.abs(p.y - point_prev.y) > threshold) verticalAverage += p.y - point_prev.y;
            }
            point_prev = p;
/*
            for (double d : horizontalDiff) {
                horizontalAverage += d / horizontalDiff.size();
            }
            for (float d : verticalDiff) {
                verticalAverage += d / verticalDiff.size();
            }
*/
            Imgproc.drawContours(rect, contours, index, new Scalar(128, 128, 128));
            Imgproc.line(rect, p, p, new Scalar(192, 192, 192), 5);
        } else {
            //pts_prev = new MatOfPoint2f();
            point_prev = null;
        }

        //Imgproc.resize(image_small, input, input.size(), 0, 0, Imgproc.INTER_NEAREST);

        return new Motion(horizontalAverage, verticalAverage, rect);
    }
}
