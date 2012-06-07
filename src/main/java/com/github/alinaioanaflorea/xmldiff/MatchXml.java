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
 * This class can match the content of two ArrayLists of Tag structures and return an ArrayList with their differences and mandatory tags.
 */
public class MatchXml extends ProcessXml
{
    /**
     * The list of mandatory tags.
     */
    private List<Tag> mandatoryTags;

    /**
     * Get the differences and similarities between two Tag lists while keeping the mandatoryTags.
     *
     * @param tags1 the first tag list to compare
     * @param tags2 the second tag list to compare
     * @param mandatoryTags the list with the mandatory tags
     * @return a list with the mandatory Tags, differences and similarities between the two input Tag lists
     */
    public List<Tag> GetTagDiff (List<Tag> tags1, List<Tag> tags2, List<Tag> mandatoryTags)
    {
        // Set the mandatory tags
        this.mandatoryTags = !mandatoryTags.isEmpty() ? mandatoryTags.get(0).childTags : new ArrayList<Tag>(); // Exclude the root tag

        Tag changes = new Tag();
        changes.tagLevel = 1; // The root tags represent the 1st row/level of tags

        return getTagDiff(tags1, tags2, true, changes, false);
    }

    /**
     * Match each tag from the first Tag list with the first tag, with the same name, from the second Tag list and build a new Tag list with their differences, similarities and mandatory tags.
     *
     * @param tags1 the first list of Tags to be compared
     * @param tags2 the second list of Tags to be compared
     * @param rootEntry specifies if the current level of tags are at root level
     * @param changes stores the changes for the current level of tags
     * @param isAttribute specifies if the content of an attribute is being processed now
     * @return a list with the mandatory Tags, differences and similarities between the two input Tag lists
     */
    private List<Tag> getTagDiff (List<Tag> tags1, List<Tag> tags2, boolean rootEntry, Tag changes, boolean isAttribute)
    {
        List<Tag> diffTags = new ArrayList<Tag>();

        int tags1Size = tags1.size(); 
        int tags2Size = tags2.size();

        // Check if both sides have records, or only one side
        if (tags1Size == 0 && tags2Size == 0)
        {
            return diffTags; // No records to compare, eg: attributes of attributes
        }
        else if (tags1Size == 0)
        {
            changes.childTagChanged = true; 
            
            for (Tag tag : tags2)
            {
                boolean isMandatory = isMandatory(tag.name);                
                if (isMandatory)
                {
                    changes.childTagMandatory = true; 
                }
             
                diffTags.add( new Tag(0, tag.name, Tag.NOT_MATCHED, tag.attributes, Tag.NOT_MATCHED, tag.value, tag.childTags, Tag.NOT_MATCHED, "N", false, isMandatory, false, changes.tagLevel, 0, 0) );
            }
        }
        else if (tags2Size == 0)
        {
            changes.childTagChanged = true;
             
            for (Tag tag : tags1)
            {
                diffTags.add( new Tag(0, tag.name, Tag.NOT_MATCHED, tag.attributes, Tag.NOT_MATCHED, tag.value, tag.childTags, Tag.NOT_MATCHED, "D", false, isMandatory(tag.name), false, changes.tagLevel, 0, 0) );
            }        
        }

        // Both sides have records    
        for (int i = 0; i < tags1Size; i++)
        {      
            Tag tag1 = tags1.get(i);

            for (int j = 0; j < tags2Size; j++)
            {
                Tag tag2 = tags2.get(j);
                if (tag2.contentMatchPercent == 100 && j != tags2Size-1)
                {
                    continue;
                }

                if (tag1.name.equals(tag2.name) || rootEntry)
                {
                    tag1.isMatched = true;
                    tag2.isMatched = true;

                    // Track local changes
                    Tag attributesChanges = new Tag();
                    Tag childChanges = new Tag();
                    childChanges.name = tag2.name;
                    childChanges.tagLevel = changes.tagLevel;

                    List<Tag> tagAttributes = getTagDiff (tag1.attributes, tag2.attributes, false, attributesChanges, true);
                    List<Tag> childTags = getChildTagDiff (tag1, tag2, childChanges);

                    if (attributesChanges.childTagChanged || childChanges.childTagChanged)
                    {
                        changes.childTagChanged = true; // Affect parent changes
                        
                        if (childTags.isEmpty() && !isAttribute)
                        {
                            childChanges.childTagChanged = false;
                        }

                        childChanges.modification = changes.modification = "C";
                    }

                    if (childChanges.childTagMandatory)
                    {
                          changes.childTagMandatory = true; // Affect parent changes
                          
                          if (childTags.isEmpty() && !isAttribute)
                          {
                              childChanges.childTagMandatory = false;
                          }
                    }
                    
                    if (!tag1.name.equals(tag2.name)) // In case the root tag's name has changed
                    {
                        childChanges.modification = changes.modification = "C";
                    }

                    int attrsMatchPercent = tagAttributes.isEmpty() ? Tag.MATCHED : calcMatchingPercentage(tagAttributes);
                    int valuesMatchPercent = childTags.isEmpty() ? childChanges.valuesMatchPercent : calcMatchingPercentage(childTags);

                    int contentMatchPercent = (attrsMatchPercent + valuesMatchPercent)/2;
                    if (tag2.contentMatchPercent < contentMatchPercent)
                    {
                        tag2.contentMatchPercent = contentMatchPercent;
                    }

                    diffTags.add( new Tag(contentMatchPercent, childChanges.name, Tag.MATCHED, tagAttributes, attrsMatchPercent, childChanges.value, childTags, valuesMatchPercent, childChanges.modification, childChanges.childTagChanged, isMandatory(childChanges.name), childChanges.childTagMandatory, changes.tagLevel, i, j) );
                }    
                else // No match
                {                                     
                    if (i == tags1Size-1 && !tag2.isMatched) // Not matched and in a last looping case
                    {                           
                        tag2.isMatched = true;
                        changes.childTagChanged = true;                                        
                        
                        boolean isMandatory = isMandatory(tag2.name);
                        if (isMandatory)
                        {
                            changes.childTagMandatory = true; 
                        }
                        
                        diffTags.add( new Tag(0, tag2.name, Tag.NOT_MATCHED, tag2.attributes, Tag.NOT_MATCHED, tag2.value, tag2.childTags, Tag.NOT_MATCHED, "N", false, isMandatory, false, changes.tagLevel, 0, 0) );
                    }
                    
                    if (j == tags2Size-1 && !tag1.isMatched) // Not matched and in a last looping case
                    {
                        tag1.isMatched = true;
                        changes.childTagChanged = true;

                        diffTags.add( new Tag(0, tag1.name, Tag.NOT_MATCHED, tag1.attributes, Tag.NOT_MATCHED, tag1.value, tag1.childTags, Tag.NOT_MATCHED, "D", false, isMandatory(tag1.name), false, changes.tagLevel, 0, 0) );
                    }
                }

                if (tag2.contentMatchPercent == 100 && i != tags1Size-1)
                {
                    break;
                }

            } // for 2

            tag1.isMatched = false; // To be able to match correctly sibling tags with the same name from tag2

        } // for 1

        // Reset also the matching state of tags2 for its tags to be able to match with other tags at different level from tags1
        for (Tag tag : tags2)
        {
            tag.isMatched = false;
            tag.contentMatchPercent = 0;
        }

        // Match/filter the tags in diffTags
        matchTags(diffTags, tags1, tags2);

        return diffTags;  
    }

    /**
     * Get the difference between the values and their kids, of the passed in tags.
     *
     * @param tag1 the first Tag to be compared
     * @param tag2 the second Tag to be compared
     * @param changes the changes for the input Tags
     * @return a list with the mandatory Tags, differences and similarities between the two input Tags
     */
    private List<Tag> getChildTagDiff (Tag tag1, Tag tag2, Tag changes)
    {
        List<Tag> diffTags = new ArrayList<Tag>();

        if (!tag1.childTags.isEmpty()) // Has kids
        {
            if (!tag2.childTags.isEmpty()) // Has kids
            {
                changes.tagLevel++;
                diffTags = getTagDiff(tag1.childTags, tag2.childTags, false, changes, false);
            } 
            else // No kids
            {
                changes.childTagChanged = true;
                
                // Add deleted tags
                for (Tag tag : tag1.childTags) 
                {                   
                    diffTags.add( new Tag(0, tag.name, Tag.NOT_MATCHED, tag.attributes, Tag.NOT_MATCHED, tag.value, tag.childTags, Tag.NOT_MATCHED, "D", false, isMandatory(tag.name), false, changes.tagLevel+1, 0, 0) );
                }    
                
                // Add the new tag
                if (isMandatory(tag2.name))
                { 
                    changes.childTagMandatory = true;
                }
                                    
                changes.name = tag2.name;
                changes.modification = "N";
                changes.attrsMatchPercent = Tag.NOT_MATCHED;
                changes.valuesMatchPercent = Tag.NOT_MATCHED;

                if (ProcessXml.isPrintable(tag2.value))
                {     
                    changes.value = tag2.value + " (mod_val=\"N\")";
                }                  	                                        
            }                    
        }
        else // No kids
        {
            if (!tag2.childTags.isEmpty()) // Has kids
            {            
                changes.childTagChanged = true;
                
                // Add new tags
                for (Tag tag : tag2.childTags) 
                {
                    boolean isMandatory = isMandatory(tag.name);
                    if (isMandatory)
                    {
                        changes.childTagMandatory = true;   
                    }
                    
                    diffTags.add( new Tag(0, tag.name, Tag.NOT_MATCHED, tag.attributes, Tag.NOT_MATCHED, tag.value, tag.childTags, Tag.NOT_MATCHED, "N", false, isMandatory, false, changes.tagLevel+1, 0, 0) );
                }

                // Add the deleted tag
                changes.name = tag1.name;
                changes.modification = "D";
                changes.attrsMatchPercent = Tag.NOT_MATCHED;
                changes.valuesMatchPercent = Tag.NOT_MATCHED;

                if (ProcessXml.isPrintable(tag1.value))
                {                           
                    changes.value = tag1.value + " (mod_val=\"D\")";
                }   
            }
            else // No kids
            {
                if (!tag1.value.equals(tag2.value)) 
                {
                    changes.childTagChanged = true;
                    changes.modification = "C";
                    changes.valuesMatchPercent = Tag.NOT_MATCHED;
                }
                else
                {
                    changes.valuesMatchPercent = Tag.MATCHED;
                }

                if (isMandatory(tag2.name))
                {
                    changes.childTagMandatory = true;
                }

                changes.name = tag2.name;
                changes.value = tag2.value;
            }                  
        }
        
        return diffTags;    
     }

    /**
     * Check is a tagName is a mandatoryTag.
     *
     * @param tagName the Tag name to be checked
     * @return true is the Tag name was found found in the list of mandatory Tags, else false
     */
    private boolean isMandatory (String tagName)
    {
        for (Tag mandTag : mandatoryTags)
        {
            if (tagName.equals(mandTag.name))
            {
                return true;
            }
        } 
       
       return false;
    }

    /**
     * Calculate the matching percentage for a matched tag based on how many names and values matched inside it.
     *
     * @param tags the content(mandatory, similar and different Tags) of the tag for which the matching percentage is being calculated
     * @return the percentage at which the input content matched
     */
    private int calcMatchingPercentage(List<Tag> tags)
    {
        int valueMatch = Tag.NOT_MATCHED;
        int nameMatch = Tag.NOT_MATCHED;

        for (Tag tag : tags)
        {
            valueMatch += tag.valuesMatchPercent;
            nameMatch += tag.nameMatchPercent;
        }

        int tagsSize = tags.size();
        valueMatch = tagsSize != 0 ? valueMatch/tagsSize : Tag.NOT_MATCHED;
        nameMatch = tagsSize != 0 ? nameMatch/tagsSize : Tag.NOT_MATCHED;

        return (valueMatch + nameMatch)/2;
    }

    /**
     * This function choose the tags that will part of the output xmlDiff.xml file, based on the List of matched Tags(diffTags).
     *
     * @param diffTags the list with the mandatory, similar and different Tags between the two next two input input lists
     * @param tags1 the first list of Tags which was compared. Needed for marking the matching and to retrieve the initial form of a Tag for new or deleted Tags.
     * @param tags2 the second list of Tags which was compared. Needed for marking the matching and to retrieve the initial form of a Tag for new or deleted Tags.
     */
    private void  matchTags(List<Tag> diffTags, List<Tag> tags1, List<Tag> tags2)
    {
        // Deleted and new tags are skipped as they are anyways going to be put in the output xml file

        int nrOfTags = diffTags.size();

        // Order tags by their contentMatchPercent in a descending order. This order ensures that the highest matching percentages are chosen first.
        int i = 0, j;
        for (; i < nrOfTags && i+1 < nrOfTags; i++)
        {
            Tag tag1 = diffTags.get(i);

            if (tag1.contentMatchPercent == 100)
            {
                // Save time by skipping 100% matches, as there's no higher values to match it with
                continue;
            }

            if (!tag1.modification.equals("D") && !tag1.modification.equals("N"))
            {
                j = i + 1;
                for (; j < nrOfTags; j++)
                {
                    Tag tag2 = diffTags.get(j);

                    if (tag1.name.equals(tag2.name) && tag1.contentMatchPercent < tag2.contentMatchPercent)
                    {
                        // Switch their order
                        diffTags.set(i, tag2);
                        diffTags.set(j, tag1);
                        tag1 = tag2;  // Refresh the value of tag1 used in the next comparisons
                    }
                }
            }
        }

        // Start matching from the beginning of the list the tags for which both their indexes are currently not matched.
        // This ensures that the highest matching percentages are chosen first. Half matches will be known only at the end of this matching.
        for (Tag tag: diffTags)
        {
            if (!tag.modification.equals("D") && !tag.modification.equals("N") && !tags1.isEmpty() && !tags2.isEmpty() && !tags1.get(tag.index1).isMatched && !tags2.get(tag.index2).isMatched)
            {
                tags1.get(tag.index1).isMatched = true;
                tags2.get(tag.index2).isMatched = true;
                tag.isMatched = true;
            }
        }

        // Mark with new/deleted half matches and remove not matched tags which have both their indexes matched.
        i = 0;
        for (; i < nrOfTags; i++)
        {
            Tag tag = diffTags.get(i);

            if (!tag.modification.equals("D") && !tag.modification.equals("N") && !tags1.isEmpty() && !tags2.isEmpty())
            {
                if (tags1.get(tag.index1).isMatched && tags2.get(tag.index2).isMatched && !tag.isMatched)
                {
                    // Remove tags that fully matched at lower matching values
                    diffTags.remove(tag);

                    // Reset counters
                    if (i!=0) i -= 1;
                    nrOfTags = diffTags.size();
                }
                else if (!tags1.get(tag.index1).isMatched && tags2.get(tag.index2).isMatched) // For half matches
                {
                    tags1.get(tag.index1).isMatched = true;

                    int tempTagLevel = tag.tagLevel;
                    tag = tags1.get(tag.index1);
                    tag.tagLevel = tempTagLevel;
                    tag.modification = "D";

                    diffTags.set(i, tag);
                }
                else if (!tags2.get(tag.index2).isMatched && tags1.get(tag.index1).isMatched) // For half matches
                {
                    tags2.get(tag.index2).isMatched = true;

                    int tempTagLevel = tag.tagLevel;
                    tag = tags2.get(tag.index2);
                    tag.modification = "N";
                    tag.tagLevel = tempTagLevel;

                    diffTags.set(i, tag);
                }
            }
        }
    }
}
