package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IGraveData;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Abstract class for minecolonies graves.
 */
public abstract class AbstractTileEntityGrave extends TileEntityRack implements INamedContainerProvider
{
    /**
     * default duration of the countdown before the grave disapear, in ticks (20 ticks / seconds)
     */
    protected static final int DEFAULT_DECAY_TIMER = TICKS_FIVE_MIN;

    /**
     * Is this grave decayed or not
     */
    protected boolean decayed;

    /**
     * The decay timer counting down before the grave decay and then disapear
     */
    protected int decay_timer;

    /**
     * The GraveData of the citizen that spawned this grave.
     */
    @Nullable
    protected IGraveData graveData;

    public AbstractTileEntityGrave(final TileEntityType<? extends AbstractTileEntityGrave> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        decay_timer = DEFAULT_DECAY_TIMER;
        decayed = false;
    }

    /**
     * Delay the decay timer by minutes
     * @param minutes number of minutes to delay the time by
     */
    public void delayDecayTimer(final double minutes)
    {
        decay_timer += minutes * TICKS_SECOND * 60;
    }

    /**
     * Get the graveData of the saved citizen
     */
    public IGraveData getGraveData()
    {
        return graveData;
    }

    /**
     * Set the graveData of the saved citizen
     * @param graveData
     */
    public void setGraveData(IGraveData graveData)
    {
        this.graveData = graveData;
        setChanged();
    }
}
