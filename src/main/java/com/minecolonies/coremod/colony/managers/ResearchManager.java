package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.research.LocalResearch;
import com.minecolonies.coremod.research.LocalResearchTree;
import com.minecolonies.coremod.research.ResearchEffectManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.minecolonies.api.util.constant.TranslationConstants.RESEARCH_CONCLUDED;

/**
 * Research manager of the colony.
 */
public class ResearchManager implements IResearchManager
{
    /**
     * The research tree of the colony, containing completed or in-progress research.
     */
    private final LocalResearchTree tree = new LocalResearchTree();

    /**
     * The active research effects of the colony.
     */
    private final IResearchEffectManager effects = new ResearchEffectManager();

    /**
     * The research Ids of any research set to start automatically when unhidden.
     */
    private final List<IGlobalResearch> autoStartResearch = new ArrayList<>();

    /**
     * The colony the ResearchManager applies toward.
     * This may be a ColonyView, where the ResearchManager is inside of a ColonyView.
     */
    private final IColony colony;

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        tree.readFromNBT(compound, effects);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        tree.writeToNBT(compound);
    }

    public ResearchManager(IColony colony)
    {
        this.colony = colony;
        autoStartResearch.addAll(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getAutostartResearches());
    }

    @Override
    public LocalResearchTree getResearchTree()
    {
        return this.tree;
    }

    @Override
    public IResearchEffectManager getResearchEffects()
    {
        return this.effects;
    }

    @Override
    public ResourceLocation getResearchEffectIdFrom(Block block)
    {
        return new ResourceLocation(block.getRegistryName().getNamespace(), "effects/" + block.getRegistryName().getPath());
    }

    @Override
    public void checkAutoStartResearch()
    {
        if(colony == null || !(colony instanceof Colony))
        {
            return;
        }
        final List<IGlobalResearch> removes = new ArrayList<>();
        for(IGlobalResearch research : autoStartResearch)
        {
            if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), colony))
            {
                continue;
            }
            // Unlockable Branch Research should trigger even if the university isn't at the required depth. Otherwise, we do need to consider it. CheckAutoStart will rerun on the university upgrade completion.
            if(IGlobalResearchTree.getInstance().getBranchData(research.getBranch()).getType() != ResearchBranchType.UNLOCKABLES)
            {
                int level = 0;
                Map<BlockPos, IBuilding> buildings = colony.getBuildingManager().getBuildings();
                for (Map.Entry<BlockPos, IBuilding> building : buildings.entrySet())
                {
                    if (building.getValue().getBuildingType() == ModBuildings.university)
                    {
                        if (building.getValue().getBuildingLevel() > level)
                        {
                            level = building.getValue().getBuildingLevel();
                        }
                    }
                }
                if (level < research.getDepth())
                {
                    continue;
                }
            }

            boolean researchAlreadyRun = false;
            for (ILocalResearch progressResearch : colony.getResearchManager().getResearchTree().getResearchInProgress())
            {
                if(progressResearch.getId().equals(research.getId()))
                {
                    researchAlreadyRun = true;
                    break;
                }
            }
            // Don't want to spam people about in-progress or already-completed research.  Because these might change within a world,
            // we can't just save them or check against effects.
            if(researchAlreadyRun || colony.getResearchManager().getResearchTree().hasCompletedResearch(research.getId()))
            {
                removes.add(research);
                continue;
            }

            // if research has item requirements, only notify player; we don't want to have items disappearing from inventories.
            if (!research.getCostList().isEmpty())
            {
                for (PlayerEntity player : colony.getMessagePlayerEntities())
                {
                    player.sendMessage(new TranslationTextComponent(TranslationConstants.RESEARCH_AVAILABLE, research.getName()), player.getUUID());
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                }
            }
            // Otherwise, we can start the research without user intervention.
            else
            {
                startCostlessResearch(research);
            }
            //  If we've successfully done all those things, now we can remove the object from the list.
            //  This will reannounce on world reload, but that's probably ideal, in case someone missed the message once.
            removes.add(research);
        }
        autoStartResearch.removeAll(removes);
    }

    /**
     * Start researches that have no item consumption cost, and notify players of available for those with a cost.
     * @param research      The global research to start.
     */
    private void startCostlessResearch(IGlobalResearch research)
    {
        boolean creativePlayer = false;
        for (PlayerEntity player : colony.getMessagePlayerEntities())
        {
            if (player.isCreative())
            {
                creativePlayer = true;
            }
        }
        tree.addResearch(research.getBranch(), new LocalResearch(research.getId(), research.getBranch(), research.getDepth()));
        if(research.isInstant() || (creativePlayer && MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get()))
        {
            ILocalResearch localResearch = tree.getResearch(research.getBranch(), research.getId());
            localResearch.setProgress(IGlobalResearchTree.getInstance().getBranchData(research.getBranch()).getBaseTime(research.getDepth()));
            localResearch.setState(ResearchState.FINISHED);
            tree.finishResearch(research.getId());
            for (IResearchEffect<?> effect : IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getEffects())
            {
                effects.applyEffect(effect);
            }
            for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
            {
                citizen.applyResearchEffects();
            }
            final TranslationTextComponent message = new TranslationTextComponent(RESEARCH_CONCLUDED + ThreadLocalRandom.current().nextInt(3),
              IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName());
            for (PlayerEntity player : colony.getMessagePlayerEntities())
            {
                player.sendMessage(message, player.getUUID());
                SoundUtils.playSuccessSound(player, player.blockPosition());
            }
        }
        else
        {
            for (PlayerEntity player : colony.getMessagePlayerEntities())
            {
                player.sendMessage(new TranslationTextComponent(TranslationConstants.RESEARCH_AVAILABLE, research.getName())
                                     .append(new TranslationTextComponent("com.minecolonies.coremod.research.started",
                                      research.getName())),
                  player.getUUID());
                SoundUtils.playSuccessSound(player, player.blockPosition());
            }
        }
    }
}
