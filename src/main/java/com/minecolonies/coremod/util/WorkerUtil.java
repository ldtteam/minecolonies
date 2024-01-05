package com.minecolonies.coremod.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.SettingsModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import com.minecolonies.coremod.entity.ai.citizen.miner.MinerLevel;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.MOVE_MINIMAL;
import static com.minecolonies.api.util.constant.CitizenConstants.ROTATION_MOVEMENT;
import static com.minecolonies.api.util.constant.TranslationConstants.MINER_MINE_NODE;
import static com.minecolonies.api.util.constant.TranslationConstants.MINER_NODES;
import static com.minecolonies.coremod.colony.buildings.AbstractBuilding.USE_SHEARS;

/**
 * Utility methods for BlockPos.
 */
public final class WorkerUtil
{
    /**
     * Default range for moving to something until we stop.
     */
    private static final double MIDDLE_BLOCK_OFFSET = 0.5D;

    /**
     * Placeholder text in a level sign.
     */
    private static final String LEVEL_SIGN_TEXT      = "level_placeholder";

    /**
     * List of tools to test blocks against, used for finding right tool.
     */
    public static List<Tuple<ToolType, ItemStack>> tools;

    private WorkerUtil()
    {
        //Hide default constructor.
    }

    /**
     * Gets or initializes the test tool list.
     *
     * @return the list of possible tools.
     */
    public static List<Tuple<ToolType, ItemStack>> getOrInitTestTools()
    {
        if (tools == null)
        {
            tools = new ArrayList<>();
            tools.add(new Tuple<>(ToolType.HOE, new ItemStack(Items.NETHERITE_HOE)));
            tools.add(new Tuple<>(ToolType.SHOVEL, new ItemStack(Items.NETHERITE_SHOVEL)));
            tools.add(new Tuple<>(ToolType.AXE, new ItemStack(Items.NETHERITE_AXE)));
            tools.add(new Tuple<>(ToolType.PICKAXE, new ItemStack(Items.NETHERITE_PICKAXE)));
        }
        return tools;
    }

    /**
     * Checks if a certain block is a pathBlock (roadBlock).
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static boolean isPathBlock(final Block block)
    {
        return block.defaultBlockState().is(ModTags.pathingBlocks);
    }

    /**
     * {@link WorkerUtil#isWorkerAtSiteWithMove(AbstractEntityCitizen, int, int, int, int)}.
     *
     * @param worker Worker to check.
     * @param site   Chunk coordinates of site to check.
     * @param range  Range to check in.
     * @return True when within range, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final EntityCitizen worker, @NotNull final BlockPos site, final int range)
    {
        return isWorkerAtSiteWithMove(worker, site.getX(), site.getY(), site.getZ(), range);
    }

    /**
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location.
     *
     * @param worker Worker to check
     * @param x      X-coordinate
     * @param y      Y-coordinate
     * @param z      Z-coordinate
     * @param range  Range to check in
     * @return True if worker is at site, otherwise false.
     */
    public static boolean isWorkerAtSiteWithMove(@NotNull final AbstractEntityCitizen worker, final int x, final int y, final int z, final int range)
    {
        if (!EntityUtils.isLivingAtSiteWithMove(worker, x, y, z, range))
        {
            //If not moving the try setting the point where the entity should move to
            if (worker.getNavigation().isDone())
            {
                EntityUtils.tryMoveLivingToXYZ(worker, x, y, z);
            }
            return false;
        }
        return true;
    }

    /**
     * Recalls the citizen, notifies player if not successful.
     *
     * @param spawnPoint the spawnPoint.
     * @param citizen    the citizen.
     * @return true if successful.
     */
    public static boolean setSpawnPoint(@Nullable final BlockPos spawnPoint, @NotNull final AbstractEntityCitizen citizen)
    {
        if (spawnPoint == null)
        {
            return false;
        }

        citizen.moveTo(
          spawnPoint.getX() + MIDDLE_BLOCK_OFFSET,
          spawnPoint.getY(),
          spawnPoint.getZ() + MIDDLE_BLOCK_OFFSET,
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        citizen.getNavigation().stop();
        return true;
    }

    /**
     * Get a Tooltype for a certain block. We need this because minecraft has a lot of blocks which have strange or no required tool.
     *
     * @param state         the target BlockState.
     * @param blockHardness the hardness.
     * @return the toolType to use.
     */
    public static IToolType getBestToolForBlock(final BlockState state, float blockHardness, final AbstractBuilding building)
    {
        if (state.getBlock() instanceof IForgeShearable && building.hasModule(SettingsModule.class) && building.getFirstModuleOccurance(SettingsModule.class).getSettingValueOrDefault(USE_SHEARS, true))
        {
            return ToolType.SHEARS;
        }

        String toolName = "";

        if (blockHardness > 0f)
        {
            for (final Tuple<ToolType, ItemStack> tool : getOrInitTestTools())
            {
                if (tool.getB() != null && tool.getB().getItem() instanceof DiggerItem)
                {
                    if (tool.getB().isCorrectToolForDrops(state))
                    {
                        toolName = tool.getA().getName();
                        break;
                    }
                }
            }
        }

        final IToolType toolType = ToolType.getToolType(toolName);
        return toolType;
    }

    /**
     * Get the correct havestlevel for a certain block. We need this because minecraft has a lot of blocks which have strange or no required harvestlevel.
     *
     * @param target the target block.
     * @return the required harvestLevel.
     */
    public static int getCorrectHarvestLevelForBlock(final BlockState target)
    {
        int required = 0;
        final List<Tier> tiers = TierSortingRegistry.getSortedTiers();
        for (final Tier tier : tiers) {
            TagKey<Block> tag = tier.getTag();
            if (tag != null && target.is(tag))
            {
                required = tiers.indexOf(tier);
                break;
            }
        }

        if (required < 0
              || target.getBlock() instanceof GlazedTerracottaBlock)
        {
            return 0;
        }
        return required;
    }

    /**
     * Change the citizens Rotation to look at said block.
     *
     * @param block   the block he should look at.
     * @param citizen the citizen that shall face the block.
     */
    public static void faceBlock(@Nullable final BlockPos block, final AbstractEntityCitizen citizen)
    {
        if (block == null)
        {
            return;
        }

        final double xDifference = block.getX() - citizen.blockPosition().getX();
        final double zDifference = block.getZ() - citizen.blockPosition().getZ();
        final double yDifference = block.getY() - (citizen.blockPosition().getY() + citizen.getEyeHeight());

        final double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        final double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        final double intendedRotationPitch = -(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI);
        citizen.setOwnRotation((float) EntityUtils.updateRotation(citizen.getRotationYaw(), intendedRotationYaw, ROTATION_MOVEMENT),
          (float) EntityUtils.updateRotation(citizen.getRotationPitch(), intendedRotationPitch, ROTATION_MOVEMENT));

        final double goToX = xDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDifference > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        //Have to move the entity minimally into the direction to render his new rotation.
        citizen.move(MoverType.SELF, new Vec3((float) goToX, 0, (float) goToZ));
    }

    /**
     * Find the first level in a structure and return it.
     *
     * @param structure the structure to scan.
     * @return the position of the sign.
     */
    @Nullable
    public static BlockPos findFirstLevelSign(final Blueprint structure, final BlockPos pos)
    {
        for (int j = 0; j < structure.getSizeY(); j++)
        {
            for (int k = 0; k < structure.getSizeZ(); k++)
            {
                for (int i = 0; i < structure.getSizeX(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockInfo te = structure.getBlockInfoAsMap().get(localPos);
                    if (te != null)
                    {
                        final CompoundTag teData = te.getTileEntityData();
                        final ResourceLocation teId = teData == null ? null : ResourceLocation.tryParse(teData.getString("id"));
                        final BlockEntityType<?> teType = teId == null ? null : ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(teId);
                        if (teType == BlockEntityType.SIGN || teType == BlockEntityType.HANGING_SIGN)
                        {
                            if (BlockEntity.loadStatic(te.getPos(), te.getState(), te.getTileEntityData()) instanceof SignBlockEntity sign)
                            {
                                if (sign.getFrontText().getMessage(0, false).getString().equals(LEVEL_SIGN_TEXT))
                                {
                                    // try to make an anchor in 0,0,0 instead of the middle of the structure
                                    return pos.subtract(structure.getPrimaryBlockOffset()).offset(localPos);
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Updated the level sign of a certain level in the world.
     *
     * @param world   the world.
     * @param level   the level to update.
     * @param levelId the id of the level.
     */
    public static void updateLevelSign(final Level world, final MinerLevel level, final int levelId)
    {
        @Nullable final BlockPos levelSignPos = level.getLevelSign();

        if (levelSignPos != null)
        {
            if (world.getBlockEntity(levelSignPos) instanceof SignBlockEntity teLevelSign)
            {
                final BlockState blockState = world.getBlockState(levelSignPos);

                final SignText text = new SignText()
                    .setMessage(0, Component.translatable(MINER_MINE_NODE).append(": " + levelId))
                    .setMessage(1, Component.literal("Y: " + (level.getDepth() + 1)))
                    .setMessage(2, Component.translatable(MINER_NODES).append(": " + level.getNumberOfBuiltNodes()))
                    .setMessage(3, Component.literal(""));

                teLevelSign.setText(text, true);
                teLevelSign.setText(text, false);

                teLevelSign.setChanged();
                world.sendBlockUpdated(levelSignPos, blockState, blockState, 3);
            }
        }
    }

    /**
     * Check if there is any already composted land.
     *
     * @param buildingFlorist the building to check.
     * @param world           the world to check it for.
     * @return true if there is any.
     */
    public static boolean isThereCompostedLand(final BuildingFlorist buildingFlorist, final Level world)
    {
        for (final BlockPos pos : buildingFlorist.getPlantGround())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityCompostedDirt)
                {
                    if (((TileEntityCompostedDirt) entity).isComposted())
                    {
                        return true;
                    }
                }
                else
                {
                    buildingFlorist.removePlantableGround(pos);
                }
            }
        }
        return false;
    }

    /**
     * Find the last ladder by iterating over the y pos in the world.
     *
     * @param pos   the starting pos.
     * @param world the world.
     * @return the y of the last one.
     */
    public static int getLastLadder(@NotNull final BlockPos pos, final Level world)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getLastLadder(pos.below(), world);
        }
        else
        {
            return pos.getY() + 1;
        }
    }


    /**
     * Check if there are too many items (i.e. more than 3) unrelated to the current recipe.
     *
     * @param currentRecipeStorage the reciep to compare it to.
     * @param inv the inventory to check in.
     * @return true if so.
     */
    public static boolean hasTooManyExternalItemsInInv(final IRecipeStorage currentRecipeStorage, final @NotNull InventoryCitizen inv)
    {
        int count = 0;
        for (int i = 0; i < inv.getSlots(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && !isPartOfRecipe(stack, currentRecipeStorage))
            {
                count++;
                if (count > 3)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if stack is part of the recipe.
     * @param stack the stack to check.
     * @param currentRecipeStorage the recipe to compare.
     * @return true if so.
     */
    public static boolean isPartOfRecipe(final ItemStack stack, final IRecipeStorage currentRecipeStorage)
    {
        if (ItemStackUtils.compareItemStacksIgnoreStackSize(stack, currentRecipeStorage.getPrimaryOutput()))
        {
            return true;
        }

        for (final ItemStack input : currentRecipeStorage.getCraftingToolsAndSecondaryOutputs())
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(input, stack))
            {
                return true;
            }
        }

        for (final ItemStorage input : currentRecipeStorage.getCleanedInput())
        {
            if (input.equals(new ItemStorage(stack)))
            {
                return true;
            }
        }
        return false;
    }
}
