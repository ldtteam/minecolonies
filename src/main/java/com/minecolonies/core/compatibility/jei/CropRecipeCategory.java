package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.colony.crafting.LootTableAnalyzer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;

/**
 * JEI crop recipe category renderer
 */
@SuppressWarnings("MethodParameterOfConcreteClass")
public class CropRecipeCategory implements IRecipeCategory<CropRecipeCategory.CropRecipe>
{
    private final int WIDTH = 150;
    private final int HEIGHT = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable chanceSlot;

    public CropRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.DIAMOND_HOE));
        this.slot = guiHelper.getSlotDrawable();
        this.chanceSlot = guiHelper.createDrawable(new ResourceLocation(Constants.MOD_ID, "textures/gui/jei_recipe.png"), 0, 121, 18, 18);
    }

    @NotNull
    public static List<CropRecipe> findRecipes()
    {
        final Set<Block> sourceBlocks = new HashSet<>();
        for (final MinecoloniesCropBlock crop : ModBlocks.getCrops())
        {
            sourceBlocks.addAll(crop.getDroppedFrom());
        }

        return sourceBlocks.stream().map(CropRecipe::new).toList();
    }

    @NotNull
    @Override
    public RecipeType<CropRecipe> getRecipeType()
    {
        return ModRecipeTypes.CROPS;
    }

    @NotNull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @NotNull
    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @NotNull
    @Override
    public Component getTitle()
    {
        return Component.translatable(PARTIAL_JEI_INFO + "crops");
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final CropRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        final EquipmentTypeEntry requiredTool = ModEquipmentTypes.hoe.get();
        builder.addSlot(RecipeIngredientRole.CATALYST, WIDTH - 18, 0)
                .setSlotName("tool")
                .setBackground(this.chanceSlot, -1, -1)
                .addItemStacks(MinecoloniesAPIProxy.getInstance().getColonyManager().getCompatibilityManager().getListOfAllItems().stream()
                        .filter(requiredTool::checkIsEquipment)
                        .sorted(Comparator.comparing(requiredTool::getMiningLevel))
                        .toList());

        builder.addSlot(RecipeIngredientRole.INPUT, 0, 0)
                .setSlotName("block")
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.source().getCloneItemStack(Minecraft.getInstance().level, BlockPos.ZERO, recipe.source().defaultBlockState()));

        final List<LootTableAnalyzer.LootDrop> drops = CustomRecipeManager.getInstance().getLootDrops(recipe.source().getLootTable());
        final int initialColumns = (WIDTH - 36) / this.slot.getWidth();
        final int rows = (drops.size() + initialColumns - 1) / initialColumns;
        final int columns = (drops.size() + rows - 1) / rows;
        final int startX = (WIDTH - (columns * this.slot.getWidth())) / 2;
        int x = startX;
        int y = HEIGHT - rows * this.slot.getHeight() + 1;
        int c = 0;

        for (final LootTableAnalyzer.LootDrop drop : drops)
        {
            final IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(this.chanceSlot, -1, -1)
                    .addItemStacks(drop.getItemStacks());
            slot.addTooltipCallback(new JobBasedRecipeCategory.LootTableTooltipCallback(drop, recipe.source().getLootTable()));
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

    public record CropRecipe(@NotNull Block source)
    {
    }
}
