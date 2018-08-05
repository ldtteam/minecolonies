package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignBakerRecipeMessage extends AbstractMessage<AssignBakerRecipeMessage, IMessage>
{

    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assign;
    private BlockPos field;
    private int		 recipePos;

    /**
     * Empty standard constructor.
     */
    public AssignBakerRecipeMessage()
    {
        super();
    }

    /**
     * Creates the message to assign a field.
     *
     * @param building the farmer to assign to or release from.
     * @param assign   assign if true, free if false.
     * @param field    the field to assign or release.
     */
    public AssignBakerRecipeMessage(@NotNull final BuildingBaker.View building,final int pos, final boolean assign, final BlockPos field)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.field = field;
        this.recipePos = pos;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assign = buf.readBoolean();
        field = BlockPosUtil.readFromByteBuf(buf);
        recipePos = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assign);
        BlockPosUtil.writeToByteBuf(buf, field);
        buf.writeInt(recipePos);
    }

    @Override
    public void messageOnServerThread(final AssignBakerRecipeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingBaker building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingBaker.class);
            if (building != null)
            {
                building.setRecipeAllowed(message.recipePos ,message.assign);
            }
        }
    }
}

