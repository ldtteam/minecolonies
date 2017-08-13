package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import com.minecolonies.coremod.network.messages.AddRemoveRecipeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Crafting gui.
 */
public class WindowGuiCrafting extends GuiContainer
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/crafting2x2.png");

    /**
     * X offset of the button.
     */
    private static final int BUTTON_X_OFFSET = 75;

    /**
     * Y offset of the button.
     */
    private static final int BUTTON_Y_POS = 210;

    /**
     * Button width.
     */
    private static final int BUTTON_WIDTH = 150;

    /**
     * Button height.
     */
    private static final int BUTTON_HEIGHT = 20;

    /**
     * Color of the gui description.
     */
    private static final int GUI_COLOR = 4_210_752;

    /**
     * X offset of the gui description.
     */
    private static final int X_OFFSET = 97;

    /**
     * Y offset of the gui description.
     */
    private static final int Y_OFFSET = 8;

    /**
     * The button to click done after finishing the recipe.
     */
    private GuiButton doneButton;

    /**
     * The building the window belongs to.
     */
    private final AbstractBuilding.View building;

    /**
     * Create a crafting gui window.
     *
     * @param playerInv the player.
     * @param worldIn   the world.
     * @param building  the building it belongs to.
     */
    public WindowGuiCrafting(InventoryPlayer playerInv, World worldIn, final AbstractBuilding.View building)
    {
        this(playerInv, worldIn, BlockPos.ORIGIN, building);
    }

    /**
     * Create a crafting gui window.
     *
     * @param playerInv     the player.
     * @param worldIn       the world.
     * @param blockPosition the position.
     * @param building      the building.
     */
    public WindowGuiCrafting(InventoryPlayer playerInv, World worldIn, BlockPos blockPosition, final AbstractBuilding.View building)
    {
        super(new CraftingGUIBuilding(playerInv, worldIn, blockPosition));
        this.building = building;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - BUTTON_X_OFFSET, BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("gui.done", new Object[0])));
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (doneButton.isMouseOver())
        {
            final List<ItemStack> input = new ArrayList<>();
            final List<ItemStack> secondaryOutput = new ArrayList<>();

            for(int i = 1; i <= 4; i++)
            {
                final ItemStack stack = inventorySlots.getInventory().get(i);
                if(ItemStackUtils.isEmpty(stack))
                {
                    continue;
                }
                final ItemStack copy = stack.copy();
                ItemStackUtils.setSize(copy, 1);

                input.add(copy);

                if(copy.getItem().hasContainerItem(copy))
                {
                    secondaryOutput.add(copy.getItem().getContainerItem(copy));
                }
            }

            final ItemStack primaryOutput =  inventorySlots.getSlot(0).getStack().copy();

            if(!ItemStackUtils.isEmpty(primaryOutput))
            {
                MineColonies.getNetwork().sendToServer(new AddRemoveRecipeMessage(input, 2, primaryOutput, secondaryOutput, building, false));
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().thePlayer, "com.minecolonies.coremod.gui.recipe.done");
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
