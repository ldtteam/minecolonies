package com.minecolonies.coremod.placementhandlers.main;

import com.ldtteam.structurize.blocks.interfaces.ILeveledBlueprintAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.network.messages.client.OpenDecoWindowMessage;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

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
        return Component.translatable("com.minecolonies.coremod.blueprint.placement");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canHandle(final Blueprint blueprint, final ClientLevel clientLevel, final Player player, final BlockPos blockPos, final PlacementSettings placementSettings)
    {
        BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (blockState.is(ModBlocks.blockHutTownHall))
        {
            return true;
        }

        final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(clientLevel, blockPos);
        if (colonyView == null)
        {
            return false;
        }

        if (!colonyView.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            return false;
        }

        return colonyView.isCoordInColony(clientLevel, blockPos) &&
                 (blockState.getBlock() instanceof AbstractBlockHut<?> || blockState.is(ModBlocks.blockDecorationPlaceholder));
    }

    @Override
    public void handle(
      final Blueprint blueprint,
      final String packName,
      final String blueprintPath,
      final boolean clientPack,
      final Level world,
      final Player player,
      final BlockPos blockPos,
      final PlacementSettings placementSettings)
    {
        if (blueprint == null)
        {
            // This can happen if the file didnt finish synching with the server from the client, or something went wrong when synching (package dropped, etc).
            MessageUtils.format(NO_CUSTOM_BUILDINGS).sendTo(player);
            SoundUtils.playErrorSound(player, player.blockPosition());
            return;
        }
        blueprint.rotateWithMirror(placementSettings.rotation, placementSettings.mirror == Mirror.NONE ? Mirror.NONE : Mirror.FRONT_BACK, world);
        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (anchor.getBlock() instanceof AbstractBlockHut<?>)
        {
            if (clientPack || !StructurePacks.hasPack(packName))
            {
                MessageUtils.format(BUILDING_MISSING).sendTo(player);
                SoundUtils.playErrorSound(player, player.blockPosition());
                return;
            }

            final ItemStack stack = new ItemStack(anchor.getBlock());
            if (anchor.getBlock() != null && EventHandler.onBlockHutPlaced(world, player, anchor.getBlock(), blockPos))
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), anchor.getBlock());
                if (slot == -1 && !player.isCreative())
                {
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }

                final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, blockPos);
                if (tempColony != null
                      && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)
                            && !(anchor.getBlock() instanceof BlockHutTownHall
                                   && IColonyManager.getInstance().isFarEnoughFromColonies(world, blockPos))))
                {
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }

                final ItemStack inventoryStack = slot == -1 ? stack : player.getInventory().getItem(slot);
                final CompoundTag compound = inventoryStack.getTag();
                if (tempColony != null && compound != null && compound.contains(TAG_COLONY_ID) && tempColony.getID() != compound.getInt(TAG_COLONY_ID))
                {
                    MessageUtils.format(WRONG_COLONY, compound.getInt(TAG_COLONY_ID)).sendTo(player);
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }

                if (tempColony != null)
                {
                    AdvancementUtils.TriggerAdvancementPlayersForColony(tempColony, playerMP -> AdvancementTriggers.PLACE_STRUCTURE.trigger(playerMP, ((AbstractBlockHut<?>) anchor.getBlock()).getBlueprintName()));
                }
                else
                {
                    AdvancementTriggers.PLACE_STRUCTURE.trigger((ServerPlayer) player, ((AbstractBlockHut<?>) anchor.getBlock()).getBlueprintName());
                }

                world.destroyBlock(blockPos, true);
                world.setBlockAndUpdate(blockPos, anchor);
                ((AbstractBlockHut<?>) anchor.getBlock()).onBlockPlacedByBuildTool(world,
                  blockPos,
                  anchor,
                  player,
                  null,
                  placementSettings.getMirror() != Mirror.NONE,
                  packName,
                  blueprintPath);

                int level = 0;
                boolean finishedUpgrade = false;
                if (compound != null)
                {
                    if (compound.contains(TAG_OTHER_LEVEL))
                    {
                        level = compound.getInt(TAG_OTHER_LEVEL);
                    }
                    if (compound.contains(TAG_PASTEABLE))
                    {
                        String newBlueprintPath = blueprintPath;
                        newBlueprintPath = newBlueprintPath.substring(0, newBlueprintPath.length() - 1);
                        newBlueprintPath += level;
                        CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, StructurePacks.getBlueprintFuture(packName, newBlueprintPath),
                          blockPos, placementSettings.getRotation(), placementSettings.getMirror() != Mirror.NONE ? Mirror.FRONT_BACK : Mirror.NONE, true, (ServerPlayer) player);
                        finishedUpgrade = true;
                    }
                }

                InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), inventoryStack, 1);

                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, blockPos);
                if (building == null)
                {
                    if (!(anchor.getBlock() instanceof BlockHutTownHall))
                    {
                        SoundUtils.playErrorSound(player, player.blockPosition());
                        Log.getLogger().error("BuildTool: building is null!", new Exception());
                    }
                }
                else
                {
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                    if (building.getTileEntity() != null)
                    {
                        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, blockPos);
                        if (colony == null)
                        {
                            Log.getLogger().info("No colony for " + player.getName().getString());
                        }
                        else
                        {
                            building.getTileEntity().setColony(colony);
                        }
                    }

                    building.setStructurePack(packName);
                    building.setBlueprintPath(blueprintPath);

                    building.setBuildingLevel(level);
                    if (level > 0)
                    {
                        building.setDeconstructed();
                    }

                    if (!(building instanceof IRSComponent))
                    {
                        ConstructionTapeHelper.placeConstructionTape(building.getCorners(), world);
                    }

                    building.setIsMirrored(placementSettings.mirror != Mirror.NONE);

                    if (finishedUpgrade)
                    {
                        building.onUpgradeComplete(building.getBuildingLevel());
                    }
                }
            }
            SoundUtils.playSuccessSound(player, player.blockPosition());
        }
        else
        {
            if (blueprint.getBlockState(blueprint.getPrimaryBlockOffset()).getBlock() instanceof ILeveledBlueprintAnchorBlock)
            {
                int level = Utils.getBlueprintLevel(blueprint.getFileName());
                if (level == -1)
                {
                    Network.getNetwork().sendToPlayer(new OpenDecoWindowMessage(blockPos, packName, blueprintPath, placementSettings.getRotation(), placementSettings.mirror), (ServerPlayer) player);
                }
                else
                {
                    Network.getNetwork().sendToPlayer(new OpenDecoWindowMessage(blockPos, packName, blueprintPath.replace(level + ".blueprint", "1.blueprint"), placementSettings.getRotation(), placementSettings.mirror), (ServerPlayer) player);
                }
            }
            else
            {
                Network.getNetwork().sendToPlayer(new OpenDecoWindowMessage(blockPos, packName, blueprintPath, placementSettings.getRotation(), placementSettings.mirror), (ServerPlayer) player);
            }
        }

        Log.getLogger().warn("Handling Survival Placement in Colony");
    }
}
