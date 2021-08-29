package com.minecolonies.coremod.client.gui.containers;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Furnace crafting gui.
 */
public class WindowFurnaceCrafting extends AbstractContainerScreen<ContainerCraftingFurnace>
{
    private static final ResourceLocation CRAFTING_FURNACE = new ResourceLocation(Constants.MOD_ID, "textures/gui/furnace.png");

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
    private final ContainerCraftingFurnace container;

    /**
     * The building assigned to this.
     */
    private final AbstractBuildingWorkerView building;

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
    public WindowFurnaceCrafting(final ContainerCraftingFurnace container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.container = container;
        this.building = (AbstractBuildingWorkerView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level.dimension(), container.getPos());
        this.module = building.getModuleViewMatching(CraftingModuleView.class, v -> v.getId().equals(container.getModuleId()));
    }

    @NotNull
    public AbstractBuildingWorkerView getBuildingView()
    {
        return building;
    }

    @Override
    protected void init()
    {
        super.init();
        final String buttonDisplay = module.canLearnFurnaceRecipes() ? I18n.get("gui.done") : LanguageHandler.format("com.minecolonies.coremod.gui.recipe.full");
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, new TextComponent(buttonDisplay), new OnButtonPress());
        this.addWidget(doneButton);
        if (!module.canLearnFurnaceRecipes())
        {
            doneButton.active = false;
        }
    }

    public class OnButtonPress implements Button.OnPress
    {
        @Override
        public void onPress(@NotNull final Button button)
        {
            if (module.canLearnFurnaceRecipes())
            {
                final List<ItemStorage> input = new ArrayList<>();
                input.add(new ItemStorage(container.slots.get(0).getItem()));
                final ItemStack primaryOutput = container.slots.get(1).getItem().copy();

                if (!ItemStackUtils.isEmpty(primaryOutput))
                {
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, 1, primaryOutput, ImmutableList.of(), false, module.getId()));
                }
            }
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final PoseStack stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CRAFTING_FURNACE);
        this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull final PoseStack stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
