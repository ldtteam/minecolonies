package com.minecolonies.core.client.render;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.event.ClientEventHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer<T extends AbstractEntityCitizen, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A>
{
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final Map<UUID, ResolvableProfile> gameProfileMap = new HashMap<>();

    /**
     * Set of items except from rendering as they're causing errors
     */
    private static Set<Item> disabledFromRendering = new HashSet<>();

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
        final ItemStack displayArmor = citizenDataView.getDisplayArmor(equipmentSlot);
        final ItemStack itemstack = displayArmor.isEmpty() ? citizen.getItemBySlot(equipmentSlot) : displayArmor;
        final Item armorItem = itemstack.getItem();

        if (armorItem instanceof ArmorItem && !disabledFromRendering.contains(armorItem))
        {
            try
            {
                if (displayArmor.isEmpty())
                {
                    super.renderArmorPiece(poseStack, bufferSource, citizen, equipmentSlot, light, armor);
                }
                else
                {
                    final int index = equipmentSlot.getIndex();
                    final ItemStack equipped = citizen.armorItems.get(index);
                    citizen.armorItems.set(index, displayArmor);
                    try
                    {
                        super.renderArmorPiece(poseStack, bufferSource, citizen, equipmentSlot, light, armor);
                    }
                    finally
                    {
                        citizen.armorItems.set(index, equipped);
                    }
                }
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Error rendering armor: " + itemstack + " report to the armor's mod.", e);
                disabledFromRendering.add(armorItem);
                ClientEventHandler.extraItemTooltips.put(armorItem,
                    Component.literal("This armor is causing errors when rendering on citizens, check your latest.log and report to the respective armor mod.").withStyle(
                        ChatFormatting.RED));
            }
        }
    }
}
