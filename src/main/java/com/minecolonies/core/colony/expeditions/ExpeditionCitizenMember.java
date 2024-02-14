package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Citizen expedition members.
 */
public final class ExpeditionCitizenMember implements IExpeditionMember
{
    /**
     * Nbt tag constants.
     */
    private static final String TAG_DIED = "died";

    /**
     * The id of the citizen.
     */
    private final int id;

    /**
     * The name of the citizen.
     */
    private final String name;

    /**
     * Whether this citizen dead or not.
     */
    private boolean died;

    /**
     * Default constructor for deserialization.
     */
    public ExpeditionCitizenMember(final CompoundTag compound)
    {
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        this.died = compound.getBoolean(TAG_DIED);
    }

    /**
     * Default constructor.
     *
     * @param citizenData the citizen to create the expedition member for.
     */
    public ExpeditionCitizenMember(final ICitizenData citizenData)
    {
        this.id = citizenData.getId();
        this.name = citizenData.getName();
        this.died = false;
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean isDead()
    {
        return this.died;
    }

    @Override
    public void died()
    {
        this.died = true;
    }

    @Override
    public @Nullable ICivilianData resolveCivilianData(final IColony colony)
    {
        return colony.getCitizenManager().getCivilian(this.id);
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putInt(TAG_ID, this.id);
        compound.putString(TAG_NAME, this.name);
        compound.putBoolean(TAG_DIED, this.died);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ExpeditionCitizenMember that = (ExpeditionCitizenMember) o;

        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}