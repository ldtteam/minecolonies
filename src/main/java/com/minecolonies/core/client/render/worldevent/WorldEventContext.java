package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;

/**
 * Main class for handling world rendering.
 * Also holds all possible values which may be needed during rendering.
 */
public class WorldEventContext extends WorldRenderMacros
{
    public static final WorldEventContext INSTANCE = new WorldEventContext();

    private WorldEventContext()
    {
        // singleton
    }

    @Nullable
    public IColonyView nearestColony;

    @Override
    protected void renderWithinContext(final Stage stage)
    {
        if (stage == STAGE_FOR_LINES)
        {
            ColonyBorderRenderer.render(this); // renders directly (not into bufferSource)
            ColonyBlueprintRenderer.renderBlueprints(this);
            ColonyWaypointRenderer.render(this);
            ColonyPatrolPointRenderer.render(this);
            GuardTowerRallyBannerRenderer.render(this);
            PathfindingDebugRenderer.render(this);
            ColonyBlueprintRenderer.renderBoxes(this);
            ItemOverlayBoxesRenderer.render(this);
            HighlightManager.render(this);
        }
    }

    boolean hasNearestColony()
    {
        return nearestColony != null;
    }

    /**
     * Checks for a nearby colony
     *
     * @param level
     */
    public void checkNearbyColony(final Level level)
    {
        if (clientPlayer != null)
        {
            nearestColony = IColonyManager.getInstance().getClosestColonyView(level, clientPlayer.blockPosition());
        }
    }

    public void renderLineBoxWithShadow(final BlockPos pos, final int argbColor, final float lineWidth)
    {
        final int red = FastColor.ARGB32.red(argbColor);
        final int green = FastColor.ARGB32.green(argbColor);
        final int blue = FastColor.ARGB32.blue(argbColor);
        final int alpha = FastColor.ARGB32.alpha(argbColor);

        renderLineBox(LINES_WITH_WIDTH_DEPTH_INVERT, pos, pos, red / 2, green / 2, blue / 2, alpha / 2, lineWidth);
        renderLineBox(LINES_WITH_WIDTH, pos, pos, red, green, blue, alpha, lineWidth);
    }

    public void renderLineAABBWithShadow(final AABB aabb, final int argbColor, final float lineWidth)
    {
        final int red = FastColor.ARGB32.red(argbColor);
        final int green = FastColor.ARGB32.green(argbColor);
        final int blue = FastColor.ARGB32.blue(argbColor);
        final int alpha = FastColor.ARGB32.alpha(argbColor);

        renderLineAABB(LINES_WITH_WIDTH_DEPTH_INVERT, aabb, red / 2, green / 2, blue / 2, alpha / 2, lineWidth);
        renderLineAABB(LINES_WITH_WIDTH, aabb, red, green, blue, alpha, lineWidth);
    }
}
