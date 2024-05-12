package com.minecolonies.core.client.render;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer<T extends AbstractEntityCitizen, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A>
{
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final Map<UUID, GameProfile> gameProfileMap = new HashMap<>();

    public CitizenArmorLayer(RenderLayerParent<T, M> parentLayer, A innerModel, A outerModel, ModelManager modelManager, final EntityModelSet modelSet)
    {
        super(parentLayer, innerModel, outerModel, modelManager);
        this.skullModels = SkullBlockRenderer.createSkullRenderers(modelSet);
    }

    @Override
    public void render(
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource bufferSource,
      int light,
      @NotNull T citizen,
      float ignore_1,
      float ignore_2,
      float partialTicks,
      float ignore_4,
      float headRotY,
      float headRotX)
    {
        if (citizen.getCitizenDataView() == null)
        {
            return;
        }

        if (citizen.getCitizenDataView().getInventory() == null)
        {
            return;
        }

        if (citizen.isInvisible())
        {
            return;
        }

        final ICitizenDataView citizenDataView = citizen.getCitizenDataView();
        if (citizenDataView.getCustomTextureUUID() != null )
        {
            final UUID textureUUID = citizenDataView.getCustomTextureUUID();
            final GameProfile gameProfile = gameProfileMap.get(textureUUID);
            if (gameProfile != null)
            {
                poseStack.pushPose();
                poseStack.scale(1.0F, -1.0F, -1.0F);

                final CompoundTag compoundTag = new CompoundTag();
                compoundTag.putUUID("Id", citizenDataView.getCustomTextureUUID());

                SkullBlock.Type type = SkullBlock.Types.PLAYER;
                SkullModelBase skullmodelbase = this.skullModels.get(type);
                RenderType rendertype = SkullBlockRenderer.getRenderType(type, gameProfile);

                poseStack.rotateAround(Axis.YP.rotationDegrees(180), 0.0f, 0.0f, 0.0f);
                poseStack.scale(-1.0F, -1.0F, 1.0F);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
                skullmodelbase.setupAnim(0f, headRotY, headRotX);

                skullmodelbase.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

                poseStack.popPose();
            }
            else
            {
                gameProfileMap.put(citizenDataView.getCustomTextureUUID(), new GameProfile(textureUUID, "mcoltexturequery"));
                Util.backgroundExecutor().execute(() ->
                {
                    Minecraft minecraft = Minecraft.getInstance();
                    final GameProfile profile = new GameProfile(textureUUID, "mcoltexturequery");
                    minecraft.getMinecraftSessionService().fillProfileProperties(profile, true);
                    minecraft.submit(() -> gameProfileMap.put(textureUUID, profile));
                });
            }
        }
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.CHEST, light, this.getArmorModel(EquipmentSlot.CHEST), citizenDataView);
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.LEGS, light, this.getArmorModel(EquipmentSlot.LEGS), citizenDataView);
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.FEET, light, this.getArmorModel(EquipmentSlot.FEET), citizenDataView);
        this.renderArmorPiece(poseStack, bufferSource, citizen, EquipmentSlot.HEAD, light, this.getArmorModel(EquipmentSlot.HEAD), citizenDataView);
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T citizen, EquipmentSlot equipmentSlot, int light, A armor, final ICitizenDataView citizenDataView)
    {
        ItemStack itemstack = citizenDataView.getInventory().getArmorInSlot(equipmentSlot);
        if (itemstack.isEmpty())
        {
            itemstack = citizen.getItemBySlot(equipmentSlot);
        }
        Item armorItem = itemstack.getItem();

        if (armorItem instanceof ArmorItem armoritem)
        {
            if (armoritem.getEquipmentSlot() == equipmentSlot)
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

                ArmorTrim.getTrim(citizen.level().registryAccess(), itemstack).ifPresent((p_289638_) -> {
                    this.renderTrim(armoritem.getMaterial(), poseStack, bufferSource, light, p_289638_, model, flag);
                });
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

    private void renderTrim(ArmorMaterial armorMaterial, PoseStack poseStack, MultiBufferSource bufferSource, int light, ArmorTrim armorItem, net.minecraft.client.model.Model model, boolean inner)
    {
        TextureAtlasSprite textureatlassprite = super.armorTrimAtlas.getSprite(inner ? armorItem.innerTexture(armorMaterial) : armorItem.outerTexture(armorMaterial));
        VertexConsumer vertexconsumer = textureatlassprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet()));
        model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
