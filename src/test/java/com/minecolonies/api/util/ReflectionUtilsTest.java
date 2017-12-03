package com.minecolonies.api.util;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
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
        @SuppressWarnings(RAWTYPES) final Set<TypeToken> types = ReflectionUtils.getSuperClasses(TypeConstants.STANDARDTOKEN);
        assertEquals(4, types.size());

        @SuppressWarnings(RAWTYPES) final Set<TypeToken> interfaceTypes = ReflectionUtils.getSuperClasses(new TypeToken<IToken<UUID>>() {
            private static final long serialVersionUID = 0;
        });
        assertEquals(2, interfaceTypes.size());
    }
}