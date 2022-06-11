package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.CompostRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JEI compost recipe category renderer
 */
@SuppressWarnings("MethodParameterOfConcreteClass")
public class CompostRecipeCategory implements IRecipeCategory<CompostRecipe>
{
    private final String title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final ITickTimer timer;

    public CompostRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        this.title = new TranslatableComponent(ModBlocks.blockBarrel.getDescriptionId()).getString();

        this.background = guiHelper.createBlankDrawable(80, 50);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.blockBarrel));
        this.slot = guiHelper.getSlotDrawable();
        this.timer = guiHelper.createTickTimer(60, BarrelType.values().length - 2, false);
    }

    @NotNull
    public static List<CompostRecipe> findRecipes()
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfCompostRecipes().entrySet().stream()
                .map(entry -> CompostRecipe.individualize(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public ResourceLocation getUid()
    {
        return CompostRecipe.ID;
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
    public Class<? extends CompostRecipe> getRecipeClass()
    {
        return CompostRecipe.class;
    }

    @NotNull
    @Override
    public Component getTitle()
    {
        return new TextComponent(this.title);
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayoutBuilder builder,
                          @NotNull final CompostRecipe recipe,
                          @NotNull final IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 0)
                .setBackground(this.slot, -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 62, 0)
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(@NotNull final CompostRecipe recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final PoseStack stack,
                     final double mouseX, final double mouseY)
    {
        final BarrelType type = BarrelType.byMetadata(this.timer.getValue());
        final BlockState barrel = ModBlocks.blockBarrel.defaultBlockState()
                .setValue(AbstractBlockBarrel.FACING, Direction.SOUTH)
                .setValue(AbstractBlockBarrel.VARIANT, type);
        RenderHelper.renderBlock(stack, barrel, 40, 20, 100, -30F, 20F, 25F);
    }
}
