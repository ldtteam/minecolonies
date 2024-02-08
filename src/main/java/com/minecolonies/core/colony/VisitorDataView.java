package com.minecolonies.core.colony;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
     * Texture UUID.
     */
    private UUID textureUUID;

    /**
     * Cached player info for custom texture.
     */
    private volatile ResourceLocation cachedTexture;

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
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        recruitmentCosts = buf.readItem();
        recruitmentCosts.setCount(buf.readInt());
        if (buf.readBoolean())
        {
            textureUUID = buf.readUUID();
        }
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
        if (cachedTexture == null)
        {
            cachedTexture = DefaultPlayerSkin.get(textureUUID).texture();
            Util.backgroundExecutor().execute(() ->
            {
                Minecraft minecraft = Minecraft.getInstance();
                final ProfileResult profile = minecraft.getMinecraftSessionService().fetchProfile(textureUUID, true);
                if (profile != null)
                {
                    final ResourceLocation newTexture = minecraft.getSkinManager().getInsecureSkin(profile.profile()).texture();
                    if (newTexture != null)
                    {
                        cachedTexture = newTexture;
                    }
                }
            });
        }
        return cachedTexture;
    }
}
