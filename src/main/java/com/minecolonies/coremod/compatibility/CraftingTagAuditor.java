package com.minecolonies.coremod.compatibility;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.modules.SimpleCraftingModule;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.colony.crafting.RecipeAnalyzer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * This class analyzes the current crafting tags and generates a CSV file reporting on which items can be crafted by
 * which crafter, as a way to verify that all intended items are covered by the appropriate crafters (or not).  It
 * also produces another file that just contains all tags for each item, both to check the crafting tags themselves
 * and to help pick appropriate tags to add to the crafting tags if something is missing.
 *
 * The logic of evaluating recipes closely resembles {@link com.minecolonies.coremod.compatibility.jei.JEIPlugin}, but
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
        createFile("tag audit", server, "tag_audit.csv", writer -> doTagAudit(writer, server));
        createFile("recipe audit", server, "recipe_audit.csv", writer -> doRecipeAudit(writer, server, customRecipeManager));
        createFile("domum audit", server, "domum_audit.csv", writer -> doDomumAudit(writer, server));
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
        items.sort(Comparator.comparing(stack -> stack.getItem().getRegistryName().toString()));
        return items;
    }

    private static void doTagAudit(@NotNull final BufferedWriter writer,
                                   @NotNull final MinecraftServer server) throws IOException
    {
        writeItemHeaders(writer);
        writer.write(",tags...");
        writer.newLine();

        for (final ItemStack item : getAllItems())
        {
            writeItemData(writer, item);

            for (final ResourceLocation tag : item.getItem().getTags())
            {
                writer.write(',');
                writer.write(tag.toString());
            }
            writer.newLine();
        }
    }

    private static void doRecipeAudit(@NotNull final BufferedWriter writer,
                                      @NotNull final MinecraftServer server,
                                      @NotNull final CustomRecipeManager customRecipeManager) throws IOException
    {
        final Map<CraftingType, List<IGenericRecipe>> vanillaRecipesMap =
                RecipeAnalyzer.buildVanillaRecipesMap(server.getRecipeManager(), server.overworld());
        final List<ICraftingBuildingModule> crafters = getCraftingModules()
                .stream()
                .sorted(Comparator.comparing(m -> m instanceof SimpleCraftingModule).reversed())
                .collect(Collectors.toList());  // sort the simple modules first (2x2 crafting, personal only)
        final Map<ItemStorage, Map<ICraftingBuildingModule, List<IGenericRecipe>>> craftingMap = new HashMap<>();

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

            final List<IGenericRecipe> recipes = RecipeAnalyzer.findRecipes(vanillaRecipesMap, crafter);
            for (final IGenericRecipe recipe : recipes)
            {
                add(customRecipeManager, craftingMap, crafter, recipe);
            }
        }
        writer.newLine();

        for (final ItemStack item : getAllItems())
        {
            writeItemData(writer, item);

            final Map<ICraftingBuildingModule, List<IGenericRecipe>> crafterMap =
                    craftingMap.getOrDefault(new ItemStorage(item, true, false), Collections.emptyMap());

            writeCrafterValue(writer, crafterMap, null);
            for (final ICraftingBuildingModule crafter : crafters)
            {
                writeCrafterValue(writer, crafterMap, crafter);
            }
            writer.newLine();
        }
    }

    private static void doDomumAudit(@NotNull final BufferedWriter writer,
                                     @NotNull final MinecraftServer server) throws IOException
    {
        final List<IGenericRecipe> cutterRecipes = new ArrayList<>(ModCraftingTypes.ARCHITECTS_CUTTER.findRecipes(server.getRecipeManager(), server.overworld()));
        cutterRecipes.sort(Comparator.comparing(r -> r.getPrimaryOutput().getItem().getRegistryName().toString()));
        final List<ICraftingBuildingModule> crafters = getCraftingModules()
                .stream()
                .filter(m -> m.canLearn(ModCraftingTypes.ARCHITECTS_CUTTER))
                .collect(Collectors.toList());

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
                    .sorted(Comparator.comparing(s -> s.getItem().getRegistryName().toString()))
                    .map(ItemStorage::getItemStack)
                    .collect(Collectors.toList());
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
        writer.write(stack.getItem().getRegistryName().toString());
        if (stack.hasTag() && !stack.isDamageableItem())
        {
            writer.write(stack.getTag().toString().replace("\"", "\"\""));
        }
        writer.write('"');
    }

    private static void writeCrafterValue(@NotNull final BufferedWriter writer,
                                          @NotNull final Map<ICraftingBuildingModule, List<IGenericRecipe>> crafterMap,
                                          @Nullable final ICraftingBuildingModule crafter) throws IOException
    {
        writer.write(',');

        final List<IGenericRecipe> recipeList = crafterMap.getOrDefault(crafter, Collections.emptyList());
        if (!recipeList.isEmpty())
        {
            writer.write(Integer.toString(recipeList.size()));
        }
    }

    private static void add(@NotNull final CustomRecipeManager customRecipeManager,
                            @NotNull final Map<ItemStorage, Map<ICraftingBuildingModule, List<IGenericRecipe>>> craftingMap,
                            @Nullable final ICraftingBuildingModule crafter,
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

    private static void add(@NotNull final Map<ItemStorage, Map<ICraftingBuildingModule, List<IGenericRecipe>>> craftingMap,
                            @Nullable final ICraftingBuildingModule crafter,
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

        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            for (final Supplier<IBuildingModule> producer : building.getModuleProducers())
            {
                final IBuildingModule module = producer.get();

                if (module instanceof ICraftingBuildingModule)
                {
                    modules.add((ICraftingBuildingModule) module);
                }
            }
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
