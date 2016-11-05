package com.schematica;

import com.jlgm.structurepreview.helpers.Structure;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to store
 */
public final class Settings
{
    /**
     * Single instance of this class.
     */
    public static final Settings                 instance  = new Settings();
    private final       BlockPos.MutableBlockPos offset    = new BlockPos.MutableBlockPos();
    private             boolean                  inHutMode = true;
    @Nullable
    private             Structure                structure = null;
    private             int                      rotation  = 0;
    private             String                   hutDec    = "";
    private             String                   style     = "";
    private             int                      level     = 0;
    public             BlockPos                 pos = null;

    private boolean isPendingReset = false;

    private Settings()
    {
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(BlockPos pos)
    {
        if (this.structure == null)
        {
            return;
        }
        this.pos = this.pos.add(pos);
    }

    /**
     * @return The schematic we are currently rendering.
     */
    @Nullable
    public Structure getActiveStructure()
    {
        return this.structure == null ? null : this.structure;
    }

    /**
     * Set a structure to render.
     *
     * @param structure structure to render.
     */
    public void setActiveSchematic(Structure structure)
    {
        if (structure != null)
        {
            this.structure = structure;
        }
        else
        {
            reset();
        }
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset()
    {
        structure = null;
        isPendingReset = false;
    }

    /**
     * @return true if the client is in hut mode.
     */
    public boolean isInHutMode()
    {
        return inHutMode;
    }

    /**
     * @param mode true if in hut mode, false if in decoration mode.
     */
    public void setInHutMode(boolean mode)
    {
        inHutMode = mode;
    }

    /**
     * Saves the schematic info when the client closes the build tool window.
     *
     * @param hutDec   Hut/decoration name.
     * @param style    AbstractBuilding style.
     * @param level    AbstractBuilding level.
     * @param rotation The number of times the building is rotated.
     */
    public void setSchematicInfo(String hutDec, String style, int level, int rotation)
    {
        this.hutDec = hutDec;
        this.style = style;
        this.level = level;
        this.rotation = rotation;
    }

    /**
     * @return The name of the hut/decoration.
     */
    public String getHutDec()
    {
        return hutDec;
    }

    /**
     * @return The name of the style.
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * @return The current level (minus 1) of the hut being rendered.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * @return The number of times the schematic is rotated.
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Call reset next tick.
     */
    public void markDirty()
    {
        isPendingReset = true;
    }

    /**
     * @return true if Settings should be reset.
     */
    public boolean isDirty()
    {
        return isPendingReset;
    }

    /**
     * @return offset
     */
    @NotNull
    public BlockPos getOffset()
    {
        return offset.toImmutable();
    }

    /**
     * Sets the rotation.
     * @param rotation the rotation to set.
     */
    public void setRotation(final int rotation)
    {
        this.rotation = rotation;
    }
}
