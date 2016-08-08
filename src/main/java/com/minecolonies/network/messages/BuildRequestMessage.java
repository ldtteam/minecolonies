package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.EntityUtils;
import com.minecolonies.util.LanguageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Adds a entry to the builderRequired map
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage implements IMessage, IMessageHandler<BuildRequestMessage, IMessage>
{
    private             BlockPos        buildingId;
    private             int              colonyId;
    private             int              mode;


    public static final int              BUILD  = 0;
    public static final int              REPAIR = 1;


    public BuildRequestMessage(){}

    /**
     * Creates a build request message
     *
     * @param building      AbstractBuilding of the request
     * @param mode          Mode of the request, 1 is repair, 0 is build
     */
    public BuildRequestMessage(AbstractBuilding.View building, int mode)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        mode = buf.readInt();
    }

    @Override
    public IMessage onMessage(BuildRequestMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null)
        {
            return null;
        }

        AbstractBuilding building = colony.getBuilding(message.buildingId);
        if (building == null)
        {
            return null;
        }

        int requiredBuildingLevel = colony.getBuilding(message.buildingId).getBuildingLevel()+1;

        switch(message.mode)
        {
            case BUILD:

                if(colony.getBuilderLevel() >= requiredBuildingLevel || building instanceof BuildingBuilder)
                {
                    building.requestUpgrade();
                }
                else
                {
                    LanguageHandler.sendPlayersLocalizedMessage(EntityUtils.getPlayersFromUUID(colony.getWorld(), colony.getPermissions().getMessagePlayers()),
                                                                "entity.builder.messageBuilderNecessary" + requiredBuildingLevel);
                }
                break;
            case REPAIR:
                building.requestRepair();
                break;
        }

        return null;
    }
}
