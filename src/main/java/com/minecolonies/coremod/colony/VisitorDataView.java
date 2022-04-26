package com.minecolonies.coremod.colony;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import net.minecraftforge.common.UsernameCache;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * View data for visitors
 */
public class VisitorDataView extends CitizenDataView implements IVisitorViewData
{
    /**
     * Two atomics to avoid semaphores.
     */
    private boolean startedDownloading = false;
    private AtomicBoolean finishedDownloading = new AtomicBoolean(false);

    /**
     * The related colony view
     */
    private final  IColonyView   colony;

    /**
     * The recruitment costs
     */
    private       ItemStack                         recruitmentCosts;

    /**
     * Texture UUID.
     */
    private UUID textureUUID;

    /**
     * Cached minecraft name.
     */
    private String cachedMinecraftName;

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
        recruitmentCosts.setCount(buf.readInt());
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
            if (finishedDownloading.get())
            {
                final Minecraft minecraft = Minecraft.getInstance();
                GameProfile profile = new GameProfile(textureUUID, cachedMinecraftName);
                profile = SkullTileEntity.updateGameprofile(profile);
                final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(profile);
                if (!map.isEmpty())
                {
                    cachedTexture = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
            }
            if (startedDownloading)
            {
                return DefaultPlayerSkin.getDefaultSkin(textureUUID);
            }
            startedDownloading = true;
            queryNameFromUUID(textureUUID);
        }
        return cachedTexture == null ? DefaultPlayerSkin.getDefaultSkin(textureUUID) : cachedTexture;
    }

    /**
     * Query the name from the mojang API.
     * @param uuid uuid of the user.
     * @return the name or null.
     */
    private void queryNameFromUUID(final UUID uuid)
    {
        new Thread(() -> {
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
                cachedMinecraftName = json.get(json.size() - 1).getAsJsonObject().get("name").getAsString();
                finishedDownloading.set(cachedMinecraftName != null);
            }
            catch (Exception ignored)
            {

            }
        }).start();
    }
}
