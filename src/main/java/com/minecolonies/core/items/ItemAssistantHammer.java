package com.minecolonies.core.items;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.interactionhandling.SimpleNotificationInteraction;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import com.minecolonies.core.network.messages.server.PlayerAssistantBuildRequestMessage;
import com.minecolonies.core.placementhandlers.SolidPlaceholderPlacementHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Assistant Hammer item used to allow the player to assist the builder in building
 */
public class ItemAssistantHammer extends AbstractItemMinecolonies
{
    /**
     * The compound tag for the last pos the tool has been clicked.
     */
    private static final String TAG_LAST_POS = "lastPos";

    private int reach = 1;

    /**
     * GuardScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemAssistantHammer(final String id, final Properties properties, final int reach)
    {
        super(id, properties);
        this.reach = reach;
    }

    /**
     * Called from both using it on air, or attacking a block
     *
     * @param player
     */
    public void useOnBlock(final Player player, final BlockPos interactPos)
    {
        final Level level = player.level();
        final IColonyView view = IColonyManager.getInstance().getColonyView(level, interactPos);

        if (view == null || level == null || !view.getPermissions().hasPermission(player, Action.PLACE_BLOCKS))
        {
            return;
        }

        boolean unclaimed = true;
        for (final IWorkOrder workOrder : view.getWorkOrders())
        {
            if (workOrder.isClaimed() && workOrder.getBoundingBox() != null && workOrder.getBoundingBox().inflate(2).contains(Vec3.atLowerCornerOf(interactPos)))
            {
                unclaimed = false;
                if (workOrder.getBlueprint() == null)
                {
                    workOrder.loadBlueprint(player.level, b -> {});
                    return;
                }

                List<IPlacementHandler> handlers = new ArrayList<>(PlacementHandlers.handlers);
                SolidPlaceholderPlacementHandler solidPlaceHolderHandler = new SolidPlaceholderPlacementHandler();
                solidPlaceHolderHandler.setReplacement(view.getBuilding(workOrder.getClaimedBy())
                    .getModuleView(BuildingModules.BUILDER_SETTINGS)
                    .getSetting(BuildingMiner.FILL_BLOCK)
                    .getValue()
                    .getBlock()
                    .defaultBlockState());
                handlers.add(0, solidPlaceHolderHandler);

                final BuildAttemptResult buildAttemptResult = tryBuildingBlockNearby(player, view, workOrder, interactPos, handlers);
                if (buildAttemptResult.areBlocksToBuildNearby() && !buildAttemptResult.didTryBuilding())
                {
                    player.displayClientMessage(Component.translatable("item.minecolonies.assistanthammer.noitems"), true);
                }

                break;
            }
        }

        if (unclaimed)
        {
            player.displayClientMessage(Component.translatable("item.minecolonies.assistanthammer.onlyactive"), true);
        }
    }

    /**
     * Tries to place a block on serverside
     * Sends a full inventory on failure, as client side does already the item logic
     *
     * @param player
     * @param colony
     * @param workOrder
     */
    public void placeBlock(final Player player, final IColony colony, final IWorkOrder workOrder, final BlockPos interactPos)
    {
        if (workOrder.isClaimed())
        {
            final BuildingProgressStage stage = workOrder.getStage();
            if (stage == BuildingProgressStage.CLEAR || stage == BuildingProgressStage.CLEAR_NON_SOLIDS)
            {
                player.displayClientMessage(Component.translatable("item.minecolonies.assistanthammer.notcleared"), true);
                player.inventoryMenu.broadcastFullState();
                return;
            }

            // Fallback incase the builder did not load it yet for reasons
            if (workOrder.getBlueprint() == null)
            {
                workOrder.loadBlueprint(player.level(), b -> {});
                player.displayClientMessage(Component.translatable("item.minecolonies.assistanthammer.notloaded"), true);
                player.inventoryMenu.broadcastFullState();
                return;
            }

            List<IPlacementHandler> handlers = new ArrayList<>(PlacementHandlers.handlers);
            SolidPlaceholderPlacementHandler solidPlaceHolderHandler = new SolidPlaceholderPlacementHandler();
            solidPlaceHolderHandler.setReplacement(colony.getBuildingManager().getBuilding(workOrder.getClaimedBy())
                .getModule(BuildingModules.BUILDER_SETTINGS)
                .getSetting(BuildingMiner.FILL_BLOCK)
                .getValue()
                .getBlock()
                .defaultBlockState());
            handlers.add(0, solidPlaceHolderHandler);

            final BuildAttemptResult buildAttemptResult = tryBuildingBlockNearby(player, colony, workOrder, interactPos, handlers);
            if (buildAttemptResult.areBlocksToBuildNearby() && !buildAttemptResult.didTryBuilding())
            {
                player.displayClientMessage(Component.translatable("item.minecolonies.assistanthammer.noitems"), true);
                player.inventoryMenu.broadcastFullState();
            }

            if (buildAttemptResult.areBlocksToBuildNearby() && buildAttemptResult.didTryBuilding() && !player.isCreative())
            {
                player.getMainHandItem().hurtAndBreak(player.getMainHandItem().getItem().damageItem(player.getMainHandItem(), 1, player, s -> {}), player, s -> {});
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            final BlockPos interactPos = context.getClickedPos().relative(context.getClickedFace());
            useOnBlock(context.getPlayer(), interactPos);
        }

        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        if (level.isClientSide)
        {
            final BlockPos interactPos = BlockPos.containing(player.getEyePosition().add(player.getLookAngle().multiply(3, 3, 3)));
            useOnBlock(player, interactPos);
        }

        return InteractionResultHolder.success(player.getMainHandItem());
    }

    /**
     * Tries to build a block nearby, checking preview positions
     *
     * @param workOrder
     * @param interactPos
     * @param handlers
     * @return
     */
    private @NotNull ItemAssistantHammer.BuildAttemptResult tryBuildingBlockNearby(
        final Player player,
        final IColony colony,
        final IWorkOrder workOrder,
        final BlockPos interactPos,
        final List<IPlacementHandler> handlers)
    {
        final BlockPos.MutableBlockPos workPos = new BlockPos.MutableBlockPos();
        boolean areBlocksToBuildNearby = false;
        player.getCooldowns().addCooldown(this, 5);

        for (int currentDistance = 0; currentDistance <= reach; currentDistance++)
        {
            for (int x = -currentDistance; x <= currentDistance; x++)
            {
                for (int y = -currentDistance; y <= currentDistance; y++)
                {
                    for (int z = -currentDistance; z <= currentDistance; z++)
                    {
                        int distanceSq = x * x + y * y + z * z;
                        if (distanceSq > currentDistance * currentDistance)
                        {
                            continue;
                        }

                        workPos.set(x + interactPos.getX(), y + interactPos.getY(), z + interactPos.getZ());
                        final BlockState levelState = player.level().getBlockState(workPos);
                        final BlockInfo blockInfo =
                            workOrder.getBlueprint().getBlockInfoAsMap().get(workPos.subtract(workOrder.getLocation()).offset(workOrder.getBlueprint().getPrimaryBlockOffset()));

                        if (blockInfo == null || blockInfo.getState() == null || blockInfo.getState().getBlock() == levelState.getBlock() || !(levelState.isAir()
                            || !levelState.getFluidState().isEmpty()) || blockInfo.getState().getBlock() == ModBlocks.blockSubstitution.get()
                            || blockInfo.getState().getBlock() == ModBlocks.blockFluidSubstitution.get()
                            || blockInfo.getState().getBlock() == ModBlocks.blockTagSubstitution.get())
                        {
                            continue;
                        }

                        List<ItemStack> requiredItem = new ArrayList<>();

                        IPlacementHandler foundHandler = null;
                        for (final IPlacementHandler handler : handlers)
                        {
                            if (handler.canHandle(player.level(), BlockPos.ZERO, blockInfo.getState()))
                            {
                                final List<ItemStack> itemList = handler.getRequiredItems(player.level(), workPos, blockInfo.getState(), blockInfo.getTileEntityData(), false);
                                requiredItem.addAll(itemList);

                                foundHandler = handler;
                                break;
                            }
                        }

                        if (foundHandler == null)
                        {
                            requiredItem.add(BlockUtils.getItemStackFromBlockState(blockInfo.getState()));
                        }

                        // Only allow single block placement, no inventory, no free blocks
                        if (requiredItem.size() != 1)
                        {
                            continue;
                        }

                        areBlocksToBuildNearby = true;
                        boolean hasItems = true;

                        if (!player.isCreative())
                        {
                            for (final ItemStack required : requiredItem)
                            {
                                boolean found = false;
                                for (final ItemStack stack : player.getInventory().items)
                                {
                                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(required, stack))
                                    {
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found)
                                {
                                    hasItems = false;
                                }
                            }
                        }

                        if (!hasItems)
                        {
                            continue;
                        }

                        final IPlacementHandler.ActionProcessingResult result = foundHandler.handle(colony.getWorld(),
                            workPos,
                            blockInfo.getState(),
                            blockInfo.getTileEntityData(),
                            false,
                            workOrder.getLocation(),
                            new PlacementSettings(workOrder.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(workOrder.getRotation())));

                        if (result == IPlacementHandler.ActionProcessingResult.DENY)
                        {
                            continue;
                        }

                        if (!colony.getWorld().isClientSide())
                        {
                            final IBuilding building = colony.getBuildingManager().getBuilding(workOrder.getLocation());
                            if (building != null)
                            {
                                building.registerBlockPosition(blockInfo.getState(), workPos, colony.getWorld());
                            }
                        }

                        if (!player.isCreative())
                        {
                            InventoryUtils.removeStacksFromItemHandler(new InvWrapper(player.getInventory()), requiredItem);
                        }

                        // Server message
                        if (colony.getWorld().isClientSide)
                        {
                            Network.getNetwork()
                                .sendToServer(new PlayerAssistantBuildRequestMessage(colony, workOrder.getID(), interactPos));
                        }
                        else
                        {
                            final IBuilding building = colony.getBuildingManager().getBuilding(workOrder.getClaimedBy());
                            for (final ItemStack stack : requiredItem)
                            {
                                building.getModule(BuildingModules.BUILDING_RESOURCES).reduceNeededResource(stack, 1);
                            }

                            if (ColonyConstants.rand.nextInt(20) == 0)
                            {
                                final var buildingBuilder = colony.getBuildingManager().getBuilding(workOrder.getClaimedBy());
                                if (buildingBuilder != null)
                                {
                                    buildingBuilder.getModule(BuildingModules.BUILDER_WORK).getAssignedCitizen()
                                        .forEach(citizen -> citizen.triggerInteraction(new SimpleNotificationInteraction(Component.translatable(
                                            "item.minecolonies.assistanthammer.happybuilder"),
                                            ChatPriority.CHITCHAT)));
                                }
                            }
                        }

                        for (int i = 0; i < 50; ++i)
                        {
                            player.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockInfo.getState()),
                                workPos.getX() + 0.5f - 0.5 + ColonyConstants.rand.nextFloat(1.5f),
                                workPos.getY() + 0.5f - 0.5 + ColonyConstants.rand.nextFloat(1.5f),
                                workPos.getZ() + 0.5f - 0.5 + ColonyConstants.rand.nextFloat(1.5f),
                                ColonyConstants.rand.nextGaussian() * 5,
                                ColonyConstants.rand.nextGaussian() * 5,
                                ColonyConstants.rand.nextGaussian() * 5);
                        }

                        // Small random hammer "clang" sound
                        if (player.level().isClientSide() && ColonyConstants.rand.nextInt(5) == 0)
                        {
                            player.level()
                                .playSound(player,
                                    workPos,
                                    SoundEvents.CHAIN_HIT,
                                    SoundSource.BLOCKS,
                                    (0.75f + ColonyConstants.rand.nextFloat(0.5f)) * 2,
                                    (0.9f + ColonyConstants.rand.nextFloat(0.2f)));
                        }

                        // placement sound of the block being placed, also heared by other players nearby
                        player.level()
                            .playSound(player,
                                workPos,
                                blockInfo.getState().getSoundType().getPlaceSound(),
                                SoundSource.BLOCKS,
                                blockInfo.getState().getSoundType().getVolume() * (0.75f + ColonyConstants.rand.nextFloat(0.5f)) * 2,
                                blockInfo.getState().getSoundType().getPitch() * (0.9f + ColonyConstants.rand.nextFloat(0.2f)));

                        return new BuildAttemptResult(true, true);
                    }
                }
            }
        }

        return new BuildAttemptResult(areBlocksToBuildNearby, false);
    }

    /**
     * Build attempt result
     *
     * @param areBlocksToBuildNearby
     * @param didTryBuilding
     */
    private record BuildAttemptResult(
        boolean areBlocksToBuildNearby,
        boolean didTryBuilding) {}

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltipList, TooltipFlag flag)
    {
        tooltipList.add(Component.translatable("item.minecolonies.assistanthammer.reach", reach).withStyle(ChatFormatting.BLUE));
        tooltipList.add(Component.translatable("item.minecolonies.assistanthammer.desc").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }
}
