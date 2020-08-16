package com.minecolonies.coremod.network.messages.client.colony.building.guard;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuardMobAttackListMessage implements IMessage
{
    /**
     * The position of the building.
     */
    private final BlockPos buildingId;

    /**
     * The colony the building is within.
     */
    private final int colonyId;

    /**
     * List of mobsToAttack.
     */
    private final List<MobEntryView> mobsToAttack;

    /**
     * Empty standard constructor.
     */
    public GuardMobAttackListMessage(final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();

        final int mobSize = buf.readInt();
        this.mobsToAttack = new ArrayList<>(mobSize);
        for (int i = 0; i < mobSize; i++)
        {
            final MobEntryView mobEntry = MobEntryView.readFromByteBuf(buf);
            this.mobsToAttack.add(mobEntry);
        }
    }

    public GuardMobAttackListMessage(final int colonyId, final BlockPos buildingId, final List<MobEntryView> mobsToAttack)
    {
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.mobsToAttack = new ArrayList<>(mobsToAttack);
    }

    @Override
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeInt(colonyId);
        byteBuf.writeBlockPos(buildingId);

        byteBuf.writeInt(this.mobsToAttack.size());
        for (final MobEntryView entry : this.mobsToAttack)
        {
            MobEntryView.writeToByteBuf(byteBuf, entry);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColonyView IColonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().world.getDimensionKey().func_240901_a_());

        if (IColonyView != null)
        {
            @Nullable final AbstractBuildingGuards.View buildingView = (AbstractBuildingGuards.View) IColonyView.getBuilding(buildingId);

            if (buildingView != null)
            {
                buildingView.setMobsToAttack(mobsToAttack);
            }
        }
    }
}
