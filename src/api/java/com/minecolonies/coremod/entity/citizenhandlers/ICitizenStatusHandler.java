package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.entity.ai.Status;
import net.minecraft.util.text.ITextComponent;

public interface ICitizenStatusHandler
{
    /**
     * Get the latest status of the citizen.
     *
     * @return a ITextComponent with the length 4 describing it.
     */
    ITextComponent[] getLatestStatus();

    /**
     * Set the latest status of the citizen and clear the existing status
     *
     * @param status the new status to set.
     */
    void setLatestStatus(ITextComponent... status);

    /**
     * Append to the existing latestStatus list.
     * This will override the oldest one if full and move the others one down in the array.
     *
     * @param status the latest status to append
     */
    void addLatestStatus(ITextComponent status);

    /**
     * Getter for the current status.
     * @return the status.
     */
    Status getStatus();

    /**
     * Setter for the current status.
     * @param status the status to set.
     */
    void setStatus(Status status);
}
