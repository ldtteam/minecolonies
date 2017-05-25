package com.minecolonies.coremod.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Methods to simplify the use of reflection.
 * <p>Created by Colton on 3/2/17.
 */
public class ReflectionUtil
{
    public static void setFinalField(final Object object, final String fieldName, final Object newValue)
      throws NoSuchFieldException, IllegalAccessException
    {
        final Field field = object.getClass().getField(fieldName);
        field.setAccessible(true);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(object, newValue);
    }

    public static void setStaticFinalField(final Class clazz, final String fieldName, final Object newValue)
      throws NoSuchFieldException, IllegalAccessException
    {
        final Field field = clazz.getField(fieldName);
        field.setAccessible(true);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
