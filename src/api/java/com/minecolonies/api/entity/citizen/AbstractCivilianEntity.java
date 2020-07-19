package com.minecolonies.api.entity.citizen;

import com.minecolonies.api.colony.ICivilianData;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCivilianEntity extends AgeableEntity implements INPC
{

    protected AbstractCivilianEntity(final EntityType<? extends AgeableEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }

    /**
     * Setter for the citizen data.
     *
     * @param data the data to set.
     */
    public abstract void setCivilianData(@Nullable ICivilianData data);

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    public abstract void markDirty();

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    public abstract int getCivilianID();

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    public abstract void setCitizenId(int id);
}
