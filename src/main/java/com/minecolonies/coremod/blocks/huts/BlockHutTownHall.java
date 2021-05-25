package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.townhall.WindowTownHallColonyManage;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.TOWNHALL_BREAKING_MESSAGE;

/**
 * Hut for the town hall. Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut<BlockHutTownHall>
{
    public BlockHutTownHall()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(HARDNESS, RESISTANCE));
    }

    /**
     * Progress in % of breaking the townHall.
     */
    private int breakProgressOnTownHall = 0;

    /**
     * Ticks at which townhall breaking started.
     */
    private long lastTownHallBreakingTick = 0;

    /**
     * Detect if the town-hall break was valid.
     */
    private boolean validTownHallBreak = false;


    @Override
    public float getPlayerRelativeBlockHardness(final BlockState state, @NotNull final PlayerEntity player, @NotNull final IBlockReader blockReader, @NotNull final BlockPos pos)
    {
        if(MineColonies.getConfig().getServer().pvp_mode.get() && player.world instanceof ServerWorld)
        {
            final IBuilding building = IColonyManager.getInstance().getBuilding(player.world, pos);
            if (building != null && building.getColony().isCoordInColony(player.world, pos)
                  && building.getColony().getPermissions().getRank(player).isHostile())
            {
                final double localProgress = breakProgressOnTownHall;
                final double hardness = state.getBlockHardness(player.world, pos) * 20.0 * 1.5;

                if (localProgress >= hardness / 10.0 * 9.0 && localProgress <= hardness / 10.0 * 9.0 + 1)
                {
                    sendPlayersMessage(building.getColony().getMessagePlayerEntities(), TOWNHALL_BREAKING_MESSAGE, player.getName(), 90);
                }
                if (localProgress >= hardness / 4.0 * 3.0 && localProgress <= hardness / 4.0 * 3.0 + 1)
                {
                    sendPlayersMessage(building.getColony().getMessagePlayerEntities(), TOWNHALL_BREAKING_MESSAGE, player.getName(), 75);
                }
                else if (localProgress >= hardness / 2.0 && localProgress <= hardness / 2.0 + 1)
                {
                    sendPlayersMessage(building.getColony().getMessagePlayerEntities(), TOWNHALL_BREAKING_MESSAGE, player.getName(), 50);
                }
                else if (localProgress >= hardness / 4.0 && localProgress <= hardness / 4.0 + 1)
                {
                    sendPlayersMessage(building.getColony().getMessagePlayerEntities(), TOWNHALL_BREAKING_MESSAGE, player.getName(), 25);
                }

                if (localProgress >= hardness - 1)
                {
                    validTownHallBreak = true;
                }

                if (player.world.getGameTime() - lastTownHallBreakingTick < 10)
                {
                    breakProgressOnTownHall++;
                }
                else
                {
                    sendPlayersMessage(building.getColony().getImportantMessageEntityPlayers(),
                      "com.minecolonies.coremod.pvp.townhall.break.start",
                      player.getName(), 100);
                    breakProgressOnTownHall = 0;
                    validTownHallBreak = false;
                }
                lastTownHallBreakingTick = player.world.getGameTime();
            }
            else
            {
                validTownHallBreak = true;
            }
        }
        else if (!MineColonies.getConfig().getServer().pvp_mode.get())
        {
            validTownHallBreak = true;
        }
        final float def = super.getPlayerRelativeBlockHardness(state, player, player.world, pos);
        return MineColonies.getConfig().getServer().pvp_mode.get() ? def / 12 : def / 10;
    }

    /**
     * Send a message to multiple players, using a translation key or string and multiple arguments.
     * @param players       players to send the message
     * @param key           Translation Key for the message contents, or a string otherwise
     * @param attacker      Attacker name to apply to the message.
     * @param value         Progress value to apply to the message.
     */
    private void sendPlayersMessage(List<PlayerEntity> players, String key, ITextComponent attacker, int value)
    {
        for(PlayerEntity player : players)
        {
            player.sendMessage(new TranslationTextComponent(key, attacker, value), player.getUniqueID());
        }
    }

    /**
     * Getter for the Block's state to breakable.
     * @return  True if the block is eligible for destruction
     */
    public boolean getValidBreak()
    {
        return validTownHallBreak;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhuttownhall";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.townHall;
    }

    /**
     * Choose a different gui when no colony view, for colony overview and creation/deletion
     *
     * @param state   the blockstate.
     * @param worldIn the world.
     * @param pos     the position.
     * @param player  the player.
     * @param hand    the hand.
     * @param ray     the raytraceresult.
     * @return the result type.
     */
    @NotNull
    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.getDimensionKey(), pos);

            if (building != null
                  && building.getColony() != null
                  && building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                building.openGui(player.isSneaking());
            }
            else
            {
                new WindowTownHallColonyManage(player, pos, worldIn).open();
            }
        }
        return ActionResultType.SUCCESS;
    }
}
