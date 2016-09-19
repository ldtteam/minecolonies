package com.minecolonies.event;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.test.AbstractTest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

public class TownHallPlacementTest extends AbstractTest
{
    @Mock
    private ColonyManager colonyManager;

    @Mock
    private Colony colony;

    @Mock
    private World world;

    @Mock
    private EntityPlayer player;

    @Mock
    private Permissions permissions;

    private static final BlockPos PLACE_POS = new BlockPos(0, 0, 0);

    @Before
    public void setupColonyPlacement() throws Exception
    {
        when(colony.getCenter()).thenReturn(PLACE_POS);
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
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic();
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testReplaceAsOwner()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(ColonyManager.getIColony(world, PLACE_POS)).thenReturn(colony);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testReplaceAsNonOwnerWithoutPermission()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Permissions.Action.PLACE_HUTS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testReplaceAsNonOwnerWithPermission()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Permissions.Action.PLACE_HUTS)).thenReturn(true);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testReplaceAsNonMember()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testAlreadyPlacedInOwnedColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testAlreadyPlacedInNonOwnedColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        //Just need to return any other colony instance.
        when(ColonyManager.getIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testAlreadyPlacedNotInColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testTryPlaceInColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testTryPlaceCloseToColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(0L);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic(never());
        ColonyManager.createColony(world, PLACE_POS, player);
    }

    @Test
    public void testTryPlaceFarAway()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(Long.MAX_VALUE);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));

        verifyStatic();
        ColonyManager.createColony(world, PLACE_POS, player);
    }
}
