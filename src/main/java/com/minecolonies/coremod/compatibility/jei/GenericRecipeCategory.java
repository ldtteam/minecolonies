package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.colony.crafting.RecipeAnalyzer;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
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
                                 @NotNull final IGuiHelper guiHelper,
                                 @NotNull final IModIdHelper modIdHelper)
    {
        super(job, createRecipeType(crafting), getCatalyst(building), guiHelper);

        this.building = building;
        this.crafting.add(crafting);
        this.arrow = guiHelper.createDrawable(TEXTURE, 20, 121, 24, 18);
        this.modIdHelper = modIdHelper;

        outputSlotX = CITIZEN_X + CITIZEN_W + 2 + (30 - this.slot.getWidth()) / 2;
        outputSlotY = CITIZEN_Y + CITIZEN_H + 1 - this.slot.getHeight();
    }

    @NotNull private final BuildingEntry building;
    @NotNull private final List<ICraftingBuildingModule> crafting = new ArrayList<>();
    @NotNull private final IDrawableStatic arrow;
    @NotNull private final IModIdHelper modIdHelper;

    private static final int LOOT_SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int LOOT_SLOTS_W = WIDTH - LOOT_SLOTS_X;
    private static final int INPUT_SLOT_X = CITIZEN_X + CITIZEN_W + 32;
    private static final int INPUT_SLOT_W = WIDTH - INPUT_SLOT_X;
    private final int outputSlotX;
    private final int outputSlotY;

    public void addModule(@NotNull final ICraftingBuildingModule module)
    {
        this.crafting.add(module);
    }

    @NotNull
    private static RecipeType<IGenericRecipe> createRecipeType(@NotNull final ICraftingBuildingModule crafting)
    {
        final ResourceLocation uid = Objects.requireNonNull(crafting.getUid());
        return RecipeType.create(uid.getNamespace(), uid.getPath(), IGenericRecipe.class);
    }

    @NotNull
    @Override
    protected List<Component> generateInfoBlocks(@NotNull IGenericRecipe recipe)
    {
        return recipe.getRestrictions();
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final IGenericRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        if (isLootBasedRecipe(recipe))
        {
            setLootBasedRecipe(builder, recipe, focuses);
        }
        else
        {
            setNormalRecipe(builder, recipe, focuses);
        }
    }

    private void setNormalRecipe(@NotNull final IRecipeLayoutBuilder builder,
                                 @NotNull final IGenericRecipe recipe,
                                 @NotNull final IFocusGroup focuses)
    {
        final ResourceLocation id = recipe.getRecipeId();

        addToolSlot(builder, recipe.getRequiredTool(), WIDTH - 18, CITIZEN_Y - 20, false);

        int x = outputSlotX;
        int y = outputSlotY;
        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .setBackground(this.slot, -1, -1)
                .addItemStacks(recipe.getAllMultiOutputs());
        if (id != null)
        {
            slot.addTooltipCallback(new RecipeIdTooltipCallback(id, this.modIdHelper));
        }
        x += this.slot.getWidth();

        for (final ItemStack extra : recipe.getAdditionalOutputs())
        {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(this.slot, -1, -1)
                    .addItemStack(extra);
            x += this.slot.getWidth();
        }

        if (recipe.getLootTable() != null)
        {
            final List<LootTableAnalyzer.LootDrop> drops = getLootDrops(recipe.getLootTable());
            for (final LootTableAnalyzer.LootDrop drop : drops)
            {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .setBackground(this.chanceSlot, -1, -1)
                        .addItemStacks(drop.getItemStacks())
                        .addTooltipCallback(new LootTableTooltipCallback(drop, recipe.getLootTable()));
                x += this.chanceSlot.getWidth();
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
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addItemStacks(input);
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

    private void setLootBasedRecipe(@NotNull final IRecipeLayoutBuilder builder,
                                    @NotNull final IGenericRecipe recipe,
                                    @NotNull final IFocusGroup focuses)
    {
        assert recipe.getLootTable() != null;
        final List<LootTableAnalyzer.LootDrop> drops = getLootDrops(recipe.getLootTable());
        final ResourceLocation id = recipe.getRecipeId();

        int x = LOOT_SLOTS_X;
        int y = CITIZEN_Y;

        final List<List<ItemStack>> inputs = recipe.getInputs();
        if (!inputs.isEmpty())
        {
            for (final List<ItemStack> input : inputs)
            {
                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addItemStacks(input);
                x += this.slot.getWidth() + 2;
            }
        }
        addToolSlot(builder, recipe.getRequiredTool(), x, y, false);

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

            for (final LootTableAnalyzer.LootDrop drop : drops)
            {
                final IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                        .setBackground(this.chanceSlot, -1, -1)
                        .addItemStacks(drop.getItemStacks());
                if (showLootTooltip)
                {
                    slot.addTooltipCallback(new LootTableTooltipCallback(drop, recipe.getLootTable()));
                }
                if (id != null)
                {
                    slot.addTooltipCallback(new RecipeIdTooltipCallback(id, this.modIdHelper));
                }
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

    @Override
    public void draw(@NotNull final IGenericRecipe recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final PoseStack stack,
                     final double mouseX, final double mouseY)
    {
        super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

        if (!isLootBasedRecipe(recipe))
        {
            this.arrow.draw(stack, CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y + (CITIZEN_H - this.arrow.getHeight()) / 2);
        }

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            final BlockState block = recipe.getIntermediate().defaultBlockState();
            RenderHelper.renderBlock(stack, block, outputSlotX + 8, CITIZEN_Y + 6, 100, -30F, 30F, 16F);
        }
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull final IGenericRecipe recipe,
                                                      @NotNull final IRecipeSlotsView recipeSlotsView,
                                                      final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            if (new Rect2i(CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y - 2, 24, 24).contains((int) mouseX, (int) mouseY))
            {
                tooltips.add(Component.translatable(TranslationConstants.PARTIAL_JEI_INFO + "intermediate.tip", recipe.getIntermediate().getName()));
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
    public List<IGenericRecipe> findRecipes(@NotNull final Map<CraftingType, List<IGenericRecipe>> vanilla,
                                            @NotNull final List<Animal> animals)
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();
        for (final ICraftingBuildingModule module : this.crafting)
        {
            recipes.addAll(RecipeAnalyzer.findRecipes(vanilla, module));
        }

        return recipes.stream()
                .sorted(Comparator.comparing(IGenericRecipe::getLevelSort)
                    .thenComparing(r -> ForgeRegistries.ITEMS.getKey(r.getPrimaryOutput().getItem())))
                .collect(Collectors.toList());
    }
}
