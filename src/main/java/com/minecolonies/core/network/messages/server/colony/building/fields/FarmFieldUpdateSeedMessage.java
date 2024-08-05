package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the farm field current plant.
 */
public class FarmFieldUpdateSeedMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "farm_field_update_seed", FarmFieldUpdateSeedMessage::new);

    /**
     * The new seed to assign to the field.
     */
    private final ItemStack newSeed;

    /**
     * The field position.
     */
    private final BlockPos position;

    /**
     * Default constructor.
     *
     * @param colony   the colony where the field is in.
     * @param newSeed  the new seed to assign to the field.
     * @param position the field position.
     */
    public FarmFieldUpdateSeedMessage(@NotNull final IColony colony, final ItemStack newSeed, final BlockPos position)
    {
        super(TYPE, colony);
        this.newSeed = newSeed;
        this.position = position;
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.getBuildingManager()
          .getField(f -> f.getFieldType().equals(FieldRegistries.farmField.get()) && f.getPosition().equals(position))
          .map(m -> (FarmField) m)
          .ifPresent(field -> field.setSeed(newSeed));
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        Utils.serializeCodecMess(buf, newSeed);
        buf.writeBlockPos(position);
    }

    protected FarmFieldUpdateSeedMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        newSeed = Utils.deserializeCodecMess(buf);
        position = buf.readBlockPos();
    }
}
