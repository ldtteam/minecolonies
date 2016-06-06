package com.schematica;

import com.minecolonies.util.BlockPosUtil;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import com.schematica.world.storage.Schematic;
import net.minecraft.util.BlockPos;
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
    public static final Settings instance = new Settings();

    private boolean inHutMode = true;

    private SchematicWorld schematic = null;

    private final BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();

    private int    rotation = 0;
    private String hutDec   = "";
    private String style    = "";
    private int    level    = 0;

    private boolean isPendingReset = false;

    private Settings()
    {
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset()
    {
        schematic = null;
        RenderSchematic.INSTANCE.setWorldAndLoadRenderers(null);

        isPendingReset = false;
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(BlockPos pos)
    {
        if(this.schematic == null)
        {
            return;
        }

        BlockPosUtil.set(offset, pos.subtract(schematic.getSchematic().getOffset()));
        BlockPosUtil.set(schematic.position, offset);
    }

    /**
     * Set a schematic to render.
     *
     * @param schematic schematic to render.
     */
    public void setActiveSchematic(Schematic schematic)
    {
        if(schematic != null)
        {
            this.schematic = new SchematicWorld(schematic);

            RenderSchematic.INSTANCE.setWorldAndLoadRenderers(this.schematic);
            this.schematic.isRendering = true;
        }
        else
        {
            reset();
        }
    }

    /**
     * @return The schematic we are currently rendering.
     */
    @Nullable
    public Schematic getActiveSchematic()
    {
        return this.schematic == null ? null : this.schematic.getSchematic();
    }

    /**
     * @return The schematic world we are currently rendering. null if not currently rendering anything.
     */
    public SchematicWorld getSchematicWorld()
    {
        return schematic;
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
     * @param hutDec Hut/decoration name.
     * @param style Building style.
     * @param level Building level.
     * @param rotation The number of times the building is rotated.
     */
    public void setSchematicInfo(String hutDec, String style, int level, int rotation)
    {
        this.hutDec   = hutDec;
        this.style    = style;
        this.level    = level;
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
        return offset.getImmutable();
    }
}
