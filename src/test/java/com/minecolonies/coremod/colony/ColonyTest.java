package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.test.ReflectionUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests around {@link ColonyManager}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ColonyTest
{
    @Mock
    private BlockPos center;

    @Mock
    private WorldProvider worldProvider;

    @Mock
    private World world;

    @Mock
    private EntityPlayer player;

    @Mock
    private EventBus eventBus;

    @Mock
    private MinecraftServer minecraftServer;

    @Mock
    private PlayerProfileCache playerProfileCache;

    @Mock
    private Colony colony1;

    private ColonyList<Colony> list;

    private Colony colony;

    private final UUID id = UUID.randomUUID();

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException
    {
        list = new ColonyList<>();
        when(worldProvider.getDimension()).thenReturn(1);
        when(player.getGameProfile()).thenReturn(new GameProfile(id, "ray"));
        ReflectionUtil.setFinalField(world, "provider", worldProvider);
        ReflectionUtil.setStaticFinalField(MinecraftForge.class, "EVENT_BUS", eventBus);
        StandardFactoryControllerInitializer.onPreInit();
        colony = list.create(world, center);
    }

    @Test
    public void testToggleAnything()
    {
        colony.setName("blahsColony");
        assertNotEquals(null, colony.getColonyTag());
    }

    @Test
    public void testToggleOverManager()
    {
        colony.getBarbManager().setCanHaveBarbEvents(true);
        assertNotEquals(null, colony.getColonyTag());
    }

    @Test
    public void testToggleInLoad()
    {
        colony.markDirty();
        NBTTagCompound compound = colony.getColonyTag();
        Colony test = Colony.loadColony(compound, world);
        assertNotEquals(null, test.getColonyTag());
    }

    @Test
    public void testLoadDifferentThanActual()
    {
        colony.markDirty();
        NBTTagCompound compound = colony.getColonyTag();
        Colony test = Colony.loadColony(compound, world);
        assertNotEquals(null, test.getColonyTag());
        test.setName("blahColony");
        assertNotEquals(compound, test.getColonyTag());
    }
}
