package com.minecolonies.structures.helpers;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
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
    @Nullable
    private             Structure                structure      = null;
    private             int                      rotation       = 0;
    private             int                      sectionIndex   = 0;
    private             int                      styleIndex     = 0;
    private             int                      schematicIndex = 0;

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
    }

    /**
     * Saves the schematic info when the client closes the build tool window.
     *
     * @param sectionIndex   Index of the section.
     * @param styleIndex     Index of the style.
     * @param schematicIndex Index of the schematic.
     * @param rotation The number of times the building is rotated.
     */
    public void setSchematicInfo(final int sectionIndex, final int styleIndex, final int schematicIndex, final int rotation)
    {
        this.sectionIndex=sectionIndex;
        this.styleIndex=styleIndex;
        this.schematicIndex=schematicIndex;
        this.rotation = rotation;
    }


    /**
     * @return The index of the section.
     */
    public int getSectionIndex()
    {
        return sectionIndex;
    }

    /**
     * @return The index of the style.
     */
    public int getStyleIndex()
    {
        return styleIndex;
    }

    /**
     * @return The index of the schematic.
     */
    public int getSchematicIndex()
    {
        return schematicIndex;
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
}
