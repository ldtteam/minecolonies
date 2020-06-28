package com.minecolonies.coremod.entity.ai.citizen.healer;

import net.minecraft.nbt.CompoundNBT;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STATUS;

/**
 * Class representing a patient.
 */
public class Patient
{
    /**
     * Different patient states.
     */
    public enum PatientState
    {
        NEW,
        REQUESTED,
        TREATED
    }

    /**
     * Citizen id of the patient.
     */
    private final int id;

    /**
     * The current patient state.
     */
    private PatientState state = PatientState.NEW;

    /**
     * Create a new patient file.
     * @param id the id of the patient.
     */
    public Patient(final int id)
    {
        this.id = id;
    }

    /**
     * Load the Patient from nbt.
     * @param patientCompound the nbt to load it from.
     */
    public Patient(final CompoundNBT patientCompound)
    {
        this.id = patientCompound.getInt(TAG_ID);
        this.state = PatientState.values()[patientCompound.getInt(TAG_STATUS)];
    }

    /**
     * Get the citizen id of the patient.
     * @return the int id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the current state of the treatment.
     * @return the enum state.
     */
    public PatientState getState()
    {
        return state;
    }

    /**
     * Progress the patient state.
     * @param state the state to set.
     */
    public void setState(final PatientState state)
    {
        this.state = state;
    }

    /**
     * Write the Patient to nbt.
     * @param compoundNBT the compound to write it to.
     */
    public void write(final CompoundNBT compoundNBT)
    {
        compoundNBT.putInt(TAG_ID, id);
        compoundNBT.putInt(TAG_STATUS, state.ordinal());
    }
}
