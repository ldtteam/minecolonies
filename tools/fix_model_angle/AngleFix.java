package tools.fix_model_angle;

import java.lang.Math;
import java.util.Scanner;

public class AngleFix
{
    public static void main(String[] args)
    {
        Scanner s = new Scanner(System.in);

        convertEulerYzxToZyx(s.nextFloat(), s.nextFloat(), s.nextFloat());
    }

    private static void convertEulerYzxToZyx(float x, float y, float z)
    {
        // Create a matrix from YZX ordered Euler angles
        float a = (float) Math.cos(x);
        float b = (float) Math.sin(x);
        float c = (float) Math.cos(y);
        float d = (float) Math.sin(y);
        float e = (float) Math.cos(z);
        float f = (float) Math.sin(z);
        float m00 = c * e;
        float m01 = b * d - a * c * f;
        float m02 = b * c * f + a * d;
        float m10 = f;
        float m11 = a * e;
        float m12 = -b * e;
        float m20 = -d * e;
        float m21 = a * d * f + b * c;
        float m22 = a * c - b * d * f;
        float m33 = 1.0F;

        // Create ZYX ordered Euler angles from the matrix
        float ox, oz;
        float oy = (float) Math.asin(clamp(-m20, -1, 1));
        if (abs(m20) < 0.99999) {
            ox = (float) Math.atan2(m21, m22);
            oz = (float) Math.atan2(m10, m00);
        } else {
            ox = 0.0F;
            oz = (float) Math.atan2(-m01, m11);
        }

        System.out.println(String.format("%fF, %fF, %fF", ox, oy, oz));
    }

    private static float clamp(float num, float min, float max)
    {
        return num < min ? min : (num > max ? max : num);
    }

    private static float abs(float value)
    {
        return value >= 0.0F ? value : -value;
    }
}