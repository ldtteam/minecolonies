package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Furnace crafting gui.
 */
public class WindowBrewingstandCrafting extends ContainerScreen<ContainerCraftingBrewingstand>
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
    public WindowBrewingstandCrafting(final ContainerCraftingBrewingstand container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.container = container;
        this.building = (AbstractBuildingView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level.dimension(), container.getPos());
        this.module = building.getModuleViewMatching(CraftingModuleView.class, v -> v.getId().equals(container.getModuleId()));
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
        final String buttonDisplay = module.canLearn(ModCraftingTypes.BREWING) ? I18n.get("gui.done") : new TranslationTextComponent("com.minecolonies.coremod.gui.recipe.full").getString();
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, new StringTextComponent(buttonDisplay), new OnButtonPress());
        this.addButton(doneButton);
        if (!module.canLearn(ModCraftingTypes.BREWING))
        {
            doneButton.active = false;
        }
    }

    public class OnButtonPress implements Button.IPressable
    {
        @Override
        public void onPress(@NotNull final Button button)
        {
            if (module.canLearn(ModCraftingTypes.BREWING))
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
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, 1, primaryOutput, false, Blocks.BREWING_STAND, module.getId()));
                }
            }
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    public void render(@NotNull final MatrixStack stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    protected void renderBg(@NotNull final MatrixStack stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BREWING_STAND_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int l = MathHelper.clamp((20 - 1) / 20, 0, 18);
        if (l > 0) {
            this.blit(stack, i + 60, j + 44, 176, 29, l, 4);
        }
    }
}
