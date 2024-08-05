package com.minecolonies.core.network.messages.server.colony.building.university;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message for the research execution.
 */
public class TryResearchMessage extends AbstractBuildingServerMessage<BuildingUniversity>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "try_research_message", TryResearchMessage::new);

    /**
     * Id of research to try research.
     */
    private final ResourceLocation researchId;

    /**
     * Id of research to try research.
     */
    private final ResourceLocation branch;

    /**
     * If the request is a reset.
     */
    private final boolean reset;

    /**
     * Construct a message to attempt to research.
     *
     * @param researchId the research id.
     * @param branch     the research branch.
     * @param building   the building we're executing on.
     */
    public TryResearchMessage(final IBuildingView building, @NotNull final ResourceLocation researchId, final ResourceLocation branch, final boolean reset)
    {
        super(TYPE, building);
        this.researchId = researchId;
        this.branch = branch;
        this.reset = reset;
    }

    protected TryResearchMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        researchId = buf.readResourceLocation();
        branch = buf.readResourceLocation();
        reset = buf.readBoolean();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeResourceLocation(researchId);
        buf.writeResourceLocation(branch);
        buf.writeBoolean(reset);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final BuildingUniversity building)
    {
        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchId);
        if(reset)
        {
            if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null)
            {
                colony.getResearchManager().getResearchTree().attemptResetResearch(player, colony, colony.getResearchManager().getResearchTree().getResearch(branch, researchId));
            }
        }
        else
        {
            if((research.canResearch(building.getBuildingLevel() == building.getMaxBuildingLevel() ? Integer.MAX_VALUE : building.getBuildingLevel(), colony.getResearchManager().getResearchTree()))
                 || player.isCreative())
            {
                colony.getResearchManager().getResearchTree().attemptBeginResearch(player, colony, research);
            }
        }
    }
}
