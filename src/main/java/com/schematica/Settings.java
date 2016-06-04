package com.schematica;

import com.minecolonies.util.BlockPosUtil;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import com.schematica.world.storage.Schematic;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class Settings
{
    public static final Settings instance = new Settings();

    private boolean inHutMode = true;

    public SchematicWorld schematic = null;

    public final BlockPos.MutableBlockPos pointA  = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointB  = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointMin = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointMax = new BlockPos.MutableBlockPos();

    public MovingObjectPosition movingObjectPosition = null;

    public final BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();

    private int    rotation = 0;
    private String hutDec   = "";
    private String style    = "";

    public boolean isRenderingGuide = false;

    public boolean isPendingReset = false;

    private Settings() {
    }

    /**
     * Reset the schematic rendering.
     */
    public void reset() {
        this.isRenderingGuide = false;

        schematic = null;
        RenderSchematic.INSTANCE.setWorldAndLoadRenderers(null);

        pointA.set(0, 0, 0);
        pointB.set(0, 0, 0);
        updatePoints();
    }

    private void updatePoints()
    {
        pointMin.set(Math.min(pointA.getX(), pointB.getX()), Math.min(pointA.getY(), pointB.getY()), Math.min(pointA.getZ(), pointB.getZ()));
        pointMax.set(Math.max(pointA.getX(), pointB.getX()), Math.max(pointA.getY(), pointB.getY()), Math.max(pointA.getZ(), pointB.getZ()));
    }

    /**
     * Set location to render current schematic.
     *
     * @param pos location to render.
     */
    public void moveTo(BlockPos pos)
    {
        BlockPosUtil.set(offset, pos.subtract(getActiveSchematic().getOffset()));
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
    public Schematic getActiveSchematic()
    {
        return this.schematic == null ? null : this.schematic.getSchematic();
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
     * @param hutDec hut/decoration name.
     * @param style building style.
     * @param rotation the number of times the building is rotated.
     */
    public void setSchematicInfo(String hutDec, String style, int rotation)
    {
        this.hutDec   = hutDec;
        this.style    = style;
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
     * @return The number of times the schematic is rotated.
     */
    public int getRotation()
    {
        return rotation;
    }
}
