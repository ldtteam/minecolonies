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
        final Set<TypeToken> types = ReflectionUtils.getSuperClasses(new TypeToken<StandardToken>() {});
        assertEquals(types.size(), 4);

        final Set<TypeToken> interfaceTypes = ReflectionUtils.getSuperClasses(new TypeToken<IToken<UUID>>() {});
        assertEquals(interfaceTypes.size(), 2);
    }
}