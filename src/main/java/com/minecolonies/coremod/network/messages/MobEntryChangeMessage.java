package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a message for changing MobEntry priorities
 */
public class MobEntryChangeMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private List<MobEntryView> mobsToAttack = new ArrayList<>();

    /**
     * The dimension of the message.
     */
    private int dimension;

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
        this.dimension = building.getColony().getDimension();
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
        buf.writeInt(dimension);
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
        dimension = buf.readInt();
    }

    @Override
    public void messageOnServerThread(final MobEntryChangeMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
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
