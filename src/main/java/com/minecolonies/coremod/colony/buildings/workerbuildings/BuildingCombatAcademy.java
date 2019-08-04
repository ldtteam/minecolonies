package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHay;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Building class for the Combat Academy.
 */
public class BuildingCombatAcademy extends AbstractBuildingWorker
{
    /**
     * The Schematic name.
     */
    private static final String SCHEMATIC_NAME = "CombatAcademy";

    /**
     * The Schematic name.
     */
    private static final String DESC = "CombatAcademy";


    /**
     * List of shooting targets in the building.
     */
    private final List<BlockPos> fightingPos = new ArrayList<>();

    /**
     * List of training partners.
     */
    private final BiMap<Integer, Integer> trainingPartners = HashBiMap.create();

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingCombatAcademy(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobCombatTraining(citizen);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block == Blocks.PUMPKIN && world.getBlockState(pos.down()).getBlock() instanceof BlockHay)
        {
            fightingPos.add(pos.down());
        }
        super.registerBlockPosition(block, pos, world);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        fightingPos.clear();

        final ListNBT targetList = compound.getList(TAG_COMBAT_TARGET, Constants.NBT.TAG_COMPOUND);
        fightingPos.addAll(NBTUtils.streamCompound(targetList).map(targetCompound -> BlockPosUtil.read(targetCompound, TAG_TARGET)).collect(Collectors.toList()));

        final ListNBT partnersTagList = compound.getList(TAG_COMBAT_PARTNER, Constants.NBT.TAG_COMPOUND);
        trainingPartners.putAll(NBTUtils.streamCompound(partnersTagList).collect(Collectors.toMap(targetCompound -> targetCompound.getInt(TAG_PARTNER1), targetCompound -> targetCompound.getInt(TAG_PARTNER2))));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        final ListNBT targetList = fightingPos.stream().map(target -> BlockPosUtil.write(new CompoundNBT(), TAG_TARGET, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_COMBAT_TARGET, targetList);

        final ListNBT partnersTagList = trainingPartners.entrySet().stream().map(BuildingCombatAcademy::writePartnerTupleToNBT).collect(NBTUtils.toListNBT());
        compound.put(TAG_COMBAT_PARTNER, partnersTagList);

        return compound;
    }

    /**
     * Writes the partner tuple to NBT
     * @param tuple the tuple to write to NBT
     * @return a compound with the data.
     */
    private static CompoundNBT writePartnerTupleToNBT(final Map.Entry<Integer, Integer> tuple)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_PARTNER1, tuple.getKey());
        compound.putInt(TAG_PARTNER2, tuple.getValue());
        return compound;
    }

    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return "melee";
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Get a random position to shoot at.
     *
     * @param random the random obj.
     * @return a random shooting target position.
     */
    public BlockPos getRandomCombatTarget(final Random random)
    {
        if (!fightingPos.isEmpty())
        {
            return fightingPos.get(random.nextInt(fightingPos.size()));
        }
        return null;
    }

    /**
     * Get a random trainings partner in the building.
     * @return the entityCitizen partner or null.
     * @param citizen the worker to get the partner for.
     */
    public AbstractEntityCitizen getRandomCombatPartner(final AbstractEntityCitizen citizen)
    {
        final ICitizenData citizenData = citizen.getCitizenData();
        if (citizenData != null)
        {
            final ICitizenData partner = getAssignedCitizen().stream()
                                              .filter(data -> data.getId() != citizenData.getId())
                                              .filter(data -> !trainingPartners.containsKey(data.getId()))
                                              .filter(data -> !trainingPartners.containsValue(data.getId()))
                                              .findFirst()
                                              .orElse(null);
            if (partner != null)
            {
                trainingPartners.put(citizenData.getId(), partner.getId());
                return partner.getCitizenEntity().orElse(null);
            }
            return null;
        }
        return null;
    }

    /**
     * Check if the worker has a combat partner assigned to him right now.
     *
     * @param citizen the citizen to check for.
     * @return true if so.
     */
    public boolean hasCombatPartner(final AbstractEntityCitizen citizen)
    {
        return getCombatPartner(citizen) != null;
    }

    /**
     * Get the citizen of the combat partner or null if not existing or available.
     * @param citizen the citizen.
     * @return the citizen or null.
     */
    public AbstractEntityCitizen getCombatPartner(final AbstractEntityCitizen citizen)
    {
        final ICitizenData data = citizen.getCitizenData();
        if (data != null)
        {
            final int citizenId;
            if (trainingPartners.containsKey(data.getId()))
            {
                citizenId = trainingPartners.get(data.getId());
            }
            else if (trainingPartners.containsValue(data.getId()))
            {
                citizenId = trainingPartners.inverse().get(data.getId());
            }
            else
            {
                return null;
            }

            final ICitizenData citizenData = getAssignedCitizen().stream().filter(cit -> cit.getId() != data.getId()).filter(cit -> cit.getId() == citizenId).findFirst().orElse(null);
            if (citizenData != null)
            {
                return citizenData.getCitizenEntity().orElse(null);
            }
        }
        return null;
    }

    /**
     * Reset the combat partner for a worker.
     * @param worker the worker to reset it for.
     */
    public void resetPartner(final AbstractEntityCitizen worker)
    {
        final ICitizenData data = worker.getCitizenData();
        if (data != null)
        {
            if (trainingPartners.containsKey(data.getId()))
            {
                trainingPartners.remove(data.getId());
            }
            else if (trainingPartners.containsValue(data.getId()))
            {
                trainingPartners.inverse().remove(data.getId());
            }
        }
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, DESC);
        }

        /**
         * Check if it has enough workers.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return getWorkerId().size() >= getBuildingLevel();
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.STRENGTH;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.DEXTERITY;
        }
    }
}
