package com.minecolonies.blockout;

import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to load xml files.
 */
public final class Loader
{
    private static final Map<String, Constructor<? extends Pane>> paneConstructorMap = new HashMap<>();
    static
    {
        register("view", View.class);
        register("group", Group.class);
        register("scrollgroup", ScrollingGroup.class);
        register("list", ScrollingList.class);
        register("text", Text.class);
        register("button", ButtonVanilla.class);
        register("buttonimage", ButtonImage.class);
        register("label", Label.class);
        register("input", TextFieldVanilla.class);
        register("image", Image.class);
        register("box", Box.class);
        register("itemicon", ItemIcon.class);
        register("switch", SwitchView.class);
        register("dropdown", DropDownList.class);
        register("overlay", OverlayView.class);
        register("gradient", Gradient.class);
    }
    private Loader()
    {
        // Hides default constructor.
    }

    private static void register(final String name, final Class<? extends Pane> paneClass)
    {
        register(name, null, paneClass);
    }

    private static void register(final String name, final String style, final Class<? extends Pane> paneClass)
    {
        final String key = makeFactoryKey(name, style);

        if (paneConstructorMap.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '"
                                                 + name + "' of style '"
                                                 + style + "' when registering Pane class mapping for "
                                                 + paneClass.getName());
        }

        try
        {
            final Constructor<? extends Pane> constructor = paneClass.getDeclaredConstructor(PaneParams.class);
            paneConstructorMap.put(key, constructor);
        }
        catch (final NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing (XMLNode) constructor for type '"
                                                 + name + "' when adding Pane class mapping for " + paneClass.getName(), exception);
        }
    }

    @NotNull
    private static String makeFactoryKey(final String name, final String style)
    {
        return name + ":" + (style != null ? style : "");
    }

    private static Pane createFromPaneParams(final PaneParams params)
    {
        //  Parse Attributes first, to full construct
        final String paneType = params.getType();
        final String style = params.getStringAttribute("style", null);

        String key = makeFactoryKey(paneType, style);
        Constructor<? extends Pane> constructor = paneConstructorMap.get(key);
        if (constructor == null && style != null)
        {
            key = makeFactoryKey(paneType, null);
            constructor = paneConstructorMap.get(key);
        }

        if (constructor != null)
        {
            try
            {
                return constructor.newInstance(params);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException exc)
            {
                Log.getLogger().error(
                  String.format("Exception when parsing XML for pane type %s", paneType),
                  exc);
            }
        }

        return null;
    }

    /**
     * Create a pane from its xml parameters.
     *
     * @param params xml parameters.
     * @param parent parent view.
     * @return the new pane.
     */
    public static Pane createFromPaneParams(final PaneParams params, final View parent)
    {
        if ("layout".equalsIgnoreCase(params.getType()))
        {
            final String resource = params.getStringAttribute("source", null);
            if (resource != null)
            {
                createFromXMLFile(resource, parent);
            }

            return null;
        }

        params.setParentView(parent);
        final Pane pane = createFromPaneParams(params);

        if (pane != null)
        {
            pane.putInside(parent);
            pane.parseChildren(params);
        }

        return pane;
    }

    /**
     * Parse an XML Document into contents for a View.
     *
     * @param doc    xml document.
     * @param parent parent view.
     */
    private static void createFromXML(final Document doc, final View parent)
    {
        doc.getDocumentElement().normalize();

        final PaneParams root = new PaneParams(doc.getDocumentElement());
        if (parent instanceof Window)
        {
            ((Window) parent).loadParams(root);
        }

        for (final PaneParams child : root.getChildren())
        {
            createFromPaneParams(child, parent);
        }
    }

    /**
     * Parse XML from an InputSource into contents for a View.
     *
     * @param input  xml file.
     * @param parent parent view.
     */
    private static void createFromXML(final InputSource input, final View parent)
    {
        try
        {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(input);

            createFromXML(doc, parent);
        }
        catch (ParserConfigurationException | SAXException | IOException exc)
        {
            Log.getLogger().error("Exception when parsing XML.", exc);
        }
    }

    /**
     * Parse an XML String into contents for a View.
     *
     * @param xmlString the xml data.
     * @param parent    parent view.
     */
    public static void createFromXML(final String xmlString, final View parent)
    {
        createFromXML(new InputSource(new StringReader(xmlString)), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window.
     *
     * @param filename the xml file.
     * @param parent   parent view.
     */
    public static void createFromXMLFile(final String filename, final View parent)
    {
        createFromXMLFile(new ResourceLocation(filename), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window.
     *
     * @param resource xml as a {@link ResourceLocation}.
     * @param parent   parent view.
     */
    public static void createFromXMLFile(final ResourceLocation resource, final View parent)
    {
        createFromXML(new InputSource(createInputStream(resource)), parent);
    }

    /**
     * Create an InputStream from a ResourceLocation.
     *
     * @param res ResourceLocation to get an InputStream from.
     * @return the InputStream created from the ResourceLocation.
     */
    private static InputStream createInputStream(final ResourceLocation res)
    {
        try
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                return Loader.class.getResourceAsStream(String.format("/assets/%s/%s", res.getNamespace(), res.getPath()));
            }
        }
        catch (final IOException e)
        {
            Log.getLogger().error("IOException Loader.java", e);
        }
        return null;
    }
}
