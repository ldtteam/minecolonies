package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the crusher mode from the GUI.
 */
public class CrusherSetModeMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the crusher.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

    /**
     * The quantity to produce.
     */
    private int quantity;

    /**
     * The crusher mode.
     */
    private ItemStack crusherMode;

    /**
     * Empty constructor used when registering the 
     */
    public CrusherSetModeMessage()
    {
        super();
    }

    /**
     * Set the mode of the crusher.
     *
     * @param building      the building to set it for.
     * @param dailyQuantity the quantity to produce.
     * @param crusherMode   the mode to set.
     */
    public CrusherSetModeMessage(@NotNull final BuildingCrusher.View building, final ItemStorage crusherMode, final int dailyQuantity)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.quantity = dailyQuantity;
        this.crusherMode = crusherMode.getItemStack();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
        quantity = buf.readInt();
        crusherMode = buf.readItemStack();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
        buf.writeInt(quantity);
        buf.writeItemStack(crusherMode);
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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingCrusher building = colony.getBuildingManager().getBuilding(buildingId, BuildingCrusher.class);
            if (building != null)
            {
                int qty = quantity;
                if (qty > building.getMaxDailyQuantity())
                {
                    qty = building.getMaxDailyQuantity();
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.crusher.toomuch", qty));
                }
                building.setCrusherMode(new ItemStorage(crusherMode), qty);
            }
        }
    }
}
