package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.test.ReflectionUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
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
    private PlayerEntity player;

    @Mock
    private EventBus eventBus;

    @Mock
    private Scoreboard board;

    private Colony colony;

    private final UUID id = UUID.randomUUID();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException
    {
        final ColonyList<Colony> list = new ColonyList<>();
        when(worldProvider.getDimension()).thenReturn(1);
        when(player.getGameProfile()).thenReturn(new GameProfile(id, "ray"));
        ReflectionUtil.setFinalField(world, "provider", worldProvider);
        ReflectionUtil.setStaticFinalField(MinecraftForge.class, "EVENT_BUS", eventBus);
        StandardFactoryControllerInitializer.onPreInit();
        when(world.getScoreboard()).thenReturn(board);
        when(board.getTeam(any())).thenReturn(new ScorePlayerTeam(board, "team"));
        colony = list.create(world, center);
    }

    @Test
    public void testToggleAnything()
    {
        colony.setName("blahsColony");
        assertNotNull(colony.getColonyTag());
    }

    @Test
    public void testToggleOverManager()
    {
        colony.getRaiderManager().setCanHaveRaiderEvents(true);
        assertNotNull(colony.getColonyTag());

    }

    @Test
    public void testToggleInLoad()
    {
        final CompoundNBT compound = colony.getColonyTag();
        final Colony test = Colony.loadColony(compound, world);
        test.setActive(false);
        assertNotNull(test.getColonyTag());
        assertEquals(compound, test.getColonyTag());
    }

    @Test
    public void testLoadDifferentThanActual()
    {
        final CompoundNBT compound = colony.getColonyTag();
        final Colony test = Colony.loadColony(compound, world);
        assertNotEquals(null, test.getColonyTag());
        test.setName("blahColony");
        test.write(new CompoundNBT());
        assertNotEquals(compound, test.getColonyTag());
    }
}
