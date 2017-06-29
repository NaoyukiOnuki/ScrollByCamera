package jp.ac.titech.itpro.sdl.scrollbycamera;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Moments;

/**
 * Created by onuki on 2017/06/27.
 */

class MyImgproc {
    final private static String TAG = "MyImgproc";

    public static Moments contourMoments(MatOfPoint contour )
    {
        Moments m = new Moments();
        int lpt = contour.checkVector(2);
        boolean is_float = true;//(contour.depth() == CvType.CV_32F);
        Point[] ptsi = contour.toArray();
//PointF[] ptsf = contour.toArray();

        //CV_Assert( contour.depth() == CV_32S || contour.depth() == CV_32F );

        if( lpt == 0 )
            return m;

        double a00 = 0, a10 = 0, a01 = 0;
        double xi, yi, xi2, yi2, xi_1, yi_1, dxy, xii_1, yii_1;


        {
            xi_1 = ptsi[lpt-1].x;
            yi_1 = ptsi[lpt-1].y;
        }

        for( int i = 0; i < lpt; i++ )
        {

            {
                xi = ptsi[i].x;
                yi = ptsi[i].y;
            }

            xi2 = xi * xi;
            yi2 = yi * yi;
            dxy = xi_1 * yi - xi * yi_1;
            xii_1 = xi_1 + xi;
            yii_1 = yi_1 + yi;

            a00 += dxy;
            a10 += dxy * xii_1;
            a01 += dxy * yii_1;
            xi_1 = xi;
            yi_1 = yi;
        }
        float FLT_EPSILON = 1.19209e-07f;
        if( Math.abs(a00) > FLT_EPSILON )
        {
            double db1_2, db1_6;

            if( a00 > 0 )
            {
                db1_2 = 0.5;
                db1_6 = 0.16666666666666666666666666666667;
            }
            else
            {
                db1_2 = -0.5;
                db1_6 = -0.16666666666666666666666666666667;
            }

            // spatial moments
            m.m00 = a00 * db1_2;
            m.m10 = a10 * db1_6;
            m.m01 = a01 * db1_6;
        }
        return m;
    }
}
