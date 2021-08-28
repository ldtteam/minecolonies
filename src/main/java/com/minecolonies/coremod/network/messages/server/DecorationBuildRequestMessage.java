package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Adds a entry to the builderRequired map.
 */
public class DecorationBuildRequestMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos pos;

    /**
     * The name of the decoration.
     */
    private String name;

    /**
     * The level of the decoration.
     */
    private int level;

    /**
     * The dimension.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public DecorationBuildRequestMessage()
    {
        super();
    }

    /**
     * Creates a build request for a decoration.
     *
     * @param pos       the position of it.
     * @param name      it's name.
     * @param level     the level.
     * @param dimension the dimension we're executing on.
     */
    public DecorationBuildRequestMessage(@NotNull final BlockPos pos, final String name, final int level, final ResourceKey<Level> dimension)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.name = buf.readUtf(32767);
        this.level = buf.readInt();
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.name);
        buf.writeInt(this.level);
        buf.writeUtf(this.dimension.location().toString());
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
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromDim(dimension, pos);
        if (colony == null)
        {
            return;
        }
        final Player player = ctxIn.getSender();

        //Verify player has permission to change this huts settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final BlockEntity entity = player.getCommandSenderWorld().getBlockEntity(pos);
        if (entity instanceof TileEntityDecorationController)
        {
            final Optional<Map.Entry<Integer, IWorkOrder>> wo = colony.getWorkManager().getWorkOrders().entrySet().stream()
                                                                  .filter(entry -> entry.getValue() instanceof WorkOrderBuildDecoration)
                                                                  .filter(entry -> ((WorkOrderBuildDecoration) entry.getValue()).getSchematicLocation().equals(pos)).findFirst();

            if (wo.isPresent())
            {
                colony.getWorkManager().removeWorkOrder(wo.get().getKey());
                return;
            }

            int difference = 0;

            final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(colony.getWorld(), this.pos, name + level, new PlacementSettings(), true);

            final Blueprint blueprint = structure.getBluePrint();

            if (blueprint != null)
            {
                final BlockState structureState = structure.getBluePrint().getBlockInfoAsMap().get(structure.getBluePrint().getPrimaryBlockOffset()).getState();
                if (structureState != null)
                {
                    if (!(structureState.getBlock() instanceof BlockDecorationController))
                    {
                        Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", name + level));
                        return;
                    }

                    final int structureRotation = structureState.getValue(BlockDecorationController.FACING).get2DDataValue();
                    final int worldRotation = colony.getWorld().getBlockState(this.pos).getValue(BlockDecorationController.FACING).get2DDataValue();

                    if (structureRotation <= worldRotation)
                    {
                        difference = worldRotation - structureRotation;
                    }
                    else
                    {
                        difference = 4 + worldRotation - structureRotation;
                    }
                }
            }

            final BlockState state = player.getCommandSenderWorld().getBlockState(pos);
            final WorkOrderBuildDecoration order = new WorkOrderBuildDecoration(name + level,
              name + level,
              difference,
              pos,
              state.getValue(BlockDecorationController.MIRROR));

            if (level != ((TileEntityDecorationController) entity).getTier())
            {
                order.setLevelUp();
            }
            colony.getWorkManager().addWorkOrder(order, false);
        }
    }
}
