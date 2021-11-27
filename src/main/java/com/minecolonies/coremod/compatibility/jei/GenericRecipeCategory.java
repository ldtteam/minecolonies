package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The main JEI recipe category GUI implementation for IGenericRecipe.
 */
@OnlyIn(Dist.CLIENT)
public class GenericRecipeCategory extends JobBasedRecipeCategory<IGenericRecipe>
{
    public GenericRecipeCategory(@NotNull final BuildingEntry building,
                                 @NotNull final IJob<?> job,
                                 @NotNull final ICraftingBuildingModule crafting,
                                 @NotNull final IGuiHelper guiHelper)
    {
        super(job, Objects.requireNonNull(crafting.getUid()), getCatalyst(building), guiHelper);

        this.building = building;
        this.crafting = crafting;
        this.arrow = guiHelper.createDrawable(TEXTURE, 20, 121, 24, 18);

        outputSlotX = CITIZEN_X + CITIZEN_W + 2 + (30 - this.slot.getWidth()) / 2;
        outputSlotY = CITIZEN_Y + CITIZEN_H + 1 - this.slot.getHeight();
    }

    @NotNull private final BuildingEntry building;
    @NotNull private final ICraftingBuildingModule crafting;
    @NotNull private final IDrawableStatic arrow;

    private static final int LOOT_SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int LOOT_SLOTS_W = WIDTH - LOOT_SLOTS_X;
    private static final int INPUT_SLOT_X = CITIZEN_X + CITIZEN_W + 32;
    private static final int INPUT_SLOT_W = WIDTH - INPUT_SLOT_X;
    private final int outputSlotX;
    private final int outputSlotY;

    @NotNull
    @Override
    public Class<? extends IGenericRecipe> getRecipeClass()
    {
        return IGenericRecipe.class;
    }

    @Override
    public void setIngredients(@NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        final List<List<ItemStack>> outputs = new ArrayList<>();
        if (!isLootBasedRecipe(recipe))
        {
            outputs.add(recipe.getAllMultiOutputs());
        }
        outputs.addAll(recipe.getAdditionalOutputs().stream().map(Collections::singletonList).collect(Collectors.toList()));

        if (recipe.getLootTable() != null)
        {
            final List<LootTableAnalyzer.LootDrop> drops = getLootDrops(recipe.getLootTable());
            outputs.addAll(drops.stream().map(LootTableAnalyzer.LootDrop::getItemStacks).collect(Collectors.toList()));
        }

        ingredients.setInputLists(VanillaTypes.ITEM, recipe.getInputs());
        ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayout layout, @NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        if (isLootBasedRecipe(recipe))
        {
            setLootBasedRecipe(layout, recipe, ingredients);
        }
        else
        {
            setNormalRecipe(layout, recipe, ingredients);
        }

        this.infoBlocks.clear();
        this.infoBlocks.addAll(calculateInfoBlocks(recipe.getRestrictions()));
    }

    private void setNormalRecipe(@NotNull final IRecipeLayout layout, @NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        final IGuiItemStackGroup guiItemStacks = layout.getItemStacks();

        int x = outputSlotX;
        int y = outputSlotY;
        int slot = 0;
        guiItemStacks.init(slot, false, x, y);
        guiItemStacks.setBackground(slot, this.slot);
        guiItemStacks.set(slot, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        x += this.slot.getWidth();
        ++slot;

        for (final ItemStack extra : recipe.getAdditionalOutputs())
        {
            guiItemStacks.init(slot, false, x, y);
            guiItemStacks.setBackground(slot, this.slot);
            guiItemStacks.set(slot, extra);
            x += this.slot.getWidth();
            ++slot;
        }

        if (recipe.getLootTable() != null)
        {
            final List<LootTableAnalyzer.LootDrop> drops = getLootDrops(recipe.getLootTable());
            guiItemStacks.addTooltipCallback(new LootTableTooltipCallback(slot, drops));
            for (final LootTableAnalyzer.LootDrop drop : drops)
            {
                guiItemStacks.init(slot, false, x, y);
                guiItemStacks.setBackground(slot, this.chanceSlot);
                guiItemStacks.set(slot, drop.getItemStacks());
                x += this.chanceSlot.getWidth();
                ++slot;
            }
        }

        final List<List<ItemStack>> inputs = recipe.getInputs();
        if (!inputs.isEmpty())
        {
            final int initialInputColumns = INPUT_SLOT_W / this.slot.getWidth();
            final int inputRows = (inputs.size() + initialInputColumns - 1) / initialInputColumns;
            final int inputColumns = (inputs.size() + inputRows - 1) / inputRows;

            x = INPUT_SLOT_X;
            y = CITIZEN_Y + (CITIZEN_H - inputRows * this.slot.getHeight()) / 2;
            int c = 0;
            for (final List<ItemStack> input : inputs)
            {
                guiItemStacks.init(slot, true, x, y);
                guiItemStacks.set(slot, input);
                ++slot;
                if (++c >= inputColumns)
                {
                    c = 0;
                    x = INPUT_SLOT_X;
                    y += this.slot.getHeight();
                }
                else
                {
                    x += this.slot.getWidth();
                }
            }
        }
    }

    private void setLootBasedRecipe(@NotNull final IRecipeLayout layout, @NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        assert recipe.getLootTable() != null;
        final IGuiItemStackGroup guiItemStacks = layout.getItemStacks();
        final List<LootTableAnalyzer.LootDrop> drops = getLootDrops(recipe.getLootTable());

        int x = LOOT_SLOTS_X;
        int y = CITIZEN_Y;
        int slot = 0;

        final List<List<ItemStack>> inputs = recipe.getInputs();
        if (!inputs.isEmpty())
        {
            for (final List<ItemStack> input : inputs)
            {
                guiItemStacks.init(slot, false, x, y);
                guiItemStacks.set(slot, input);
                ++slot;

                x += this.slot.getWidth() + 2;
            }
        }

        boolean showLootTooltip = true;
        if (drops.isEmpty())
        {
            // this is a temporary workaround for cases where we currently fail to load the loot table
            // (mostly when it's in a datapack).  assume that someone has set the alternate-outputs
            // appropriately, but we can't display the percentage chances.
            showLootTooltip = false;
            drops.addAll(recipe.getAdditionalOutputs().stream()
                    .map(stack -> new LootTableAnalyzer.LootDrop(Collections.singletonList(stack), 0, 0, false))
                    .collect(Collectors.toList()));
        }
        if (!drops.isEmpty())
        {
            final int initialColumns = LOOT_SLOTS_W / this.slot.getWidth();
            final int rows = (drops.size() + initialColumns - 1) / initialColumns;
            final int columns = (drops.size() + rows - 1) / rows;
            final int startX = LOOT_SLOTS_X + (LOOT_SLOTS_W - (columns * this.slot.getWidth())) / 2;
            x = startX;
            y = CITIZEN_Y + CITIZEN_H - rows * this.slot.getHeight() + 1;
            int c = 0;

            if (showLootTooltip)
            {
                guiItemStacks.addTooltipCallback(new LootTableTooltipCallback(slot, drops));
            }
            for (final LootTableAnalyzer.LootDrop drop : drops)
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
                } else
                {
                    x += this.slot.getWidth();
                }
            }
        }
    }

    @Override
    public void draw(@NotNull final IGenericRecipe recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        super.draw(recipe, matrixStack, mouseX, mouseY);

        if (!isLootBasedRecipe(recipe))
        {
            this.arrow.draw(matrixStack, CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y + (CITIZEN_H - this.arrow.getHeight()) / 2);
        }

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            final BlockState block = recipe.getIntermediate().defaultBlockState();
            RenderHelper.renderBlock(matrixStack, block, outputSlotX + 8, CITIZEN_Y + 6, 100, -30F, 30F, 16F);
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getTooltipStrings(@NotNull final IGenericRecipe recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = new ArrayList<>(super.getTooltipStrings(recipe, mouseX, mouseY));

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            if (new Rectangle2d(CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y - 2, 24, 24).contains((int) mouseX, (int) mouseY))
            {
                tooltips.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "intermediate.tip", recipe.getIntermediate().getName()));
            }
        }

        return tooltips;
    }

    private static boolean isLootBasedRecipe(@NotNull final IGenericRecipe recipe)
    {
        return recipe.getLootTable() != null && recipe.getPrimaryOutput().isEmpty();
    }

    private static List<LootTableAnalyzer.LootDrop> getLootDrops(@NotNull final ResourceLocation lootTableId)
    {
        final List<LootTableAnalyzer.LootDrop> drops = CustomRecipeManager.getInstance().getLootDrops(lootTableId);
        return drops.size() > 18 ? LootTableAnalyzer.consolidate(drops) : drops;
    }

    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull final Map<IRecipeType<?>, List<IGenericRecipe>> vanilla)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();

        // vanilla shaped and shapeless crafting recipes
        if (this.crafting.canLearnCraftingRecipes())
        {
            for (final IGenericRecipe recipe : vanilla.get(IRecipeType.CRAFTING))
            {
                if (!this.crafting.canLearnLargeRecipes() && recipe.getGridSize() > 2) continue;
                if (!this.crafting.isRecipeCompatible(recipe)) continue;

                recipes.add(recipe);
            }
        }

        // vanilla furnace recipes (do we want to check smoking and blasting too?)
        if (this.crafting.canLearnFurnaceRecipes())
        {
            for (final IGenericRecipe recipe : vanilla.get(IRecipeType.SMELTING))
            {
                if (!this.crafting.isRecipeCompatible(recipe)) continue;

                recipes.add(recipe);
            }
        }

        // custom MineColonies additional recipes
        for (final CustomRecipe customRecipe : CustomRecipeManager.getInstance().getRecipes(this.crafting.getCustomRecipeKey()))
        {
            final IRecipeStorage recipeStorage = customRecipe.getRecipeStorage();
            if (!recipeStorage.getAlternateOutputs().isEmpty())
            {
                // this is a multi-output recipe; assume it replaces a bunch of vanilla
                // recipes we already added above
                recipes.removeIf(r -> ItemStackUtils.compareItemStacksIgnoreStackSize(recipeStorage.getPrimaryOutput(), r.getPrimaryOutput()));
                recipes.removeIf(r -> recipeStorage.getAlternateOutputs().stream()
                        .anyMatch(s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, r.getPrimaryOutput())));
            }
            recipes.add(GenericRecipeUtils.create(customRecipe, recipeStorage));
        }

        // and even more recipes that can't be taught, but are just inherent in the worker AI
        recipes.addAll(this.crafting.getAdditionalRecipesForDisplayPurposesOnly());

        return recipes.stream()
                .sorted(Comparator.comparing(IGenericRecipe::getLevelSort)
                    .thenComparing(r -> r.getPrimaryOutput().getItem().getRegistryName()))
                .collect(Collectors.toList());
    }
}
