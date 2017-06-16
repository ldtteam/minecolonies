package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingLumberjack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for setting which trees the lj should cut.
 */
public class LumberjackSaplingSelectorMessage extends AbstractMessage<LumberjackSaplingSelectorMessage, IMessage>
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The lumberjacks building id.
     */
    private BlockPos buildingId;

    /**
     * The ItemStack of the sapling.
     */
    private ItemStack stack;

    /**
     * Wether the lumberjack should cut or not.
     */
    private boolean shouldCut;

    /**
     * Empty standard constructor.
     */
    public LumberjackSaplingSelectorMessage()
    {
        super();
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Creates a message which will be sent to set the new settings in the lumberjack.
     * @param building the building view of the lumberjack.
     * @param saplingStack the stack to set.
     * @param shouldCut wether or not the tree should be cut.
     */
    public LumberjackSaplingSelectorMessage(final BuildingLumberjack.View building, final ItemStack saplingStack, final boolean shouldCut)
    {

        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.stack = saplingStack;
        this.shouldCut = shouldCut;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        stack = ByteBufUtils.readItemStack(buf);
        shouldCut = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeBoolean(shouldCut);
    }

    @Override
    public void messageOnServerThread(final LumberjackSaplingSelectorMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingWorker building = colony.getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building instanceof BuildingLumberjack)
            {
                ((BuildingLumberjack) building).setTreeToCut(message.stack, message.shouldCut);
            }
        }
    }
}
