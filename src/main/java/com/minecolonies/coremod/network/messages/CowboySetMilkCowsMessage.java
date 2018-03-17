package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingCowboy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CowboySetMilkCowsMessage extends AbstractMessage<CowboySetMilkCowsMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private boolean milkCows;

    /**
     * Empty standard constructor.
     */
    public CowboySetMilkCowsMessage()
    {
        super();
    }

    /**
     * Creates object for the CowboySetMilk message.
     *
     * @param building       View of the building to read data from.
     * @param milkCows       Whether Cowboy should milk cows.
     */
    public CowboySetMilkCowsMessage(@NotNull final BuildingCowboy.View building, final boolean milkCows)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.milkCows = milkCows;
    }


    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        colonyId = byteBuf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(byteBuf);
        milkCows = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);
        byteBuf.writeBoolean(milkCows);
    }

    @Override
    public void messageOnServerThread(final CowboySetMilkCowsMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingCowboy building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingCowboy.class);
            if (building != null)
            {
                building.setMilkCows(message.milkCows);
            }
        }
    }
}
