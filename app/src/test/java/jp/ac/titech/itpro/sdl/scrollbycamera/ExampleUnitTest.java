package jp.ac.titech.itpro.sdl.scrollbycamera;

import org.junit.Test;

import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void vector() throws Exception {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 100; i++) {
            Vector2D vector = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D polar = Vector2D.polarVector(vector.length(), vector.angle());
            Vector2D vertical = vector.vertical();
            assertTrue(abs(vector.x - polar.x) < 0.00001);
            assertTrue(abs(vector.y - polar.y) < 0.00001);
            assertTrue(abs(vector.length() - polar.length()) < 0.00001);
            assertTrue(abs(vector.angle() - polar.angle()) < 0.00001);
            assertTrue(abs(vector.product(vertical) - 0) < 0.00001);
        }
    }

    @Test
    public void vector_dual_basis() throws Exception {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 100; i++) {
            Vector2D v1 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D v2 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D[] origin = {v1, v2};
            Vector2D[] dual = Vector2D.dualBasis(v1, v2);

            assertTrue(abs(origin[0].product(dual[0]) - 1) < 0.00001);
            assertTrue(abs(origin[0].product(dual[1]) - 0) < 0.00001);
            assertTrue(abs(origin[1].product(dual[0]) - 0) < 0.00001);
            assertTrue(abs(origin[1].product(dual[1]) - 1) < 0.00001);
        }

        // vertical vector
        {
            Vector2D v1 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D v2 = v1.vertical();
            Vector2D[] origin = {v1, v2};
            Vector2D[] dual = Vector2D.dualBasis(v1, v2);

            assertTrue(abs(origin[0].product(dual[0]) - 1) < 0.00001);
            assertTrue(abs(origin[0].product(dual[1]) - 0) < 0.00001);
            assertTrue(abs(origin[1].product(dual[0]) - 0) < 0.00001);
            assertTrue(abs(origin[1].product(dual[1]) - 1) < 0.00001);
        }
    }

    @Test
    public void vector_decompose() throws Exception {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 10000; i++) {
            Vector2D vector1 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D e1 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            Vector2D e2 = new Vector2D(rand.nextDouble(), rand.nextDouble());
            double[] decomp = Vector2D.decompose(vector1, e1, e2);
            Vector2D vector2 = e1.mult(decomp[0]).plus(e2.mult(decomp[1]));

            assertTrue(abs(vector1.x - vector2.x) < 0.00001);
            assertTrue(abs(vector1.y - vector2.y) < 0.00001);
        }
    }


}