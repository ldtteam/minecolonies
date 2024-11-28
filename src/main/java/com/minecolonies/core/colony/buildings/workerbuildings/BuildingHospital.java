package com.minecolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.datalistener.model.Disease;
import com.minecolonies.core.datalistener.DiseasesListener;
import com.minecolonies.core.entity.ai.workers.util.Patient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the hospital building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingHospital extends AbstractBuilding
{
    /**
     * The hospital string.
     */
    private static final String HOSPITAL_DESC = "hospital";

    /**
     * Max building level of the hospital.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Map from beds to patients, 0 is empty.
     */
    @NotNull
    private final Map<BlockPos, Integer> bedMap = new HashMap<>();

    /**
     * Map of patients of this hospital.
     */
    private final Map<Integer, Patient> patients = new HashMap<>();

    /**
     * Instantiates a new hospital building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingHospital(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return HOSPITAL_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag bedTagList = compound.getList(TAG_BEDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < bedTagList.size(); ++i)
        {
            final CompoundTag bedCompound = bedTagList.getCompound(i);
            final BlockPos bedPos = BlockPosUtil.read(bedCompound, TAG_POS);
            if (!bedMap.containsKey(bedPos))
            {
                bedMap.put(bedPos, bedCompound.getInt(TAG_ID));
            }
        }

        final ListTag patientTagList = compound.getList(TAG_PATIENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < patientTagList.size(); ++i)
        {
            final CompoundTag patientCompound = patientTagList.getCompound(i);
            final int patientId = patientCompound.getInt(TAG_ID);
            if (!patients.containsKey(patientId))
            {
                patients.put(patientId, new Patient(patientCompound));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        if (!bedMap.isEmpty())
        {
            @NotNull final ListTag bedTagList = new ListTag();
            for (@NotNull final Map.Entry<BlockPos, Integer> entry : bedMap.entrySet())
            {
                final CompoundTag bedCompound = new CompoundTag();
                BlockPosUtil.write(bedCompound, NbtTagConstants.TAG_POS, entry.getKey());
                bedCompound.putInt(TAG_ID, entry.getValue());
                bedTagList.add(bedCompound);
            }
            compound.put(TAG_BEDS, bedTagList);
        }

        if (!patients.isEmpty())
        {
            @NotNull final ListTag patientTagList = new ListTag();
            for (@NotNull final Patient patient : patients.values())
            {
                final CompoundTag patientCompound = new CompoundTag();
                patient.write(patientCompound);
                patientTagList.add(patientCompound);
            }
            compound.put(TAG_PATIENTS, patientTagList);
        }

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(blockState, pos, world);

        BlockPos registrationPosition = pos;
        if (blockState.getBlock() instanceof BedBlock)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.FOOT)
            {
                registrationPosition = registrationPosition.relative(blockState.getValue(BedBlock.FACING));
            }

            if (!bedMap.containsKey(registrationPosition))
            {
                bedMap.put(registrationPosition, 0);
            }
        }
    }

    /**
     * Get the list of beds.
     *
     * @return immutable copy
     */
    @NotNull
    public List<BlockPos> getBedList()
    {
        return ImmutableList.copyOf(bedMap.keySet());
    }

    /**
     * Get the list of patient files.
     *
     * @return immutable copy.
     */
    public List<Patient> getPatients()
    {
        return ImmutableList.copyOf(patients.values());
    }

    /**
     * Remove a patient from the list.
     *
     * @param patient the patient to remove.
     */
    public void removePatientFile(final Patient patient)
    {
        patients.remove(patient.getId());
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> map = super.getRequiredItemsAndAmount();
        map.put(BuildingHospital::isCureItem, new Tuple<>(10, false));
        return map;
    }

    /**
     * Check if the given itemstack is a cure item.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    private static boolean isCureItem(final ItemStack stack)
    {
        for (final Disease disease : DiseasesListener.getDiseases())
        {
            for (final ItemStorage cureItem : disease.cureItems())
            {
                return Disease.isCureItem(stack, cureItem);
            }
        }
        return false;
    }

    /**
     * Add a new patient to the list.
     *
     * @param citizenId patient to add.
     */
    public void checkOrCreatePatientFile(final int citizenId)
    {
        if (!patients.containsKey(citizenId))
        {
            patients.put(citizenId, new Patient(citizenId));
        }
    }

    /**
     * Register a citizen.
     *
     * @param bedPos    the pos.
     * @param citizenId the citizen id.
     */
    public void registerPatient(final BlockPos bedPos, final int citizenId)
    {
        bedMap.put(bedPos, citizenId);
        setBedOccupation(bedPos, citizenId != 0);
    }

    /**
     * Helper method to set bed occupation.
     *
     * @param bedPos   the position of the bed.
     * @param occupied if occupied.
     */
    private void setBedOccupation(final BlockPos bedPos, final boolean occupied)
    {
        final BlockState state = colony.getWorld().getBlockState(bedPos);
        if (state.is(BlockTags.BEDS))
        {
            colony.getWorld().setBlock(bedPos, state.setValue(BedBlock.OCCUPIED, occupied), 0x03);

            final BlockPos feetPos = bedPos.relative(state.getValue(BedBlock.FACING).getOpposite());
            final BlockState feetState = colony.getWorld().getBlockState(feetPos);

            if (feetState.is(BlockTags.BEDS))
            {
                colony.getWorld().setBlock(feetPos, feetState.setValue(BedBlock.OCCUPIED, occupied), 0x03);
            }
        }
    }

    @Override
    public void onWakeUp()
    {
        for (final Map.Entry<BlockPos, Integer> entry : new ArrayList<>(bedMap.entrySet()))
        {
            final BlockState state = colony.getWorld().getBlockState(entry.getKey());
            if (state.getBlock() instanceof BedBlock)
            {
                if (entry.getValue() == 0 && state.getValue(BedBlock.OCCUPIED))
                {
                    setBedOccupation(entry.getKey(), false);
                }
                else if (entry.getValue() != 0)
                {
                    final ICitizenData citizen = colony.getCitizenManager().getCivilian(entry.getValue());
                    if (citizen != null)
                    {
                        if (state.getValue(BedBlock.OCCUPIED))
                        {
                            if (!citizen.isAsleep() || citizen.getEntity().isEmpty() || citizen.getEntity().get().blockPosition().distSqr(entry.getKey()) > 2.0)
                            {
                                setBedOccupation(entry.getKey(), false);
                                bedMap.put(entry.getKey(), 0);
                            }
                        }
                        else
                        {
                            if (citizen.isAsleep() && citizen.getEntity().isPresent() && citizen.getEntity().get().blockPosition().distSqr(entry.getKey()) < 2.0)
                            {
                                setBedOccupation(entry.getKey(), true);
                            }
                        }
                    }
                    else
                    {
                        bedMap.put(entry.getKey(), 0);
                    }
                }
            }
            else
            {
                bedMap.remove(entry.getKey());
            }
        }
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (isCureItem(stack))
        {
            return false;
        }

        return super.canEat(stack);
    }
}
