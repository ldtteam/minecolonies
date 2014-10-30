package com.blockout;

import com.blockout.controls.*;
import com.blockout.views.Group;
import com.blockout.views.ScrollingList;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Loader
{
    private static Map<String, Constructor<? extends Pane>> paneConstructorMap = new HashMap<String, Constructor<? extends Pane>>();
    private static Map<String, Integer> nameToColorMap = new HashMap<String, Integer>();

    static
    {
        register("view",        View.class);
        register("group",       Group.class);
        register("list",        ScrollingList.class);
        register("button",      ButtonVanilla.class);
        register("label",       Label.class);
        register("text",        TextFieldVanilla.class);
        register("textfield",   TextFieldVanilla.class);    //  Alternate name
        register("field",       TextFieldVanilla.class);    //  Alternate name
        register("image",       Image.class);
        register("box",         Box.class);

        //  Would love to load these from a file
        nameToColorMap.put("aqua",      0x00FFFF);
        nameToColorMap.put("black",     0x000000);
        nameToColorMap.put("blue",      0x0000FF);
        nameToColorMap.put("cyan",      0x00FFFF);
        nameToColorMap.put("fuchsia",   0xFF00FF);
        nameToColorMap.put("green",     0x008000);
        nameToColorMap.put("ivory",     0xFFFFF0);
        nameToColorMap.put("lime",      0x00FF00);
        nameToColorMap.put("magenta",   0xFF00FF);
        nameToColorMap.put("orange",    0xFFA500);
        nameToColorMap.put("orangered", 0xFF4500);
        nameToColorMap.put("purple",    0x800080);
        nameToColorMap.put("red",       0xFF0000);
        nameToColorMap.put("white",     0xFFFFFF);
        nameToColorMap.put("yellow",    0xFFFF00);
    }

    public static String makeFactoryKey(String name, String style)
    {
        return name + ":" + (style != null ? style : "");
    }

    public static void register(String name, String style, Class<? extends Pane> paneClass)
    {
        String key = makeFactoryKey(name, style);

        if (paneConstructorMap.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' of style '" + style + "' when registering Pane class mapping for " + paneClass.getName());
        }

        try
        {
            Constructor<? extends Pane> constructor = paneClass.getDeclaredConstructor(PaneParams.class);
            paneConstructorMap.put(key, constructor);
        }
        catch (NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing (XMLNode) constructor for type '" + name + "' when adding Pane class mapping for " + paneClass.getName());
        }
    }

    public static void register(String name, Class<? extends Pane> paneClass)
    {
        register(name, null, paneClass);
    }


    public static Pane createFromPaneParams(PaneParams params)
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
                Pane pane = (Pane)constructor.newInstance(params);
                return pane;
            }
            catch (Exception exc)
            {
                exc.printStackTrace();
                MineColonies.logger.error("Exception when parsing XML.", exc);
            }
        }

        return null;
    }

    public static Pane createFromPaneParams(PaneParams params, View parent)
    {
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
            String inherit = root.getStringAttribute("inherit", null);
            if (inherit != null)
            {
                createFromXMLFile(new ResourceLocation(inherit), parent);
            }

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
    public static void createFromXML(InputSource input, View parent)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);

            createFromXML(doc, parent);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            MineColonies.logger.error("Exception when parsing XML.", exc);
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
        createFromXML(new InputSource(getStream(resource)), parent);
    }

    private static InputStream getStream(ResourceLocation res)
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
            e.printStackTrace();
        }
        return null;
    }

    public static int getColorByName(String name, int def)
    {
        Integer i = nameToColorMap.get(name.toLowerCase());
        return i != null ? i : def;
    }
}
