package com.minecolonies.core.util;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && BuiltInRegistries.BLOCK.getKey(((BlockItem) item.getItem()).getBlock())
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
}
