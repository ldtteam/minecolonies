package com.minecolonies.coremod.placementhandlers;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.BlockSolidSubstitution;
import com.minecolonies.coremod.blocks.BlockWaypoint;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingWareHouse;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Class containing all placement handler implementations.
 * <p>
 * We suppress warning squid:S2972 which handles the max size of internal classes.
 * This doesn't apply here since it wouldn't make sense extracting all of those in separate classes.
 */
@SuppressWarnings("squid:S2972")
public final class PlacementHandlers
{
    public static final List<IPlacementHandler> handlers = new ArrayList<>();
    static
    {
        handlers.add(new AirPlacementHandler());
        handlers.add(new FireplacementHandler());
        handlers.add(new GrassPlacementHandler());
        handlers.add(new DoorPlacementHandler());
        handlers.add(new BedPlacementHandler());
        handlers.add(new DoublePlantPlacementHandler());
        handlers.add(new SpecialBlockPlacementAttemptHandler());
        handlers.add(new FlowerPotPlacementHandler());
        handlers.add(new BlockGrassPathPlacementHandler());
        handlers.add(new StairBlockPlacementHandler());
        handlers.add(new BlockSolidSubstitutionPlacementHandler());
        handlers.add(new ChestPlacementHandler());
        handlers.add(new WayPointBlockPlacementHandler());
        handlers.add(new RackPlacementHandler());
        handlers.add(new GeneralBlockPlacementHandler());
    }

    private PlacementHandlers()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Check the placers inventory for the items in the itemList and remove it of the list if found.
     *
     * @param placer   the placer.
     * @param itemList the list to check.
     * @return true if need to request.
     */
    public static boolean checkForListInInvAndRequest(@NotNull final AbstractEntityAIStructure<?> placer, final List<ItemStack> itemList)
    {
        final List<ItemStack> foundStacks = InventoryUtils.filterItemHandler(new InvWrapper(placer.getWorker().getInventoryCitizen()),
          itemStack -> itemList.stream()
                         .anyMatch(targetStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, targetStack)));
        itemList.removeIf(itemStack -> ItemStackUtils.isEmpty(itemStack) || foundStacks.stream()
                                         .anyMatch(targetStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, targetStack)));

        for (final ItemStack placedStack : itemList)
        {
            if(ItemStackUtils.isEmpty(placedStack))
            {
                return true;
            }

            if (placer.getOwnBuilding()
                  .getOpenRequestsOfTypeFiltered(
                    placer.getWorker().getCitizenData(),
                          TypeConstants.DELIVERABLE,
                    (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(placedStack))
                  .isEmpty())
            {
                final Stack stackRequest = new Stack(placer.getTotalAmount(placedStack));
                placer.getWorker().getCitizenData().createRequest(stackRequest);

                return true;
            }
        }
        return false;
    }

    //If he woudln't count the bracket spaces we'd be under 25 easily.
    public static class FireplacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final AbstractEntityAIStructure<?> placer,
                              final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockFire))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null)
            {
                if (!infiniteResources)
                {
                    final EntityCitizen citizen = placer.getWorker();
                    final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(new InvWrapper(citizen.getInventoryCitizen()), s ->
                                                                                                                                            s.getItem() == Items.FLINT_AND_STEEL);

                    if (slot == -1)
                    {
                        final ItemStack tool = new ItemStack(Items.FLINT_AND_STEEL, 1);
                        final List<ItemStack> foundStacks = InventoryUtils.filterItemHandler(new InvWrapper(placer.getWorker().getInventoryCitizen()),
                          tool::isItemEqualIgnoreDurability);

                        if (foundStacks.isEmpty())
                        {
                            if (citizen.getWorkBuilding()
                                  .getOpenRequestsOfTypeFiltered(citizen.getCitizenData(), TypeToken.of(Stack.class), (IRequest<? extends Stack> s) ->
                                                                                                                        s.getRequest().getStack().getItem()
                                                                                                                          == Items.FLINT_AND_STEEL)
                                  .isEmpty())
                            {
                                final Stack stack = new Stack(new ItemStack(Items.FLINT_AND_STEEL, 1)).setMatchMeta(false).setMatchNBT(false);
                                placer.getWorker().getCitizenData().createRequest(stack);
                            }
                            return ActionProcessingResult.DENY;
                        }
                    }

                    final ItemStack item = slot == -1 ? ItemStackUtils.EMPTY : citizen.getInventoryCitizen().getStackInSlot(slot);
                    if (ItemStackUtils.isEmpty(item) || !(item.getItem() instanceof ItemFlintAndSteel))
                    {
                        return ActionProcessingResult.DENY;
                    }
                    citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item);
                    item.damageItem(1, citizen);
                }
                placer.handleBuildingOverBlock(pos);
            }
            world.setBlockState(pos, blockState, UPDATE_FLAG);
            return ActionProcessingResult.ACCEPT;
        }
    }

    public static class GrassPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (blockState.getBlock() != Blocks.GRASS)
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(placer.getTotalAmount(placer.getTotalAmount(new ItemStack(Blocks.DIRT))));
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }

                placer.handleBuildingOverBlock(pos);

                if (!world.setBlockState(pos, Blocks.DIRT.getDefaultState(), UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }
            else if (!world.setBlockState(pos, Blocks.GRASS.getDefaultState(), UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return Blocks.DIRT.getDefaultState();
        }
    }

    public static class DoorPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockDoor))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState)));
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
            }

            if (blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
            {
                if (placer != null)
                {
                    placer.handleBuildingOverBlock(pos);
                }
                ItemDoor.placeDoor(world, pos, blockState.getValue(BlockDoor.FACING), blockState.getBlock(), false);
            }

            return blockState;
        }
    }

    public static class BedPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockBed))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState)));
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
            }

            final EnumFacing facing = blockState.getValue(BlockBed.FACING);

            //Set other part of the bed, to the opposite PartType
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                if (placer != null)
                {
                    placer.handleBuildingOverBlock(pos);
                }
                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos.offset(facing), blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), UPDATE_FLAG);
                world.setBlockState(pos, blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), UPDATE_FLAG);
                return blockState;
            }
            return ActionProcessingResult.ACCEPT;
        }
    }

    public static class DoublePlantPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockDoublePlant))
            {
                return ActionProcessingResult.IGNORE;
            }


            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState)));
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
            }

            if (blockState.getValue(BlockDoublePlant.HALF).equals(BlockDoublePlant.EnumBlockHalf.LOWER))
            {
                if (placer != null)
                {
                    placer.handleBuildingOverBlock(pos);
                }
                world.setBlockState(pos, blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlockState(pos.up(), blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), UPDATE_FLAG);
                return blockState;
            }
            return ActionProcessingResult.ACCEPT;
        }
    }

    public static class SpecialBlockPlacementAttemptHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (blockState instanceof BlockEndPortal
                  || blockState instanceof BlockMobSpawner
                  || blockState instanceof BlockDragonEgg
                  || blockState instanceof BlockPortal)
            {
                return ActionProcessingResult.ACCEPT;
            }
            return ActionProcessingResult.IGNORE;
        }
    }

    public static class FlowerPotPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final AbstractEntityAIStructure<?> placer,
                              final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockFlowerPot))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null)
            {
                if (!infiniteResources)
                {
                    final List<ItemStack> itemList = new ArrayList<>();
                    itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
                    itemList.addAll(placer.getItemsFromTileEntity());

                    itemList.removeIf(ItemStackUtils::isEmpty);

                    if (checkForListInInvAndRequest(placer, itemList))
                    {
                        return IPlacementHandler.ActionProcessingResult.DENY;
                    }
                }

                placer.handleBuildingOverBlock(pos);
            }
            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return false;
            }

            if (placer != null)
            {
                placer.handleTileEntityPlacement(pos);
            }
            return blockState;
        }
    }

    public static class AirPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (blockState.getBlock() instanceof BlockAir)
            {
                if (placer != null)
                {
                    placer.getWorker().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);

                    //Meaning there is not supposed to be an entity at this location
                    if (placer.getEntityInfo() == null)
                    {
                        final List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos),
                          entity -> !(entity instanceof EntityLiving || entity instanceof EntityItem));
                        if (!entityList.isEmpty())
                        {
                            for (final Entity entity : entityList)
                            {
                                entity.attackEntityFrom(DamageSource.ANVIL, Float.MAX_VALUE);
                            }
                        }
                    }

                    placer.handleBuildingOverBlock(pos);
                }
                world.setBlockToAir(pos);
                return ActionProcessingResult.ACCEPT;
            }

            return ActionProcessingResult.IGNORE;
        }
    }

    public static class BlockGrassPathPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockGrassPath))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                if (!placer.holdEfficientTool(blockState.getBlock()))
                {
                    return ActionProcessingResult.DENY;
                }
                final ItemStack placedStack = placer.getTotalAmount(new ItemStack(Blocks.DIRT, 1));
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(placedStack);
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }

                placer.handleBuildingOverBlock(pos);
            }

            if (!world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return Blocks.DIRT.getDefaultState();
        }
    }

    public static class StairBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            //Workaround as long as we didn't rescan all of our buildings since BlockStairs now have different metadata values.
            if (blockState.getBlock() instanceof BlockStairs
                  && world.getBlockState(pos).getBlock() instanceof BlockStairs
                  && world.getBlockState(pos).getValue(BlockStairs.FACING) == blockState.getValue(BlockStairs.FACING)
                  && blockState.getBlock() == world.getBlockState(pos).getBlock())
            {
                return ActionProcessingResult.ACCEPT;
            }

            return ActionProcessingResult.IGNORE;
        }
    }

    public static class BlockSolidSubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockSolidSubstitution))
            {
                return ActionProcessingResult.IGNORE;
            }

            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(newBlockState));
                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
                placer.handleBuildingOverBlock(pos);
            }

            if (complete)
            {
                if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }
            else
            {
                if (!world.setBlockState(pos, newBlockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }

            return newBlockState;
        }
    }

    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.ACCEPT;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
                itemList.addAll(placer.getItemsFromTileEntity());
                itemList.removeIf(ItemStackUtils::isEmpty);

                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
                placer.handleBuildingOverBlock(pos);
            }

            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (placer != null)
            {
                placer.handleTileEntityPlacement(pos);
            }

            return blockState;
        }
    }

    public static class WayPointBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockWaypoint))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer == null)
            {
                final Colony colony = ColonyManager.getClosestColony(world, pos);
                if (colony != null && !complete)
                {
                    colony.addWayPoint(pos, Blocks.AIR.getDefaultState());
                }
                else
                {
                    return ActionProcessingResult.IGNORE;
                }
            }
            else
            {
                placer.handleBuildingOverBlock(pos);
                placer.addWayPoint(pos);
            }
            world.setBlockToAir(pos);

            return blockState;
        }
    }

    public static class ChestPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockChest))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
                itemList.addAll(placer.getItemsFromTileEntity());

                itemList.removeIf(ItemStackUtils::isEmpty);

                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
            }

            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                BuildingWareHouse.handleBuildingOverChest(pos, (TileEntityChest) entity, world);
            }
            else if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return blockState;
        }
    }

    public static class RackPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                              @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                              @Nullable final AbstractEntityAIStructure<?> placer, final boolean infiniteResources, final boolean complete)
        {
            if (!(blockState.getBlock() instanceof BlockMinecoloniesRack))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (placer != null && !infiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));

                for(final ItemStack stack : placer.getItemsFromTileEntity())
                {
                    if(!ItemStackUtils.isEmpty(stack))
                    {
                        itemList.add(stack);
                    }
                }

                if (checkForListInInvAndRequest(placer, itemList))
                {
                    return IPlacementHandler.ActionProcessingResult.DENY;
                }
            }

            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                BuildingWareHouse.handleBuildingOverChest(pos, (TileEntityChest) entity, world);
            }
            else if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            return blockState;
        }
    }
}
