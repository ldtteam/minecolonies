package com.minecolonies.core.util;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class BuildingUtils
{
    /**
     * Private constructor to hide public one.
     */
    private BuildingUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get the hut from the inventory.
     *
     * @param inventory the inventory to search.
     * @param hut       the hut to fetch.
     * @return the stack or if not found empty.
     */
    public static ItemStack getItemStackForHutFromInventory(final Inventory inventory, final String hut)
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(inventory.player,
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && ForgeRegistries.BLOCKS.getKey(((BlockItem) item.getItem()).getBlock())
                  .getPath()
                  .endsWith(hut));

        if (slot != -1)
        {
            return inventory.getItem(slot);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Checks a hut block for job= tags to restrict which jobs are allowed to exist there.
     *
     * @param world       the world.
     * @param buildingPos the position of the hut block.
     * @return            a predicate that returns true if the specified job is permitted.  (note
     *                    that there may be other reasons the job isn't allowed, such as being the
     *                    wrong building type for that job; this does not check that.)
     *                    it returns {@link #UNRESTRICTED} when there are no explicit restrictions.
     */
    @NotNull
    public static Predicate<JobEntry> getAllowedJobs(@NotNull final Level world, @NotNull final BlockPos buildingPos)
    {
        if (world.getBlockEntity(buildingPos) instanceof final IBlueprintDataProviderBE provider)
        {
            final Set<String> jobTags = provider.getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()).stream()
                    .filter(t -> t.startsWith("job="))
                    .map(t -> t.substring(4))
                    .collect(Collectors.toSet());
            if (!jobTags.isEmpty())
            {
                return job -> jobTags.contains(job.getKey().getPath()) || jobTags.contains(job.getKey().toString());
            }
        }

        return UNRESTRICTED;
    }

    /**
     * Indicates that there are no explicit restrictions on which jobs are allowed at a building.
     */
    public static final Predicate<JobEntry> UNRESTRICTED = job -> true;

    /**
     * Check if the given building should try to automatically hire a new citizen.
     *
     * @param building   the building to check.
     * @param hiringMode the current hiring mode of the job.
     * @param job        the job to hire, or null for a non-specific check.
     * @return           true if automatic hiring is allowed.
     */
    public static boolean canAutoHire(@NotNull final IBuilding building,
                                      @NotNull final HiringMode hiringMode,
                                      @Nullable final JobEntry job)
    {
        return building.canAssignCitizens()
                && (hiringMode == HiringMode.DEFAULT && !building.getColony().isManualHiring() || hiringMode == HiringMode.AUTO)
                && (job == null || getAllowedJobs(building.getColony().getWorld(), building.getPosition()).test(job));
    }

    /**
     * Reports the clockwise rotations for a hut or decoration relative to its original blueprint.
     * @param world the world.
     * @param pos   the anchor pos.
     * @return the number of rotations, or -1 if unable to calculate.
     */
    public static int getRotationFromBlueprint(@NotNull final Level world, @NotNull final BlockPos pos)
    {
        if (!WorldUtil.isBlockLoaded(world, pos))
        {
            return -1;
        }

        try
        {
            if (world.getBlockEntity(pos) instanceof final IBlueprintDataProviderBE blueprintDataProvider)
            {
                final String pack = blueprintDataProvider.getPackName();
                final String path = blueprintDataProvider.getBlueprintPath();

                Blueprint blueprint = StructurePacks.getBlueprint(pack, path, true);
                if (blueprint == null && path.endsWith("0.blueprint"))
                {
                    blueprint = StructurePacks.getBlueprint(pack, path.replace("0.blueprint", "1.blueprint"), true);
                }
                if (blueprint != null)
                {
                    final BlockState worldState = world.getBlockState(pos);
                    final BlockState structureState = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getState();
                    if (structureState != null)
                    {
                        final int structureRotation = getRotationFromBlock(structureState);
                        final int worldRotation = getRotationFromBlock(worldState);

                        if (structureRotation == -1 || worldRotation == -1)
                        {
                            Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", path));
                            return -1;
                        }

                        return (4 + worldRotation - structureRotation) % 4;
                    }
                }

                Log.getLogger().error(String.format("Failed to get rotation of building at pos: %s with path: %s", pos.toShortString(), path));
            }
            else
            {
                Log.getLogger().error(String.format("Failed to get rotation of building at pos: %s", pos.toShortString()));
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error(String.format("Failed to get rotation of building at pos: %s", pos.toShortString()), e);
        }
        return -1;
    }

    /**
     * Reports the clockwise rotations for a hut or decoration block.
     * @param blockState the hut or decoration block.
     * @return the number of rotations, or -1 if an unexpected block type.
     */
    public static int getRotationFromBlock(@NotNull final BlockState blockState)
    {
        if (blockState.getBlock() instanceof AbstractBlockHut<?>)
        {
            return blockState.getValue(AbstractBlockHut.FACING).get2DDataValue();
        }
        else if (blockState.getBlock() instanceof DirectionalBlock)
        {
            return blockState.getValue(DirectionalBlock.FACING).get2DDataValue();
        }
        return -1;
    }
}
