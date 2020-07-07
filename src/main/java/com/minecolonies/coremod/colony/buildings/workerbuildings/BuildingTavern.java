package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.sounds.TavernSounds;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutTavern;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
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
import net.minecraft.util.Tuple;
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
     * Music interval
     */
    private static final int    FOUR_MINUTES_TICKS = 4800;
    private static final String TAG_NOVISITTIME    = "novisit";

    /**
     * Cooldown for the music, to not play it too much/not overlap with itself
     */
    private int musicCooldown = 0;

    /**
     * List of additional citizens
     */
    private final List<Integer> externalCitizens = new ArrayList<>();

    private final List<BlockPos> sitPositions  = new ArrayList<>();
    private final List<BlockPos> workPositions = new ArrayList<>();

    private boolean initTags = false;

    /**
     * Penalty for not spawning visitors after a death
     */
    private int noVisitorTime = 0;

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
        if (musicCooldown <= 0 && getBuildingLevel() > 0)
        {
            Tuple<Integer, Integer> corner1 = getCorners().getA();
            Tuple<Integer, Integer> corner2 = getCorners().getB();

            final int x = (int) ((corner1.getA() + corner1.getB()) * 0.5);
            final int z = (int) ((corner2.getA() + corner2.getB()) * 0.5);

            final PlayMusicAtPosMessage message = new PlayMusicAtPosMessage(TavernSounds.tavernTheme, new BlockPos(x, getPosition().getY(), z), colony.getWorld(), 0.8f, 1.0f);
            for (final ServerPlayerEntity curPlayer : colony.getPackageManager().getCloseSubscribers())
            {
                Network.getNetwork().sendToPlayer(message, curPlayer);
            }
            musicCooldown = FOUR_MINUTES_TICKS;
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
            noVisitorTime -= 25;
        }

        if (getBuildingLevel() > 0 && externalCitizens.size() < 3 * getBuildingLevel() && noVisitorTime <= 0)
        {
            spawnVisitor();
            noVisitorTime = colony.getWorld().getRandom().nextInt(3000) + 6000;
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
        IVisitorData newCitizen = (IVisitorData) colony.getVisitorManager().createAndRegisterNewCitizenData();
        newCitizen.initForNewCitizen();

        externalCitizens.add(newCitizen.getId());

        newCitizen.setBedPos(getPosition());
        newCitizen.setHomeBuilding(this);

        int recruitLevel = colony.getWorld().rand.nextInt(10 * getBuildingLevel()) + 15;

        List<Tuple<Item, Integer>> recruitCosts = IColonyManager.getInstance().getCompatibilityManager().getRecruitmentCostsWeights();

        if (newCitizen.getName().contains("Ray"))
        {
            newCitizen.setRecruitCosts(new ItemStack(Items.BAKED_POTATO, 64));
        }

        newCitizen.getCitizenSkillHandler().init(recruitLevel);

        VisitorCitizen citizenEntity = (VisitorCitizen) ModEntities.VISITOR.create(colony.getWorld());
        newCitizen.setCitizenEntity(citizenEntity);
        newCitizen.initEntityValues();

        Tuple<Item, Integer> cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));
        if (recruitLevel > 20)
        {
            // Leather
            citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.LEATHER_BOOTS));
        }
        if (recruitLevel > 25)
        {
            // Gold
            citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }
        if (recruitLevel > 30)
        {
            if (cost.getB() <= 3)
            {
                cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));
            }
            // Iron
            citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
        }
        if (recruitLevel > 35)
        {
            if (cost.getB() <= 3)
            {
                cost = recruitCosts.get(colony.getWorld().rand.nextInt(recruitCosts.size()));
            }
            // Diamond
            citizenEntity.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        }

        newCitizen.setRecruitCosts(new ItemStack(cost.getA(), recruitLevel * 3 / cost.getB()));

        newCitizen.triggerInteraction(new RecruitmentInteraction(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.recruitstory1"), ChatPriority.IMPORTANT));

        colony.getWorld().addEntity(citizenEntity);

        BlockPos spawnPos = BlockPosUtil.findAround(colony.getWorld(), getPosition(), 1, 1, bs -> bs.getMaterial() == Material.AIR);
        if (spawnPos == null)
        {
            spawnPos = getPosition();
        }

        citizenEntity.setPositionAndUpdate(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
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
            if (colony.getVisitorManager().getCitizen(id) != null)
            {
                externalCitizens.add(id);
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
            positions.remove(colony.getVisitorManager().getVisitor(id).getSittingPosition());
        }

        if (!positions.isEmpty())
        {
            return positions.get(colony.getWorld().rand.nextInt(positions.size()));
        }
// TODO :, recruitment GUI & texts
        return null;
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for (final Integer id : externalCitizens)
        {
            colony.getVisitorManager().removeCitizen(colony.getVisitorManager().getVisitor(id));
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
