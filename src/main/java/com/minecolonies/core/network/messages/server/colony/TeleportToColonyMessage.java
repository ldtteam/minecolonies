package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.connections.DiplomacyStatus;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingGateHouse;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_GATE;

/**
 * Message for trying to teleport to a friends colony.
 */
public class TeleportToColonyMessage extends AbstractColonyServerMessage
{
    /**
     * Origin colony id.
     */
    private int originColonyId;

    /**
     * Teleportation cost.
     */
    private int cost;

    /**
     * Gatehouse pos to teleport to.
     */
    private BlockPos pos;

    public TeleportToColonyMessage()
    {
        super();
    }

    public TeleportToColonyMessage(final ResourceKey<Level> dimensionId, final int colonyId, final BlockPos pos, final int originColonyId, final int cost)
    {
        super(dimensionId, colonyId);
        this.pos = pos;
        this.originColonyId = originColonyId;
        this.cost = cost;
    }

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return null;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (ctxIn.getSender() == null)
        {
            return;
        }

        final IColony originColony = IColonyManager.getInstance().getColonyByDimension(originColonyId, ctxIn.getSender().level.dimension());
        if (originColony == null)
        {
            return;
        }

        if (originColony.getConnectionManager().getColonyDiplomacyStatus(colony.getID()) != DiplomacyStatus.ALLIES)
        {
            return;
        }

        if (originColony.getPermissions().hasPermission(ctxIn.getSender(), Action.TELEPORT_TO_COLONY) || colony.getPermissions().hasPermission(ctxIn.getSender(), Action.TELEPORT_TO_COLONY))
        {
            final BlockEntity gateHouse = colony.getWorld().getBlockEntity(pos);
            if (gateHouse instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) gateHouse).getBuilding() instanceof BuildingGateHouse)
            {
                if (cost > 0)
                {
                    if (InventoryUtils.attemptReduceStackInItemHandler(new InvWrapper(ctxIn.getSender().getInventory()), new ItemStack(Items.GOLD_NUGGET), cost))
                    {
                        int output = cost/2;
                        if (output <= STACKSIZE)
                        {
                            InventoryUtils.addItemStackToItemHandler(((TileEntityColonyBuilding) gateHouse).getInventory(), new ItemStack(Items.GOLD_NUGGET, output));
                        }
                        else
                        {
                            for (int i = 0; i < output/STACKSIZE; i++)
                            {
                                if (output > 0)
                                {
                                    final int qty = Math.min(STACKSIZE, output);
                                    InventoryUtils.addItemStackToItemHandler(((TileEntityColonyBuilding) gateHouse).getInventory(), new ItemStack(Items.GOLD_NUGGET, qty));
                                    output -= qty;
                                }
                            }
                        }
                    }
                }

                final List<BlockPos> posList = ((TileEntityColonyBuilding) gateHouse).getCachedWorldTagNamePosMap().get(TAG_GATE);
                if (posList == null || posList.isEmpty())
                {
                    TeleportHelper.colonyTeleport(ctxIn.getSender(), colony, pos);
                }
                else
                {
                    TeleportHelper.colonyTeleport(ctxIn.getSender(), colony, posList.get(MathUtils.RANDOM.nextInt(posList.size())));
                }
            }
            else
            {
                TeleportHelper.colonyTeleport(ctxIn.getSender(), colony, pos);
            }
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(originColonyId);
        buf.writeInt(cost);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.originColonyId = buf.readInt();
        this.cost = buf.readInt();
    }
}
