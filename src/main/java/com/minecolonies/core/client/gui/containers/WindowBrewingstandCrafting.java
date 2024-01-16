package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_MAXIMUM_NUMBER_RECIPES;
import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_GUI_DONE;

/**
 * BrewingStand crafting gui.
 */
public class WindowBrewingstandCrafting extends AbstractContainerScreen<ContainerCraftingBrewingstand>
{
    private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");

    /**
     * X offset of the button.
     */
    private static final int BUTTON_X_OFFSET = 1;

    /**
     * Y offset of the button.
     */
    private static final int BUTTON_Y_POS = 170;

    /**
     * Button width.
     */
    private static final int BUTTON_WIDTH = 150;

    /**
     * Button height.
     */
    private static final int BUTTON_HEIGHT = 20;

    /**
     * The building the window belongs to.
     */
    private final ContainerCraftingBrewingstand container;

    /**
     * The building assigned to this.
     */
    private final AbstractBuildingView building;

    /**
     * The module this crafting window is for.
     */
    private final CraftingModuleView module;

    /**
     * Create a crafting gui window.
     *
     * @param container       the container.
     * @param playerInventory the player inv.
     * @param iTextComponent  the display text component.
     */
    public WindowBrewingstandCrafting(final ContainerCraftingBrewingstand container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.container = container;
        this.building = (AbstractBuildingView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level.dimension(), container.getPos());
        this.module = (CraftingModuleView) building.getModuleView(container.getModuleId());
    }

    @NotNull
    public AbstractBuildingView getBuildingView()
    {
        return building;
    }

    @Override
    protected void init()
    {
        super.init();
        final Component buttonDisplay = Component.translatable(module.canLearn(ModCraftingTypes.BREWING.get()) ? BASE_GUI_DONE : WARNING_MAXIMUM_NUMBER_RECIPES);
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, buttonDisplay, new WindowBrewingstandCrafting.OnButtonPress());
        this.addRenderableWidget(doneButton);
        if (!module.canLearn(ModCraftingTypes.BREWING.get()))
        {
            doneButton.active = false;
        }
    }

    public class OnButtonPress implements Button.OnPress
    {
        @Override
        public void onPress(@NotNull final Button button)
        {
            if (module.canLearn(ModCraftingTypes.BREWING.get()))
            {
                final List<ItemStorage> input = new ArrayList<>();
                input.add(new ItemStorage(container.slots.get(0).getItem()));
                input.add(new ItemStorage(container.slots.get(1).getItem()));
                input.add(new ItemStorage(container.slots.get(2).getItem()));
                input.add(new ItemStorage(container.slots.get(3).getItem()));

                final ItemStack
                  primaryOutput = net.minecraftforge.common.brewing.BrewingRecipeRegistry.getOutput(container.slots.get(3).getItem(), container.slots.get(0).getItem()).copy();
                primaryOutput.setCount(3);

                if (!ItemStackUtils.isEmpty(primaryOutput))
                {
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, 1, primaryOutput, false, Blocks.BREWING_STAND, module.getProducer().getRuntimeID()));
                }
            }
        }
    }

    @Override
    public void render(@NotNull final PoseStack stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    protected void renderBg(@NotNull final PoseStack stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BREWING_STAND_LOCATION);
        this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int l = Mth.clamp((20 - 1) / 20, 0, 18);
        if (l > 0) {
            this.blit(stack, mouseX + 60, mouseY + 44, 176, 29, l, 4);
        }
    }
}
