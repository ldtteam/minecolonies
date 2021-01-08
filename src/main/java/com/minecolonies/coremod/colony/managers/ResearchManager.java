package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.IResearchManager;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.research.LocalResearch;
import com.minecolonies.coremod.research.LocalResearchTree;
import com.minecolonies.coremod.research.ResearchEffectManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;
import static com.minecolonies.api.util.constant.TranslationConstants.RESEARCH_CONCLUDED;

/**
 * Research manager of the colony.
 */
public class ResearchManager implements IResearchManager
{
    /**
     * The research tree of the colony.
     */
    private final LocalResearchTree tree = new LocalResearchTree();

    /**
     * The research effects of the colony.
     */
    private final IResearchEffectManager effects = new ResearchEffectManager();

    /**
     * The research Ids of any research set to start automatically when unhidden.
     */
    private final List<IGlobalResearch> autoStartResearch = new ArrayList<>();

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

    public ResearchManager()
    {
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
    public void checkAutoStartResearch(final IColony colony)
    {
        final List<IGlobalResearch> removes = new ArrayList<>();
        for(IGlobalResearch research : autoStartResearch)
        {
            if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), colony))
            {
                continue;
            }
            Map<BlockPos, IBuilding> buildings = colony.getBuildingManager().getBuildings();
            int level = 0;
            for (Map.Entry<BlockPos, IBuilding> building : buildings.entrySet())
            {
                if (building.getValue().getBuildingRegistryEntry() == ModBuildings.university)
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
                    player.sendMessage(new TranslationTextComponent(TranslationConstants.RESEARCH_AVAILABLE, research.getName()), player.getUniqueID());
                    SoundUtils.playSuccessSound(player, player.getPosition());
                }
            }
            // Otherwise, we can start the research without user intervention.
            else
            {
                startCostlessResearch(colony, research);
            }
            //  If we've successfully done all those things, now we can remove the object from the list.
            //  This will reannounce on world reload, but that's probably ideal, in case someone missed the message once.
            removes.add(research);
        }
        autoStartResearch.removeAll(removes);
    }

    /**
     * Start researches that have no item consumption cost.
     * @param colony        The colony to begin the researches in.
     * @param research      The global research to start.
     */
    private void startCostlessResearch(IColony colony, IGlobalResearch research)
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
            tree.getResearch(research.getBranch(), research.getId())
              .setProgress((int) (BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(research.getBranch()) * Math.pow(2, research.getDepth() - 1)));
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
              new TranslationTextComponent(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()));
            for (PlayerEntity player : colony.getMessagePlayerEntities())
            {
                player.sendMessage(message, player.getUniqueID());
                SoundUtils.playSuccessSound(player, player.getPosition());
            }
        }
        else
        {
            for (PlayerEntity player : colony.getMessagePlayerEntities())
            {
                player.sendMessage(new TranslationTextComponent(TranslationConstants.RESEARCH_AVAILABLE, research.getName())
                                     .append(new TranslationTextComponent("com.minecolonies.coremod.research.started",
                                       new TranslationTextComponent(research.getName()))),
                  player.getUniqueID());
                SoundUtils.playSuccessSound(player, player.getPosition());
            }
        }
    }
}
