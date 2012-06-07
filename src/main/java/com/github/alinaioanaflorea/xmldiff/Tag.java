/*
Copyright (C) 2012 Alina Ioana Florea (alina.ioana.florea@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

http://www.opensource.org/licenses/mit-license.php
 */

package com.github.alinaioanaflorea.xmldiff;

import java.util.List;

/**
 * This class layouts the content of an XML tag and helpful flags in working with it.
 */
public class Tag
{
    /**
     * The tag's data.
     */
    String name = "";
    List<Tag> attributes;
    String value = "";
    List<Tag> childTags;

    /**
     *  Flags that show the status of the tag's data.
     */
    public static final int MATCHED = 100;
    public static final int NOT_MATCHED = 0;

    int contentMatchPercent = NOT_MATCHED;
    int nameMatchPercent = NOT_MATCHED;
    int attrsMatchPercent = NOT_MATCHED;
    int valuesMatchPercent = NOT_MATCHED;

    /**
     * Specify how the tag changes:
     * <ul>
     * <li>C-changed
     * <li>N-new
     * <li>D-deleted
     * <li>S-same
     * </ul>
     */
    String modification = "S";

    /**
     * Keep track if some child tags have changed or not.
     */
    boolean childTagChanged = false;

    /**
     * Keep track if this tag is mandatory or not.
     */
    boolean isMandatory = false;

    /**
     * Keep track if some child tags are mandatory or not.
     */
    boolean childTagMandatory = false;

    /**
     * Keep track if this tag has been processed already or not.
     */
    boolean isMatched = false;

    /**
     * Mark the location of the tag under the root/1st tag.
     */
    int tagLevel = 0;

    /**
     * The following indexes represent the tags which matched to form the current tag, from the two xml files which are being compared.
     */
    int index1 = 0;
    int index2 = 0;

    /**
     * The default constructor.
     */
    public Tag()
    {
    }

    /**
     * Constructor.
     */
    public Tag (String name, List<Tag> attributes, String value, List<Tag> childTags)
    {   
        this(0, name, NOT_MATCHED, attributes, NOT_MATCHED, value, childTags, NOT_MATCHED, "S", false, false, false, 0, 0, 0);
    }

    /**
     * Constructor.
     */
    public Tag (int contentMatchPercent, String name, int nameMatchPercent, List<Tag> attributes, int attrsMatchPercent, String value, List<Tag> childTags, int valuesMatchPercent, String modification, boolean childTagChanged, boolean isMandatory, boolean childTagMandatory, int tagLevel, int index1, int index2)
    {
        this.name = name;
        this.attributes = attributes;
        this.value = value;   
        this.childTags = childTags;

        this.contentMatchPercent = contentMatchPercent;
        this.nameMatchPercent = nameMatchPercent;
        this.attrsMatchPercent = attrsMatchPercent;
        this.valuesMatchPercent = valuesMatchPercent;
               
        this.modification = modification; 
        this.childTagChanged = childTagChanged; 
        this.isMandatory = isMandatory;  
        this.childTagMandatory = childTagMandatory;

        this.tagLevel = tagLevel;

        this.index1 = index1;
        this.index2 = index2;
    }
}
