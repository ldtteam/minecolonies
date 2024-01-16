package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the farm field current plant.
 */
public class FarmFieldUpdateSeedMessage extends AbstractColonyServerMessage
{
    /**
     * The new seed to assign to the field.
     */
    private ItemStack newSeed;

    /**
     * The field position.
     */
    private BlockPos position;

    /**
     * Forge default constructor
     */
    public FarmFieldUpdateSeedMessage()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param colony   the colony where the field is in.
     * @param newSeed  the new seed to assign to the field.
     * @param position the field position.
     */
    public FarmFieldUpdateSeedMessage(@NotNull IColony colony, ItemStack newSeed, BlockPos position)
    {
        super(colony);
        this.newSeed = newSeed;
        this.position = position;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        colony.getBuildingManager()
          .getField(f -> f.getFieldType().equals(FieldRegistries.farmField.get()) && f.getPosition().equals(position))
          .map(m -> (FarmField) m)
          .ifPresent(field -> field.setSeed(newSeed));
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeItem(newSeed);
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        newSeed = buf.readItem();
        position = buf.readBlockPos();
    }
}
