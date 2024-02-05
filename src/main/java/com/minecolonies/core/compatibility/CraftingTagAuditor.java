package com.minecolonies.core.compatibility;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.buildings.modules.SimpleCraftingModule;
import com.minecolonies.core.colony.crafting.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * This class analyzes the current crafting tags and generates a CSV file reporting on which items can be crafted by
 * which crafter, as a way to verify that all intended items are covered by the appropriate crafters (or not).  It
 * also produces another file that just contains all tags for each item, both to check the crafting tags themselves
 * and to help pick appropriate tags to add to the crafting tags if something is missing.
 *
 * The logic of evaluating recipes closely resembles {@link com.minecolonies.core.compatibility.jei.JEIPlugin}, but
 * we want this to happen server-side without depending on anything from that.
 */
public class CraftingTagAuditor
{
    /**
     * Run the auditor to generate the output file.
     * @param server the current Minecraft server
     * @param customRecipeManager the custom recipe manager (mainly used for loot checks)
     */
    public static void doRecipeAudit(@NotNull final MinecraftServer server,
                                     @NotNull final CustomRecipeManager customRecipeManager)
    {
        createFile("item tag audit", server, "tag_item_audit.csv", writer -> doItemTagAudit(writer, server));
        createFile("block tag audit", server, "tag_block_audit.csv", writer -> doBlockTagAudit(writer, server));
        createFile("recipe audit", server, "recipe_audit.csv", writer -> doRecipeAudit(writer, server, customRecipeManager));
        createFile("domum audit", server, "domum_audit.csv", writer -> doDomumAudit(writer, server));
        createFile("tools audit", server, "tools_audit.csv", writer -> doToolsAudit(writer, server));
    }

    private static boolean createFile(@NotNull final String description,
                                      @NotNull final MinecraftServer server,
                                      @NotNull final String filename,
                                      @NotNull final Writeable generator)
    {
        final Path outputPath = server.getWorldPath(LevelResource.ROOT).resolve(MOD_ID).resolve(filename);

        Log.getLogger().info("Beginning " + description + "...");
        try
        {
            Files.createDirectories(outputPath.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(outputPath))
            {
                generator.write(writer);
            }

            Log.getLogger().info("Completed " + description + "; written to " + outputPath);
            return true;
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Failed to write " + description + " to " + outputPath, ex);
            return false;
        }
    }

    @FunctionalInterface
    private interface Writeable
    {
        void write(@NotNull BufferedWriter writer) throws IOException;
    }

    private static List<ItemStack> getAllItems()
    {
        final ICompatibilityManager compatibility = IColonyManager.getInstance().getCompatibilityManager();
        final List<ItemStack> items = new ArrayList<>(compatibility.getListOfAllItems());
        items.sort(Comparator.comparing(stack -> ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()));
        return items;
    }

    private static void doItemTagAudit(@NotNull final BufferedWriter writer,
                                       @NotNull final MinecraftServer server) throws IOException
    {
        writeItemHeaders(writer);
        writer.write(",tags...");
        writer.newLine();

        for (final ItemStack item : getAllItems())
        {
            writeItemData(writer, item);

            item.getTags()
                    .map(t -> t.location().toString())
                    .sorted()
                    .forEach(t ->
            {
                try
                {
                    writer.write(',');
                    writer.write(t);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
            writer.newLine();
        }
    }

    private static void doBlockTagAudit(@NotNull final BufferedWriter writer,
                                        @NotNull final MinecraftServer server) throws IOException
    {
        writer.write("block,name,tags...");
        writer.newLine();

        for (final Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            writer.write(entry.getKey().location().toString());
            writer.write(',');
            writer.write('"');
            writer.write(Component.translatable(entry.getValue().getDescriptionId()).getString().replace("\"", "\"\""));
            writer.write('"');
            ForgeRegistries.BLOCKS.tags().getReverseTag(entry.getValue()).ifPresent(tags ->
            {
                tags.getTagKeys()
                        .map(t -> t.location().toString())
                        .sorted()
                        .forEach(t ->
                        {
                            try
                            {
                                writer.write(',');
                                writer.write(t);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        });
            });
            writer.newLine();
        }
    }

    private static void doRecipeAudit(@NotNull final BufferedWriter writer,
                                      @NotNull final MinecraftServer server,
                                      @NotNull final CustomRecipeManager customRecipeManager) throws IOException
    {
        final Map<CraftingType, List<IGenericRecipe>> vanillaRecipesMap =
                RecipeAnalyzer.buildVanillaRecipesMap(server.getRecipeManager(), server.overworld());
        final List<Animal> animals = RecipeAnalyzer.createAnimals(server.overworld());
        final List<ICraftingBuildingModule> crafters = getCraftingModules()
                .stream()
                .sorted(Comparator.comparing(m -> m instanceof SimpleCraftingModule).reversed())
                .toList();  // sort the simple modules first (2x2 crafting, personal only)
        final List<AnimalHerdingModule> herders = getHerdingModules();
        final Map<ItemStorage, Map<Object, List<IGenericRecipe>>> craftingMap = new HashMap<>();

        // initially map every vanilla craftable
        for (final List<IGenericRecipe> recipeList : vanillaRecipesMap.values())
        {
            for (final IGenericRecipe recipe : recipeList)
            {
                add(customRecipeManager, craftingMap, null, recipe);
            }
        }

        writeItemHeaders(writer);
        writer.write(",player");
        for (final ICraftingBuildingModule crafter : crafters)
        {
            writer.write(',');
            writer.write(crafter.getCustomRecipeKey());

            final List<IGenericRecipe> recipes = RecipeAnalyzer.findRecipes(vanillaRecipesMap, crafter, server.overworld());
            for (final IGenericRecipe recipe : recipes)
            {
                add(customRecipeManager, craftingMap, crafter, recipe);
            }
        }
        for (final AnimalHerdingModule herder : herders)
        {
            writer.write(',');
            writer.write(herder.getHerdingJob().getJobRegistryEntry().getKey().getPath());

            final List<IGenericRecipe> recipes = RecipeAnalyzer.findRecipes(animals, herder);
            for (final IGenericRecipe recipe : recipes)
            {
                add(customRecipeManager, craftingMap, herder, recipe);
            }
        }
        writer.newLine();

        for (final ItemStack item : getAllItems())
        {
            writeItemData(writer, item);

            final Map<Object, List<IGenericRecipe>> crafterMap =
                    craftingMap.getOrDefault(new ItemStorage(item, true, false), Collections.emptyMap());

            writeCrafterValue(writer, crafterMap, null);
            for (final ICraftingBuildingModule crafter : crafters)
            {
                writeCrafterValue(writer, crafterMap, crafter);
            }
            for (final AnimalHerdingModule herder : herders)
            {
                writeCrafterValue(writer, crafterMap, herder);
            }
            writer.newLine();
        }
    }

    private static void doDomumAudit(@NotNull final BufferedWriter writer,
                                     @NotNull final MinecraftServer server) throws IOException
    {
        final List<IGenericRecipe> cutterRecipes = new ArrayList<>(ModCraftingTypes.ARCHITECTS_CUTTER.get().findRecipes(server.getRecipeManager(), server.overworld()));
        cutterRecipes.sort(Comparator.comparing(r -> ForgeRegistries.ITEMS.getKey(r.getPrimaryOutput().getItem()).toString()));
        final List<ICraftingBuildingModule> crafters = getCraftingModules()
                .stream()
                .filter(m -> m.canLearn(ModCraftingTypes.ARCHITECTS_CUTTER.get()))
                .toList();

        writer.write("type,");
        writeItemHeaders(writer);
        for (final ICraftingBuildingModule crafter : crafters)
        {
            writer.write(',');
            writer.write(crafter.getCustomRecipeKey());
        }
        writer.newLine();

        for (final IGenericRecipe recipe : cutterRecipes)
        {
            boolean first = true;

            final List<ItemStack> allSkins = recipe.getInputs().stream()
                    .flatMap(Collection::stream)
                    .map(ItemStorage::new)
                    .distinct()
                    .sorted(Comparator.comparing(s -> ForgeRegistries.ITEMS.getKey(s.getItem()).toString()))
                    .map(ItemStorage::getItemStack)
                    .toList();
            for (final ItemStack skin : allSkins)
            {
                if (first)
                {
                    writeItemStack(writer, recipe.getPrimaryOutput());
                    first = false;
                }
                writer.write(',');

                writeItemData(writer, skin);

                for (final ICraftingBuildingModule crafter : crafters)
                {
                    writer.write(crafter.getIngredientValidator().test(skin).orElse(false) ? ",1" : ",");
                }

                writer.newLine();
            }
        }
    }

    private static void doToolsAudit(@NotNull final BufferedWriter writer,
                                     @NotNull final MinecraftServer server) throws IOException
    {
        final List<ToolUsage> toolUsages = ToolsAnalyzer.findTools();

        writeItemHeaders(writer);
        for (final ToolUsage tool : toolUsages)
        {
            writer.write(',');
            writer.write(tool.tool().getName());
        }
        writer.newLine();

        for (final ItemStack item : getAllItems())
        {
            writeItemData(writer, item);

            for (final ToolUsage tool : toolUsages)
            {
                writer.write(',');
                for (int level = 0; level < tool.toolLevels().size(); ++level)
                {
                    final List<ItemStack> stacks = tool.toolLevels().get(level);
                    if (ItemStackUtils.compareItemStackListIgnoreStackSize(stacks, item, false, true))
                    {
                        writer.write(Integer.toString(level));
                        break;
                    }
                }
            }

            writer.newLine();
        }
    }

    private static void writeItemHeaders(@NotNull final BufferedWriter writer) throws IOException
    {
        writer.write("item,name");
    }

    private static void writeItemData(@NotNull final BufferedWriter writer,
                                      @NotNull final ItemStack stack) throws IOException
    {
        writeItemStack(writer, stack);
        writer.write(",\"");
        writer.write(stack.getDisplayName().getString().replace("\"", "\"\""));
        writer.write('"');
    }

    private static void writeItemStack(@NotNull final BufferedWriter writer,
                                       @NotNull final ItemStack stack) throws IOException
    {
        writer.write('"');
        writer.write(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.hasTag() && !stack.isDamageableItem())
        {
            writer.write(stack.getTag().toString().replace("\"", "\"\""));
        }
        writer.write('"');
    }

    private static void writeCrafterValue(@NotNull final BufferedWriter writer,
                                          @NotNull final Map<Object, List<IGenericRecipe>> crafterMap,
                                          @Nullable final Object crafter) throws IOException
    {
        writer.write(',');

        final List<IGenericRecipe> recipeList = crafterMap.getOrDefault(crafter, Collections.emptyList());
        if (!recipeList.isEmpty())
        {
            writer.write(Integer.toString(recipeList.size()));
        }
    }

    private static void add(@NotNull final CustomRecipeManager customRecipeManager,
                            @NotNull final Map<ItemStorage, Map<Object, List<IGenericRecipe>>> craftingMap,
                            @Nullable final Object crafter,
                            @NotNull final IGenericRecipe recipe)
    {
        for (final ItemStack stack : recipe.getAllMultiOutputs())
        {
            add(craftingMap, crafter, recipe, stack);
        }

        if (recipe.getLootTable() != null)
        {
            for (final LootTableAnalyzer.LootDrop drop : customRecipeManager.getLootDrops(recipe.getLootTable()))
            {
                for (ItemStack stack : drop.getItemStacks())
                {
                    add(craftingMap, crafter, recipe, stack);
                }
            }
        }
    }

    private static void add(@NotNull final Map<ItemStorage, Map<Object, List<IGenericRecipe>>> craftingMap,
                            @Nullable final Object crafter,
                            @NotNull final IGenericRecipe recipe,
                            @NotNull final ItemStack stack)
    {
        craftingMap
                .computeIfAbsent(new ItemStorage(stack, true, false), s -> new HashMap<>())
                .computeIfAbsent(crafter, c -> new ArrayList<>())
                .add(recipe);
    }

    private static List<ICraftingBuildingModule> getCraftingModules()
    {
        final List<ICraftingBuildingModule> modules = new ArrayList<>();

        for (final String producerKey : BuildingEntry.getALlModuleProducers().keySet())
        {
            final var module = BuildingEntry.produceModuleWithoutBuilding(producerKey);

            if (module == null)
            {
                continue;
            }

            if (module instanceof ICraftingBuildingModule crafting)
            {
                modules.add(crafting);
            }
        }

        return modules;
    }

    private static List<AnimalHerdingModule> getHerdingModules()
    {
        final List<AnimalHerdingModule> modules = new ArrayList<>();

        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
          /*  for (final Supplier<IBuildingModule> producer : building.getModuleProducers())
            {
                final IBuildingModule module = producer.get();

                if (module instanceof AnimalHerdingModule herding)
                {
                    modules.add(herding);
                }
            }*/
        }

        return modules;
    }

    private CraftingTagAuditor()
    {
        /*
         * Intentionally left empty.
         */
    }
}
