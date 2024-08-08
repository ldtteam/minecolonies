package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.Constants.STORAGE_STYLE;

public class ColonyPatrolPointRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static BlueprintPreviewData partolPointTemplate;

    private static Future<Blueprint> pendingTemplate;

    /**
     * Renders the guard scepter objects into the world.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.scepterGuard)
        {
            return;
        }

        final ModDataComponents.ColonyId colonyComponent = ctx.mainHandItem.get(ModDataComponents.COLONY_ID_COMPONENT);
        final ModDataComponents.Pos posComponent = ctx.mainHandItem.get(ModDataComponents.POS_COMPONENT);

        if (colonyComponent == null || posComponent == null)
        {
            return;
        }

        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyComponent.id(), colonyComponent.dimension());

        if (colony == null)
        {
            return;
        }

        final IBuildingView guardTowerView = colony.getBuilding(posComponent.pos());
        if (guardTowerView == null)
        {
            return;
        }

        if (pendingTemplate == null && partolPointTemplate == null)
        {
            pendingTemplate = StructurePacks.getBlueprintFuture(STORAGE_STYLE, "infrastructure/misc/patrolpoint.blueprint", ctx.clientLevel.registryAccess());
            return;
        }
        else if (pendingTemplate != null && pendingTemplate.isDone())
        {
            try
            {
                final BlueprintPreviewData tempPreviewData = new BlueprintPreviewData();
                tempPreviewData.setBlueprint(pendingTemplate.get());
                tempPreviewData.setPos(BlockPos.ZERO);
                partolPointTemplate = tempPreviewData;
                pendingTemplate = null;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            return;
        }

        if (guardTowerView instanceof AbstractBuildingGuards.View guardTower)
        {
            BlueprintHandler.getInstance().drawAtListOfPositions(partolPointTemplate, guardTower.getPatrolTargets(), ctx.stageEvent);
        }
    }
}
