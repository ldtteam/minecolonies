package com.minecolonies.core.client.render;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
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
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer<T extends AbstractEntityCitizen, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A>
{
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final Map<UUID, ResolvableProfile> gameProfileMap = new HashMap<>();

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
            final ResolvableProfile gameProfile = gameProfileMap.get(textureUUID);
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
                skullmodelbase.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
            else
            {
                gameProfileMap.put(citizenDataView.getCustomTextureUUID(), new ResolvableProfile(new GameProfile(textureUUID, "mcoltexturequery")));
                Util.backgroundExecutor().execute(() ->
                {
                    Minecraft minecraft = Minecraft.getInstance();
                    final ProfileResult profile = minecraft.getMinecraftSessionService().fetchProfile(textureUUID, true);
                    if (profile != null)
                    {
                        minecraft.submit(() -> gameProfileMap.put(textureUUID, new ResolvableProfile(profile.profile())));
                    }
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
                ArmorMaterial armormaterial = armoritem.getMaterial().value();

                int i = itemstack.is(ItemTags.DYEABLE) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(itemstack, -6265536)) : -1;

                for (ArmorMaterial.Layer armormaterial$layer : armormaterial.layers())
                {
                    int j = armormaterial$layer.dyeable() ? i : -1;
                    var texture = net.neoforged.neoforge.client.ClientHooks.getArmorTexture(citizen, itemstack, armormaterial$layer, flag, equipmentSlot);
                    this.renderModel(poseStack, bufferSource, light, model, j, texture);
                }

                ArmorTrim armortrim = itemstack.get(DataComponents.TRIM);
                if (armortrim != null)
                {
                    this.renderTrim(armoritem.getMaterial(), poseStack, bufferSource, light, armortrim, model, flag);
                }

                if (itemstack.hasFoil())
                {
                    this.renderGlint(poseStack, bufferSource, light, model);
                }
            }
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int light, net.minecraft.client.model.Model armorItem, int color, ResourceLocation armorResource) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(armorResource));
        armorItem.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int light, net.minecraft.client.model.Model model)
    {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
    }

    private void renderTrim(Holder<ArmorMaterial> armorMaterial, PoseStack p_289687_, MultiBufferSource p_289643_, int p_289683_, ArmorTrim p_289692_, net.minecraft.client.model.Model p_289663_, boolean p_289651_)
    {
        TextureAtlasSprite textureatlassprite = this.armorTrimAtlas
                                                  .getSprite(p_289651_ ? p_289692_.innerTexture(armorMaterial) : p_289692_.outerTexture(armorMaterial));
        VertexConsumer vertexconsumer = textureatlassprite.wrap(p_289643_.getBuffer(Sheets.armorTrimsSheet(p_289692_.pattern().value().decal())));
        p_289663_.renderToBuffer(p_289687_, vertexconsumer, p_289683_, OverlayTexture.NO_OVERLAY);
    }
}
