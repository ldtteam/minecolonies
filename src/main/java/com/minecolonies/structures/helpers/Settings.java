package com.minecolonies.structures.helpers;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
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
    private             BlockPos                 pos            = null;
    private             boolean                  isMirrored     = false;
    @Nullable
    private             Structure                structure      = null;
    private             int                      rotation       = 0;
    private             String                   structureName  = null;
    private             boolean                  isPendingReset = false;

    /**
     * Check if the tool is in the static schematic mode.
     */
    private boolean staticSchematicMode = false;

    /**
     * Name of the static schematic if existent.
     */
    private String staticSchematicName = "";

    /**
     * Possible free to place structure.
     */
    private WindowBuildTool.FreeMode freeMode;

    /**
     * Private constructor to hide implicit one.
     */
    private Settings()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Set up the static mode.
     * @param name the name of the schematic.
     * @param freeMode the mode.
     */
    public void setupStaticMode(final String name, final WindowBuildTool.FreeMode freeMode)
    {
        this.staticSchematicMode = true;
        this.staticSchematicName = name;
        this.freeMode = freeMode;
    }

    /**
     * set the position.
     *
     * @return the position
     */
    public BlockPos getPosition()
    {
        return pos;
    }

    /**
     * set the position.
     *
     * @param position to render
     */
    public void setPosition(final BlockPos position)
    {
        pos = position;
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
        staticSchematicMode = false;
        freeMode = null;
        staticSchematicName = "";
    }

    /**
     * Saves the schematic info when the client closes the build tool window.
     *
     * @param structureName name of the structure.
     * @param rotation      The number of times the building is rotated.
     */
    public void setSchematicInfo(final String structureName, final int rotation)
    {
        this.structureName = structureName;
        this.rotation = rotation;
    }

    /**
     * @return the structure name currently used.
     */
    public String getStructureName()
    {
        return structureName;
    }

    public void setStructureName(final String structureName)
    {
        this.structureName = structureName;
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
     *
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

    /**
     * Check if static mode.
     * @return true if so.
     */
    public boolean isStaticSchematicMode()
    {
        return staticSchematicMode;
    }

    /**
     * Get the schematic name of the static mode.
     * @return the string.
     */
    public String getStaticSchematicName()
    {
        return staticSchematicName;
    }

    /**
     * Getter of the mode in static mode.
     * @return the FreeMode (enum).
     */
    public WindowBuildTool.FreeMode getFreeMode()
    {
        return freeMode;
    }
}
