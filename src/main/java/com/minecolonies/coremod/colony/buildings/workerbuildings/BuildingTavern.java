package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.sounds.TavernSounds;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutTavern;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.network.messages.client.colony.PlayMusicAtPosMessage;
import net.minecraft.block.material.Material;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VISITORS;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_WORK;

/**
 * Tavern building for the colony. Houses 4 citizens Plays a tavern theme on entering Spawns/allows citizen recruitment Spawns trader/quest npcs
 */
public class BuildingTavern extends BuildingHome
{
    /**
     * Schematic name
     */
    public static final String TARVERN_SCHEMATIC = "tavern";
    public static final String TAG_VISITOR_ID    = "visitor";

    /**
     * Skill levels
     */
    private static final int LEATHER_SKILL_LEVEL = 20;
    private static final int GOLD_SKILL_LEVEL    = 25;
    private static final int IRON_SKILL_LEVEL    = 30;
    private static final int DIAMOND_SKILL_LEVEL = 35;

    private static final int MAX_STORY = 20;

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

    /**
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingTavern(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TARVERN_SCHEMATIC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.tavern;
    }

    @Override
    public int getMaxInhabitants()
    {
        if (getBuildingLevel() <= 0)
        {
            return 0;
        }

        return 4;
    }

    @Override
    public void onPlayerEnterBuilding(final PlayerEntity player)
    {
        if (musicCooldown <= 0 && getBuildingLevel() > 0 && !colony.isDay())
        {
            net.minecraft.util.Tuple<Integer, Integer> corner1 = getCorners().getA();
            net.minecraft.util.Tuple<Integer, Integer> corner2 = getCorners().getB();

            final int x = (int) ((corner1.getA() + corner1.getB()) * 0.5);
            final int z = (int) ((corner2.getA() + corner2.getB()) * 0.5);

            final PlayMusicAtPosMessage message = new PlayMusicAtPosMessage(TavernSounds.tavernTheme, new BlockPos(x, getPosition().getY(), z), colony.getWorld(), 0.7f, 1.0f);
            for (final ServerPlayerEntity curPlayer : colony.getPackageManager().getCloseSubscribers())
            {
                Network.getNetwork().sendToPlayer(message, curPlayer);
            }
            musicCooldown = TWENTY_MINUTES;
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        if (musicCooldown > 0)
        {
            musicCooldown -= MAX_TICKRATE;
        }

        final Iterator<Integer> it = externalCitizens.iterator();
        while (it.hasNext())
        {
            final Integer id = it.next();
            if (colony.getVisitorManager().getVisitor(id) == null)
            {
                it.remove();
            }
        }

        if (noVisitorTime > 0)
        {
            noVisitorTime -= 500;
        }

        if (getBuildingLevel() > 0 && externalCitizens.size() < 3 * getBuildingLevel() && noVisitorTime <= 0)
        {
            spawnVisitor();
            noVisitorTime =
              colony.getWorld().getRandom().nextInt(3000) + (6000 / getBuildingLevel()) * colony.getCitizenManager().getCurrentCitizenCount() / colony.getCitizenManager()
                                                                                                                                                  .getMaxCitizens();
        }
    }

    @Override
    public void onUpgradeComplete(final int newlevel)
    {
        super.onUpgradeComplete(newlevel);
        initTags = false;
    }

    /**
     * Spawns a recruitable visitor citizen.
     */
    private void spawnVisitor()
    {
        IVisitorData newCitizen = (IVisitorData) colony.getVisitorManager().createAndRegisterCivilianData();
        externalCitizens.add(newCitizen.getId());

        newCitizen.setBedPos(getPosition());
        newCitizen.setHomeBuilding(this);

        int recruitLevel = colony.getWorld().rand.nextInt(10 * getBuildingLevel()) + 15;
        List<com.minecolonies.api.util.Tuple<Item, Integer>> recruitCosts = IColonyManager.getInstance().getCompatibilityManager().getRecruitmentCostsWeights();

        if (newCitizen.getName().contains("Ray"))
        {
            newCitizen.setRecruitCosts(new ItemStack(Items.BAKED_POTATO, 64));
        }

        newCitizen.getCitizenSkillHandler().init(recruitLevel);

        BlockPos spawnPos = BlockPosUtil.findAround(colony.getWorld(), getPosition(), 1, 1, bs -> bs.getMaterial() == Material.AIR);
        if (spawnPos == null)
        {
            spawnPos = getPosition();
        }

        colony.getVisitorManager().spawnOrCreateCivilian(newCitizen, colony.getWorld(), spawnPos, true);
        Tuple<Item, Integer> cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));


        if (newCitizen.getEntity().isPresent())
        {
            final AbstractEntityCitizen citizenEntity = newCitizen.getEntity().get();

            if (recruitLevel > LEATHER_SKILL_LEVEL)
            {
                // Leather
                citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.LEATHER_BOOTS));
            }
            if (recruitLevel > GOLD_SKILL_LEVEL)
            {
                // Gold
                citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
            }
            if (recruitLevel > IRON_SKILL_LEVEL)
            {
                if (cost.getB() <= 2)
                {
                    cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));
                }
                // Iron
                citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
            }
            if (recruitLevel > DIAMOND_SKILL_LEVEL)
            {
                if (cost.getB() <= 3)
                {
                    cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));
                }
                // Diamond
                citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            }
        }

        newCitizen.setRecruitCosts(new ItemStack(cost.getA(), (int)(recruitLevel * 3.0 / cost.getB())));
        newCitizen.triggerInteraction(new RecruitmentInteraction(new TranslationTextComponent(
          "com.minecolonies.coremod.gui.chat.recruitstory" + (colony.getWorld().rand.nextInt(MAX_STORY) + 1), newCitizen.getName().split(" ")[0]), ChatPriority.IMPORTANT));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = super.serializeNBT();
        final ListNBT visitorlist = new ListNBT();
        for (final Integer id : externalCitizens)
        {
            CompoundNBT visitorCompound = new CompoundNBT();
            visitorCompound.putInt(TAG_VISITOR_ID, id);
            visitorlist.add(visitorCompound);
        }

        nbt.put(TAG_VISITORS, visitorlist);
        nbt.putInt(TAG_NOVISITTIME, noVisitorTime);

        return nbt;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        super.deserializeNBT(nbt);
        final ListNBT visitorlist = nbt.getList(TAG_VISITORS, TAG_COMPOUND);
        for (final INBT data : visitorlist)
        {
            final int id = ((CompoundNBT) data).getInt(TAG_VISITOR_ID);
            final ICitizenData citizenData = colony.getVisitorManager().getCivilian(id);
            if (citizenData != null)
            {
                externalCitizens.add(id);
                citizenData.setHomeBuilding(this);
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
            final IVisitorData data = colony.getVisitorManager().getVisitor(id);
            if (data != null)
            {
                positions.remove(data.getSittingPosition());
            }
        }

        if (!positions.isEmpty())
        {
            return positions.get(colony.getWorld().rand.nextInt(positions.size()));
        }

        return null;
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for (final Integer id : externalCitizens)
        {
            colony.getVisitorManager().removeCivilian(colony.getVisitorManager().getVisitor(id));
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
            return workPositions.get(colony.getWorld().rand.nextInt(workPositions.size()));
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

        final IBlueprintDataProvider te = getTileEntity();
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
    public void removeCitizen(final Integer id)
    {
        externalCitizens.remove(id);
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
    public static class View extends BuildingHome.View
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
