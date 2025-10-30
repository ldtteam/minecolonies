package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to regenerate civilian names with mismatched name packs.
 */
public class ColonyRegenerateNamesMessage extends AbstractColonyServerMessage
{
    /**
     * Default constructor.
     */
    public ColonyRegenerateNamesMessage()
    {
        super();
    }

    /**
     * Constructs a message to regenerate names.
     *
     * @param colony the colony to regenerate names for.
     */
    public ColonyRegenerateNamesMessage(final IColony colony)
    {
        super(colony);
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        // Calculate required nametags: 1 per 25 citizens without special names, minimum 1
        final long citizensWithoutSpecialNames = colony.getCitizenManager().getCitizens().stream()
            .filter(citizen -> !citizen.hasSpecialName())
            .count();
        final int requiredNametags = Math.max(1, (int) (citizensWithoutSpecialNames / 25));

        // Check if player has enough nametags (skip check for creative mode)
        if (!player.isCreative())
        {
            final int availableNametags = InventoryUtils.getItemCountInItemHandler(
                new InvWrapper(player.getInventory()),
                Items.NAME_TAG);

            if (availableNametags < requiredNametags)
            {
                return;
            }

            InventoryUtils.reduceStackInItemHandler(
                new InvWrapper(player.getInventory()),
                new ItemStack(Items.NAME_TAG),
                requiredNametags);
        }

        colony.regenerateAllNames();
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
    }
}
