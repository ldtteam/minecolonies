package com.minecolonies.coremod.network.messages.server.colony.building.beekeeper;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Message to set the beekeeper scepter in the player inventory.
 */
public class BeekeeperScepterMessage extends AbstractBuildingServerMessage<BuildingBeekeeper>
{
    /**
     * Empty standard constructor.
     */
    public BeekeeperScepterMessage()
    {
        super();
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }

    public BeekeeperScepterMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingBeekeeper building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final ItemStack scepter = InventoryUtils.getOrCreateItemAndPutToHotbarAndSelectOrDrop(ModItems.scepterBeekeeper,
            player,
            ModItems.scepterBeekeeper::getDefaultInstance,
            true);
        final CompoundNBT compound = scepter.getOrCreateTag();

        BlockPosUtil.write(compound, TAG_POS, building.getID());
        compound.putInt(TAG_ID, colony.getID());

        player.inventory.markDirty();
    }
}
