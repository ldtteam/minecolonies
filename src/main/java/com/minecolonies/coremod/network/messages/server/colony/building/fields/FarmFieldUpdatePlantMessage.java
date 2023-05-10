package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.IFieldDataManager;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the farm field current plant.
 */
public class FarmFieldUpdatePlantMessage extends AbstractColonyServerMessage
{
    /**
     * The new seed to assign to the field.
     */
    private ItemStack newSeed;

    /**
     * The field matcher.
     */
    private FarmField.Matcher matcher;

    /**
     * Forge default constructor
     */
    public FarmFieldUpdatePlantMessage()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param colony  the colony where the field is in.
     * @param newSeed the new seed to assign to the field.
     * @param matcher the field matcher.
     */
    public FarmFieldUpdatePlantMessage(@NotNull IColony colony, ItemStack newSeed, FarmField.Matcher matcher)
    {
        super(colony);
        this.newSeed = newSeed;
        this.matcher = matcher;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        FarmField field = (FarmField) colony.getBuildingManager().getField(matcher);
        if (field == null)
        {
            field = FarmField.create(colony, matcher.getPosition());
        }

        field.setSeed(newSeed);
        colony.getBuildingManager().addOrUpdateField(field);
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeItem(newSeed);
        matcher.toBytes(buf);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        newSeed = buf.readItem();
        matcher = (FarmField.Matcher) IFieldDataManager.getInstance().matcherFromBytes(buf);
    }
}
