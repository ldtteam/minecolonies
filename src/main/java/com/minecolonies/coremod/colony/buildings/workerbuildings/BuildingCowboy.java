package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.IntSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
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
     * Milking amount setting.
     */
    public static final ISettingKey<IntSetting> MILKING_AMOUNT  = new SettingKey<>(IntSetting.class, new ResourceLocation(MOD_ID, "milking_amount"));

    /**
     * Milking days setting.
     */
    public static final ISettingKey<IntSetting> MILKING_DAYS  = new SettingKey<>(IntSetting.class, new ResourceLocation(MOD_ID, "milking_days"));

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
    public static class MilkingModule extends AbstractCraftingBuildingModule.Custom implements IBuildingEventsModule
    {
        private int currentMilk;
        private int currentMilkDays;

        public MilkingModule()
        {
            super(ModJobs.cowboy.get());
        }

        @Override
        public void serializeNBT(@NotNull CompoundTag compound)
        {
            super.serializeNBT(compound);

            compound.putInt("milkValue", currentMilk);
            compound.putInt("milkDays", currentMilkDays);
        }

        @Override
        public void deserializeNBT(CompoundTag compound)
        {
            super.deserializeNBT(compound);

            this.currentMilk = compound.getInt("milkValue");
            this.currentMilkDays = compound.getInt("milkDays");
        }

        @Override
        public void onWakeUp()
        {
            ++this.currentMilkDays;

            if (this.currentMilkDays >= getBuilding().getSetting(MILKING_DAYS).getValue())
            {
                this.currentMilk = 0;
                this.currentMilkDays = 0;
            }
        }

        /**
         * @return true if the cowboy should be allowed to try to milk (not yet reached limit)
         */
        public boolean canTryToMilk()
        {
            return this.currentMilk < getBuilding().getSetting(MILKING_AMOUNT).getValue();
        }

        /**
         * Called to record successful milking.
         */
        public void onMilked()
        {
            ++this.currentMilk;
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            final ShapelessRecipe milk = new ShapelessRecipe(new ResourceLocation(""), "", CraftingBookCategory.MISC,
                    new ItemStack(Items.MILK_BUCKET), NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BUCKET)));
            recipes.add(GenericRecipe.of(milk, world));

            return recipes;
        }
    }
}
