package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityScarecrow;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class which creates the GUI of our field inventory.
 */
@SideOnly(Side.CLIENT)
public class GuiField extends GuiContainer
{
    /**
     * The resource location of the GUI background.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/scarecrow.png");

    /**
     * The X-OFFSET of the string in the GUI.
     */
    private static final int X_OFFSET = 8;

    /**
     * Y-OFFSET of the string in the GUI.
     */
    private static final int Y_OFFSET = 6;

    /**
     * The text color of the string in the GUI.
     */
    private static final int TEXT_COLOR = 0x404040;

    /**
     * Tile entity of the scarecrow.
     */
    private final TileEntityScarecrow tileEntity;

    /**
     * Constructor of the GUI.
     *
     * @param parPlayerInventory the player inventory.
     * @param tileEntity         the tileEntity of the field, contains the inventory.
     * @param world              the world the field is in.
     * @param location           the location the field is at.
     */
    protected GuiField(final PlayerInventory parPlayerInventory, final TileEntityScarecrow tileEntity, final World world, final BlockPos location)
    {
        super(new ContainerField(tileEntity, parPlayerInventory, world, location));
        this.tileEntity = tileEntity;
    }

    /**
     * Method called to draw the foreground of the GUI.
     *
     * @param layer1 the first layer.
     * @param layer2 the second layer.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int layer1, final int layer2)
    {
        this.fontRenderer.drawString(tileEntity.getDesc(), X_OFFSET, Y_OFFSET, TEXT_COLOR);
    }

    /**
     * Does draw the background of the GUI.
     *
     * @param partialTicks the ticks delivered.
     * @param mouseX       the mouseX position.
     * @param mouseY       the mouseY position.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        final int marginHorizontal = (width - xSize) / 2;
        final int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
