package com.minecolonies.core.client.render;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer<T extends AbstractEntityCitizen, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A>
{
    public CitizenArmorLayer(RenderLayerParent<T, M> parentLayer, A innerModel, A outerModel)
    {
        super(parentLayer, innerModel, outerModel);
    }

    @Override
    public void render(
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource bufferSource,
      int light,
      @NotNull T citizen,
      float ignore_1,
      float ignore_2,
      float ignore_3,
      float ignore_4,
      float ignore_5,
      float ignore_6)
    {
        if (citizen.getCitizenDataView() == null)
        {
            return;
        }

        if (citizen.getCitizenDataView().getInventory() == null)
        {
            return;
        }
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.CHEST, light, this.getArmorModel(EquipmentSlot.CHEST));
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.LEGS, light, this.getArmorModel(EquipmentSlot.LEGS));
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.FEET, light, this.getArmorModel(EquipmentSlot.FEET));
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.HEAD, light, this.getArmorModel(EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T citizen, EquipmentSlot equipmentSlot, int light, A armor)
    {
        ItemStack itemstack = citizen.getCitizenDataView().getInventory().getArmorInSlot(equipmentSlot);
        if (itemstack.isEmpty())
        {
            itemstack = citizen.getItemBySlot(equipmentSlot);
        }
        Item armorItem = itemstack.getItem();
        if (armorItem instanceof ArmorItem armoritem)
        {
            if (armoritem.getSlot() == equipmentSlot)
            {
                this.getParentModel().copyPropertiesTo(armor);
                this.setPartVisibility(armor, equipmentSlot);
                net.minecraft.client.model.Model model = getArmorModelHook(citizen, itemstack, equipmentSlot, armor);
                boolean flag = this.usesInnerModel(equipmentSlot);
                if (armoritem instanceof net.minecraft.world.item.DyeableLeatherItem)
                {
                    int i = ((net.minecraft.world.item.DyeableLeatherItem) armoritem).getColor(itemstack);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float f1 = (float) (i >> 8 & 255) / 255.0F;
                    float f2 = (float) (i & 255) / 255.0F;
                    this.renderModel(poseStack, bufferSource, light, armoritem, model, flag, f, f1, f2, this.getArmorResource(citizen, itemstack, equipmentSlot, null));
                    this.renderModel(poseStack, bufferSource, light, armoritem, model, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(citizen, itemstack, equipmentSlot, "overlay"));
                }
                else
                {
                    this.renderModel(poseStack, bufferSource, light, armoritem, model, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(citizen, itemstack, equipmentSlot, null));
                }

                if (itemstack.hasFoil())
                {
                    this.renderGlint(poseStack, bufferSource, light, model);
                }
            }
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int light, ArmorItem armorItem, net.minecraft.client.model.Model model, boolean ignore, float red, float green, float blue, ResourceLocation armorResource)
    {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(armorResource));
        model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int light, net.minecraft.client.model.Model model)
    {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
