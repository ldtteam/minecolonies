package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * View data for visitors
 */
public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    /**
     * The related colony view
     */
    private final IColonyView colony;

    /**
     * The recruitment costs
     */
    private       ItemStack                         recruitmentCosts;

    /**
     * Texture UUID.
     */
    private UUID textureUUID;

    /**
     * Cached player info for custom texture.
     */
    private ResourceLocation cachedTexture;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorDataView(final int id, final IColonyView colony)
    {
        super(id);
        this.colony = colony;
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        super.deserialize(buf);
        recruitmentCosts = buf.readItem();
        if (buf.readBoolean())
        {
            textureUUID = buf.readUUID();
        }
    }

    @Override
    public IColonyView getColonyView()
    {
        return colony;
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
            Minecraft minecraft = Minecraft.getInstance();
            GameProfile profile = new GameProfile(textureUUID, "_Raycoms_");
            profile = SkullTileEntity.updateGameprofile(profile);
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(profile);
            if (!map.isEmpty())
            {
                cachedTexture = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }
        }
        return cachedTexture == null ? DefaultPlayerSkin.getDefaultSkin(textureUUID) : cachedTexture;
    }
}
