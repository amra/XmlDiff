/*
Copyright (C) 2012 Alina Ioana Florea (alina.ioana.florea@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

http://www.opensource.org/licenses/mit-license.php
 */

package com.github.alinaioanaflorea.xmldiff;

import java.util.List;
import java.util.ArrayList;

/**
 * This class produces an xmlDiff.xml file with the difference between two input xml files it receives, while keeping the mandatory tags specified in a third optional xml file passed as input.
 */
public class XmlDiff
{
    /**
     * The variable keeps track if the code is being run for debug or release.
     */
    public static boolean isDebugBuild = false;

    /**
     * Entry point to XmlDiff.
     *
     * @param args The input arguments for XmlDiff
     */
    public static void main(String[] args)
    {
        processInputArgs(args);

        try
        {
            MatchXml matchXml = new MatchXml();

            List<Tag> mandatoryTags = new ArrayList<Tag>();
            if (args.length == 3)
            {
                // Get the given mandatory tags
                mandatoryTags = matchXml.getTags (args[2]);
            }

            // Get the tag lists from the input files
            List<Tag> tags1 = matchXml.getTags (args[0]);
            List<Tag> tags2 = matchXml.getTags (args[1]);

            // Get the difference between the two tag lists, while keeping the mandatory tags
            List<Tag> tagDiff = matchXml.GetTagDiff (tags1, tags2, mandatoryTags);
            
            // Print the difference to the standard output and to the xmlDiff.xml file
            matchXml.print (tagDiff, "xmlDiff.xml");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Process the input arguments received by XmlDiff
     *
     * @param args The input arguments for XmlDiff
     */
    static void processInputArgs(String[] args)
    {
        isDebugBuild = Boolean.getBoolean("xmlDiff.isDebugBuild");

        if (args.length < 2 || args.length > 3)
        {
            System.out.println ("This application expects the following arguments:\n" +
                                "- two xml to be compared (first the old version, then the new version) \n" +
                                "- an optional xml file with the mandatory tags (if given, it must contain at least an empty root tag, which can contain 0 or more empty mandatory tags) \n");

            System.exit(1);
        }
    }
}
