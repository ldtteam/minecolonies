package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.Log;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.PlantationSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the plantation building. Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuilding
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<PlantationSetting> MODE =
      new SettingKey<>(PlantationSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "mode"));

    /**
     * Description string of the building.
     */
    private static final String PLANTATION = "plantation";

    /**
     * List of sand blocks to grow onto.
     */
    private final List<BlockPos> sand = new ArrayList<>();

    /**
     * The current phase (default sugarcane).
     */
    private Item currentPhase = Items.SUGAR_CANE;

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingPlantation(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == Blocks.SAND)
        {
            final Block down = world.getBlockState(pos.below()).getBlock();
            if (down == Blocks.COBBLESTONE || down == Blocks.STONE_BRICKS)
            {
                sand.add(pos);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT sandPos = compound.getList(TAG_PLANTGROUND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            sand.add(NBTUtil.readBlockPos(sandPos.getCompound(i).getCompound(TAG_POS)));
        }
        this.currentPhase = ItemStack.of(compound.getCompound(TAG_CURRENT_PHASE)).getItem();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT sandCompoundList = new ListNBT();
        for (@NotNull final BlockPos entry : sand)
        {
            @NotNull final CompoundNBT sandCompound = new CompoundNBT();
            sandCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);
        compound.put(TAG_CURRENT_PHASE, new ItemStack(currentPhase).save(new CompoundNBT()));
        return compound;
    }

    /**
     * Get a list of positions to check for crops for the current phase.
     *
     * @return the list of positions.
     */
    public List<BlockPos> getPosForPhase()
    {
        final List<BlockPos> filtered = new ArrayList<>();
        if (tileEntity != null && !tileEntity.getPositionedTags().isEmpty())
        {
            final Item phase = nextPlantPhase();
            for (final Map.Entry<BlockPos, List<String>> entry : tileEntity.getPositionedTags().entrySet())
            {
                if ((entry.getValue().contains("bamboo") && phase == Items.BAMBOO)
                      || (entry.getValue().contains("sugar") && phase == Items.SUGAR_CANE)
                      || (entry.getValue().contains("cactus") && phase == Items.CACTUS))
                {
                    filtered.add(getPosition().offset(entry.getKey()));
                }
            }
        }

        return filtered;
    }

    /**
     * Iterates over available plants
     *
     * @return the item of the new or unchanged plant phase
     */
    public Item nextPlantPhase()
    {
        final List<Item> availablePlants = getAvailablePlants();
        if (availablePlants.isEmpty())
        {
            return Items.SUGAR_CANE;
        }

        if (!availablePlants.contains(currentPhase))
        {
            // Setting may have changed causing the current phase plant to no longer be available.
            // Therefore, we reset the current phase back to the first item of the available plants.
            currentPhase = availablePlants.get(0);
        }
        else
        {
            // Find the index of the current phase plant, increase that index and set the current phase
            // to the next available plant in the cycle.
            int selectedIndex = IntStream.range(0, availablePlants.size())
              .filter(i -> currentPhase.equals(availablePlants.get(i)))
              .findFirst()
              .orElse(0);

            selectedIndex++;
            if (selectedIndex >= availablePlants.size()) {
                selectedIndex = 0;
            }

            currentPhase = availablePlants.get(selectedIndex);
        }

        return currentPhase;
    }

    private List<Item> getAvailablePlants()
    {
        final String setting = getSetting(MODE).getValue();

        List<Item> items = new ArrayList<>();
        if (setting.contains(Items.SUGAR_CANE.getDescriptionId()))
        {
            items.add(Items.SUGAR_CANE);
        }
        if (setting.contains(Items.CACTUS.getDescriptionId()))
        {
            items.add(Items.CACTUS);
        }
        if (setting.contains(Items.BAMBOO.getDescriptionId()))
        {
            items.add(Items.BAMBOO);
        }

        // Add sugar cane as the default plant cycle as a fallback in case nothing exists in the settings
        if (items.isEmpty())
        {
            items.add(Items.SUGAR_CANE);
            Log.getLogger().log(Level.WARN, "Plantation plant setting contains none of the preconfigured plants, please report this to the developers!");
        }
        return items;
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_PLANTATION)
              .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_PLANTATION);
            return isRecipeAllowed.orElse(false);
        }
    }
}
