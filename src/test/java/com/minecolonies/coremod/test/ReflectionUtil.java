package com.minecolonies.coremod.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Methods to simplify the use of reflection.
 * <p>Created by Colton on 3/2/17.
 */
public class ReflectionUtil
{
    public static void setFinalField(Object object, String fieldName, Object newValue)
      throws NoSuchFieldException, IllegalAccessException
    {
        Field field = object.getClass().getField(fieldName);
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(object, newValue);
    }

    public static void setStaticFinalField(Class clazz, String fieldName, Object newValue)
      throws NoSuchFieldException, IllegalAccessException
    {
        Field field = clazz.getField(fieldName);
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
