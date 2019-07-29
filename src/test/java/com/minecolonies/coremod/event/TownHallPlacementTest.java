package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

// TODO make these tests actually test something, now that createColony call is moved...
public class TownHallPlacementTest extends AbstractMockStaticsTest
{
    /**
     * The position the townhall will get placed.
     */
    private static final BlockPos PLACE_POS = new BlockPos(1000, 100, 1000);

    /**
     * The position the world spawn is.
     */
    private static final BlockPos SPAWN_POS = new BlockPos(0, 100, 0);

    @Mock
    private Colony       colony;
    @Mock
    private World        world;
    @Mock
    private EntityPlayer player;
    @Mock
    private Permissions  permissions;

    @Before
    public void setupColonyPlacement() throws Exception
    {
        when(colony.getCenter()).thenReturn(PLACE_POS);
        when(world.getSpawnPoint()).thenReturn(SPAWN_POS);

        final GameProfile playerProfile = Mockito.mock(GameProfile.class);
        when(playerProfile.getId()).thenReturn(UUID.randomUUID());
        when(player.getGameProfile()).thenReturn(playerProfile);
    }

    //first townhall
    // replace townhall
    //  owner
    //  nonowner without permission
    //  nonowner with permission
    //  nonmember
    //already placed townhall
    // in colony as owner
    // in colony as nonowner
    // close to other colony
    // far away from other colonies
    //  ^last two merged into one test: not in colony
    //trying to place inside a colony
    //trying to place too close
    //placing townhall succesfully

    @Test
    public void testNothingNearby()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic();
        //ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testReplaceAsOwner()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(IColonyManager.getInstance().getIColony(world, PLACE_POS)).thenReturn(colony);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testReplaceAsNonOwnerWithoutPermission()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Action.PLACE_HUTS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testReplaceAsNonOwnerWithPermission()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Action.PLACE_HUTS)).thenReturn(true);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testReplaceAsNonMember()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testAlreadyPlacedInOwnedColony()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testAlreadyPlacedInNonOwnedColony()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.hasTownHall()).thenReturn(false);
        //Just need to return any other colony instance.
        when(IColonyManager.getInstance().getIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testAlreadyPlacedNotInColony()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testTryPlaceInColony()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testTryPlaceCloseToColony()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(0L);
        when(IColonyManager.getInstance().isTooCloseToColony(world, PLACE_POS)).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        IColonyManager.getInstance().createColony(world, PLACE_POS, player, Constants.DEFAULT_STYLE);
    }

    @Test
    public void testTryPlaceFarAway()
    {
        when(IColonyManager.getInstance().getIColonyByOwner(world, player)).thenReturn(null);
        when(IColonyManager.getInstance().getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(Long.MAX_VALUE);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic();
        //ColonyManager.createColony(world, PLACE_POS, player);
    }
}
