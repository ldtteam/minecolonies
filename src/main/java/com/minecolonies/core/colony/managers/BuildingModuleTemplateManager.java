package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ITemplateModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.colony.managers.interfaces.IBuildingModuleTemplateManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.network.messages.client.colony.ColonyViewModuleTemplateManagerViewMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Implementation for the building module template manager.
 */
public class BuildingModuleTemplateManager implements IBuildingModuleTemplateManager
{
    /**
     * NBT tags.
     */
    private static final String TAG_TEMPLATES                                   = "templates";
    private static final String TAG_TEMPLATE_BUILDING_KEY                       = "building";
    private static final String TAG_TEMPLATE_MODULES                            = "modules";
    private static final String TAG_TEMPLATE_MODULE_STORAGE_KEY                 = "storage_key";
    private static final String TAG_TEMPLATE_MODULE_ITEMS                       = "items";
    private static final String TAG_TEMPLATE_MODULE_ITEM_DATA                   = "data";
    private static final String TAG_TEMPLATE_MODULE_ITEM_NAME                   = "name";
    private static final String TAG_TEMPLATE_MODULE_ITEM_ASSIGNED_BUILDINGS     = "assigned_buildings";
    private static final String TAG_TEMPLATE_MODULE_ITEM_ASSIGNED_BUILDINGS_POS = "pos";

    /**
     * The colony this manager instance is for.
     */
    private final IColony colony;

    /**
     * The map of templates. Separated by building, storage key and name.
     */
    private final Map<BuildingEntry, Map<ResourceLocation, Map<String, ModuleTemplateData>>> templates;

    /**
     * Dirty flag.
     */
    private boolean dirty = false;

    /**
     * Default constructor.
     *
     * @param colony the colony this manager is for.
     */
    public BuildingModuleTemplateManager(final IColony colony)
    {
        this.colony = colony;
        this.templates = new HashMap<>();
    }

    @Override
    @NotNull
    public List<ModuleTemplateDescriptor> getTemplates(final BuildingEntry buildingEntry, final ResourceLocation key)
    {
        return getModules(buildingEntry, key).values().stream().map(ModuleTemplateData::descriptor).toList();
    }

    @Override
    @Nullable
    public CompoundTag getTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name)
    {
        return Optional.ofNullable(getModules(buildingEntry, key).get(name)).map(ModuleTemplateData::data).orElse(null);
    }

    @Override
    public void applyTemplate(@NotNull final HolderLookup.Provider provider, final ITemplateModule module, final String name)
    {
        final CompoundTag data = getTemplate(module.getBuilding().getBuildingType(), module.getTemplateStorageId(), name);

        module.deserializeNBT(provider, data);
        module.markDirty();

        Optional.ofNullable(getModules(module.getBuilding().getBuildingType(), module.getTemplateStorageId()).get(name))
            .ifPresent(moduleTemplateData -> moduleTemplateData.assignedBuildings.add(module.getBuilding().getID()));

        markDirty();
    }

    @Override
    public boolean isApplied(final BuildingEntry buildingEntry, final ResourceLocation key, final String name, final BlockPos buildingPos)
    {
        return Optional.ofNullable(getModules(buildingEntry, key).get(name)).map(module -> module.assignedBuildings.contains(buildingPos)).orElse(false);
    }

    @Override
    public void ignoreTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final BlockPos buildingPos)
    {
        getModules(buildingEntry, key).forEach((s, moduleTemplateData) -> moduleTemplateData.assignedBuildings.remove(buildingPos));
        markDirty();
    }

    @Override
    public void ignoreTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name, final BlockPos buildingPos)
    {
        Optional.ofNullable(getModules(buildingEntry, key).get(name)).ifPresent(moduleTemplateData -> moduleTemplateData.assignedBuildings.remove(buildingPos));
        markDirty();
    }

    @Override
    public void updateTemplate(@NotNull final HolderLookup.Provider provider, final ITemplateModule module, final String name)
    {
        final CompoundTag newTemplateData = new CompoundTag();
        module.serializeNBT(provider, newTemplateData);

        final ModuleTemplateData newModuleData = getModules(module.getBuilding().getBuildingType(), module.getTemplateStorageId()).compute(name, (id, old) -> {
            final ModuleTemplateDescriptor itemDescriptor = new ModuleTemplateDescriptor(id, Optional.ofNullable(old).map(m -> m.assignedBuildings().size()).orElse(0));
            final ModuleTemplateData moduleTemplateData =
                new ModuleTemplateData(newTemplateData, itemDescriptor, Optional.ofNullable(old).map(ModuleTemplateData::assignedBuildings).orElse(new HashSet<>()));
            moduleTemplateData.assignedBuildings.add(module.getBuilding().getID());
            return moduleTemplateData;
        });

        markDirty();

        final Iterator<BlockPos> iterator = newModuleData.assignedBuildings.iterator();
        while (iterator.hasNext())
        {
            final BlockPos buildingPos = iterator.next();
            final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
            if (!Objects.equals(building.getBuildingType(), module.getBuilding().getBuildingType()))
            {
                iterator.remove();
            }

            try
            {
                final ITemplateModule moduleMatching =
                    building.getModuleMatching(ITemplateModule.class, templateModule -> Objects.equals(templateModule.getTemplateStorageId(), module.getTemplateStorageId()));
                moduleMatching.deserializeNBT(provider, newTemplateData);
                moduleMatching.markDirty();
            }
            catch (Exception ex)
            {
                iterator.remove();
            }
        }
    }

    @Override
    public void removeTemplate(final BuildingEntry buildingEntry, final ResourceLocation key, final String name)
    {
        getModules(buildingEntry, key).remove(name);
        markDirty();
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound)
    {
        final Map<BuildingEntry, Map<ResourceLocation, Map<String, ModuleTemplateData>>> newBuildingModuleStorageTemplates = new HashMap<>();
        final ListTag buildingTemplateTagList = compound.getList(TAG_TEMPLATES, Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingTemplateTagList.size(); i++)
        {
            final CompoundTag buildingTemplateCompound = buildingTemplateTagList.getCompound(i);
            final ResourceLocation buildingKey = ResourceLocation.tryParse(buildingTemplateCompound.getString(TAG_TEMPLATE_BUILDING_KEY));
            final BuildingEntry buildingEntry = IBuildingRegistry.getInstance().get(buildingKey);
            if (buildingEntry == null)
            {
                Log.getLogger().warn("Problem during loading module templates. A building could not be found. Skipping building with key '{}'.", buildingKey);
                continue;
            }

            final Map<ResourceLocation, Map<String, ModuleTemplateData>> newModuleStorageTemplates = new HashMap<>();
            final ListTag moduleTemplateTagList = buildingTemplateCompound.getList(TAG_TEMPLATE_MODULES, Tag.TAG_COMPOUND);
            for (int ii = 0; ii < moduleTemplateTagList.size(); ii++)
            {
                final CompoundTag moduleTemplateCompound = moduleTemplateTagList.getCompound(ii);
                final ResourceLocation storageKey = ResourceLocation.tryParse(moduleTemplateCompound.getString(TAG_TEMPLATE_MODULE_STORAGE_KEY));
                if (storageKey == null)
                {
                    continue;
                }

                final Map<String, ModuleTemplateData> newModuleTemplates = new HashMap<>();
                final ListTag itemTemplateTagList = moduleTemplateCompound.getList(TAG_TEMPLATE_MODULE_ITEMS, Tag.TAG_COMPOUND);
                for (int iii = 0; iii < itemTemplateTagList.size(); iii++)
                {
                    final CompoundTag itemTemplateCompound = itemTemplateTagList.getCompound(iii);
                    final CompoundTag itemData = itemTemplateCompound.getCompound(TAG_TEMPLATE_MODULE_ITEM_DATA);
                    final String itemName = itemTemplateCompound.getString(TAG_TEMPLATE_MODULE_ITEM_NAME);

                    final Set<BlockPos> itemAssignedBuildings = new HashSet<>();
                    final ListTag assignedBuildingsList = itemTemplateCompound.getList(TAG_TEMPLATE_MODULE_ITEM_ASSIGNED_BUILDINGS, Tag.TAG_INT_ARRAY);
                    for (final Tag tag : assignedBuildingsList)
                    {
                        itemAssignedBuildings.add(NBTUtils.readBlockPos(tag));
                    }

                    final ModuleTemplateDescriptor itemDescriptor = new ModuleTemplateDescriptor(itemName, itemAssignedBuildings.size());

                    newModuleTemplates.put(itemName, new ModuleTemplateData(itemData, itemDescriptor, itemAssignedBuildings));
                }

                newModuleStorageTemplates.put(storageKey, newModuleTemplates);
            }

            newBuildingModuleStorageTemplates.put(buildingEntry, newModuleStorageTemplates);
        }

        templates.clear();
        templates.putAll(newBuildingModuleStorageTemplates);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = new CompoundTag();
        final ListTag buildingTemplateTagList = new ListTag();
        for (final Map.Entry<BuildingEntry, Map<ResourceLocation, Map<String, ModuleTemplateData>>> buildingTemplate : templates.entrySet())
        {
            final CompoundTag buildingTemplateCompound = new CompoundTag();
            buildingTemplateCompound.putString(TAG_TEMPLATE_BUILDING_KEY, buildingTemplate.getKey().getRegistryName().toString());

            final ListTag moduleTemplateTagList = new ListTag();
            for (final Map.Entry<ResourceLocation, Map<String, ModuleTemplateData>> moduleTemplate : buildingTemplate.getValue().entrySet())
            {
                final CompoundTag moduleTemplateCompound = new CompoundTag();
                moduleTemplateCompound.putString(TAG_TEMPLATE_MODULE_STORAGE_KEY, moduleTemplate.getKey().toString());

                final ListTag itemTemplateTagList = new ListTag();
                for (final Map.Entry<String, ModuleTemplateData> itemTemplate : moduleTemplate.getValue().entrySet())
                {
                    final CompoundTag itemTemplateCompound = new CompoundTag();
                    itemTemplateCompound.put(TAG_TEMPLATE_MODULE_ITEM_DATA, itemTemplate.getValue().data());
                    itemTemplateCompound.putString(TAG_TEMPLATE_MODULE_ITEM_NAME, itemTemplate.getValue().descriptor().name());
                    itemTemplateCompound.put(TAG_TEMPLATE_MODULE_ITEM_ASSIGNED_BUILDINGS,
                        itemTemplate.getValue().assignedBuildings().stream().map(NBTUtils::writeBlockPos).collect(NBTUtils.toListNBT()));
                    itemTemplateTagList.add(itemTemplateCompound);
                }

                moduleTemplateCompound.put(TAG_TEMPLATE_MODULE_ITEMS, itemTemplateTagList);
                moduleTemplateTagList.add(moduleTemplateCompound);
            }

            buildingTemplateCompound.put(TAG_TEMPLATE_MODULES, moduleTemplateTagList);
            buildingTemplateTagList.add(buildingTemplateCompound);
        }
        compound.put(TAG_TEMPLATES, buildingTemplateTagList);
        return compound;
    }

    @Override
    public void sendPackets(final @NotNull Set<ServerPlayer> closeSubscribers, final Set<ServerPlayer> newSubscribers)
    {
        if (dirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayer> players = new HashSet<>();
            if (dirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);

            new ColonyViewModuleTemplateManagerViewMessage(colony, this).sendToPlayer(players);
        }
        clearDirty();
    }

    @Override
    public void overWriteData(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound)
    {
        deserializeNBT(provider, compound);
        markDirty();
    }

    @Override
    public void markDirty()
    {
        dirty = true;
    }

    @Override
    public boolean checkDirty()
    {
        return dirty;
    }

    @Override
    public void clearDirty()
    {
        dirty = false;
    }

    /**
     * Internal method for grabbing the list of module templates safely.
     *
     * @param buildingEntry the type of building.
     * @param key           the storage key.
     * @return the map of module templates.
     */
    @NotNull
    private Map<String, ModuleTemplateData> getModules(final BuildingEntry buildingEntry, final ResourceLocation key)
    {
        return templates.computeIfAbsent(buildingEntry, (k) -> new HashMap<>()).computeIfAbsent(key, (k) -> new HashMap<>());
    }

    /**
     * The module template data.
     *
     * @param data              the underlying compound data.
     * @param descriptor        the module template descriptor data.
     * @param assignedBuildings the set of assigned building positions.
     */
    private record ModuleTemplateData(
        @NotNull CompoundTag data,
        @NotNull ModuleTemplateDescriptor descriptor,
        @NotNull Set<BlockPos> assignedBuildings)
    {}
}
