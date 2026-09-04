package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.items.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Generates simple generated-layer item models and the MC 26 item definitions which point to them.
 */
public class DefaultItemModelProvider implements DataProvider
{
    private static final Identifier DISABLED_GOGGLES = Identifier.fromNamespaceAndPath(MOD_ID, "build_goggles_disabled");
    private static final Identifier GOGGLES = Identifier.fromNamespaceAndPath(MOD_ID, "build_goggles");
    private static final Identifier GOGGLES_ITEM_DEFINITION = Identifier.fromNamespaceAndPath(MOD_ID, "build_goggles");

    private final PackOutput.PathProvider modelPathProvider;
    private final PackOutput.PathProvider itemPathProvider;

    public DefaultItemModelProvider(final PackOutput packOutput)
    {
        this.modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.itemPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public String getName()
    {
        return "MineColonies Item Models";
    }

    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final Map<Identifier, JsonObject> models = new LinkedHashMap<>();
        models.put(DISABLED_GOGGLES, generatedModel("item/build_goggles_disabled"));
        models.put(GOGGLES, gogglesWithOverride());

        for (final Item foodItem : ModItems.getAllIngredients())
        {
            models.put(key(foodItem), generatedModel("item/food/" + BuiltInRegistries.ITEM.getKey(foodItem).getPath()));
        }
        for (final Item foodItem : ModItems.getAllFoods())
        {
            models.put(key(foodItem), generatedModel("item/food/" + BuiltInRegistries.ITEM.getKey(foodItem).getPath()));
        }

        final Map<Identifier, JsonObject> itemDefinitions = new LinkedHashMap<>();
        itemDefinitions.put(GOGGLES_ITEM_DEFINITION, rangeDispatch(GOGGLES));
        itemDefinitions.put(Identifier.fromNamespaceAndPath(MOD_ID, "spear"), throwingSpearDefinition());
        for (final Identifier model : models.keySet())
        {
            if (!model.equals(GOGGLES))
            {
                itemDefinitions.put(definitionId(model), plainDefinition(model));
            }
        }

        // MC 26.2 resolves item visuals through assets/<namespace>/items/*.json
        // definitions.  The legacy MineColonies assets already contain item
        // models for the remaining registered items (including block items),
        // so expose each of them through the corresponding plain definition.
        // Keep the explicit definitions above for items with dispatch logic.
        for (final var entry : BuiltInRegistries.ITEM.entrySet())
        {
            final Identifier itemId = entry.getKey().identifier();
            if (MOD_ID.equals(itemId.getNamespace()))
            {
                final int[] spawnEggColors = spawnEggColors(itemId.getPath());
                itemDefinitions.putIfAbsent(itemId, spawnEggColors == null
                    ? plainDefinition(itemId)
                    : spawnEggDefinition(itemId, spawnEggColors));
            }
        }

        final List<CompletableFuture<?>> saves = new ArrayList<>();
        models.forEach((id, model) -> saves.add(DataProvider.saveStable(cache, model, modelPathProvider.json(id))));
        itemDefinitions.forEach((id, definition) -> saves.add(DataProvider.saveStable(cache, definition, itemPathProvider.json(id))));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static Identifier key(final Item item)
    {
        return BuiltInRegistries.ITEM.wrapAsHolder(item).getKey().identifier();
    }

    private static Identifier definitionId(final Identifier model)
    {
        return Identifier.fromNamespaceAndPath(model.getNamespace(), model.getPath());
    }

    private static JsonObject generatedModel(final String texturePath)
    {
        final JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");
        final JsonObject textures = new JsonObject();
        textures.addProperty("layer0", Identifier.fromNamespaceAndPath(MOD_ID, texturePath).toString());
        model.add("textures", textures);
        return model;
    }

    private static JsonObject gogglesWithOverride()
    {
        final JsonObject model = generatedModel("item/build_goggles");
        return model;
    }

    private static JsonObject plainDefinition(final Identifier model)
    {
        final JsonObject definition = new JsonObject();
        definition.add("model", modelReference(itemModel(model)));
        return definition;
    }

    /**
     * Restores the two-layer spawn egg appearance used by the pre-26.2 assets.
     * Minecraft 26.2 moved the tint values out of the item and into its item
     * definition, so the old generic spawn-egg model needs to supply them here.
     */
    private static JsonObject spawnEggDefinition(final Identifier model, final int[] colors)
    {
        final JsonObject definition = plainDefinition(model);
        final JsonArray tints = new JsonArray();
        tints.add(constantTint(colors[0]));
        tints.add(constantTint(colors[1]));
        definition.getAsJsonObject("model").add("tints", tints);
        return definition;
    }

    private static JsonObject constantTint(final int color)
    {
        final JsonObject tint = new JsonObject();
        tint.addProperty("type", "minecraft:constant");
        tint.addProperty("value", color);
        return tint;
    }

    private static int[] spawnEggColors(final String itemId)
    {
        return switch (itemId)
        {
            case "barbarianegg" -> new int[]{0xFFA500, 0x000000};
            case "barbarcheregg" -> new int[]{0xFFA500, 0x008000};
            case "barbchiefegg" -> new int[]{0xFFA500, 0xFFFF00};
            case "pirateegg" -> new int[]{0xFF0000, 0xFFFFFF};
            case "piratearcheregg" -> new int[]{0xFF0000, 0x008000};
            case "piratecaptainegg" -> new int[]{0xFF0000, 0xFFFF00};
            case "mummyegg" -> new int[]{0xFFFF00, 0xFFFFFF};
            case "mummyarcheregg" -> new int[]{0xFFFF00, 0x008000};
            case "pharaoegg" -> new int[]{0xFFFF00, 0xFFFF00};
            case "shieldmaidenegg" -> new int[]{0x000000, 0xFFFFFF};
            case "norsemenarcheregg" -> new int[]{0x000000, 0x008000};
            case "norsemenchiefegg" -> new int[]{0x000000, 0xFFFF00};
            case "amazonegg" -> new int[]{0x008000, 0xFFFFFF};
            case "amazonspearmanegg" -> new int[]{0x008000, 0x008000};
            case "amazonchiefegg" -> new int[]{0x008000, 0xFFFF00};
            case "drownedpirateegg" -> new int[]{0x0000FF, 0xFFFFFF};
            case "drownedpiratearcheregg" -> new int[]{0x0000FF, 0x008000};
            case "drownedpiratecaptainegg" -> new int[]{0x0000FF, 0xFFFF00};
            default -> null;
        };
    }

    private static Identifier itemModel(final Identifier itemId)
    {
        return itemId.getPath().startsWith("item/")
            ? itemId
            : Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
    }

    private static JsonObject modelReference(final Identifier model)
    {
        final JsonObject reference = new JsonObject();
        reference.addProperty("type", "minecraft:model");
        reference.addProperty("model", model.toString());
        return reference;
    }

    private static JsonObject rangeDispatch(final Identifier disabledModel)
    {
        final JsonObject definition = new JsonObject();
        final JsonObject dispatch = new JsonObject();
        dispatch.addProperty("type", "minecraft:range_dispatch");

        dispatch.addProperty("property", "minecraft:custom_model_data");
        dispatch.addProperty("index", 0);

        final JsonArray entries = new JsonArray();
        final JsonObject entry = new JsonObject();
        entry.addProperty("threshold", 1.0F);
        entry.add("model", modelReference(itemModel(disabledModel)));
        entries.add(entry);
        dispatch.add("entries", entries);
        dispatch.add("fallback", modelReference(itemModel(GOGGLES)));
        definition.add("model", dispatch);
        return definition;
    }

    private static JsonObject throwingSpearDefinition()
    {
        final JsonObject definition = new JsonObject();
        final JsonObject displayContext = new JsonObject();
        displayContext.addProperty("type", "minecraft:select");

        final JsonArray cases = new JsonArray();
        final JsonObject flatCase = new JsonObject();
        flatCase.add("model", modelReference(Identifier.fromNamespaceAndPath(MOD_ID, "item/spear_gui")));
        final JsonArray flatContexts = new JsonArray();
        flatContexts.add("gui");
        flatContexts.add("ground");
        flatContexts.add("fixed");
        flatContexts.add("on_shelf");
        flatCase.add("when", flatContexts);
        cases.add(flatCase);
        displayContext.add("cases", cases);
        displayContext.add("fallback", throwingSpearHandModel());
        displayContext.addProperty("property", "minecraft:display_context");
        definition.add("model", displayContext);
        return definition;
    }

    private static JsonObject throwingSpearHandModel()
    {
        final JsonObject definition = new JsonObject();
        definition.addProperty("type", "minecraft:condition");
        definition.add("on_false", specialSpearModel("item/spear_in_hand"));
        definition.add("on_true", specialSpearModel("item/spear_throwing"));
        definition.addProperty("property", "minecraft:using_item");
        return definition;
    }

    private static JsonObject specialSpearModel(final String baseModel)
    {
        final JsonObject definition = new JsonObject();
        definition.addProperty("type", "minecraft:special");
        definition.addProperty("base", Identifier.fromNamespaceAndPath(MOD_ID, baseModel).toString());
        final JsonObject renderer = new JsonObject();
        renderer.addProperty("type", MOD_ID + ":spear");
        definition.add("model", renderer);
        return definition;
    }
}
