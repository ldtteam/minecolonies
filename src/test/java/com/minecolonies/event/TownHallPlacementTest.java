package com.minecolonies.event;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({ColonyManager.class, LanguageHandler.class})
@RunWith(PowerMockRunner.class)
public class TownHallPlacementTest
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
    public void setup() throws Exception
    {
        mockStatic(ColonyManager.class);
        mockStatic(LanguageHandler.class);
        doNothing().when(LanguageHandler.class, "sendPlayerLocalizedMessage", anyObject(), anyString());
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
    //trying to place inside a colony
    //trying to place too close
    //placing townhall succesfully

    @Test
    public void nothingNearby()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void replaceAsOwner()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(ColonyManager.getIColony(world, PLACE_POS)).thenReturn(colony);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void replaceAsNonOwnerWithoutPermission()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Permissions.Action.PLACE_HUTS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void replaceAsNonOwnerWithPermission()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(true);
        when(permissions.hasPermission(player, Permissions.Action.PLACE_HUTS)).thenReturn(true);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void replaceAsNonMember()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        when(colony.getPermissions()).thenReturn(permissions);
        when(permissions.isColonyMember(player)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void alreadyPlacedInOwnedColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void alreadyPlacedInNonOwnedColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(false);
        //Just need to return any other colony instance.
        when(ColonyManager.getIColony(world, PLACE_POS)).thenReturn(null);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void alreadyPlacedCloseToColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void alreadyPlacedFarAway()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void tryPlaceInColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(true);
        when(colony.hasTownHall()).thenReturn(true);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void tryPlaceCloseToColony()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(0F);

        Assert.assertFalse(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }

    @Test
    public void tryPlaceFarAway()
    {
        when(ColonyManager.getIColonyByOwner(world, player)).thenReturn(null);
        when(ColonyManager.getClosestIColony(world, PLACE_POS)).thenReturn(colony);
        when(colony.isCoordInColony(world, PLACE_POS)).thenReturn(false);
        when(colony.getDistanceSquared(PLACE_POS)).thenReturn(Float.MAX_VALUE);

        Assert.assertTrue(EventHandler.onTownHallPlaced(world, player, PLACE_POS));
    }
}
