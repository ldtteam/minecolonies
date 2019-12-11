package com.minecolonies.api.colony.interactionhandling;

import com.google.gson.*;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Custom ITextComponentSerializer.
 */
public class CustomITextComponentSerializer implements JsonDeserializer<ITextComponent>, JsonSerializer<ITextComponent>
{
    /**
     * Serializer for the ITextComponents.
     */
    public static final CustomITextComponentSerializer SERIALIZER = new CustomITextComponentSerializer();

    /**
     * Gson serializer.
     */
    private final Gson gson;

    /**
     * Private constructor, no need for instantiation.
     */
    private CustomITextComponentSerializer()
    {
        super();
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.registerTypeHierarchyAdapter(ITextComponent.class, this);
        gsonbuilder.registerTypeHierarchyAdapter(Style.class, new net.minecraft.util.text.Style.Serializer());
        gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
        gson = gsonbuilder.create();
    }

    @Override
    public ITextComponent deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonPrimitive())
        {
            return new TextComponentString(json.getAsString());
        }
        else if (!json.isJsonObject())
        {
            if (json.isJsonArray())
            {
                JsonArray jsonArray = json.getAsJsonArray();
                ITextComponent textComponent = null;

                for (final JsonElement jsonelement : jsonArray)
                {
                    ITextComponent subComponent = this.deserialize(jsonelement, jsonelement.getClass(), context);
                    if (textComponent == null)
                    {
                        textComponent = subComponent;
                    }
                    else
                    {
                        textComponent.appendSibling(subComponent);
                    }
                }

                return textComponent;
            }
            else
            {
                throw new JsonParseException("Don't know how to turn " + json + " into a Component");
            }
        }
        else
        {
            JsonObject jsonobject = json.getAsJsonObject();
            ITextComponent itextcomponent;
            if (jsonobject.has("text"))
            {
                itextcomponent = new TextComponentString(jsonobject.get("text").getAsString());
            }
            else if (jsonobject.has("translate"))
            {
                String s = jsonobject.get("translate").getAsString();
                if (jsonobject.has("with"))
                {
                    JsonArray jsonarray = jsonobject.getAsJsonArray("with");
                    Object[] args = new Object[jsonarray.size()];

                    for (int i = 0; i < args.length; ++i)
                    {
                        args[i] = this.deserialize(jsonarray.get(i), type, context);
                        if (args[i] instanceof TextComponentString)
                        {
                            TextComponentString textcomponentstring = (TextComponentString) args[i];
                            if (textcomponentstring.getStyle().isEmpty() && textcomponentstring.getSiblings().isEmpty())
                            {
                                args[i] = textcomponentstring.getText();
                            }
                        }
                    }

                    itextcomponent = new TranslationTextComponent(s, args);
                }
                else
                {
                    itextcomponent = new TranslationTextComponent(s);
                }
            }
            else if (jsonobject.has("score"))
            {
                JsonObject jsonObj = jsonobject.getAsJsonObject("score");
                if (!jsonObj.has("name") || !jsonObj.has("objective"))
                {
                    throw new JsonParseException("A score component needs a least a name and an objective");
                }

                itextcomponent = new TextComponentScore(JsonUtils.getString(jsonObj, "name"), JsonUtils.getString(jsonObj, "objective"));
                if (jsonObj.has("value"))
                {
                    ((TextComponentScore) itextcomponent).setValue(JsonUtils.getString(jsonObj, "value"));
                }
            }
            else if (jsonobject.has("selector"))
            {
                itextcomponent = new TextComponentSelector(JsonUtils.getString(jsonobject, "selector"));
            }
            else
            {
                if (!jsonobject.has("keybind"))
                {
                    throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                }

                itextcomponent = new TextComponentKeybind(JsonUtils.getString(jsonobject, "keybind"));
            }

            if (jsonobject.has("extra"))
            {
                JsonArray jsonArray = jsonobject.getAsJsonArray("extra");
                if (jsonArray.size() <= 0)
                {
                    throw new JsonParseException("Unexpected empty array of components");
                }

                for (int j = 0; j < jsonArray.size(); ++j)
                {
                    itextcomponent.appendSibling(this.deserialize(jsonArray.get(j), type, context));
                }
            }

            itextcomponent.setStyle(context.deserialize(json, Style.class));
            return itextcomponent;
        }
    }

    private void serializeChatStyle(Style style, JsonObject object, JsonSerializationContext ctx)
    {
        JsonElement jsonelement = ctx.serialize(style);
        if (jsonelement.isJsonObject())
        {
            JsonObject jsonobject = (JsonObject) jsonelement;
            for (final Map.Entry<String, JsonElement> stringJsonElementEntry : jsonobject.entrySet())
            {
                object.add(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue());
            }
        }
    }

    @Override
    public JsonElement serialize(final ITextComponent component, final Type type, final JsonSerializationContext context)
    {
        JsonObject jsonobject = new JsonObject();
        if (!component.getStyle().isEmpty())
        {
            this.serializeChatStyle(component.getStyle(), jsonobject, context);
        }

        if (!component.getSiblings().isEmpty())
        {
            final JsonArray jsonarray = new JsonArray();

            for (final ITextComponent itextcomponent : component.getSiblings())
            {
                jsonarray.add(this.serialize(itextcomponent, itextcomponent.getClass(), context));
            }

            jsonobject.add("extra", jsonarray);
        }

        if (component instanceof TextComponentString)
        {
            jsonobject.addProperty("text", ((TextComponentString) component).getText());
        }
        else if (component instanceof TranslationTextComponent)
        {
            TranslationTextComponent TranslationTextComponent = (TranslationTextComponent) component;
            jsonobject.addProperty("translate", TranslationTextComponent.getKey());
            TranslationTextComponent.getFormatArgs();
            if (TranslationTextComponent.getFormatArgs().length > 0)
            {
                JsonArray jsonElements = new JsonArray();
                Object[] formatArgs = TranslationTextComponent.getFormatArgs();

                for (final Object arg : formatArgs)
                {
                    if (arg instanceof ITextComponent)
                    {
                        jsonElements.add(this.serialize((ITextComponent) (arg), arg.getClass(), context));
                    }
                    else
                    {
                        jsonElements.add(new JsonPrimitive(String.valueOf(arg)));
                    }
                }

                jsonobject.add("with", jsonElements);
            }
        }
        else if (component instanceof TextComponentScore)
        {
            TextComponentScore textcomponentscore = (TextComponentScore) component;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", textcomponentscore.getName());
            jsonObject.addProperty("objective", textcomponentscore.getObjective());
            jsonObject.addProperty("value", textcomponentscore.getUnformattedComponentText());
            jsonobject.add("score", jsonObject);
        }
        else if (component instanceof TextComponentSelector)
        {
            TextComponentSelector textcomponentselector = (TextComponentSelector) component;
            jsonobject.addProperty("selector", textcomponentselector.getSelector());
        }
        else
        {
            if (!(component instanceof TextComponentKeybind))
            {
                throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
            }

            TextComponentKeybind textcomponentkeybind = (TextComponentKeybind) component;
            jsonobject.addProperty("keybind", textcomponentkeybind.getKeybind());
        }

        return jsonobject;
    }

    public static String componentToJson(ITextComponent component)
    {
        return SERIALIZER.gson.toJson(component);
    }

    @Nullable
    public static ITextComponent jsonToComponent(String json)
    {
        return JsonUtils.gsonDeserialize(SERIALIZER.gson, json, ITextComponent.class, false);
    }

    @Nullable
    public static ITextComponent fromJsonLenient(String json)
    {
        return JsonUtils.gsonDeserialize(SERIALIZER.gson, json, ITextComponent.class, true);
    }
}
