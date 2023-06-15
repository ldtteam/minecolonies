package com.minecolonies.coremod.placementhandlers.main;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.OpenPlantationFieldBuildWindowMessage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Minecolonies plantation field blueprint handler.
 */
public class PlantationFieldPlacementHandler implements ISurvivalBlueprintHandler
{
    public static final String ID = MOD_ID + ":plantation_field";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("com.minecolonies.coremod.blueprint.placement");
    }

    @Override
    public boolean canHandle(final Blueprint blueprint, final ClientLevel level, final Player player, final BlockPos pos, final PlacementSettings placementSettings)
    {
        BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (!blockState.is(ModBlocks.blockPlantationField))
        {
            return false;
        }

        final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(level, pos);
        if (colonyView == null)
        {
            return false;
        }
        if (!colonyView.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            return false;
        }

        return colonyView.isCoordInColony(level, pos);
    }

    @Override
    public void handle(
      final Blueprint blueprint,
      final String packName,
      final String blueprintPath,
      final boolean clientPack,
      final Level level,
      final Player player,
      final BlockPos blockPos,
      final PlacementSettings placementSettings)
    {
        Network.getNetwork()
          .sendToPlayer(new OpenPlantationFieldBuildWindowMessage(blockPos, packName, blueprintPath, placementSettings.getRotation(), placementSettings.mirror),
            (ServerPlayer) player);
    }
}
