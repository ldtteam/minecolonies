package com.blockout;

import com.blockout.controls.*;
import com.blockout.views.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class Loader
{
    public static Logger logger = LogManager.getLogger("BlockOut");

    private static Map<String, Constructor<? extends Pane>> paneConstructorMap = new HashMap<>();

    static
    {
        register("view", View.class);
        register("group", Group.class);
        register("scrollgroup", ScrollingGroup.class);
        register("list", ScrollingList.class);
        register("textContent", Text.class);
        register("button", ButtonVanilla.class);
        register("buttonimage", ButtonImage.class);
        register("textContent", Label.class);
        register("input", TextFieldVanilla.class);
        register("image", Image.class);
        register("box", Box.class);
        register("itemicon", ItemIcon.class);
        register("switch", SwitchView.class);
    }

    private static String makeFactoryKey(String name, String style)
    {
        return name + ":" + (style != null ? style : "");
    }

    private static void register(String name, String style, Class<? extends Pane> paneClass)
    {
        String key = makeFactoryKey(name, style);

        if (paneConstructorMap.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '"
                                               + name + "' of style '"
                                               + style + "' when registering Pane class mapping for "
                                               + paneClass.getName());
        }

        try
        {
            Constructor<? extends Pane> constructor = paneClass.getDeclaredConstructor(PaneParams.class);
            paneConstructorMap.put(key, constructor);
        }
        catch (NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing (XMLNode) constructor for type '"
                                               + name + "' when adding Pane class mapping for " + paneClass.getName());
        }
    }

    private static void register(String name, Class<? extends Pane> paneClass)
    {
        register(name, null, paneClass);
    }


    private static Pane createFromPaneParams(PaneParams params)
    {
        //  Parse Attributes first, to full construct
        String paneType = params.getType();
        String style = params.getStringAttribute("style", null);

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
                logger.error(
                        String.format("Exception when parsing XML for pane type %s", paneType),
                        exc);
            }
        }

        return null;
    }

    public static Pane createFromPaneParams(PaneParams params, View parent)
    {
        if (params.getType().equalsIgnoreCase("layout"))
        {
            String resource = params.getStringAttribute("source", null);
            if (resource != null)
            {
                createFromXMLFile(resource, parent);
            }

            return null;
        }

        params.setParentView(parent);
        Pane pane = createFromPaneParams(params);

        if (pane != null)
        {
            pane.putInside(parent);
            pane.parseChildren(params);
        }

        return pane;
    }

    /**
     * Parse an XML Document into contents for a View
     * @param doc
     * @param parent
     */
    private static void createFromXML(Document doc, View parent)
    {
        doc.getDocumentElement().normalize();

        PaneParams root = new PaneParams(doc.getDocumentElement());
        if (parent instanceof Window)
        {
            ((Window)parent).loadParams(root);
        }

        for (PaneParams child : root.getChildren())
        {
            createFromPaneParams(child, parent);
        }
    }

    /**
     * Parse XML from an InputSource into contents for a View
     *
     * @param input
     * @param parent
     */
    private static void createFromXML(InputSource input, View parent)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);

            createFromXML(doc, parent);
        }
        catch (ParserConfigurationException | SAXException | IOException exc)
        {
            logger.error("Exception when parsing XML.", exc);
        }
    }

    /**
     * Parse an XML String into contents for a View
     *
     * @param xmlString
     * @param parent
     */
    public static void createFromXML(String xmlString, View parent)
    {
        createFromXML(new InputSource(new StringReader(xmlString)), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window
     *
     * @param filename
     * @param parent
     */
    public static void createFromXMLFile(String filename, View parent)
    {
        createFromXMLFile(new ResourceLocation(filename), parent);
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window
     *
     * @param resource
     * @param parent
     */
    public static void createFromXMLFile(ResourceLocation resource, View parent)
    {
        createFromXML(new InputSource(createInputStream(resource)), parent);
    }

    /**
     * Create an InputStream from a ResourceLocation
     *
     * @param res
     * @return
     */
    private static InputStream createInputStream(ResourceLocation res)
    {
        try
        {
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                return Loader.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
        }
        catch(IOException e)
        {
            logger.error("IOException Loader.java", e);
        }
        return null;
    }
}
