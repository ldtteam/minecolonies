package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IJobView
{
    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    String getName();

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    Set<IToken<?>> getAsyncRequests();

    /**
     * Deserialize the job from the buffer.
     * @param buffer the buffer to read it from.
     */
    void deserialize(final PacketBuffer buffer);
}
