package com.minecolonies.core.network.messages.server;

import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.workorders.WorkOrderDecoration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.text.WordUtils;
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
     * The display name of the decoration.
     */
    private String packName;

    /**
     * The name of the decoration.
     */
    private String path;

    /**
     * The rotation.
     */
    private Rotation rotation;

    /**
     * If mirrored.
     */
    private boolean mirror;

    /**
     * The dimension.
     */
    private ResourceKey<Level> dimension;

    /**
     * Type of workorder.
     */
    private WorkOrderType workOrderType;

    /**
     * The builder, or ZERO to auto-assign.
     */
    private BlockPos builder;

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
     * @param pos         the position of it.
     * @param packName    pack name.
     * @param path        blueprint path.
     * @param dimension   the dimension we're executing on.
     */
    public DecorationBuildRequestMessage(final WorkOrderType workOrderType, @NotNull final BlockPos pos, final String packName, final String path, final ResourceKey<Level> dimension, final Rotation rotation, final boolean mirror, final BlockPos builder)
    {
        super();
        this.workOrderType = workOrderType;
        this.pos = pos;
        this.packName = packName;
        this.path = path;
        this.dimension = dimension;
        this.rotation = rotation;
        this.mirror = mirror;
        this.builder = builder;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.workOrderType = WorkOrderType.values()[buf.readInt()];
        this.pos = buf.readBlockPos();
        this.packName = buf.readUtf(32767);
        this.path = buf.readUtf(32767);
        this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.mirror = buf.readBoolean();
        this.rotation = Rotation.values()[buf.readInt()];
        this.builder = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(this.workOrderType.ordinal());
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.packName);
        buf.writeUtf(this.path);
        buf.writeUtf(this.dimension.location().toString());
        buf.writeBoolean(this.mirror);
        buf.writeInt(this.rotation.ordinal());
        buf.writeBlockPos(this.builder);
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

        //Verify player has permission to change this hut its settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final Optional<Map.Entry<Integer, IWorkOrder>> wo = colony.getWorkManager().getWorkOrders().entrySet().stream()
          .filter(entry -> entry.getValue() instanceof WorkOrderDecoration)
          .filter(entry -> entry.getValue().getLocation().equals(pos)).findFirst();

        if (wo.isPresent())
        {
            colony.getWorkManager().removeWorkOrder(wo.get().getKey());
            return;
        }

        ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(packName, path),
          player.level,
          (blueprint -> {
              if (blueprint == null)
              {
                  Log.getLogger().error(String.format("Schematic %s doesn't exist on the server.", path));
                  return;
              }

              final String[] split = path.split("/");
              final String displayName = split[split.length - 1].replace(".blueprint", "");

              final BlockState structureState = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getState();
              final WorkOrderType type = structureState != null && !(structureState.getBlock() instanceof BlockDecorationController)
                      ? WorkOrderType.BUILD : workOrderType;

              final WorkOrderDecoration order = WorkOrderDecoration.create(
                  type,
                  packName,
                  path,
                  WordUtils.capitalizeFully(displayName),
                  pos,
                  rotation.ordinal(),
                  mirror,
                  0);

              if (!builder.equals(BlockPos.ZERO))
              {
                  final IBuilding building = colony.getBuildingManager().getBuilding(builder);
                  if (building instanceof AbstractBuildingStructureBuilder)
                  {
                      order.setClaimedBy(builder);
                  }
              }

              colony.getWorkManager().addWorkOrder(order, false);
          })));
    }
}

