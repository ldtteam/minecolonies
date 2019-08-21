package com.minecolonies.blockout.controls;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    private static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    private static final float GUI_ITEM_Z_TRANSLATE = 32.0F;
    /**
     * ItemStack represented in the itemIcon.
     */
    private ItemStack itemStack;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public ItemIcon(final PaneParams params)
    {
        super(params);

        final String itemName = params.getStringAttribute("item", null);
        if (itemName != null)
        {
            final Item item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
            if (item != null)
            {
                itemStack = new ItemStack(item, 1);
            }
        }
    }

    /**
     * Set the item of the icon.
     * @param itemStack the itemstack to set.
     */
    public void setItem(final ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    /**
     * Get the itemstack of the icon.
     * @return the stack of it.
     */
    public ItemStack getItem()
    {
        return this.itemStack;
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        if (itemStack != null)
        {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            drawItemStack(itemStack, x, y);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Modified from GuiContainer
     */
    private void drawItemStack(final ItemStack stack, final int x, final int y)
    {
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return;
        }

        final RenderItem itemRender = mc.getRenderItem();

        GlStateManager.translate(x, y, GUI_ITEM_Z_TRANSLATE);
        GlStateManager.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1f);

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null)
        {
            font = mc.fontRenderer;
        }
        itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
        itemRender.renderItemOverlayIntoGUI(font, stack, 0, 0, null);
    }
}
