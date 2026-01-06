package com.minecolonies.core.colony;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.util.Utils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * View data for visitors
 */
public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    /**
     * The recruitment costs
     */
    private ItemStack recruitmentCosts;

    /**
     * Cached player info for custom texture.
     */
    private volatile ResourceLocation cachedTexture;

    /**
     * Session profile cache for a given special visitor.
     */
    private GameProfile cachedProfile = null;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorDataView(final int id, final IColonyView colony)
    {
        super(id, colony);
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.deserialize(buf);
        recruitmentCosts = Utils.deserializeCodecMess(buf);
        recruitmentCosts.setCount(buf.readInt());
    }

    @Override
    public ItemStack getRecruitCost()
    {
        return recruitmentCosts;
    }

    @Override
    public ResourceLocation getCustomTexture()
    {
        if (textureUUID == null)
        {
            return null;
        }

        if (cachedProfile == null)
        {
            Util.backgroundExecutor().execute(() ->
            {
                if (cachedProfile == null)
                {
                    final ProfileResult profile = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(textureUUID, true);
                    if (profile != null)
                    {
                        cachedProfile = profile.profile();
                    }
                }
            });
        }

        if (cachedProfile != null && cachedTexture == null)
        {
            final ResourceLocation texture = Minecraft.getInstance().getSkinManager().getInsecureSkin(cachedProfile).texture();
            if (texture != DefaultPlayerSkin.get(textureUUID).texture())
            {
                cachedTexture = texture;
            }
        }

        return cachedTexture == null ? DefaultPlayerSkin.get(textureUUID).texture() : cachedTexture;
    }
}
