package com.minecolonies.coremod.datalistener;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory;
import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.TextureCategory;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.client.render.modularcitizen.CitizenResourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This runs on the client during a Resource Manager reload event.  Normally, that means once on client startup, on changes to Resource Pack settings, or from F3 + T.
 * Avoid very long computation here (>10s), as F3 + T may cause client inconsistency if that slow.
 */
public class ClientResourceListener extends ReloadListener
{
    public static void register()
    {
        // Yes, it can be null, even in client-specific situations: cfe runData.
        if(Minecraft.getInstance() != null)
        {
            ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ClientResourceListener());
        }
    }

    @Override
    protected Object prepare(@NotNull final IResourceManager resourceManagerIn, @NotNull final IProfiler profilerIn)
    {
        final CitizenResourceRegistry registry = ((CitizenResourceRegistry)IMinecoloniesAPI.getInstance().getCitizenResourceRegistry());
        // During a reload event, the entire CitizenResourceRegistry may have inconsistent data, and this occurs in a background thread of unpredictable time complexity.
        // Set the registry to an unloaded state, and only after completion of the registration, then set it back to a completed one.
        registry.setLoaded(false);
        return registerTexturesAndModels(resourceManagerIn);
    }

    @Override
    protected void apply(@NotNull final Object completed, @NotNull final IResourceManager resourceManagerIn, @NotNull final IProfiler profilerIn)
    {
        // This form of data passing is probably overkill right now, as registration can't fail (yet) and _should_ be atomic.
        ((CitizenResourceRegistry)IMinecoloniesAPI.getInstance().getCitizenResourceRegistry()).setLoaded((boolean) completed);
    }

    private boolean registerTexturesAndModels(final IResourceManager manager)
    {
        // TODO: this function is... not great, with bad performance ramifications, especially during getAllResourceLocations.
        // It's off-main-thread, but eventually worth evaluating File.IO versus Minecraft's Resource Manager behaviors, along with better cleanup of typing.
        // A good implementation of settingIdentifiers is likely to involve tweaks across a ton of unrelated classes, though, so leave for a different PR.

        // The resourceMap here uses base/work/home -> isFemale -> style -> settingIdentifier (usually building).
        // Internally, CitizenResourceContainers hold slot -> resourceLocations maps for models and textures.
        // Style and settingIdentifier can't be easily made enums, as they're dynamic, but may eventually be worth modifying into cleaner forms.
        final Map<Boolean, Map<String, Map<String, ModularCitizenResourceContainer>>> resourceMap = new HashMap<>();

        final Collection<ResourceLocation> rlModels = manager.getAllResourceLocations("models\\entity\\modularcitizen", s -> s.contains(".json"));
        // ModelMaps use style - settingIdentifier (usually building) - part - resourceLocations
        // during collection, we'll wrap all three into an additional layer to hold the base/home/work distinctions, but these will be separated for storage/access in CitizenResourceRegistry
        for(final ResourceLocation res : rlModels)
        {
            parseModelLocationString(res, resourceMap);
        }

        final Collection<ResourceLocation> rls = manager.getAllResourceLocations("textures\\entity\\modularcitizen", s -> s.contains(".png"));
        for(final ResourceLocation res : rls)
        {
            parseTextureLocationString(res, resourceMap);
        }

        ((CitizenResourceRegistry)IMinecoloniesAPI.getInstance().getCitizenResourceRegistry()).putResourceRegistry(resourceMap);
        return true;
    }
    
    private void parseModelLocationString(final ResourceLocation res, final Map<Boolean, Map<String, Map<String, ModularCitizenResourceContainer>>> resourceMap)
    {
        // Model resource location format are "minecolonies:models/entity/modularcitizen/<style>/<gender>/<bodypart>/<base OR home OR work>/<settingID>.json"
        // If we re-slice texture files, they will use the format "minecolonies:textures/entity/citizen/<style>/<gender>/<bodypart>/<base OR home OR work>/<settingID>_<optional identifier>.png"
        final String[] stringParts = res.getPath().split("/");
        if(stringParts.length == 8)
        {
            final ModelCategory category = ModelCategory.value(stringParts[6]);
            if(category == null)
            {
                Log.getLogger().warn("Modular Citizen Model did not represent a valid type at " + res);
                return;
            }
            final CitizenSlots slot = CitizenSlots.value(stringParts[5]);
            if(slot == null)
            {
                Log.getLogger().warn("Modular Citizen Model had an invalid body slot value at " + res);
                return;
            }
            final boolean isFemale;
            if(stringParts[4].equals("female"))
            {
                isFemale = true;
            }
            else if (stringParts[4].equals("male"))
            {
                isFemale = false;
            }
            else
            {
                Log.getLogger().warn("Modular Citizen Model had a gender value not currently supported at " + res);
                return;
            }
            if(!resourceMap.containsKey(isFemale))
            {
                resourceMap.put(isFemale, new HashMap<>());
            }
            final String style = stringParts[3];
            if(!resourceMap.get(isFemale).containsKey(style))
            {
                resourceMap.get(isFemale).put(style, new HashMap<>());
            }
            final String jobName = stringParts[7].substring(0, stringParts[7].lastIndexOf("."));
            if(!resourceMap.get(isFemale).get(style).containsKey(jobName))
            {
                resourceMap.get(isFemale).get(style).put(jobName, new ModularCitizenResourceContainer());
            }

            resourceMap.get(isFemale).get(style).get(jobName).putModel(category, slot, res);
        }
    }

    private void parseTextureLocationString(final ResourceLocation res, final Map<Boolean, Map<String, Map<String, ModularCitizenResourceContainer>>> resourceMap)
    {
        // Current resource location format are "minecolonies:textures/entity/citizen/<style>/clothing/<settingID>_<optional gender>_<id>.png",
        // "minecolonies:textures/entity/citizen/<style>/hair/<settingID>_<optional gender>_<id>.png"
        // "minecolonies:textures/entity/citizen/<style>/base/<settingID>_<optional gender>_<id>.png"
        final String[] stringParts = res.getPath().split("/");
        if(stringParts.length == 6)
        {
            final String style = stringParts[3];
            final String[] fileNameParts = stringParts[5].substring(0, stringParts[5].indexOf(".png")).split("_");
            if (fileNameParts.length == 0)
            {
                // 0-length filenames are legal! But bad.
                return;
            }
            final TextureCategory texType = TextureCategory.value(stringParts[4]);
            if (texType == null)
            {
                return;
            }
            final String settingId = fileNameParts[0];

            final boolean[] genders;
            if (fileNameParts.length >= 2 && fileNameParts[1].contains("female"))
            {
                genders = new boolean[] {true};
            }
            else if (fileNameParts.length >= 2 && fileNameParts[1].contains("male"))
            {
                genders = new boolean[] {false};
            }
            else
            {
                genders = new boolean[] {true, false};
            }
            for (final boolean isFemale : genders)
            {
                if (!resourceMap.containsKey(isFemale))
                {
                    resourceMap.put(isFemale, new HashMap<>());
                }
                if (!resourceMap.get(isFemale).containsKey(style))
                {
                    resourceMap.get(isFemale).put(style, new HashMap<>());
                }
                if (!resourceMap.get(isFemale).get(style).containsKey(settingId))
                {
                    resourceMap.get(isFemale).get(style).put(settingId, new ModularCitizenResourceContainer());
                }
                resourceMap.get(isFemale).get(style).get(settingId).putTexture(texType, res);
            }
        }
    }
}
