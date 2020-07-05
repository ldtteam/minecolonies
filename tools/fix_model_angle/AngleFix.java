package tools.fix_model_angle;

import java.lang.Math;
import java.util.Scanner;

public class AngleFix
{
    public static void main(final String[] args)
    {
        final Scanner s = new Scanner(System.in);

        convertEulerYzxToZyx(s.nextFloat(), s.nextFloat(), s.nextFloat());
    }

    private static void convertEulerYzxToZyx(final float x, final float y, final float z)
    {
        // Create a matrix from YZX ordered Euler angles
        final float a = (float) Math.cos(x);
        final float b = (float) Math.sin(x);
        final float c = (float) Math.cos(y);
        final float d = (float) Math.sin(y);
        final float e = (float) Math.cos(z);
        final float f = (float) Math.sin(z);
        final float m00 = c * e;
        final float m01 = b * d - a * c * f;
        final float m10 = f;
        final float m11 = a * e;
        final float m20 = -d * e;
        final float m21 = a * d * f + b * c;
        final float m22 = a * c - b * d * f;

        // Create ZYX ordered Euler angles from the matrix
        final float ox;
        final float oz;
        final float oy = (float) Math.asin(clamp(-m20, -1, 1));
        if (abs(m20) < 0.99999) {
            ox = (float) Math.atan2(m21, m22);
            oz = (float) Math.atan2(m10, m00);
        } else {
            ox = 0.0F;
            oz = (float) Math.atan2(-m01, m11);
        }

        System.out.println(String.format("%fF, %fF, %fF", ox, oy, oz));
    }

    private static float clamp(final float num, final float min, final float max)
    {
        return num < min ? min : (num > max ? max : num);
    }

    private static float abs(final float value)
    {
        return value >= 0.0F ? value : -value;
    }
}