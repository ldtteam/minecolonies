package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.modules.stat.IStat;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.sounds.TavernSounds;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.huts.WindowHutTavern;
import com.minecolonies.coremod.colony.buildings.views.LivingBuildingView;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.VisitorSpawnedEvent;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.datalistener.CustomVisitorListener;
import com.minecolonies.coremod.network.messages.client.colony.PlayMusicAtPosMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.Constants.MAX_STORY;
import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VISITORS;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_WORK;

/**
 * Tavern building for the colony. Houses 4 citizens Plays a tavern theme on entering Spawns/allows citizen recruitment Spawns trader/quest npcs
 */
public class TavernBuildingModule extends AbstractBuildingModule implements IDefinesCoreBuildingStatsModule, IBuildingEventsModule, IPersistentModule, ITickingModule
{
    /**
     * Schematic name
     */
    public static final String TAG_VISITOR_ID    = "visitor";

    /**
     * Skill levels
     */
    private static final int LEATHER_SKILL_LEVEL = 20;
    private static final int GOLD_SKILL_LEVEL    = 25;
    private static final int IRON_SKILL_LEVEL    = 30;
    private static final int DIAMOND_SKILL_LEVEL = 35;

    /**
     * Music interval
     */
    private static final int    TWENTY_MINUTES  = 20 * 60 * 20;
    private static final String TAG_NOVISITTIME = "novisit";

    /**
     * Cooldown for the music, to not play it too much/not overlap with itself
     */
    private int musicCooldown = 0;

    /**
     * List of additional citizens
     */
    private final List<Integer> externalCitizens = new ArrayList<>();

    /**
     * List of sitting positions for this building
     */
    private final List<BlockPos> sitPositions = new ArrayList<>();

    /**
     * List of work positions for this building
     */
    private final List<BlockPos> workPositions = new ArrayList<>();

    private boolean initTags = false;

    /**
     * Penalty for not spawning visitors after a death
     */
    private int noVisitorTime = 10000;

    @Override
    public IStat<Integer> getMaxInhabitants()
    {
        if (building.getBuildingLevel() <= 0)
        {
            return (prev) -> 0;
        }

        return (prev) -> 4;
    }

    @Override
    public void onPlayerEnterBuilding(final PlayerEntity player)
    {
        if (musicCooldown <= 0 && building.getBuildingLevel() > 0 && !building.getColony().isDay())
        {
            int count = 0;
            BlockPos avg = BlockPos.ZERO;
            for (final Integer id : externalCitizens)
            {
                final IVisitorData data = building.getColony().getVisitorManager().getVisitor(id);
                if (data != null)
                {
                    if (!data.getSittingPosition().equals(BlockPos.ZERO))
                    {
                        count++;
                        avg.offset(data.getSittingPosition());
                    }
                }
            }

            if (count < 2)
            {
                return;
            }

            avg = new BlockPos(avg.getX() / count, avg.getY() / count, avg.getZ() / count);
            final PlayMusicAtPosMessage message = new PlayMusicAtPosMessage(TavernSounds.tavernTheme, avg, building.getColony().getWorld(), 0.7f, 1.0f);
            for (final ServerPlayerEntity curPlayer : building.getColony().getPackageManager().getCloseSubscribers())
            {
                Network.getNetwork().sendToPlayer(message, curPlayer);
            }
            musicCooldown = TWENTY_MINUTES;
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (musicCooldown > 0)
        {
            musicCooldown -= MAX_TICKRATE;
        }

        externalCitizens.removeIf(id -> colony.getVisitorManager().getVisitor(id) == null);

        if (noVisitorTime > 0)
        {
            noVisitorTime -= 500;
        }

        if (building.getBuildingLevel() > 0 && externalCitizens.size() < 3 * building.getBuildingLevel() && noVisitorTime <= 0)
        {
            spawnVisitor();
            noVisitorTime =
              colony.getWorld().getRandom().nextInt(3000) + (6000 / building.getBuildingLevel()) * colony.getCitizenManager().getCurrentCitizenCount() / colony.getCitizenManager()
                                                                                                                                                  .getMaxCitizens();
        }
    }

    @Override
    public void onUpgradeComplete(final int newlevel)
    {
        initTags = false;
    }

    /**
     * Spawns a recruitable visitor citizen.
     */
    private void spawnVisitor()
    {
        IVisitorData newCitizen = (IVisitorData) building.getColony().getVisitorManager().createAndRegisterCivilianData();
        externalCitizens.add(newCitizen.getId());

        newCitizen.setBedPos(building.getPosition());
        newCitizen.setHomeBuilding(building);

        int recruitLevel = building.getColony().getWorld().random.nextInt(10 * building.getBuildingLevel()) + 15;
        List<com.minecolonies.api.util.Tuple<Item, Integer>> recruitCosts = IColonyManager.getInstance().getCompatibilityManager().getRecruitmentCostsWeights();

        if (newCitizen.getName().contains("Ray"))
        {
            newCitizen.setRecruitCosts(new ItemStack(Items.BAKED_POTATO, 64));
        }

        newCitizen.getCitizenSkillHandler().init(recruitLevel);

        BlockPos spawnPos = BlockPosUtil.findSpawnPosAround(building.getColony().getWorld(), building.getPosition());
        if (spawnPos == null)
        {
            spawnPos = building.getPosition();
        }

        building.getColony().getVisitorManager().spawnOrCreateCivilian(newCitizen, building.getColony().getWorld(), spawnPos, true);
        Tuple<Item, Integer> cost = recruitCosts.get(building.getColony().getWorld().random.nextInt(recruitCosts.size()));

        building.getColony().getEventDescriptionManager().addEventDescription(new VisitorSpawnedEvent(spawnPos, newCitizen.getName()));

        if (newCitizen.getEntity().isPresent())
        {
            final AbstractEntityCitizen citizenEntity = newCitizen.getEntity().get();

            if (recruitLevel > LEATHER_SKILL_LEVEL)
            {
                // Leather
                citizenEntity.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.LEATHER_BOOTS));
            }
            if (recruitLevel > GOLD_SKILL_LEVEL)
            {
                // Gold
                citizenEntity.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
            }
            if (recruitLevel > IRON_SKILL_LEVEL)
            {
                if (cost.getB() <= 2)
                {
                    cost = recruitCosts.get(building.getColony().getWorld().random.nextInt(recruitCosts.size()));
                }
                // Iron
                citizenEntity.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
            }
            if (recruitLevel > DIAMOND_SKILL_LEVEL)
            {
                if (cost.getB() <= 3)
                {
                    cost = recruitCosts.get(building.getColony().getWorld().random.nextInt(recruitCosts.size()));
                }
                // Diamond
                citizenEntity.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            }
        }

        newCitizen.setRecruitCosts(new ItemStack(cost.getA(), (int)(recruitLevel * 3.0 / cost.getB())));

        if (!CustomVisitorListener.chanceCustomVisitors(newCitizen))
        {
            newCitizen.triggerInteraction(new RecruitmentInteraction(new TranslationTextComponent(
              "com.minecolonies.coremod.gui.chat.recruitstory" + (building.getColony().getWorld().random.nextInt(MAX_STORY) + 1), newCitizen.getName().split(" ")[0]), ChatPriority.IMPORTANT));
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT nbt)
    {
        final ListNBT visitorlist = new ListNBT();
        for (final Integer id : externalCitizens)
        {
            CompoundNBT visitorCompound = new CompoundNBT();
            visitorCompound.putInt(TAG_VISITOR_ID, id);
            visitorlist.add(visitorCompound);
        }

        nbt.put(TAG_VISITORS, visitorlist);
        nbt.putInt(TAG_NOVISITTIME, noVisitorTime);
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final ListNBT visitorlist = nbt.getList(TAG_VISITORS, TAG_COMPOUND);
        for (final INBT data : visitorlist)
        {
            final int id = ((CompoundNBT) data).getInt(TAG_VISITOR_ID);
            final ICitizenData citizenData = building.getColony().getVisitorManager().getCivilian(id);
            if (citizenData != null)
            {
                externalCitizens.add(id);
                citizenData.setHomeBuilding(building);
            }
        }
        noVisitorTime = nbt.getInt(TAG_NOVISITTIME);
    }

    /**
     * Gets a free sitting position
     *
     * @return a blockpos to sit at
     */
    public BlockPos getFreeSitPosition()
    {
        final List<BlockPos> positions = new ArrayList<>(getSitPositions());

        if (positions.isEmpty())
        {
            return null;
        }

        for (final Integer id : externalCitizens)
        {
            final IVisitorData data = building.getColony().getVisitorManager().getVisitor(id);
            if (data != null)
            {
                positions.remove(data.getSittingPosition());
            }
        }

        if (!positions.isEmpty())
        {
            return positions.get(building.getColony().getWorld().random.nextInt(positions.size()));
        }

        return null;
    }

    @Override
    public void onDestroyed()
    {
        for (final Integer id : externalCitizens)
        {
            building.getColony().getVisitorManager().removeCivilian(building.getColony().getVisitorManager().getVisitor(id));
        }
    }

    /**
     * Gets the assigned visitor ids
     *
     * @return list of ids
     */
    public List<Integer> getExternalCitizens()
    {
        return externalCitizens;
    }

    /**
     * Get a random work position
     *
     * @return a random work pos
     */
    public BlockPos getWorkPos()
    {
        if (!getWorkPositions().isEmpty())
        {
            return workPositions.get(building.getColony().getWorld().random.nextInt(workPositions.size()));
        }
        return null;
    }

    /**
     * Get the list of sitting positions
     *
     * @return sit pos list
     */
    private List<BlockPos> getSitPositions()
    {
        initTagPositions();
        return sitPositions;
    }

    /**
     * Get the list of work positions
     *
     * @return work pos list
     */
    private List<BlockPos> getWorkPositions()
    {
        initTagPositions();
        return workPositions;
    }

    /**
     * Initializes the sitting and work position lists
     */
    public void initTagPositions()
    {
        if (initTags)
        {
            return;
        }

        final IBlueprintDataProvider te = building.getTileEntity();
        if (te != null)
        {
            initTags = true;
            for (final Map.Entry<BlockPos, List<String>> entry : te.getWorldTagPosMap().entrySet())
            {
                if (entry.getValue().contains(TAG_SITTING))
                {
                    sitPositions.add(entry.getKey());
                }

                if (entry.getValue().contains(TAG_WORK))
                {
                    workPositions.add(entry.getKey());
                }
            }
        }
    }

    /**
     * Removes the given citizen id
     *
     * @param id to remove
     */
    public boolean removeCitizen(final Integer id)
    {
        externalCitizens.remove(id);
        return false;
    }

    /**
     * Sets the delay time before a new visitor spawns
     *
     * @param noVisitorTime time in ticks
     */
    public void setNoVisitorTime(final int noVisitorTime)
    {
        this.noVisitorTime = noVisitorTime;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends LivingBuildingView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutTavern(this);
        }
    }
}
