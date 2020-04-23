package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
     * The dimension of the 
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
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(this.buildingId);

        buf.writeInt(this.mobsToAttack.size());
        for (final MobEntryView entry : this.mobsToAttack)
        {
            MobEntryView.writeToByteBuf(buf, entry);
        }
        buf.writeInt(dimension);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();

        final int mobSize = buf.readInt();
        for (int i = 0; i < mobSize; i++)
        {
            final MobEntryView mobEntry = MobEntryView.readFromByteBuf(buf);
            mobsToAttack.add(mobEntry);
        }
        dimension = buf.readInt();
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

            @Nullable final AbstractBuildingGuards building = colony.getBuildingManager().getBuilding(buildingId, AbstractBuildingGuards.class);
            if (building != null)
            {
                building.setMobsToAttack(mobsToAttack);
            }
        }
    }
}
