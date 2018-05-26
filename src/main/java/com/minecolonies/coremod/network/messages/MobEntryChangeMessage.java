package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a message for changing MobEntry priorities
 */
public class MobEntryChangeMessage extends AbstractMessage<MobEntryChangeMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private List<MobEntryView> mobsToAttack = new ArrayList<>();

    /**
     * Empty standard constructor.
     */
    public MobEntryChangeMessage()
    {
        super();
    }

    public MobEntryChangeMessage(
      @NotNull final AbstractBuildingGuards.View building,
                                  final List<MobEntryView> mobsToAttack
    )
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mobsToAttack = new ArrayList<>(mobsToAttack);
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        buf.writeInt(this.colonyId);
        BlockPosUtil.writeToByteBuf(buf, this.buildingId);

        buf.writeInt(this.mobsToAttack.size());
        for (final MobEntryView entry : this.mobsToAttack)
        {
            MobEntryView.writeToByteBuf(buf, entry);
        }
    }

    @Override
    public void fromBytes(final ByteBuf buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);

        final int mobSize = buf.readInt();
        for (int i = 0; i < mobSize; i++)
        {
            final MobEntryView mobEntry = MobEntryView.readFromByteBuf(buf);
            mobsToAttack.add(mobEntry);
        }
    }

    @Override
    public void messageOnServerThread(final MobEntryChangeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingGuards building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingGuards.class);
            if (building != null)
            {
                building.setMobsToAttack(message.mobsToAttack);
            }
        }
    }
}
