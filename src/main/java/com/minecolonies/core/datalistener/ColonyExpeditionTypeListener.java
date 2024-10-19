package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.expeditions.IColonyExpeditionManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeParser;
import com.minecolonies.core.network.messages.client.GlobalExpeditionTypeSyncMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Loader for json based expedition types.
 */
public class ColonyExpeditionTypeListener extends SimpleJsonResourceReloadListener
{
    /**
     * The gson instance.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * The map of all possible expedition types.
     */
    private static Map<ResourceLocation, ColonyExpeditionType> POSSIBLE_TYPES = new HashMap<>();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: (namespace)/colony/expedition_types/(path)
     */
    public ColonyExpeditionTypeListener()
    {
        super(GSON, "colony/expedition_types");
    }

    /**
     * Get the provided expedition type from its id.
     *
     * @param id the id.
     * @return the expedition type instance.
     */
    @Nullable
    public static ColonyExpeditionType getExpeditionType(final ResourceLocation id)
    {
        return POSSIBLE_TYPES.get(id);
    }

    /**
     * Obtain a random expedition type from the map of possible expedition types.
     * The target dimension must be reachable according to {@link IColonyExpeditionManager#canGoToDimension(ResourceKey)}.
     * This method can also return null, if there are no expedition types available at all.
     *
     * @param colony the colony reference to get the expedition manager for.
     * @return the expedition type.
     */
    @Nullable
    public static ColonyExpeditionType getRandomExpeditionType(final IColony colony)
    {
        final IColonyExpeditionManager expeditionManager = colony.getExpeditionManager();
        final List<ColonyExpeditionType> expeditionTypes = new ArrayList<>(POSSIBLE_TYPES.values());

        ColonyExpeditionType chosenExpeditionType = null;
        while (!expeditionTypes.isEmpty() && chosenExpeditionType == null)
        {
            final ColonyExpeditionType colonyExpeditionType = expeditionTypes.get(colony.getWorld().getRandom().nextInt(expeditionTypes.size()));
            if (!expeditionManager.canGoToDimension(colonyExpeditionType.dimension()))
            {
                expeditionTypes.removeIf(type -> type.dimension().equals(colonyExpeditionType.dimension()));
                continue;
            }

            chosenExpeditionType = colonyExpeditionType;
        }

        return chosenExpeditionType;
    }

    /**
     * Sync to client.
     *
     * @param player to send it to.
     */
    public static void sendGlobalExpeditionTypePacket(final ServerPlayer player)
    {
        final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeInt(POSSIBLE_TYPES.size());
        for (final Map.Entry<ResourceLocation, ColonyExpeditionType> entry : POSSIBLE_TYPES.entrySet())
        {
            ColonyExpeditionTypeParser.toBuffer(entry.getValue(), byteBuf);
        }
        Network.getNetwork().sendToPlayer(new GlobalExpeditionTypeSyncMessage(byteBuf), player);
    }

    /**
     * Read the data from the packet and parse it.
     *
     * @param byteBuf pck.
     */
    public static void readGlobalExpeditionTypePackets(final FriendlyByteBuf byteBuf)
    {
        final Map<ResourceLocation, ColonyExpeditionType> newTypes = new HashMap<>();
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ColonyExpeditionType expeditionType = ColonyExpeditionTypeParser.fromBuffer(byteBuf);
            newTypes.put(expeditionType.id(), expeditionType);
        }
        POSSIBLE_TYPES = Collections.unmodifiableMap(newTypes);
    }

    @Override
    protected void apply(
      @NotNull final Map<ResourceLocation, JsonElement> object, @NotNull final ResourceManager resourceManager, @NotNull final ProfilerFiller profiler)
    {
        Log.getLogger().info("Beginning load of expedition types for colony.");

        final Map<ResourceLocation, ColonyExpeditionType> newTypes = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final ColonyExpeditionType parsed = ColonyExpeditionTypeParser.parse(key, entry.getValue().getAsJsonObject());
                newTypes.put(key, parsed);
            }
            catch (final Exception e)
            {
                Log.getLogger().error(new FormattedMessage("Error parsing expedition type {}", new Object[] {key}, e));
            }
        }
        POSSIBLE_TYPES = Collections.unmodifiableMap(newTypes);
    }
}