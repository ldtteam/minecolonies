package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuardMobAttackListMessage implements IMessage
{
    /**
     * The position of the building.
     */
    private BlockPos buildingId;

    /**
     * The colony the building is within.
     */
    private int colonyId;

    /**
     * List of mobsToAttack.
     */
    private List<MobEntryView> mobsToAttack = new ArrayList<>();

    /**
     * Empty standard constructor.
     */
    public GuardMobAttackListMessage()
    {
        super();
    }

    public GuardMobAttackListMessage(final int colonyId, final BlockPos buildingId, final List<MobEntryView> mobsToAttack)
    {
        super();
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.mobsToAttack = new ArrayList<>(mobsToAttack);
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        this.colonyId = byteBuf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(byteBuf);

        final int mobSize = byteBuf.readInt();
        for (int i = 0; i < mobSize; i++)
        {
            final MobEntryView mobEntry = MobEntryView.readFromByteBuf(byteBuf);
            mobsToAttack.add(mobEntry);
        }
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);

        byteBuf.writeInt(this.mobsToAttack.size());
        for (final MobEntryView entry : this.mobsToAttack)
        {
            MobEntryView.writeToByteBuf(byteBuf, entry);
        }
    }

    @Override
    protected void messageOnClientThread(final GuardMobAttackListMessage message, final MessageContext ctx)
    {
        final IColonyView iColonyView = IColonyManager.getInstance().getColonyView(message.colonyId, Minecraft.getMinecraft().world.world.getDimension().getType().getId());

        if (IColonyView != null)
        {
            @Nullable final AbstractBuildingGuards.View buildingView = (AbstractBuildingGuards.View) IColonyView.getBuilding(message.buildingId);

            if (buildingView != null)
            {
                buildingView.setMobsToAttack(message.mobsToAttack);
            }
        }
    }
}
