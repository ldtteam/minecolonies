package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

public class ColonyPatrolPointRenderer
{
    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint partolPointTemplate;

    private static Future<Blueprint> pendingTemplate;

    /**
     * Renders the guard scepter objects into the world.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.scepterGuard || !ctx.mainHandItem.hasTag())
        {
            return;
        }

        final CompoundTag itemStackNbt = ctx.mainHandItem.getTag();
        final IColonyView colony = IColonyManager.getInstance().getColonyView(itemStackNbt.getInt(TAG_ID), ctx.clientLevel.dimension());

        if (colony == null)
        {
            return;
        }

        final IBuildingView guardTowerView = colony.getBuilding(BlockPosUtil.read(itemStackNbt, TAG_POS));
        if (guardTowerView == null)
        {
            return;
        }

        if (pendingTemplate == null && partolPointTemplate == null)
        {
            pendingTemplate = StructurePacks.getBlueprintFuture("Default", "infrastructure/roads/patrolpoint");
            return;
        }
        else if (pendingTemplate != null && pendingTemplate.isDone())
        {
            try
            {
                partolPointTemplate = pendingTemplate.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            return;
        }

        if (guardTowerView instanceof AbstractBuildingGuards.View guardTower)
        {
            StructureClientHandler.renderStructureAtPosList(partolPointTemplate,
                ctx.partialTicks,
                guardTower.getPatrolTargets().stream().map(BlockPos::above).toList(),
                ctx.poseStack);
        }
    }
}
