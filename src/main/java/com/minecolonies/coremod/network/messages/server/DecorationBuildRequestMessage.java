package com.minecolonies.coremod.network.messages.server;

import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Adds a entry to the builderRequired map.
 */
public class DecorationBuildRequestMessage extends AbstractBuildRequestMessage
{
    /**
     * The name of the decoration.
     */
    private String name;

    /**
     * The level of the decoration.
     */
    private int level;

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
     */
    public DecorationBuildRequestMessage(@NotNull final BlockPos pos, final String name, final int level)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.fromBytes(buf);
        this.name = buf.readUtf(32767);
        this.level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeUtf(this.name);
        buf.writeInt(this.level);
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
        final Level world = CompatibilityUtils.getWorldFromEntity(ctxIn.getSender());
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
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

            final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(colony.getWorld(), this.pos, name + level, new PlacementSettings(), true);
            final PlacementSettings settings = ((TileEntityDecorationController) entity).calculatePlacementSettings(structure.getBluePrint());

            final String woName = BlueprintUtil.getDescriptiveName(new StructureName(structure.getBluePrint().getName()), settings.getWallExtents());

            final WorkOrderBuildDecoration order = new WorkOrderBuildDecoration(
                    name + level,
                    woName,
                    pos,
                    settings);
            if (!builder.equals(BlockPos.ZERO))
            {
                order.setClaimedBy(builder);
            }

            if (level != ((TileEntityDecorationController) entity).getTier())
            {
                order.setLevelUp();
            }
            colony.getWorkManager().addWorkOrder(order, false);
        }
    }
}
