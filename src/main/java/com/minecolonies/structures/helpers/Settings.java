package com.minecolonies.structures.helpers;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to store.
 */
public final class Settings
{
    /**
     * Single instance of this class.
     */
    public static final Settings                 instance       = new Settings();
    private final       BlockPos.MutableBlockPos offset         = new BlockPos.MutableBlockPos();
    /**
     * The position of the structure.
     */
    public              BlockPos                 pos            = null;
    private             boolean                  isMirrored     = false;
    private             boolean                  inHutMode      = true;
    @Nullable
    private             Structure                structure      = null;
    private             int                      rotation       = 0;
    private             String                   hutDec         = "";
    private             String                   style          = "";
    private             int                      level          = 0;
    private             boolean                  isPendingReset = false;

    private Settings()
    {
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(final BlockPos pos)
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
        if (structure != null && structure.isTemplateMissing())
        {
            this.structure = null;
        }

        return this.structure;
    }

    /**
     * Set a structure to render.
     *
     * @param structure structure to render.
     */
    public void setActiveSchematic(final Structure structure)
    {
        if (structure == null)
        {
            reset();
        }
        else
        {
            this.structure = structure;
        }
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset()
    {
        structure = null;
        isPendingReset = false;
        offset.setPos(0, 0, 0);
        rotation = 0;
        isMirrored = false;
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
    public void setInHutMode(final boolean mode)
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
    public void setSchematicInfo(final String hutDec, final String style, final int level, final int rotation)
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
     * Sets the rotation.
     *
     * @param rotation the rotation to set.
     */
    public void setRotation(final int rotation)
    {
        this.rotation = rotation;
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
     * Calculates the offset regarding the blockHut.
     *
     * @param settings depending on the rotation.
     * @return the offset a blockPos.
     */
    @NotNull
    public BlockPos getOffset(final PlacementSettings settings)
    {
        if (structure != null)
        {
            for (final Template.BlockInfo info : structure.getBlockInfoWithSettings(settings))
            {
                if (info.blockState.getBlock() instanceof AbstractBlockHut)
                {
                    offset.setPos(info.pos);
                    return info.pos;
                }
            }
        }
        return new BlockPos(0, 0, 0);
    }

    /**
     * Makes the building mirror.
     */
    public void mirror()
    {
        if (structure == null)
        {
            return;
        }
        isMirrored = !isMirrored;

        structure.setPlacementSettings(structure.getSettings().setMirror(getMirror()));
    }

    /**
     * Get the mirror.
     * @return the mirror object.
     */
    public Mirror getMirror()
    {
        if (isMirrored)
        {
            return Mirror.FRONT_BACK;
        }
        else
        {
            return Mirror.NONE;
        }
    }
}
