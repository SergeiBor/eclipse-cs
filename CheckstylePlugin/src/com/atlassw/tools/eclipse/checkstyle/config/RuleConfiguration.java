//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.config;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Configuration;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Represents the configuration of a specific rule.
 */
public class RuleConfiguration implements Cloneable, Configuration, Comparable
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private String mImplClassname;

    private String mComment = "";

    private SeverityLevel mSeverityLevel;

    private HashMap mConfigProperties = new HashMap();

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     *  Constructor.
     * 
     *  @param  implClassname   Fully qualified class name of the
     *                           rule's implementation class.
     */
    public RuleConfiguration(String implClassname)
    {
        mImplClassname = implClassname;
    }

    /**
     *  Construct from a config file DOM node.
     */
    RuleConfiguration(Node node) throws CheckstylePluginException
    {
        String temp = XMLUtil.getNodeAttributeValue(node, XMLTags.CLASSNAME_TAG);
        if (temp != null)
        {
            mImplClassname = temp.trim();
        }
        else
        {
            String message = "Rule missing implementation classname";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }

        temp = XMLUtil.getNodeAttributeValue(node, XMLTags.SEVERITY_TAG);
        if (temp != null)
        {
            mSeverityLevel = SeverityLevel.getInstance(temp.trim());
        }
        else
        {
            String message = "Rule missing severity level in metadata";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }

        temp = XMLUtil.getNodeAttributeValue(node, XMLTags.COMMENT_TAG);
        if (temp != null)
        {
            mComment = temp;
        }

        Node configItems = XMLUtil.getChildNode(node, XMLTags.CONFIG_PROPERTIES_TAG);
        NodeList children = configItems.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = children.item(i);
            if (child.getNodeName().equals(XMLTags.CONFIG_PROPERTY_TAG))
            {
                ConfigProperty prop = new ConfigProperty(child);
                mConfigProperties.put(prop.getName(), prop);
            }
            else if (child.getNodeName().equals(XMLTags.COMMENT_TAG))
            {
                mComment = XMLUtil.getNodeTextValue(child);
            }
        }
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Get a configuration property.
     * 
     *  @param name  Name of the property.
     * 
     *  @return  Value of the property.
     */
    public ConfigProperty getConfigProperty(String name)
    {
        return (ConfigProperty)mConfigProperties.get(name);
    }

    /**
     * Set the property values. <B>Note on severity level:</B>  If the properties
     * being set do not contain a severity level config then there could be 
     * problems.  Be sure to call setSeverityLevel after you make this call.
     * This is a necessary work around until severity is refactored into a property
     * fully. 
     * 
     * @param items  A new set of property values.
     */
    public void setConfigProperties(HashMap items)
    {
        mConfigProperties = items;
    }
    
    /**
     * @return  Severity level of the rule.
     */
    public SeverityLevel getSeverityLevel()
    {
        return mSeverityLevel;
    }
    
    /**
     * @param level  Severity level of the rule.
     */
    public void setSeverityLevel(SeverityLevel level)
    {
        mSeverityLevel = level;

        ConfigProperty prop = (ConfigProperty)mConfigProperties.get(XMLTags.SEVERITY_TAG);
        String value = (level == null) ? null : level.getName();
        if (prop == null)
        {

            prop = new ConfigProperty(XMLTags.SEVERITY_TAG, value);
            mConfigProperties.put(XMLTags.SEVERITY_TAG, prop);
        }
        else
        {
            prop.setValue(value);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    /**
     * Returns the implClassname.
     * @return String
     */
    public String getImplClassname()
    {
        return mImplClassname;
    }

    /**
     * Sets the implClassname.
     * @param implClassname The implClassname to set
     */
    void setImplClassname(String implClassname)
    {
        mImplClassname = implClassname;
    }

    /**
     *  Create an XML DOM node representation of the rule configuration.
     * 
     *  @param  doc  The document to create the node within.
     * 
     *  @return  DOM containing the rule metadata.
     */
    Node toDOMNode(Document doc)
    {
        Element ruleNode = null;

        try
        {
            ruleNode = doc.createElement(XMLTags.RULE_CONFIG_TAG);
            ruleNode.setAttribute(XMLTags.CLASSNAME_TAG, mImplClassname);
            String severity = mSeverityLevel.getName();
            ruleNode.setAttribute(XMLTags.SEVERITY_TAG, severity);

            //
            //  Add the rule's comment, if one exists.
            //
            if ((mComment != null) && (mComment.trim().length() > 0))
            {
                ruleNode.setAttribute(XMLTags.COMMENT_TAG, mComment);
            }

            Node cfgPropsNode = doc.createElement(XMLTags.CONFIG_PROPERTIES_TAG);
            ruleNode.appendChild(cfgPropsNode);

            //
            //  Add the properties.
            //
            LinkedList props = new LinkedList(mConfigProperties.values());
            Collections.sort(props);
            Iterator iter = props.iterator();
            while (iter.hasNext())
            {
                ConfigProperty prop = (ConfigProperty)iter.next();

                //
                //  Don't output severity as a property, internally its considered 
                //  an attribute of the rule.
                //
                if (prop.getName().equals(XMLTags.SEVERITY_TAG))
                {
                    continue;
                }

                Node propNode = prop.toDOMNode(doc);
                cfgPropsNode.appendChild(propNode);
            }
        }
        catch (DOMException e)
        {
            ruleNode = null;
            CheckstyleLog.warning("Failed to create XML DOM node for rule, ignoring rule");
        }

        return ruleNode;
    }
    
    /**
     * Get an attribute value.
     * 
     * @param  name  Name of the attribute.
     * 
     * @return Value of the attribute.
     * 
     * @throws com.puppycrawl.tools.checkstyle.api.CheckstyleException
     *         Error occured.
     */
    public String getAttribute(String name)
        throws com.puppycrawl.tools.checkstyle.api.CheckstyleException
    {
        if (name.equals(XMLTags.SEVERITY_TAG))
        {
            return mSeverityLevel.getName();
        }

        ConfigProperty prop = (ConfigProperty)mConfigProperties.get(name);
        if (prop == null)
        {
            String msg = "Invalid attribute name";
            throw new com.puppycrawl.tools.checkstyle.api.CheckstyleException(msg);
        }
        String result = prop.getValue();
        if ((result != null) && (result.length() == 0))
        {
            result = null;
        }
        return result;
    }
    
    /**
     * Get all of the attribute names.
     * 
     * @return  Attribute names.
     */
    public String[] getAttributeNames()
    {
        LinkedList nonNullProps = new LinkedList();
        Iterator iter = mConfigProperties.values().iterator();
        while (iter.hasNext())
        {
            ConfigProperty prop = (ConfigProperty)iter.next();
            String value = prop.getValue();

            if ((value != null) && (value.length() > 0))
            {
                nonNullProps.add(prop.getName());
            }
        }

        //
        //  Need to prevent multiple severity property types here
        //  This is necessary since severity it not internally treated as a
        //  "property"
        //
        if (!nonNullProps.contains(XMLTags.SEVERITY_TAG))
        {
            nonNullProps.add(XMLTags.SEVERITY_TAG);
        }

        String[] result = new String[nonNullProps.size()];
        Collections.sort(nonNullProps);
        result = (String[])nonNullProps.toArray(result);
        return result;
    }
    
    /**
     *  Method needed by base Checkstyle.  Returns an empty array of children.
     * 
     *  @return  Empty array.
     */
    public Configuration[] getChildren()
    {
        Configuration[] result = new Configuration[0];
        return result;
    }
    
    /**
     *  Method used by base Checkstyle.
     * 
     *  @return The rule implementation's fully qualified classname.
     */
    public String getName()
    {
        return getImplClassname();
    }

    /**
     * Returns the comment.
     * @return String
     */
    public String getComment()
    {
        return mComment;
    }

    /**
     * Sets the comment.
     * @param comment The comment to set
     */
    public void setComment(String comment)
    {
        mComment = comment;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object obj)
    {
        int result = 0;
        RuleConfiguration that = (RuleConfiguration)obj;
        result = this.mImplClassname.compareTo(that.mImplClassname);
        if (result == 0)
        {
            //
            //  Check the severity setting as the tie breaker.
            //
            result = mSeverityLevel.getName().compareTo(that.mSeverityLevel.getName());
        }

        return result;
    }

}
