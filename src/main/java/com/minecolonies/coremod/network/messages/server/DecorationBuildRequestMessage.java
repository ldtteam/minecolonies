package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.colony.workorders.WorkOrderDecoration;
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
     * @param pos         the position of it.
     * @param packName    pack name.
     * @param path        blueprint path.
     * @param level       the level.
     * @param dimension   the dimension we're executing on.
     */
    public DecorationBuildRequestMessage(@NotNull final BlockPos pos, final String packName, final String path, final int level, final ResourceKey<Level> dimension)
    {
        super();
        this.pos = pos;
        this.packName = packName;
        this.path = path;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.packName = buf.readUtf(32767);
        this.path = buf.readUtf(32767);
        this.level = buf.readInt();
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.packName);
        buf.writeUtf(this.path);
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

        //Verify player has permission to change this hut its settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final BlockEntity entity = player.getCommandSenderWorld().getBlockEntity(pos);
        if (entity instanceof TileEntityDecorationController)
        {
            final Optional<Map.Entry<Integer, IWorkOrder>> wo = colony.getWorkManager().getWorkOrders().entrySet().stream()
              .filter(entry -> entry.getValue() instanceof WorkOrderDecoration)
              .filter(entry -> entry.getValue().getLocation().equals(pos)).findFirst();

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
                        Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", path + level));
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
            final int currentLevel = ((TileEntityDecorationController) entity).getTier();
            WorkOrderDecoration order;
            if (level > currentLevel)
            {
                //todo
                order = WorkOrderDecoration.create(
                  WorkOrderType.UPGRADE,
                  path + level,
                  WordUtils.capitalizeFully(displayName),
                  pos,
                  difference,
                  state.getValue(BlockDecorationController.MIRROR),
                  currentLevel);
            }
            else if (level == currentLevel)
            {
                order = WorkOrderDecoration.create(
                  WorkOrderType.REPAIR,
                  name + level,
                  WordUtils.capitalizeFully(displayName),
                  pos,
                  difference,
                  state.getValue(BlockDecorationController.MIRROR),
                  currentLevel);
            }
            else
            {
                order = WorkOrderDecoration.create(
                  WorkOrderType.BUILD,
                  name + level,
                  WordUtils.capitalizeFully(displayName),
                  pos,
                  difference,
                  state.getValue(BlockDecorationController.MIRROR),
                  currentLevel);
            }

            colony.getWorkManager().addWorkOrder(order, false);
        }
    }
}
