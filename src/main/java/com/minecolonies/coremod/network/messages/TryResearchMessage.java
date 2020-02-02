package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;

/**
 * Message for the research execution.
 */
public class TryResearchMessage implements IMessage
{
    /**
     * ID of the colony
     */
    private int colonyId;

    /**
     * The dimension of the
     */
    private int dimension;

    /**
     * Id of research to try research.
     */
    private String researchId;

    /**
     * Id of research to try research.
     */
    private String branch;

    /**
     * Position of university in colony.
     */
    private BlockPos university;

    /**
     * Default constructor for forge
     */
    public TryResearchMessage() {super();}

    /**
     * Construct a message to attempt to research.
     * @param researchId the research id.
     * @param branch the research branch.
     * @param colonyId the colony id.
     * @param dimension the dimension.
     */
    public TryResearchMessage(@NotNull final String researchId, final String branch, final int colonyId, final int dimension, final BlockPos university)
    {
        super();
        this.researchId = researchId;
        this.branch = branch;
        this.colonyId = colonyId;
        this.dimension = dimension;
        this.university = university;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        dimension = buf.readInt();
        researchId = buf.readString(32767);
        branch = buf.readString(32767);
        university = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(dimension);
        buf.writeString(researchId);
        buf.writeString(branch);
        buf.writeBlockPos(university);
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

        if (colony == null)
        {
            return;
        }
        final PlayerEntity player = ctxIn.getSender();
        if (player == null || !colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        if (colony.getResearchManager().getResearchTree().getResearch(branch, researchId) == null)
        {
            final IBuilding uni = colony.getBuildingManager().getBuilding(university);
            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchId);
            if (research.canResearch(uni.getBuildingLevel(), colony.getResearchManager().getResearchTree()) && research.hasEnoughResources(new InvWrapper(player.inventory)))
            {
                if (!research.getResearchRequirement().isFulfilled(colony))
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.requirementnotmet"));
                    return;
                }

                if (player.isCreative())
                {
                    research.startResearch(player, colony.getResearchManager().getResearchTree());
                    colony.getResearchManager().getResearchTree().getResearch(branch, researchId).setProgress((int) (BASE_RESEARCH_TIME * Math.pow(2, research.getDepth()-1)));
                }
                else
                {
                    for (final ItemStorage cost : research.getCostList())
                    {
                        InventoryUtils.removeStackFromItemHandler(new InvWrapper(player.inventory), cost.getItemStack(), cost.getAmount());
                    }

                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.started"));
                    research.startResearch(player, colony.getResearchManager().getResearchTree());
                }
                colony.markDirty();
                // Remove items from player
            }
        }
        else
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.alreadystarted"));
        }
    }
}
