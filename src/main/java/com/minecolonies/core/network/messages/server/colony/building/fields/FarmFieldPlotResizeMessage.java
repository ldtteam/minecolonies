package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.tileentities.TileEntityScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;

import static com.minecolonies.core.colony.buildingextensions.FarmField.MAX_RANGE;

/**
 * Message to change the farmer field plot size.
 */
public class FarmFieldPlotResizeMessage implements IMessage
{
    /**
     * The new radius of the field plot.
     */
    private int size;

    /**
     * The specified direction for the new radius.
     */
    private Direction direction;

    /**
     * The field position.
     */
    private BlockPos position;

    /**
     * Forge default constructor
     */
    public FarmFieldPlotResizeMessage()
    {
        super();
    }

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param position  the field position.
     */
    public FarmFieldPlotResizeMessage(int size, Direction direction, BlockPos position)
    {
        super();
        this.size = size;
        this.direction = direction;
        this.position = position;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final BlockEntity fieldBlock = ctxIn.getSender().level.getBlockEntity(position);
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
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(size);
        buf.writeInt(direction.get2DDataValue());
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        size = buf.readInt();
        direction = Direction.from2DDataValue(buf.readInt());
        position = buf.readBlockPos();
    }
}
