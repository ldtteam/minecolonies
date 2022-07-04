package com.minecolonies.coremod.placementhandlers.main;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Minecolonies survival blueprint handler.
 */
public class SurvivalHandler implements ISurvivalBlueprintHandler
{

    @Override
    public String getId()
    {
        return Constants.MOD_ID;
    }

    @Override
    public Component getDisplayName()
    {
        return new TranslatableComponent("com.minecolonies.coremod.blueprint.placement");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canHandle(final Blueprint blueprint, final ClientLevel clientLevel, final Player player, final BlockPos blockPos, final PlacementSettings placementSettings)
    {
        final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(clientLevel, blockPos);
        if (colonyView == null)
        {
            return false;
        }

        if (!colonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return false;
        }

        return colonyView.isCoordInColony(clientLevel, blockPos);
    }

    @Override
    public void handle(final Blueprint blueprint, final boolean clientPack, final Level level, final Player player, final BlockPos blockPos, final PlacementSettings placementSettings)
    {
        //todo, check if decoration (through anchor), if so, we're alright. Just gotta make sure that decos can't place upgraded hut blocks
        //todo if deco, then we make a build request for the deco (we also want to double check the deco controller, maybe even ask for a deco controller beforehand)
        //todo if building, then we check if the blueprint exists on the server side, if not

        // todo remove block from player inventory
        // todo register the stuff.

        Log.getLogger().warn("Handling Survival Placement in Colony");
    }
}
