package com.minecolonies.core.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.network.messages.client.colony.ColonyViewResearchManagerViewMessage;
import com.minecolonies.core.research.LocalResearch;
import com.minecolonies.core.research.LocalResearchTree;
import com.minecolonies.core.research.ResearchEffectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

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

    /**
     * Whether synch to client is necessary.
     */
    private boolean dirty;

    @Override
    public void readFromNBT(@NotNull final CompoundTag compound)
    {
        tree.readFromNBT(compound, effects);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        tree.writeToNBT(compound);
    }

    @Override
    public void sendPackets(final Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers)
    {
        if (dirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();
            if (dirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);

            final ColonyViewResearchManagerViewMessage message = new ColonyViewResearchManagerViewMessage(colony, this);
            players.forEach(player -> Network.getNetwork().sendToPlayer(message, player));

        }
        clearDirty();
    }

    @Override
    public final void markDirty()
    {
        dirty = true;
    }

    @Override
    public final boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void clearDirty()
    {
        dirty = false;
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
        return new ResourceLocation(BuiltInRegistries.BLOCK.getKey(block).getNamespace(), "effects/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
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
                    if (building.getValue().getBuildingType() == ModBuildings.university.get())
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
                MessageUtils.format(RESEARCH_AVAILABLE, MutableComponent.create(research.getName())).sendTo(colony).forAllPlayers();
                for (Player player : colony.getMessagePlayerEntities())
                {
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                }
            }
            // Otherwise, we can start the research without user intervention.
            else
            {
                startCostlessResearch(research);
            }
            //  If we've successfully done all those things, now we can remove the object from the list.
            //  This will re-announce on world reload, but that's probably ideal, in case someone missed the message once.
            removes.add(research);
        }
        autoStartResearch.removeAll(removes);
        markDirty();
    }

    /**
     * Start researches that have no item consumption cost, and notify players of available for those with a cost.
     * @param research      The global research to start.
     */
    private void startCostlessResearch(IGlobalResearch research)
    {
        markDirty();
        boolean creativePlayer = false;
        for (Player player : colony.getMessagePlayerEntities())
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

            MessageUtils.format(RESEARCH_CONCLUDED + ThreadLocalRandom.current().nextInt(3),
                MutableComponent.create(IGlobalResearchTree.getInstance().getResearch(research.getBranch(), research.getId()).getName()))
              .sendTo(colony)
              .forAllPlayers();
            for (Player player : colony.getMessagePlayerEntities())
            {
                SoundUtils.playSuccessSound(player, player.blockPosition());
            }
        }
        else
        {
            MessageUtils.format(RESEARCH_AVAILABLE, MutableComponent.create(research.getName()))
              .append(MESSAGE_RESEARCH_STARTED, MutableComponent.create(research.getName()))
              .sendTo(colony)
              .forAllPlayers();
            for (Player player : colony.getMessagePlayerEntities())
            {
                SoundUtils.playSuccessSound(player, player.blockPosition());
            }
        }
    }
}
