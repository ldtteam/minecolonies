package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Creates a new building for the Cowboy.
 */
public class BuildingCowboy extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String COWBOY = "cowboy";

    /**
     * The hut name, used for the lang string in the GUI
     */
    private static final String HUT_NAME = "cowboyhut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Milking setting.
     */
    public static final ISettingKey<BoolSetting> MILKING  = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "milking"));

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingCowboy(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COWBOY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * Custom crafting module to indicate that we produce milk buckets.
     * (This is just for JEI and does not mean they're crafted on demand... although that could be changed.)
     */
    public static class MilkingModule extends AbstractCraftingBuildingModule.Custom
    {
        public MilkingModule()
        {
            super(ModJobs.cowboy);
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            final ShapelessRecipe milk = new ShapelessRecipe(new ResourceLocation(""), "",
                    new ItemStack(Items.MILK_BUCKET), NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BUCKET)));
            recipes.add(GenericRecipe.of(milk));

            return recipes;
        }
    }
}
