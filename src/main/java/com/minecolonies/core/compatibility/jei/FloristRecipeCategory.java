package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFlorist;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;

/**
 * JEI recipe category showing supported flowers.
 */
public class FloristRecipeCategory extends JobBasedRecipeCategory<FloristRecipeCategory.FloristRecipe>
{
    /**
     * Constructor
     */
    public FloristRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(ModJobs.florist.get().produceJob(null), ModRecipeTypes.FLOWERS,
                new ItemStack(ModBuildings.florist.get().getBuildingBlock()), guiHelper);
    }

    private static final int LOOT_SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int LOOT_SLOTS_W = WIDTH - LOOT_SLOTS_X;
    private static final int MAX_LOOT_SLOTS = 24;

    @NotNull
    @Override
    protected List<Component> generateInfoBlocks(@NotNull final FloristRecipeCategory.FloristRecipe recipe)
    {
        return Collections.singletonList(
                Component.translatable(PARTIAL_JEI_INFO + "onelevelrestriction",
                        recipe.level()));
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final FloristRecipeCategory.FloristRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.CATALYST, WIDTH - 18, CITIZEN_Y - 20)
                .setSlotName("compost")
                .setBackground(this.slot, -1, -1)
                .addItemStack(new ItemStack(ModItems.compost));

        final int initialColumns = LOOT_SLOTS_W / this.slot.getWidth();
        final int rows = (recipe.flowers().size() + initialColumns - 1) / initialColumns;
        final int columns = (recipe.flowers().size() + rows - 1) / rows;
        final int startX = LOOT_SLOTS_X + (LOOT_SLOTS_W - (columns * this.slot.getWidth())) / 2;
        int x = startX;
        int y = CITIZEN_Y + CITIZEN_H - rows * this.slot.getHeight() + 1;
        int c = 0;

        for (final List<ItemStack> flowers : recipe.flowers())
        {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(this.chanceSlot, -1, -1)
                    .addItemStacks(flowers);
            if (++c >= columns)
            {
                c = 0;
                x = startX;
                y += this.slot.getHeight();
            }
            else
            {
                x += this.slot.getWidth();
            }
        }
    }

    @Override
    public void draw(@NotNull final FloristRecipeCategory.FloristRecipe recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final GuiGraphics stack,
                     final double mouseX, final double mouseY)
    {
        super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

        final BlockState block = ModBlocks.blockCompostedDirt.defaultBlockState();
        RenderHelper.renderBlock(stack.pose(), block, WIDTH - 38, CITIZEN_Y - 20, 100, -30F, 30F, 16F);
    }

    @NotNull
    public static List<FloristRecipeCategory.FloristRecipe> findRecipes()
    {
        final List<FloristRecipeCategory.FloristRecipe> recipes = new ArrayList<>();

        for (int level = 1; level <= MAX_BUILDING_LEVEL; ++level)
        {
            recipes.add(new FloristRecipe(level, compactify(BuildingFlorist.getPlantablesForBuildingLevel(level))));
        }

        return recipes;
    }

    /**
     * Produces no more than MAX_LOOT_SLOTS lists of lists of items, to avoid overflowing the GUI with slots.
     */
    private static List<List<ItemStack>> compactify(@NotNull final Set<ItemStorage> flowers)
    {
        final List<List<ItemStack>> slots = new ArrayList<>();
        final List<ItemStack> flowerList = flowers.stream().map(ItemStorage::getItemStack).toList();

        if (flowerList.size() < MAX_LOOT_SLOTS)
        {
            for (final ItemStack item : flowerList)
            {
                slots.add(List.of(item));
            }
        }
        else
        {
            int itemsPerList = flowerList.size() / MAX_LOOT_SLOTS;
            int extraItems = flowerList.size() % MAX_LOOT_SLOTS;
            int currentIndex = 0;

            for (int i = 0; i < MAX_LOOT_SLOTS; ++i)
            {
                final List<ItemStack> sublist = new ArrayList<>();
                for (int j = 0; j < itemsPerList; ++j)
                {
                    sublist.add(flowerList.get(currentIndex++));
                }
                if (extraItems > 0)
                {
                    sublist.add(flowerList.get(currentIndex++));
                    --extraItems;
                }
                slots.add(sublist);
            }
        }

        return slots;
    }

    /**
     * Represents the flowers available at the specified level.
     * @param level   the building level.
     * @param flowers the flowers available at that level, grouped into display slots.
     */
    public record FloristRecipe(int level, @NotNull List<List<ItemStack>> flowers)
    {
    }
}
