package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.workorders.WorkOrderDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Adds a entry to the builderRequired map.
 */
public class DecorationBuildRequestMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "decoration_build_request", DecorationBuildRequestMessage::new);

    /**
     * The id of the building.
     */
    private final BlockPos pos;

    /**
     * The display name of the decoration.
     */
    private final String packName;

    /**
     * The name of the decoration.
     */
    private final String path;

    /**
     * The rotation.
     */
    private final RotationMirror rotationMirror;

    /**
     * The dimension.
     */
    private final ResourceKey<Level> dimension;

    /**
     * Type of workorder.
     */
    private final WorkOrderType workOrderType;

    /**
     * The builder, or ZERO to auto-assign.
     */
    private final BlockPos builder;

    /**
     * Creates a build request for a decoration.
     *
     * @param pos         the position of it.
     * @param packName    pack name.
     * @param path        blueprint path.
     * @param dimension   the dimension we're executing on.
     */
    public DecorationBuildRequestMessage(final WorkOrderType workOrderType, @NotNull final BlockPos pos, final String packName, final String path, final ResourceKey<Level> dimension, final RotationMirror rotationMirror, final BlockPos builder)
    {
        super(TYPE);
        this.workOrderType = workOrderType;
        this.pos = pos;
        this.packName = packName;
        this.path = path;
        this.dimension = dimension;
        this.rotationMirror = rotationMirror;
        this.builder = builder;
    }

    protected DecorationBuildRequestMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.workOrderType = WorkOrderType.values()[buf.readInt()];
        this.pos = buf.readBlockPos();
        this.packName = buf.readUtf(32767);
        this.path = buf.readUtf(32767);
        this.dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        this.rotationMirror = RotationMirror.values()[buf.readByte()];
        this.builder = buf.readBlockPos();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(this.workOrderType.ordinal());
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.packName);
        buf.writeUtf(this.path);
        buf.writeUtf(this.dimension.location().toString());
        buf.writeByte(rotationMirror.ordinal());
        buf.writeBlockPos(this.builder);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromDim(dimension, pos);
        if (colony == null)
        {
            return;
        }

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

        ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(packName, path, colony.getWorld().registryAccess()),
          player.level(),
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
                  rotationMirror,
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

