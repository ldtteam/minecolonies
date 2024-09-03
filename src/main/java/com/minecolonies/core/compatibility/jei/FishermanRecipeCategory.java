package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.items.ModToolTypes;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.colony.crafting.LootTableAnalyzer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;

/**
 * The JEI recipe category for the fisherman.
 */
@OnlyIn(Dist.CLIENT)
public class FishermanRecipeCategory extends JobBasedRecipeCategory<FishermanRecipeCategory.FishingRecipe>
{
    public FishermanRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(ModJobs.fisherman.get().produceJob(null), ModRecipeTypes.FISHING,
                new ItemStack(ModBuildings.fisherman.get().getBuildingBlock()), guiHelper);
    }

    private static final int LOOT_SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int LOOT_SLOTS_W = WIDTH - LOOT_SLOTS_X;

    @NotNull
    @Override
    protected List<Component> generateInfoBlocks(@NotNull FishingRecipe recipe)
    {
        return Collections.singletonList(
                Component.translatable(PARTIAL_JEI_INFO + "onelevelrestriction",
                        recipe.getLevel()));
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final FishingRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        addToolSlot(builder, ModToolTypes.fishing_rod.get(), WIDTH - 18, CITIZEN_Y - 20, true);

        if (!recipe.getDrops().isEmpty())
        {
            final int initialColumns = LOOT_SLOTS_W / this.slot.getWidth();
            final int rows = (recipe.getDrops().size() + initialColumns - 1) / initialColumns;
            final int columns = (recipe.getDrops().size() + rows - 1) / rows;
            final int startX = LOOT_SLOTS_X + (LOOT_SLOTS_W - (columns * this.slot.getWidth())) / 2;
            int x = startX;
            int y = CITIZEN_Y + CITIZEN_H - rows * this.slot.getHeight() + 1;
            int c = 0;

            for (final LootTableAnalyzer.LootDrop drop : recipe.getDrops())
            {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .setBackground(this.chanceSlot, -1, -1)
                        .addItemStacks(drop.getItemStacks())
                        .addTooltipCallback(new LootTableTooltipCallback(drop, recipe.getId()));
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
    }

    @NotNull
    public static List<FishingRecipe> findRecipes()
    {
        final List<LootTableAnalyzer.LootDrop> commonDrops = CustomRecipeManager.getInstance().getLootDrops(ModLootTables.FISHING);

        final List<FishingRecipe> recipes = new ArrayList<>();
        for (final Map.Entry<Integer, ResourceLocation> level : ModLootTables.FISHERMAN_BONUS.entrySet())
        {
            final List<LootTableAnalyzer.LootDrop> drops = new ArrayList<>(commonDrops);
            drops.addAll(CustomRecipeManager.getInstance().getLootDrops(level.getValue()));
            recipes.add(new FishingRecipe(level.getValue(), level.getKey(), drops));
        }
        return recipes;
    }

    public static class FishingRecipe
    {
        private final ResourceLocation id;
        private final int level;
        @NotNull
        private final List<LootTableAnalyzer.LootDrop> drops;

        public FishingRecipe(@NotNull final ResourceLocation id, final int level, @NotNull final List<LootTableAnalyzer.LootDrop> drops)
        {
            this.id = id;
            this.level = level;
            this.drops = drops.size() > 18 ? LootTableAnalyzer.consolidate(drops) : drops;
        }

        @NotNull
        public ResourceLocation getId()
        {
            return id;
        }

        public int getLevel()
        {
            return level;
        }

        @NotNull
        public List<LootTableAnalyzer.LootDrop> getDrops()
        {
            return drops;
        }
    }
}
