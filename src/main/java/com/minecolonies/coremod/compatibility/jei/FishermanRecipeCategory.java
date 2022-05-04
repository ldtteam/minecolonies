package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;

/**
 * The JEI recipe category for the fisherman.
 */
@OnlyIn(Dist.CLIENT)
public class FishermanRecipeCategory extends JobBasedRecipeCategory<FishermanRecipeCategory.FishingRecipe>
{
    public FishermanRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(ModJobs.fisherman.produceJob(null), ModJobs.FISHERMAN_ID,
                new ItemStack(ModBuildings.fisherman.getBuildingBlock()), guiHelper);
    }

    private static final int LOOT_SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int LOOT_SLOTS_W = WIDTH - LOOT_SLOTS_X;

    @NotNull
    @Override
    public Class<? extends FishingRecipe> getRecipeClass()
    {
        return FishingRecipe.class;
    }

    @NotNull
    @Override
    protected List<ITextComponent> generateInfoBlocks(@NotNull FishingRecipe recipe)
    {
        return Collections.singletonList(
                new TranslationTextComponent(PARTIAL_JEI_INFO + "onelevelrestriction",
                        recipe.getLevel()));
    }

    @Override
    public void setIngredients(@NotNull final FishingRecipe recipe, @NotNull final IIngredients ingredients)
    {
        ingredients.setInput(VanillaTypes.ITEM, new ItemStack(Items.FISHING_ROD));
        ingredients.setOutputLists(VanillaTypes.ITEM, new ArrayList<>(recipe.getDrops().stream()
                .map(LootTableAnalyzer.LootDrop::getItemStacks)
                .collect(Collectors.toList())));
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayout layout, @NotNull final FishingRecipe recipe, @NotNull final IIngredients ingredients)
    {
        final IGuiItemStackGroup guiItemStacks = layout.getItemStacks();

        guiItemStacks.init(0, true, WIDTH - 18, CITIZEN_Y - 20);
        guiItemStacks.setBackground(0, slot);
        guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        if (!recipe.getDrops().isEmpty())
        {
            final int initialColumns = LOOT_SLOTS_W / this.slot.getWidth();
            final int rows = (recipe.getDrops().size() + initialColumns - 1) / initialColumns;
            final int columns = (recipe.getDrops().size() + rows - 1) / rows;
            final int startX = LOOT_SLOTS_X + (LOOT_SLOTS_W - (columns * this.slot.getWidth())) / 2;
            int x = startX;
            int y = CITIZEN_Y + CITIZEN_H - rows * this.slot.getHeight() + 1;
            int c = 0;
            int slot = 1;

            guiItemStacks.addTooltipCallback(new LootTableTooltipCallback(slot, recipe.getDrops(), recipe.getId()));
            for (final LootTableAnalyzer.LootDrop drop : recipe.getDrops())
            {
                guiItemStacks.init(slot, true, x, y);
                guiItemStacks.setBackground(slot, this.chanceSlot);
                guiItemStacks.set(slot, drop.getItemStacks());
                ++slot;
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
