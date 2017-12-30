package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.test.ReflectionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests around {@link ColonyList}.
 * <p>Created by Colton on 2/28/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ColonyTest
{
    @Mock
    private BlockPos center;

    @Mock
    private Colony colony1;

    @Mock
    private Colony colony1Copy;

    @Mock
    private Colony colony2;

    @Mock
    private WorldProvider worldProvider;

    @Mock
    private World world;

    @Mock
    private EventBus eventBus;

    private ColonyList<Colony> list;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException
    {
        list = new ColonyList<>();

        when(colony1.getID()).thenReturn(1);
        when(colony1Copy.getID()).thenReturn(1);
        when(colony2.getID()).thenReturn(2);
        when(colony1.getCenter()).thenReturn(center);
        when(colony1Copy.getCenter()).thenReturn(center);
        when(colony2.getCenter()).thenReturn(center);
        when(colony1.getWorld()).thenReturn(world);
        when(colony1Copy.getWorld()).thenReturn(world);
        when(colony2.getWorld()).thenReturn(world);

        when(worldProvider.getDimension()).thenReturn(1);
        ReflectionUtil.setFinalField(world, "provider", worldProvider);
        ReflectionUtil.setStaticFinalField(MinecraftForge.class, "EVENT_BUS", eventBus);
        StandardFactoryControllerInitializer.onPreInit();
    }

    @Test
    public void testCreate()
    {
        colony1.setName("blahsColony");
        assertNotEquals(null, colony1.getT);
    }
}
