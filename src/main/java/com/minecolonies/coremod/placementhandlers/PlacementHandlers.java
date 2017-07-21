package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.blocks.BlockSolidSubstitution;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all placement handler implementations.
 */
public final class PlacementHandlers
{
    public static final List<IPlacementHandler> handlers = new ArrayList<>();

    private PlacementHandlers()
    {
        /**
         * Intentionally left empty.
         */
    }

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
        handlers.add(new GeneralBlockPlacementHandler());
    }

    //If he woudln't count the bracket spaces we'd be under 25 easily.
    public static class FireplacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockFire))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (!Configurations.gameplay.builderInfiniteResources)
            {
                if (placer.checkOrRequestItems(false, new ItemStack(Items.FLINT_AND_STEEL, 1)))
                {
                    return ActionProcessingResult.DENY;
                }

                final EntityCitizen citizen = placer.getWorker();
                final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(new InvWrapper(citizen.getInventoryCitizen()), s ->
                        s.getItem() == Items.FLINT_AND_STEEL);
                final ItemStack item = slot == -1 ? ItemStackUtils.EMPTY : citizen.getInventoryCitizen().getStackInSlot(slot);
                if (ItemStackUtils.isEmpty(item) || !(item.getItem() instanceof ItemFlintAndSteel))
                {
                    return ActionProcessingResult.DENY;
                }
                citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item);
                item.damageItem(1, citizen);
            }
            placer.handleBuildingOverBlock(pos);
            world.setBlockState(pos, blockState, 0x03);
            return ActionProcessingResult.ACCEPT;
        }
    }

    public static class GrassPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (blockState.getBlock() != Blocks.GRASS)
            {
                return ActionProcessingResult.IGNORE;
            }

            if(!Configurations.gameplay.builderInfiniteResources && placer.checkOrRequestItems(placer.getTotalAmount(new ItemStack(Blocks.DIRT))))
            {
                return ActionProcessingResult.DENY;
            }
            placer.handleBuildingOverBlock(pos);
            if (!world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 0x03))
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
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockDoor))
            {
                return ActionProcessingResult.IGNORE;
            }

            if(!Configurations.gameplay.builderInfiniteResources && placer.checkOrRequestItems(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState))))
            {
                return ActionProcessingResult.DENY;
            }

            if (blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
            {
                placer.handleBuildingOverBlock(pos);
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
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockBed))
            {
                return ActionProcessingResult.IGNORE;
            }


            if (!Configurations.gameplay.builderInfiniteResources && blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT
                    && placer.checkOrRequestItems(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState))))
            {
                return ActionProcessingResult.DENY;
            }

            final EnumFacing facing = blockState.getValue(BlockBed.FACING);

            //Set other part of the bed, to the opposite PartType
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                placer.handleBuildingOverBlock(pos);
                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos.offset(facing), blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), 0x03);
                world.setBlockState(pos, blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), 0x03);
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
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockDoublePlant))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (!Configurations.gameplay.builderInfiniteResources && blockState.getValue(BlockDoublePlant.HALF).equals(BlockDoublePlant.EnumBlockHalf.LOWER)
                    && placer.checkOrRequestItems(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(blockState))))
            {
                return ActionProcessingResult.DENY;
            }

            if (blockState.getValue(BlockDoublePlant.HALF).equals(BlockDoublePlant.EnumBlockHalf.LOWER))
            {
                placer.handleBuildingOverBlock(pos);
                world.setBlockState(pos, blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
                world.setBlockState(pos.up(), blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
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
                @Nullable final AbstractEntityAIStructure<?> placer)
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
        public Object handle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockFlowerPot))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (!Configurations.gameplay.builderInfiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
                itemList.addAll(placer.getItemsFromTileEntity());

                for (final ItemStack stack : itemList)
                {
                    if (stack != null && placer.checkOrRequestItems(placer.getTotalAmount(stack)))
                    {
                        return ActionProcessingResult.DENY;
                    }
                }
            }

            placer.handleBuildingOverBlock(pos);
            if (!world.setBlockState(pos, blockState, 0x03))
            {
                return false;
            }

            placer.handleFlowerPots(pos);
            return blockState;
        }
    }

    public static class AirPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (blockState.getBlock() instanceof BlockAir)
            {
                placer.getWorker().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);

                placer.handleBuildingOverBlock(pos);
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
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockGrassPath))
            {
                return ActionProcessingResult.IGNORE;
            }

            if (!Configurations.gameplay.builderInfiniteResources)
            {
                if (!(placer.holdEfficientTool(blockState.getBlock()) || placer.checkOrRequestItems(placer.getTotalAmount(new ItemStack(Blocks.DIRT,1)))))
                {
                    return ActionProcessingResult.DENY;
                }
                placer.handleBuildingOverBlock(pos);
            }

            if (!world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), 0x03))
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
                @Nullable final AbstractEntityAIStructure<?> placer)
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
        public Object handle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!(blockState.getBlock() instanceof BlockSolidSubstitution))
            {
                return ActionProcessingResult.IGNORE;
            }

            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);

            if(!Configurations.gameplay.builderInfiniteResources)
            {
                if(placer.checkOrRequestItems(placer.getTotalAmount(BlockUtils.getItemStackFromBlockState(newBlockState))))
                {
                    return ActionProcessingResult.DENY;
                }
                placer.handleBuildingOverBlock(pos);
            }

            if (!world.setBlockState(pos, newBlockState, 0x03))
            {
                return ActionProcessingResult.DENY;
            }

            return newBlockState;
        }
    }

    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public Object handle(
                @NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState,
                @Nullable final AbstractEntityAIStructure<?> placer)
        {
            if (!Configurations.gameplay.builderInfiniteResources)
            {
                final List<ItemStack> itemList = new ArrayList<>();
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
                itemList.addAll(placer.getItemsFromTileEntity());

                for (final ItemStack stack : itemList)
                {
                    if (stack != null && placer.checkOrRequestItems(placer.getTotalAmount(stack)))
                    {
                        return ActionProcessingResult.DENY;
                    }
                }
            }

            placer.handleBuildingOverBlock(pos);
            if (!world.setBlockState(pos, blockState, 0x03))
            {
                return ActionProcessingResult.DENY;
            }

            return blockState;
        }
    }
}
