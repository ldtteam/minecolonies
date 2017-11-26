package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
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
        final Set<TypeToken> types = ReflectionUtils.getSuperClasses(TypeConstants.STANDARDTOKEN);
        assertEquals(4, types.size());

        final Set<TypeToken> interfaceTypes = ReflectionUtils.getSuperClasses(new TypeToken<IToken<UUID>>() {});
        assertEquals(2, interfaceTypes.size());
    }
}