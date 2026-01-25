package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounter;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounterParser;
import com.minecolonies.core.network.messages.client.GlobalExpeditionEncounterSyncMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loader for json based expedition encounters.
 */
public class ExpeditionEncounterListener extends SimpleJsonResourceReloadListener
{
    /**
     * The gson instance.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * The map of all possible expedition encounters.
     */
    private static Map<ResourceLocation, ExpeditionEncounter> POSSIBLE_TYPES = new HashMap<>();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: (namespace)/colony/expedition_encounters/(path)
     */
    public ExpeditionEncounterListener()
    {
        super(GSON, "colony/expedition_encounters");
    }

    /**
     * Get an encounter from its id.
     *
     * @param encounter the encounter id.
     * @return the encounter instance or null.
     */
    public static ExpeditionEncounter getEncounter(final ResourceLocation encounter)
    {
        return POSSIBLE_TYPES.get(encounter);
    }

    /**
     * Sync to client.
     *
     * @param player to send it to.
     */
    public static void sendGlobalExpeditionEncounterPacket(final ServerPlayer player)
    {
        final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeInt(POSSIBLE_TYPES.size());
        for (final Map.Entry<ResourceLocation, ExpeditionEncounter> entry : POSSIBLE_TYPES.entrySet())
        {
            ExpeditionEncounterParser.toBuffer(entry.getValue(), byteBuf);
        }
        Network.getNetwork().sendToPlayer(new GlobalExpeditionEncounterSyncMessage(byteBuf), player);
    }

    /**
     * Read the data from the packet and parse it.
     *
     * @param byteBuf pck.
     */
    public static void readGlobalExpeditionEncounterPackets(final FriendlyByteBuf byteBuf)
    {
        final Map<ResourceLocation, ExpeditionEncounter> newTypes = new HashMap<>();
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ExpeditionEncounter encounter = ExpeditionEncounterParser.fromBuffer(byteBuf);
            newTypes.put(encounter.id(), encounter);
        }
        POSSIBLE_TYPES = Collections.unmodifiableMap(newTypes);
    }

    @Override
    protected void apply(
      @NotNull final Map<ResourceLocation, JsonElement> object,
      @NotNull final ResourceManager resourceManager,
      @NotNull final ProfilerFiller profiler)
    {
        Log.getLogger().info("Beginning load of expedition encounters.");

        final Map<ResourceLocation, ExpeditionEncounter> newTypes = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation key = entry.getKey();
            try
            {
                final ExpeditionEncounter parsed = ExpeditionEncounterParser.parse(key, entry.getValue().getAsJsonObject());
                newTypes.put(key, parsed);
            }
            catch (final JsonParseException | NullPointerException e)
            {
                Log.getLogger().error(new FormattedMessage("Error parsing expedition encounter {}", new Object[] {key}, e));
            }
        }

        POSSIBLE_TYPES = Collections.unmodifiableMap(newTypes);
    }
}