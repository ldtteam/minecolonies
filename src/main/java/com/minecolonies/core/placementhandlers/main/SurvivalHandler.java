package com.minecolonies.core.placementhandlers.main;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blocks.interfaces.ILeveledBlueprintAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ISurvivalBlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.items.ModDataComponents.ColonyId;
import com.minecolonies.api.items.ModDataComponents.HutBlockData;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.huts.BlockHutTownHall;
import com.minecolonies.core.entity.ai.workers.util.ConstructionTapeHelper;
import com.minecolonies.core.event.EventHandler;
import com.minecolonies.core.network.messages.client.OpenDecoBuildWindowMessage;
import com.minecolonies.core.network.messages.client.OpenPlantationFieldBuildWindowMessage;
import com.minecolonies.core.util.AdvancementUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

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
        return Component.translatableEscape("com.minecolonies.coremod.blueprint.placement");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canHandle(final Blueprint blueprint, final ClientLevel clientLevel, final Player player, final BlockPos blockPos, final RotationMirror rotMir)
    {
        if (IMinecoloniesAPI.getInstance().getConfig().getServer().blueprintBuildMode.get())
        {
            final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(clientLevel, blockPos);
            return colonyView != null;
        }

        return true;
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
      final RotationMirror rotMir)
    {
        if (blueprint == null)
        {
            // This can happen if the file didnt finish synching with the server from the client, or something went wrong when synching (package dropped, etc).
            MessageUtils.format(NO_CUSTOM_BUILDINGS).sendTo(player);
            SoundUtils.playErrorSound(player, player.blockPosition());
            return;
        }

        blueprint.setRotationMirror(rotMir, world);
        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());

        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, blockPos);
        final boolean isInColony = tempColony != null && tempColony.isCoordInColony(world, blockPos);
        if (isInColony && !tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            MessageUtils.format(BP_NO_PERM).sendTo(player);
            SoundUtils.playErrorSound(player, player.blockPosition());
            return;
        }

        boolean successfulTownHallLocation = false;
        if (anchor.getBlock() instanceof BlockHutTownHall)
        {
            if (isInColony || IColonyManager.getInstance().isFarEnoughFromColonies(world, blockPos))
            {
                successfulTownHallLocation = true;
            }
            else
            {
                MessageUtils.format(TOWNHALL_TOO_CLOSE).sendTo(player);
                SoundUtils.playErrorSound(player, player.blockPosition());
                return;
            }
        }

        if ((!isInColony || !isBlueprintInColony(blueprint, tempColony, blockPos)) && !successfulTownHallLocation)
        {
            MessageUtils.format(BP_OUTSIDE_COLONY).sendTo(player);
            SoundUtils.playErrorSound(player, player.blockPosition());
            return;
        }

        if (anchor.is(ModBlocks.blockPlantationField))
        {
            new OpenPlantationFieldBuildWindowMessage(blockPos, packName, blueprintPath, rotMir).sendToPlayer((ServerPlayer) player);
        }
        if (anchor.getBlock() instanceof AbstractBlockHut<?>)
        {
            if (clientPack || !StructurePacks.hasPack(packName) || blueprintPath.startsWith("scans/"))
            {
                MessageUtils.format(BUILDING_MISSING).sendTo(player);
                SoundUtils.playErrorSound(player, player.blockPosition());
                return;
            }

            final ItemStack stack = new ItemStack(anchor.getBlock());
            if (EventHandler.onBlockHutPlaced(world, player, anchor.getBlock(), blockPos))
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), anchor.getBlock());
                if (slot == -1 && !player.isCreative())
                {
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }

                final ItemStack inventoryStack = slot == -1 ? stack : player.getInventory().getItem(slot);

                final ModDataComponents.ColonyId colonyComponent = ColonyId.readFromItemStack(stack);
                if (colonyComponent != null && tempColony != null && tempColony.getID() != colonyComponent.id())
                {
                    MessageUtils.format(WRONG_COLONY, colonyComponent.id()).sendTo(player);
                    SoundUtils.playErrorSound(player, player.blockPosition());
                    return;
                }

                world.destroyBlock(blockPos, true);
                world.setBlockAndUpdate(blockPos, anchor);
                ((AbstractBlockHut<?>) anchor.getBlock()).onBlockPlacedByBuildTool(world,
                  blockPos,
                  anchor,
                  player,
                  null,
                  rotMir,
                  packName,
                  blueprintPath);
                NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(world.dimension(), world, blockPos), world.getBlockState(blockPos.below()), player));
                InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), inventoryStack, 1);

                if (tempColony == null)
                {
                    // Townhall Placement
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                    return;
                }

                AdvancementUtils.TriggerAdvancementPlayersForColony(tempColony, playerMP -> AdvancementTriggers.PLACE_STRUCTURE.get().trigger(playerMP, ((AbstractBlockHut<?>) anchor.getBlock()).getBlueprintName()));


                int level = 0;
                boolean finishedUpgrade = false;
                final ModDataComponents.HutBlockData hutComponent = HutBlockData.readFromItemStack(stack);
                if (hutComponent != null)
                {
                    if (hutComponent.level() != -1)
                    {
                        level = hutComponent.level();
                    }
                    if (hutComponent.pastable())
                    {
                        String newBlueprintPath = blueprintPath;
                        newBlueprintPath = newBlueprintPath.substring(0, newBlueprintPath.length() - 1);
                        newBlueprintPath += level;
                        CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level(), StructurePacks.getBlueprintFuture(packName, newBlueprintPath, world.registryAccess()),
                          blockPos, rotMir, true, (ServerPlayer) player);
                        finishedUpgrade = true;
                    }
                }

                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, blockPos);
                if (building == null)
                {
                    if (!(anchor.getBlock() instanceof BlockHutTownHall))
                    {
                        SoundUtils.playErrorSound(player, player.blockPosition());
                        Log.getLogger().error("BuildTool: building is null!", new Exception());
                        return;
                    }
                }
                else
                {
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
                        ConstructionTapeHelper.placeConstructionTape(building.getCorners(), building.getColony());
                    }

                    building.setRotationMirror(rotMir);

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
                    new OpenDecoBuildWindowMessage(blockPos, packName, blueprintPath, rotMir).sendToPlayer((ServerPlayer) player);
                }
                else
                {
                    new OpenDecoBuildWindowMessage(blockPos, packName, blueprintPath.replace(level + ".blueprint", "1.blueprint"), rotMir).sendToPlayer((ServerPlayer) player);
                }
            }
            else
            {
                new OpenDecoBuildWindowMessage(blockPos, packName, blueprintPath, rotMir).sendToPlayer((ServerPlayer) player);
            }
        }

        Log.getLogger().warn("Handling Survival Placement in Colony");
    }

    /**
     * Check if the blueprint is fully inside colony boundaries.
     * @param blueprint the blueprint to check.
     * @param colony the colony to check for.
     * @param blockPos the position to check at.
     * @return true if so.
     */
    private boolean isBlueprintInColony(final Blueprint blueprint, final IColony colony, final BlockPos blockPos)
    {
        final Level world = colony.getWorld();
        final BlockPos zeroPos = blockPos.subtract(blueprint.getPrimaryBlockOffset());

        final BlockPos pos1 = new BlockPos(zeroPos.getX(), zeroPos.getY(), zeroPos.getZ());
        final BlockPos pos2 = new BlockPos(zeroPos.getX() + blueprint.getSizeX() - 1, zeroPos.getY() + blueprint.getSizeY() - 1, zeroPos.getZ() + blueprint.getSizeZ() - 1);

        final int minX = Math.min(pos1.getX(), pos2.getX()) + 1;
        final int maxX = Math.max(pos1.getX(), pos2.getX());

        final int minZ = Math.min(pos1.getZ(), pos2.getZ()) + 1;
        final int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x < maxX; x += 16)
        {
            for (int z = minZ; z < maxZ; z += 16)
            {
                final int chunkX = x >> 4;
                final int chunkZ = z >> 4;
                final ChunkPos pos = new ChunkPos(chunkX, chunkZ);

                if (ColonyUtils.getOwningColony(world.getChunk(pos.x, pos.z)) != colony.getID())
                {
                    return false;
                }
            }
        }
        return true;
    }
}
