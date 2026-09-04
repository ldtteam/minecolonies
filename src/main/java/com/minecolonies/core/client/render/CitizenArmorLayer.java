package com.minecolonies.core.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.CitizenRenderState;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.NBTUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.ClientAsset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class CitizenArmorLayer
    extends HumanoidArmorLayer<CitizenRenderState, CitizenModel<CitizenRenderState>, HumanoidModel<CitizenRenderState>>
{
    private final SkullModelBase skullModel;
    private final Map<UUID, RenderType> playerSkinRenderTypes = new HashMap<>();

    public CitizenArmorLayer(
      final RenderLayerParent<CitizenRenderState, CitizenModel<CitizenRenderState>> parentLayer,
      final ArmorModelSet<ModelLayerLocation> armorLocations,
      final EquipmentLayerRenderer equipmentRenderer,
      final EntityModelSet modelSet)
    {
        super(parentLayer,
            ArmorModelSet.bake(armorLocations, modelSet, HumanoidModel::new),
            equipmentRenderer);
        this.skullModel = SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.PLAYER);
    }

    @Override
    public void submit(@NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector submitNodeCollector,
                       final int lightCoords,
                       @NotNull final CitizenRenderState citizenState,
                       final float headRotY,
                       final float headRotX)
    {
        final AbstractEntityCitizen citizen = citizenState.getCitizen();
        final ICitizenDataView citizenDataView = citizen == null ? null : citizen.getCitizenDataView();
        if (citizenDataView == null || citizenDataView.getInventory() == null || citizen.isInvisible())
        {
            return;
        }

        final UUID textureUUID = citizenDataView.getCustomTextureUUID();
        if (textureUUID != null)
        {
            if (!playerSkinRenderTypes.containsKey(textureUUID))
            {
                playerSkinRenderTypes.put(textureUUID, SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.PLAYER, null));
                loadPlayerSkin(textureUUID);
            }
            final RenderType renderType = playerSkinRenderTypes.get(textureUUID);

            poseStack.pushPose();
            poseStack.scale(1.0F, -1.0F, -1.0F);
            if (this.skullModel != null)
            {
                final SkullModelBase.State skullState = new SkullModelBase.State();
                skullState.yRot = headRotY;
                skullState.xRot = headRotX;
                this.skullModel.setupAnim(skullState);
                SkullBlockRenderer.submitSkull(0.0F, poseStack, submitNodeCollector, lightCoords,
                    this.skullModel, renderType, 0, null);
            }
            poseStack.popPose();

        }

        super.submit(poseStack, submitNodeCollector, lightCoords, citizenState, headRotY, headRotX);
    }

    private void loadPlayerSkin(final UUID textureUUID)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final GameProfile profile = new GameProfile(textureUUID, "mcoltexturequery");
        minecraft.getSkinManager().get(profile).thenAccept(skin ->
            minecraft.execute(() -> {
                final RenderType renderType = skin.map(PlayerSkin::body)
                    .map(ClientAsset.Texture::texturePath)
                    .map(SkullBlockRenderer::getPlayerSkinRenderType)
                    .orElseGet(() -> SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.PLAYER, null));
                playerSkinRenderTypes.put(textureUUID, renderType);
            }));
    }

}
