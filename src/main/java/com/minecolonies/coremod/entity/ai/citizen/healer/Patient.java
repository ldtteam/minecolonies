package com.minecolonies.coremod.entity.ai.citizen.healer;

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
}
