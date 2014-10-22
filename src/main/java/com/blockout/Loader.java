package com.blockout;

import com.minecolonies.MineColonies;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringReader;
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
        register("button",      ButtonVanilla.class);
        register("label",       Label.class);
        register("textfield",   TextFieldVanilla.class);

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

    public static String makeFactorKey(String name, String style)
    {
        return name + ":" + (style != null ? style : "");
    }

    public static void register(String name, String style, Class<? extends Pane> paneClass)
    {
        String key = makeFactorKey(name, style);

        if (paneConstructorMap.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' of style '" + style + "' when registering Pane class mapping for " + paneClass.getName());
        }

        try
        {
            Constructor<? extends Pane> constructor = paneClass.getDeclaredConstructor(XMLNode.class);
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


    public static Pane createFromXML(XMLNode xml, View newParent)
    {
        //  Parse Attributes first, to full construct
        String paneType = xml.getName();
        String style = xml.getStringAttribute("style", null);

        String key = makeFactorKey(paneType, style);
        Constructor<? extends Pane> constructor = paneConstructorMap.get(key);
        if (constructor == null && style != null)
        {
            key = makeFactorKey(paneType, null);
            constructor = paneConstructorMap.get(key);
        }

        if (constructor != null)
        {
            try
            {
                Pane pane = (Pane)constructor.newInstance(xml);
                //  TODO - Decide on put in parent before add children (top down)
                //  TODO - or add children before put in parent? (bottom up)
                pane.putInside(newParent);
                pane.parseChildren(xml);
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

    public static void createFromXML(String xmlString, Window window)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

            doc.getDocumentElement().normalize();

            Node node = doc.getDocumentElement().getFirstChild();
            while (node != null)
            {
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    createFromXML(new XMLNode(node), window);
                }
                node = node.getNextSibling();
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            MineColonies.logger.error("Exception when parsing XML.", exc);
        }
    }

    public static int getColorByName(String name, int def)
    {
        Integer i = nameToColorMap.get(name.toLowerCase());
        return i != null ? i : def;
    }
}
