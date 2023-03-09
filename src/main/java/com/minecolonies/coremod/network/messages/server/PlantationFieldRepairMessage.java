package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.workorders.WorkOrderDecoration;
import com.minecolonies.coremod.tileentities.TileEntityPlantationField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class PlantationFieldRepairMessage implements IMessage
{
    /**
     * The position of the plantation field.
     */
    private BlockPos position;

    /**
     * The display name of the plantation field.
     */
    private String displayName;

    /**
     * The name of the plantation field.
     */
    private String name;

    /**
     * The dimension.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public PlantationFieldRepairMessage()
    {
        super();
    }

    /**
     * Creates a build request for a decoration.
     *
     * @param position    the field matcher type.
     * @param displayName it's display name.
     * @param name        it's name.
     * @param dimension   the dimension we're executing on.
     */
    public PlantationFieldRepairMessage(@NotNull final BlockPos position, final String displayName, final String name, final ResourceKey<Level> dimension)
    {
        super();
        this.position = position;
        this.displayName = displayName;
        this.name = name;
        this.dimension = dimension;
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.position);
        buf.writeUtf(this.displayName);
        buf.writeUtf(this.name);
        buf.writeUtf(this.dimension.location().toString());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.position = buf.readBlockPos();
        this.displayName = buf.readUtf(32767);
        this.name = buf.readUtf(32767);
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
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
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromDim(dimension, position);
        if (colony == null)
        {
            return;
        }
        final Player player = ctxIn.getSender();

        // Verify player has permission to change this hut its settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final BlockEntity entity = player.getCommandSenderWorld().getBlockEntity(position);
        if (entity instanceof TileEntityPlantationField)
        {
            final Optional<Map.Entry<Integer, IWorkOrder>> wo = colony.getWorkManager().getWorkOrders().entrySet().stream()
                                                                  .filter(entry -> entry.getValue() instanceof WorkOrderDecoration)
                                                                  .filter(entry -> entry.getValue().getLocation().equals(position)).findFirst();

            if (wo.isPresent())
            {
                colony.getWorkManager().removeWorkOrder(wo.get().getKey());
                return;
            }

            int difference = 0;

            //final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(colony.getWorld(), position, name, new PlacementSettings(), true);
            //
            //final Blueprint blueprint = structure.getBluePrint();
            //
            //if (blueprint != null)
            //{
            //    final BlockState structureState = structure.getBluePrint().getBlockInfoAsMap().get(structure.getBluePrint().getPrimaryBlockOffset()).getState();
            //    if (structureState != null)
            //    {
            //        if (!(structureState.getBlock() instanceof BlockPlantationField))
            //        {
            //            Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", name));
            //            return;
            //        }
            //
            //        final int structureRotation = structureState.getValue(BlockPlantationField.FACING).get2DDataValue();
            //        final int worldRotation = colony.getWorld().getBlockState(position).getValue(BlockPlantationField.FACING).get2DDataValue();
            //
            //        if (structureRotation <= worldRotation)
            //        {
            //            difference = worldRotation - structureRotation;
            //        }
            //        else
            //        {
            //            difference = 4 + worldRotation - structureRotation;
            //        }
            //    }
            //}
            //
            //final BlockState state = player.getCommandSenderWorld().getBlockState(position);
            //WorkOrderDecoration order = WorkOrderDecoration.create(
            //  WorkOrderType.REPAIR,
            //  name,
            //  WordUtils.capitalizeFully(displayName),
            //  position,
            //  difference,
            //  state.getValue(BlockPlantationField.MIRROR),
            //  1);

            //colony.getWorkManager().addWorkOrder(order, false);
        }
    }
}