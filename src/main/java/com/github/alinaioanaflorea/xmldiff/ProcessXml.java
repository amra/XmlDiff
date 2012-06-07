/*
Copyright (C) 2012 Alina Ioana Florea (alina.ioana.florea@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

http://www.opensource.org/licenses/mit-license.php
 */

package com.github.alinaioanaflorea.xmldiff;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * This class can:
 * - convert the content of an XML file to an ArrayList of Tag structures
 * - print the content of an ArrayList of Tag structures to the standard output and into a given file, if specified, overwriting it
 */
public class ProcessXml
{
    /**
     *  Get the content of the input file as a Document.
     *
     * @param  filePath the path to a file
     * @return Document the content of the input file as a Document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    static public Document getDocument (String filePath) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(new File(filePath));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Get the content of the input file as an ArrayList of Tag structures.
     *
     * @param  filePath the path to a file
     * @return the content of the input file as an ArrayList of Tag structures
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public List<Tag> getTags (String filePath) throws SAXException, IOException, ParserConfigurationException
    {
        Node rootTag = getDocument(filePath).getChildNodes().item(0);
        return getTags(rootTag);
    }

    /**
     * Get the content of a tag as an ArrayList of Tag structures.
     *
     * @param tag the Node tag to be processed
     * @return the content of the input Node tag as an ArrayList of Tag structures
     */
    private List<Tag> getTags (Node tag)  
    {
        List<Tag> tags = new ArrayList<Tag>();

        // Build the tag's attributes
        List<Tag> tagAttributes = getAttributes(tag);
        
        // Build the tags's child tags
        List<Tag> childtags = new ArrayList<Tag>();
        NodeList nodeChildTags = tag.getChildNodes();
                         
        int totalChildTags = nodeChildTags.getLength();
        for (int i = 0; i < totalChildTags ; i++)
        {
            Node childTag = nodeChildTags.item(i);

            if (childTag.getNodeType() == Node.ELEMENT_NODE) 
            {                  
                List<Tag> childAtributes = getAttributes(childTag);

                String childValue = childTag.getNodeValue();
                List<Tag> grandChildTags = new ArrayList<Tag>();
                
                int nrOfGrandChildTags = childTag.getChildNodes().getLength();
                if (nrOfGrandChildTags > 1) 
                {      
                    grandChildTags = getTags(childTag).get(0).childTags;
                } 
                else if (nrOfGrandChildTags == 1)
                {
                    // The tag has only a value in it
                    childValue = childTag.getChildNodes().item(0).getNodeValue();
                }
                
                if (childValue == null)
                {
                    childValue = "";
                }
                else
                {
                    // Trim/remove leading and trailing white spaces
                    childValue = childValue.trim();
                }
                
                childtags.add( new Tag(childTag.getNodeName(), childAtributes, childValue, grandChildTags) );
            }                 
        }

        String tagValue = tag.getNodeValue();
        if (tagValue == null)
        {
            tagValue = "";
        }
        else
        {
            tagValue = tagValue.trim();
        }
        
        tags.add( new Tag(tag.getNodeName(), tagAttributes, tagValue, childtags) );

        return tags;
    }

    /**
     * Get the attributes of a tag.
     *
     * @param tag the Node tag to be processed
     * @return the attributes of the input Node tag as an ArrayList of Tag structures
     */
    private List<Tag> getAttributes(Node tag)
    {
        List<Tag> attributes = new ArrayList<Tag>();
        NamedNodeMap nodeAttributes = tag.getAttributes();
           
        int nrOfAttributes = nodeAttributes.getLength();
        for (int i = 0; i < nrOfAttributes; i++) 
        {
            Attr attr = (Attr)nodeAttributes.item(i);

            attributes.add( new Tag(attr.getNodeName(), new ArrayList<Tag>(), attr.getNodeValue(), new ArrayList<Tag>()) );
        }

        return attributes;
    }

    /**
     * Print the content of an ArrayList of Tag structures to the standard output and to a file.
     * If specified the file will be overwritten if it already exists.
     *
     * @param tags the ArrayList of Tag structures to be printed
     * @param fileName the name of the file in which the input list should be printed
     * @throws IOException
     */
    public void print (List<Tag> tags, String fileName) throws IOException
    {
        String content;

        if (!XmlDiff.isDebugBuild)
        {    
            // Release build: get only the differences and the mandatory tags and their parents   
            content = getXmlDiffString(tags, "", false); 
        }
        else
        {
            // Debug build: get similarities, differences and the mandatory tags and their parents
            content = getXmlString(tags, ""); 
        }

        if (!content.equals(""))
        {
            content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + content;
        }
        else
        {
            content = "No changes! \n";
        }
               
        // Print to the standard output
        System.out.println(content);
        
        // Print to a file, if given
        if (!fileName.equals(""))
        {
            printToFile(fileName, content);
        }
    }

    /**
     * Print the content of a List of Tag structures to a file, overwritten if it already exists.
     *
     * @param fileName the file to write to
     * @param content  the content to be written into the file
     * @throws IOException
     */
    static public void printToFile (String fileName, String content) throws IOException
    {
        FileOutputStream file = new FileOutputStream(fileName);
        Writer out = new OutputStreamWriter(file, "UTF8");

        out.write(content);
        out.close();
    }

    /**
     * Convert an ArrayList of Tags to a String, taking only the differences and mandatory tag
     *
     * @param tags                 the ArrayList to be converted to a String format
     * @param indent               the indentation for the current tag level
     * @param parentIsDelNewOrMand specifies if the parent of the current tag is marked as "D" or "N"
     * @return the input ArrayList in String format
     */
    private String getXmlDiffString (List<Tag> tags, String indent, boolean parentIsDelNewOrMand)
    {
        String xmlDiffString = "";

        for (Tag tag : tags)
        {
            if (indent.equals("") && tag.modification.equals("D"))
            {
                // If the root name changed, show only the new root tag and its kids 
                continue;
            }

            if (!tag.modification.equals("S") || tag.childTagChanged || tag.isMandatory || tag.childTagMandatory || parentIsDelNewOrMand)
            {
                // Build the start tag      
                xmlDiffString += indent + "<" + tag.name;
                
                // Specify the modification
                if (!tag.modification.equals("S"))
                {
                    xmlDiffString += " mod=\"" + tag.modification + "\"";
                } 
                
                // Specify if its mandatory
                if (tag.isMandatory)
                {
                    xmlDiffString += " mand=\"y\"";
                }   
                
                // Insert the tag's attributes
                for (Tag attr: tag.attributes)
                {
                    if (!attr.modification.equals("S") || tag.modification.equals("N") || tag.modification.equals("D") || parentIsDelNewOrMand)
                    {
                        xmlDiffString += " " + attr.name + "=" + "\"" + attr.value + "\"";

                        if (!attr.modification.equals("S"))
                        {
                            xmlDiffString += " mod_" + attr.name + "=\"" + attr.modification + "\"";
                        }
                    }
                }

                xmlDiffString += ">";
                
                // Build the tag's content
                if (!tag.value.equals(""))
                {
                    xmlDiffString += tag.value;
                }  
                                      
                if(!tag.childTags.isEmpty())
                {                
                    xmlDiffString += "\n";
                    xmlDiffString += getXmlDiffString (tag.childTags, indent + "    ", tag.modification.equals("D") || tag.modification.equals("N") || tag.isMandatory || parentIsDelNewOrMand);
                    xmlDiffString += indent;               
                }
                
                // Build the end tag  
                xmlDiffString += "</" + tag.name + "> \n";  
                
                // Leave an empty line between the root tag's children: the second row/level of tags in an xml file, after the root tag
                if (tag.tagLevel == 2)
                {
                    xmlDiffString += "\n";
                }    
            } 
            else if (indent.equals(""))
            {
                // Nothing changed and there are no mandatory tags
                xmlDiffString += indent + "<" + tag.name;
                xmlDiffString += ">";
                
                //xmlDiffString += " No changes! ";
                
                xmlDiffString += "</" + tag.name + "> \n"; 
                
                break;
            }      
        } // for

        return xmlDiffString;  
    }

    /**
     * Convert an ArrayList of Tags to a String, taking similarities, differences and mandatory tags.
     *
     * @param tags                 the ArrayList to be converted to a String format
     * @param indent               the indentation for the current tag level
     * @return the input ArrayList in String format
     */
    private String getXmlString (List<Tag> tags, String indent)
    {          
        String xmlString = "";
                              
        for (Tag tag : tags)
        { 
            if (indent.equals(""))
            {
                if (tag.modification.equals("D"))
                {
                    // If the root name changed, show only the new root tag and its kids 
                    continue;
                }
                else if (tag.modification.equals("S") && !tag.childTagChanged && !tag.isMandatory && !tag.childTagMandatory)
                {
                    // Nothing changed
                    xmlString += indent + "<" + tag.name;
                    xmlString += ">";
                    
                    //xmlString += " No changes! ";
                    
                    xmlString += "</" + tag.name + "> \n"; 
                    
                    break;
                }
            }                
            
            // Build the start tag      
            xmlString += indent + "<" + tag.name;
            
            // Specify the modification
            xmlString += " mod=\"" + tag.modification + "\"";

            // Specify if any of its child tags changed
            xmlString += " mod_kids=\"" + tag.childTagChanged + "\"";

            // Specify if its mandatory
            xmlString += " mand=\"" + tag.isMandatory + "\"";

            // Specify if any of its kids are mandatory
            xmlString += " mand_kids=\"" + tag.childTagMandatory + "\"";

            // Specify if its mandatory
            xmlString += " tagLevel=\"" + tag.tagLevel + "\"";                
            
            // Insert the attributes          
            for (Tag attr: tag.attributes)
            {
                xmlString += " " + attr.name + "=" + "\"" + attr.value + "\"";
                
                xmlString += " mod_" + attr.name + "=\"" + attr.modification + "\"";          
            } 
            
            xmlString += ">";
            
            // Build the tag's content
            if (!tag.value.equals(""))
            {
                xmlString += tag.value;
            }  
                                    
            if(!tag.childTags.isEmpty())
            {                
                xmlString += "\n";
                xmlString += getXmlString (tag.childTags, indent + "    ");
                xmlString += indent;               
            }
            
            // Build the end tag  
            xmlString += "</" + tag.name + "> \n";

            // Leave an empty line between the root tag's children: the second row/level of tags in an xml file, after the root tag
            if (tag.tagLevel == 2)
            {
                xmlString += "\n";
            } 
                
        } // for
        
        return xmlString;  
    }

    /**
     * Check if a String contains some information or only white spaces.
     *
     * @param value the String value to be checked for whitespaces
     * @return true if the input String value contains also other characters than whitespaces, else false
     */
    static public boolean isPrintable(String value)
    {
        int valueLength = value.length();
        for (int i = 0; i < valueLength; i++)
        {
            if (!Character.isWhitespace(value.charAt(i)))
            {
                // The string contains some data too
                return true;
            }
        }

        return false;
    }
}
