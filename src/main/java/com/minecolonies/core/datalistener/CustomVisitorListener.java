package com.minecolonies.core.datalistener;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.interactionhandling.RecruitmentInteraction;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Loads and listens to custom visitor data added
 */
public class CustomVisitorListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    public static final String VISITOR_TEXTURE          = "texture";
    public static final String VISITOR_NAME             = "name";
    public static final String VISITOR_STORYKEY         = "storylangkey";
    public static final String VISITOR_CHANCE           = "chance";
    public static final String VISITOR_CITIZEN_SUFFIX   = "citizensuffix";
    public static final String VISITOR_RECRUITCOST      = "recruitcost";
    public static final String VISITOR_RECRUITCOSTCOUNT = "recruitcostcount";
    public static final String VISITOR_GENDER           = "gender";

    /**
     * List of custom visitor data
     */
    public static ImmutableList<CustomVisitorData> visitorDataPack = ImmutableList.of();

    /**
     * Random
     */
    private static final Random rand = new Random();

    public CustomVisitorListener()
    {
        super(GSON, "visitors");
    }

    @Override
    protected void apply(
      final Map<ResourceLocation, JsonElement> jsonElementMap, final ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        visitorDataPack = ImmutableList.of();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            tryParse(entry);
        }
    }

    /**
     * Tries to parse the entry
     *
     * @param entry
     */
    private void tryParse(final Map.Entry<ResourceLocation, JsonElement> entry)
    {
        try
        {
            final JsonObject data = (JsonObject) entry.getValue();
            final CustomVisitorData dataEntry = new CustomVisitorData();

            if (data.has(VISITOR_TEXTURE))
            {
                dataEntry.texture = UUID.fromString(data.get(VISITOR_TEXTURE).getAsString());
            }

            if (data.has(VISITOR_NAME))
            {
                dataEntry.name = data.get(VISITOR_NAME).getAsString();
            }

            if (data.has(VISITOR_STORYKEY))
            {
                dataEntry.storykey = data.get(VISITOR_STORYKEY).getAsString();
            }

            if (data.has(VISITOR_CHANCE))
            {
                dataEntry.chance = data.get(VISITOR_CHANCE).getAsDouble();
            }

            if (data.has(VISITOR_CITIZEN_SUFFIX))
            {
                dataEntry.citizenSuffix = data.get(VISITOR_CITIZEN_SUFFIX).getAsString();
            }

            if (data.has(VISITOR_RECRUITCOST))
            {
                dataEntry.recruitCost = ItemStackUtils.idToItemStack(data.get(VISITOR_RECRUITCOST).getAsString());
            }

            if (data.has(VISITOR_RECRUITCOSTCOUNT))
            {
                if (dataEntry.recruitCost != null)
                {
                    dataEntry.recruitCost.setCount(data.get(VISITOR_RECRUITCOSTCOUNT).getAsInt());
                }
            }

            if (data.has(VISITOR_GENDER))
            {
                dataEntry.gender = data.get(VISITOR_GENDER).getAsString().substring(0, 1);
                if (!(dataEntry.gender.equals("m") || dataEntry.gender.equals("f")))
                {
                    Log.getLogger().warn("Could not parse visitor gender(m/f) for:" + entry.getKey());
                    return;
                }
            }

            visitorDataPack = ImmutableList.<CustomVisitorData>builder().addAll(visitorDataPack).add(dataEntry).build();
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Could not parse visitor for:" + entry.getKey(), e);
        }
    }

    /**
     * Custom visitor data
     */
    class CustomVisitorData
    {
        /**
         * Texture
         */
        private UUID texture = null;

        /**
         * Name
         */
        private String name = null;

        /**
         * Story key
         */
        private String storykey = null;

        /**
         * Appearance chance
         */
        private double chance = 0d;

        /**
         * Citizen suffix
         */
        private String citizenSuffix = null;

        /**
         * Recruitment costs
         */
        private ItemStack recruitCost;

        /**
         * Gender setting
         */
        private String gender = null;

        /**
         * Modifies the given visitor data
         *
         * @param visitorData
         */
        public void applyToVisitor(final IVisitorData visitorData)
        {
            if (texture != null)
            {
                visitorData.setCustomTexture(texture);
            }

            if (gender != null)
            {
                visitorData.setGender(gender.equals("f"));
            }

            if (name != null)
            {
                visitorData.setName(name);
            }

            if (citizenSuffix != null)
            {
                visitorData.setSuffix(citizenSuffix);
            }

            if (recruitCost != null)
            {
                visitorData.setRecruitCosts(recruitCost);
            }

            if (storykey != null)
            {
                visitorData.triggerInteraction(new RecruitmentInteraction(Component.translatableEscape(storykey, visitorData.getName().split(" ")[0]), ChatPriority.IMPORTANT));
            }

            visitorData.markDirty(0);
        }
    }

    /**
     * Rolls the custom visitor chance
     *
     * @param original
     * @return
     */
    public static boolean chanceCustomVisitors(final IVisitorData original)
    {
        for (final CustomVisitorData customVisitorData : visitorDataPack)
        {
            if (rand.nextDouble() < customVisitorData.chance)
            {
                customVisitorData.applyToVisitor(original);
                return true;
            }
        }
        return false;
    }
}
