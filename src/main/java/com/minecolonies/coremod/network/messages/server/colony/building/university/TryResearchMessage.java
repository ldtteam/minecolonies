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
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;
import static com.minecolonies.api.research.util.ResearchConstants.MAX_DEPTH;

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
     * If the request is a reset.
     */
    private boolean reset;

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
    public TryResearchMessage(final IBuildingView building, @NotNull final String researchId, final String branch, final boolean reset)
    {
        super(building);
        this.researchId = researchId;
        this.branch = branch;
        this.reset = reset;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        researchId = buf.readString(32767);
        branch = buf.readString(32767);
        reset = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeString(researchId);
        buf.writeString(branch);
        buf.writeBoolean(reset);
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
        if(reset)
        {
            if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId) != null)
            {
                attemptResetResearch(player, colony, research);
            }
        }
        else
        {
            attemptBeginResearch(player, colony, building, research);
        }
    }

    /**
     * Attempt to begin a research.
     * @param player     the player making the request (and to apply costs toward)
     * @param colony     the colony doing the research
     * @param building   the university doing the research
     * @param research   the research.
     */
    private void attemptBeginResearch(final PlayerEntity player, final IColony colony, final BuildingUniversity building, final IGlobalResearch research)
    {
        if (colony.getResearchManager().getResearchTree().getResearch(branch, researchId) == null)
        {
            if (research.canResearch(building.getBuildingLevel() == building.getMaxBuildingLevel() ? Integer.MAX_VALUE : building.getBuildingLevel(),
              colony.getResearchManager().getResearchTree()) && research.hasEnoughResources(new InvWrapper(player.inventory)) || player.isCreative())
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
                    return;
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
                    // Remove items from player
                    if (!InventoryUtils.tryRemoveStackFromItemHandler(new InvWrapper(player.inventory), research.getCostList()))
                    {
                        player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable", new TranslationTextComponent(research.getDesc())),
                          player.getUniqueID());
                        return;
                    }
                }
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.started", new TranslationTextComponent(research.getDesc())),
                  player.getUniqueID());
                research.startResearch(colony.getResearchManager().getResearchTree());
            }
        }
        else
        {
            if(player.isCreative())
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get())
                {
                    colony.getResearchManager()
                      .getResearchTree()
                      .getResearch(branch, research.getId())
                      .setProgress((int) (BASE_RESEARCH_TIME * Math.pow(2, research.getDepth() - 1)));
                }
            }
            else
            {
                player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.alreadystarted"), player.getUniqueID());
            }
        }
        colony.markDirty();
    }

    /**
     * Attempt to reset research for a colony.
     * @param player     the player making the request (and to apply costs toward)
     * @param colony     the colony to remove the research from.
     * @param research   the research.
     */
    private void attemptResetResearch(final PlayerEntity player, final IColony colony, final IGlobalResearch research)
    {
        // If in progress and get another request, cancel research, and remove it from the local tree.
        if(colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.IN_PROGRESS)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.stopped", new TranslationTextComponent(research.getDesc())),
              player.getUniqueID());
            colony.getResearchManager().getResearchTree().cancelResearch(branch, researchId, null);
        }
        // If complete, it's a request to undo the research.
        else if (colony.getResearchManager().getResearchTree().getResearch(branch, researchId).getState() == ResearchState.FINISHED)
        {
            if(!player.isCreative())
            {
                final List<ItemStorage> costList = new ArrayList<>();
                for (final String cost : IGlobalResearchTree.getInstance().getResearchResetCosts())
                {
                    // Validated cost metrics during ResearchListener, so doesn't need to be redone here.
                    // Do, however, need to check against air, in case item type does not exist.
                    final String[] costParts = cost.split(":");
                    final Item costItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1]));
                    if(!costItem.equals(Items.AIR))
                    {
                        costList.add(new ItemStorage(new ItemStack(costItem, Integer.parseInt(costParts[2])), false, true));
                    }
                }
                if (!InventoryUtils.tryRemoveStackFromItemHandler(new InvWrapper(player.inventory), costList))
                {
                    player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.costnotavailable", new TranslationTextComponent(research.getDesc())),
                      player.getUniqueID());
                    return;
                }
            }
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.undo", new TranslationTextComponent(research.getDesc())),
              player.getUniqueID());
            colony.getResearchManager().getResearchTree().cancelResearch(branch, researchId, colony);
        }
        colony.markDirty();
    }
}
