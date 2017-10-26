package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link ReflectionUtils} class.
 * Tests the return amount of classes in the map.
 */
public class ReflectionUtilsTest
{
    @Test
    public void getSuperClasses()
    {
        final Set<Class> types = ReflectionUtils.getSuperClasses(StandardToken.class);
        assertEquals(types.size(), 4);

        final Set<Class> interfaceTypes = ReflectionUtils.getSuperClasses(IToken.class);
        assertEquals(interfaceTypes.size(), 2);
    }
}