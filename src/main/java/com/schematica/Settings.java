package com.schematica;

import com.minecolonies.MineColonies;
import com.minecolonies.util.BlockPosUtil;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class Settings {
    public static final Settings instance = new Settings();

    public SchematicWorld schematic = null;

    public final BlockPos.MutableBlockPos pointA = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointB = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointMin = new BlockPos.MutableBlockPos();
    public final BlockPos.MutableBlockPos pointMax = new BlockPos.MutableBlockPos();

    public MovingObjectPosition movingObjectPosition = null;

    public final BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();

    public int rotation = 0;
    public String hut = "";
    public String style = "";

    public boolean isRenderingGuide = false;

    public boolean isPendingReset = false;

    private Settings() {
    }

    public void reset() {
        this.isRenderingGuide = false;

        MineColonies.proxy.setActiveSchematic(null);
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

    public void moveTo(BlockPos pos)
    {
        BlockPosUtil.set(offset, pos.subtract(MineColonies.proxy.getActiveSchematic().getOffset()));
        BlockPosUtil.set(schematic.position, offset);
    }
}
