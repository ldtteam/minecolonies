package com.minecolonies.coremod.colony;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
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
    public void deserialize(@NotNull final FriendlyByteBuf buf)
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
            final GameProfile profile = new GameProfile(textureUUID, getNameFromUUID(textureUUID));
            SkullBlockEntity.updateGameprofile(profile, (gameProfile -> {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(profile);
                if (!map.isEmpty())
                {
                    cachedTexture = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
            }));

        }
        return cachedTexture == null ? DefaultPlayerSkin.getDefaultSkin(textureUUID) : cachedTexture;
    }

    /**
     * Query the name from the mojang API.
     * @param uuid uuid of the user.
     * @return the name or null.
     */
    private static String getNameFromUUID(final UUID uuid)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
              new URL("https://api.mojang.com/user/profiles/" + uuid.toString()+ "/names")
                .openConnection().getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();
            JsonArray json = new Gson().fromJson(response.toString(), JsonArray.class);
            return json.get(json.size() - 1).getAsJsonObject().get("name").getAsString();
        }
        catch (Exception ignored)
        {
        }
        return null;
    }
}
