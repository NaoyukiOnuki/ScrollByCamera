package jp.ac.titech.itpro.sdl.trackballemulator;

import static java.lang.Math.*;

/**
 * Created by onuki on 2017/06/20.
 */

public class Vector2D {

    double x;
    double y;

    Vector2D(double ix, double iy) {
        x = ix;
        y = iy;
    }

    Vector2D plus(Vector2D v) {
        return new Vector2D(x+v.x, y+v.y);
    }
    Vector2D minus(Vector2D v) {
        return new Vector2D(x-v.x, y-v.y);
    }
    Vector2D mult(double d) {
        return new Vector2D(x*d, y*d);
    }
    Vector2D div(double d) {
        return new Vector2D(x/d, y/d);
    }
    double product(Vector2D v) {
        return x*v.x + y*v.y;
    }
    Vector2D normalization() {
        return new Vector2D(x/length(), y/length());
    }

    double length() {
        return sqrt(x*x + y*y);
    }
    double angle() {
        if (x == 0) {
            if (y > 0) return PI/2;
            else return PI*3/2;
        } else {
            double angle = atan(y/x);
            if (x < 0) return angle + PI;
            else return angle;
        }
    }

    // 垂直なベクトル
    Vector2D vertical() {
        return Vector2D.polarVector(length(), angle()+PI/2);
    }

    static Vector2D[] dualBasis(Vector2D e1, Vector2D e2) {
        Vector2D[] ret = new Vector2D[2];
        Vector2D v1 = e2.vertical();
        Vector2D v2 = e1.vertical();

        double p1 = v1.product(e1);
        double p2 = v2.product(e2);

        ret[0] = v1.div(p1);
        ret[1] = v2.div(p2);

        return ret;
    }

    // a = decompose[0], b = decompose[1]
    // input = a*e1 + b*e2
    static double[] decompose(Vector2D input, Vector2D e1, Vector2D e2) {
        Vector2D[] dual = dualBasis(e1, e2);
        double[] ret = new double[2];
        ret[0] = input.product(dual[0]);
        ret[1] = input.product(dual[1]);
        return ret;
    }

    static Vector2D polarVector(double r, double d) {
        return new Vector2D(r*cos(d), r*sin(d));
    }
}
