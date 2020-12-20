package com.minecolonies.coremod.network.messages.server.colony.building.university;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;

/**
 * Message for the research execution.
 */
public class TryResearchMessage extends AbstractBuildingServerMessage<BuildingUniversity>
{
    /**
     * Id of research to try research.
     */
    private String researchId;

    /**
     * Id of research to try research.
     */
    private String branch;

    /**
     * Default constructor for forge
     */
    public TryResearchMessage() {super();}

    /**
     * Construct a message to attempt to research.
     *
     * @param researchId the research id.
     * @param branch     the research branch.
     * @param building   the building we're executing on.
     */
    public TryResearchMessage(final IBuildingView building, @NotNull final String researchId, final String branch)
    {
        super(building);
        this.researchId = researchId;
        this.branch = branch;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        researchId = buf.readString(32767);
        branch = buf.readString(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeString(researchId);
        buf.writeString(branch);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingUniversity building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchId);
        if (colony.getResearchManager().getResearchTree().getResearch(branch, researchId) == null ||
              (colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null
                 && colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.CANCELED))
        {
            if (research.canResearch(building.getBuildingLevel() == building.getMaxBuildingLevel() ? Integer.MAX_VALUE : building.getBuildingLevel(), colony.getResearchManager().getResearchTree())
                  && research.hasEnoughResources(new InvWrapper(player.inventory)) || player.isCreative()
                  || (colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null
                        && colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.CANCELED))
            {
                if (player.isCreative())
                {
                    research.startResearch(colony.getResearchManager().getResearchTree());
                    if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get())
                    {
                        colony.getResearchManager()
                          .getResearchTree()
                          .getResearch(branch, research.getId())
                          .setProgress((int) (BASE_RESEARCH_TIME * Math.pow(2, research.getDepth() - 1)));
                    }
                }
                else if (!research.getResearchRequirement().isEmpty())
                {
                    for (IResearchRequirement requirement : research.getResearchRequirement())
                    {
                        if (!requirement.isFulfilled(colony))
                        {
                            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.requirementnotmet"), player.getUniqueID());
                            return;
                        }
                    }
                }
                if (!player.isCreative() && colony.getResearchManager().getResearchTree().getResearch(branch, research.getId()) != null &&
                      colony.getResearchManager().getResearchTree().getResearch(branch, research.getId()).getState() != ResearchState.CANCELED)
                {
                    // Remove items from player
                    for (final ItemStorage cost : research.getCostList())
                    {
                        InventoryUtils.removeStackFromItemHandler(new InvWrapper(player.inventory), cost.getItemStack(), cost.getAmount());
                    }
                }
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.started", new TranslationTextComponent(research.getDesc())),
                  player.getUniqueID());
                research.startResearch(colony.getResearchManager().getResearchTree());
                colony.markDirty();
            }
        }
        else
        {
            if (player.isCreative() && MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get()
                  && colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.IN_PROGRESS)
            {
                colony.getResearchManager().getResearchTree().getResearch(branch, research.getId()).setProgress((int) (BASE_RESEARCH_TIME * Math.pow(2, research.getDepth() - 1)));
            }
            // If in progress and get another request, cancel research, and set progress to zero.
            else if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.IN_PROGRESS)
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.stopped", new TranslationTextComponent(research.getDesc())),
                  player.getUniqueID());
                colony.getResearchManager().getResearchTree().cancelResearch(branch, researchId, false);
            }
            // If complete, it's a request to undo the research.
            else if (colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.FINISHED)
            {
                if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null)
                {
                    player.sendMessage(new TranslationTextComponent("Research Removal not enabled."),
                      player.getUniqueID());
                    colony.getResearchManager().getResearchTree().cancelResearch(branch, researchId, true);
                }
            }
            else
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.alreadystarted"), player.getUniqueID());
            }
            colony.markDirty();
        }
    }
}
