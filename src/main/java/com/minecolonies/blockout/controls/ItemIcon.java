package com.minecolonies.blockout.controls;

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
    private static final float GUI_ITEM_Z_LEVEL = 200.0F;
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

    public void setItem(final ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    @Override
    protected void drawSelf(final int mx, final int my)
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
    private void drawItemStack(ItemStack stack, int x, int y)
    {
        final RenderItem itemRender = mc.getRenderItem();

        GlStateManager.translate(0.0F, 0.0F, GUI_ITEM_Z_TRANSLATE);
        this.zLevel = GUI_ITEM_Z_LEVEL;
        itemRender.zLevel = GUI_ITEM_Z_LEVEL;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null)
        {
            font = mc.fontRendererObj;
        }
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, stack, x, y, null);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }
}
