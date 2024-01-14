package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * ------------ Class not Documented ------------
 */
public class WindowCitizenInventory extends AbstractContainerScreen<ContainerCitizenInventory>
{
    /**
     * Texture res loc.
     */
    private static final ResourceLocation TEXT = new ResourceLocation(Constants.MOD_ID, "textures/gui/citizen_container.png");

    /**
     * Offset inside the texture to use.
     */
    private static final int TEXTURE_OFFSET = 130;

    /**
     * Offset of each slot.
     */
    private static final int SLOT_OFFSET = 18;

    /**
     * Size of the custom texture.
     */
    private static final int TEXTURE_SIZE = 350;

    /**
     * Offet of the screen for the texture.
     */
    private static final int TEXTURE_HEIGHT = 96;

    /**
     * General y offset.
     */
    private static final int Y_OFFSET = 114;

    /**
     * Amount of slots each row.
     */
    private static final int SLOTS_EACH_ROW = 9;

    /**
     * window height is calculated with these values; the more rows, the heigher
     */
    private final int inventoryRows;

    public WindowCitizenInventory(final ContainerCitizenInventory container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.inventoryRows = (container.getItems().size() - 36) / 9;

        this.imageHeight = Y_OFFSET + Math.min(SLOTS_EACH_ROW, this.inventoryRows) * SLOT_OFFSET;
        this.imageWidth = 245;
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, final int mouseX, final int mouseY)
    {
        stack.drawString(this.font, this.menu.getDisplayName(), 80, 9, 4210752, false);
        stack.drawString(this.font, this.playerInventoryTitle.getString(), 8, 25 + this.inventoryRows * SLOT_OFFSET, 4210752, false);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, float partialTicks, int mouseX, int mouseY)
    {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;


        stack.blit(TEXT, i, j, 0, 0, this.imageWidth,  10 + this.inventoryRows * SLOT_OFFSET + 12, TEXTURE_SIZE, TEXTURE_SIZE);


        stack.blit(TEXT, i, j + 10 + this.inventoryRows * SLOT_OFFSET + 12, 0, TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);


        //stack.blit(TEXT, i, j, 0, 0, this.imageWidth,  this.inventoryRows * SLOT_OFFSET + 12, TEXTURE_SIZE, TEXTURE_SIZE);


        stack.blit(TEXT, i + 172, j + 22, 0, 227, 49, 72, TEXTURE_SIZE, TEXTURE_SIZE);

        for (int index = 0; index < 4; index++)
        {
            stack.blit(TEXT, i + 222, j + 22 + index * 18, 0, 300, 18, 18, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        renderEntityInInventoryFollowsMouse(stack, i + 197, j + 88, 30, (float)(i + 51) - mouseX, (float)(j + 75 - 50) - mouseY, this.menu.getEntity());
    }


    public static void renderEntityInInventoryFollowsMouse(GuiGraphics stack, int x, int y, int scale, float mouseX, float mouseY, Optional<? extends Entity> optionalEntity) {
        optionalEntity.ifPresent(entity -> {
            float relativeMouseX = (float)Math.atan(mouseX / 40.0F);
            float relativeMouseY = (float)Math.atan(mouseY / 40.0F);
            renderEntityInInventoryFollowsAngle(stack, x, y, scale, relativeMouseX, relativeMouseY, (LivingEntity) entity);
        });
    }

    public static void renderEntityInInventoryFollowsAngle(GuiGraphics stack, int x, int y, int scale, float angleXComponent, float angleYComponent, LivingEntity entity) {
        float f = angleXComponent;
        float f1 = angleYComponent;
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        renderEntityInInventory(stack, x, y, scale, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
    }

    public static void renderEntityInInventory(GuiGraphics stack, int x, int y, int scale, Quaternionf quaternionf, @Nullable Quaternionf quaternionf1, LivingEntity entity) {
        stack.pose().pushPose();
        stack.pose().translate(x, y, 50.0D);
        stack.pose().mulPoseMatrix((new Matrix4f()).scaling((float)scale, (float)scale, (float)(-scale)));
        stack.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf1 != null) {
            quaternionf1.conjugate();
            entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        }

        entityrenderdispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, stack.pose(), stack.bufferSource(), 15728880);
        });
        stack.flush();
        entityrenderdispatcher.setRenderShadow(true);
        stack.pose().popPose();
        Lighting.setupFor3DItems();
    }
}
