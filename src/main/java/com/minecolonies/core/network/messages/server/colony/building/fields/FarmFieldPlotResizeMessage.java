package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.tileentities.TileEntityScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

import static com.minecolonies.core.colony.buildingextensions.FarmField.DEFAULT_RANGE;
import static com.minecolonies.core.colony.buildingextensions.FarmField.MAX_RANGE;

/**
 * Message to change the farmer field plot size.
 */
public class FarmFieldPlotResizeMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "farm_field_plot_resize", FarmFieldPlotResizeMessage::new);

    /**
     * The new radius of the field plot.
     */
    private final int size;

    /**
     * The specified direction for the new radius.
     */
    private final Direction direction;

    /**
     * The field position.
     */
    private final BlockPos position;

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param position  the field position.
     */
    public FarmFieldPlotResizeMessage(final int size, final Direction direction, final BlockPos position)
    {
        super(TYPE);
        this.size = size;
        this.direction = direction;
        this.position = position;
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        final BlockEntity fieldBlock = player.level().getBlockEntity(position);
        if (fieldBlock instanceof TileEntityScarecrow scarecrow)
        {
            final int currentSum = Arrays.stream(scarecrow.getFieldSize()).sum();
            final int currentDirSize = scarecrow.getFieldSize()[direction.get2DDataValue()];

            if (size < 0 || (size > currentDirSize && currentSum - currentDirSize + size > MAX_RANGE))
            {
                return;
            }

            scarecrow.setFieldSize(direction, size);
            final IColony colony = scarecrow.getCurrentColony();
            if (colony != null)
            {
                colony.getServerBuildingManager()
                    .getMatchingBuildingExtension(f -> f.getBuildingExtensionType().equals(BuildingExtensionRegistries.farmField.get()) && f.getPosition().equals(position))
                    .map(m -> (FarmField) m)
                    .ifPresent(field -> field.setRadius(direction, size));
            }
        }
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(size);
        buf.writeInt(direction.get2DDataValue());
        buf.writeBlockPos(position);
    }

    protected FarmFieldPlotResizeMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        size = buf.readInt();
        direction = Direction.from2DDataValue(buf.readInt());
        position = buf.readBlockPos();
    }
}
