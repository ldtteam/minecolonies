package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Visitor expedition members.
 */
public final class ExpeditionVisitorMember implements IExpeditionMember<IVisitorData>
{
    /**
     * Nbt tag constants.
     */
    private static final String TAG_DIED = "died";

    /**
     * The id of the visitor.
     */
    private final int id;

    /**
     * The name of the visitor.
     */
    private final String name;

    /**
     * Whether this visitor dead or not.
     */
    private boolean died;

    /**
     * Default constructor for deserialization.
     */
    public ExpeditionVisitorMember(final CompoundTag compound)
    {
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        this.died = compound.getBoolean(TAG_DIED);
    }

    /**
     * Default constructor.
     *
     * @param visitorData the visitor to create the expedition member for.
     */
    public ExpeditionVisitorMember(final IVisitorData visitorData)
    {
        this.id = visitorData.getId();
        this.name = visitorData.getName();
        this.died = false;
    }

    /**
     * Default constructor.
     *
     * @param visitorData the visitor to create the expedition member for.
     */
    public ExpeditionVisitorMember(final IVisitorViewData visitorData)
    {
        this.id = visitorData.getId();
        this.name = visitorData.getName();
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
    @Nullable
    public IVisitorData resolveCivilianData(final IColony colony)
    {
        return colony.getVisitorManager().getCivilian(this.id);
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

        final ExpeditionVisitorMember that = (ExpeditionVisitorMember) o;

        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}