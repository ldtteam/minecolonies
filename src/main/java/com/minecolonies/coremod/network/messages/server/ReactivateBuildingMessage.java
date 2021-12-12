package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactivate a building.
 */
public class ReactivateBuildingMessage implements IMessage
{
    /**
     * The position to reactivate it.
     */
    private BlockPos pos;

    /**
     * Empty constructor used when registering the
     */
    public ReactivateBuildingMessage()
    {
        super();
    }

    /**
     * Reactivate the building.
     *
     * @param pos the position of the building.
     */
    public ReactivateBuildingMessage(final BlockPos pos)
    {
        super();
        this.pos = pos;
    }

    /**
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final Level world = player.getCommandSenderWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            AbstractBuilding building = (AbstractBuilding) colony.getBuildingManager().getBuilding(pos);
            if (building == null)
            {
                final BlockEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof @NotNull final TileEntityColonyBuilding hut)
                {
                    hut.reactivate();
                    colony.getBuildingManager().addNewBuilding(hut, world);
                    colony.getProgressManager().progressBuildingPlacement(tileEntity.getBlockState().getBlock());
                }
            }
        }
    }
}
