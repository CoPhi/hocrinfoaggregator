/*
 * This file is part of eu.himeros_hocraggregator_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2012 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.himeros.digitaledition;

import eu.himeros.transcoder.Transcoder;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class AlignedQuotationParser {

    private SAXBuilder builder = null;
    private Document docIn = null;
    private Element rootIn = null;
    private Element rootOut = null;
    private Stack<String> langStack = null;
    private String langScope = null;
    private Transcoder betaTrans = null;
    private Transcoder upperTrans = null;
    private String defaultLang = "grc";
    private String hyphenatedFirstPart = null;
    private HashMap<String, Integer> occHm=null;
    private int id=1;

    public AlignedQuotationParser() throws Exception{
        init(defaultLang);
    }
    
    public AlignedQuotationParser(String defaultLang) throws Exception {
        init(defaultLang);
    }
    
    private void init(String defaultLang) throws Exception{
        this.defaultLang=defaultLang;
        occHm=new HashMap<>(2048);
        langStack = new Stack<>();
        betaTrans = new Transcoder(this.getClass().getResourceAsStream("/eu/himeros/resources/transcoders/beta2u.txt"));
        upperTrans=new Transcoder(this.getClass().getResourceAsStream("/eu/himeros/resources/transcoders/low2up.txt"));
        langStack.push(defaultLang);
        rootOut=new Element("text");
    }
    


    public Element parse(Content content) {
        switch (content.getCType()) {
            case Text:
            case CDATA:
                String text = ((Text) content).getText();
                if (defaultLang.equals(langStack.peek())) {
                    parseTextLine(betaTrans.parse(text));
                }
                break;
            case Element:
                List<Content> children = ((Element) content).getContent();
                for (Content child : children) {
                    switch (child.getCType()) {
                        case Text:
                        case CDATA:
                            parse(child);
                            break;
                        case Element:
                            langScope = (((Element) child).getAttributeValue("lang") == null ? langScope : ((Element) child).getAttributeValue("lang"));
                            langStack.push(langScope);
                            parse(child);
                            langStack.pop();
                    }
                }
        }
        return rootOut;
    }
    
    public Element parse(String inFile) throws Exception{
        rootIn=getRoot(inFile);
        rootOut=parse(rootIn);
        injectOcc(rootOut);
        appendToAnchor(rootOut);
        return appendToAnchor(rootOut);
    }
    
    private Element appendToAnchor(Element root) throws Exception{
        Element anchorRoot=new Element("text");
        List<Element> words=root.getChildren();
        Element currAnchor=new Element("w");
        currAnchor.setAttribute("id","0");
        anchorRoot.addContent(currAnchor);
        Element currNode;
        for(Element word:words){
            currNode=word.clone();
            if("1".equals(word.getAttributeValue("occ"))){
                currAnchor=currNode;
                anchorRoot.addContent(currAnchor);
            }else{
                currAnchor.addContent(currNode);
            }
        }
        return anchorRoot;
    }

    private void parseTextLine(String textLine) {
        String[] tokens = textLine.split(" ");
        for (String token : tokens) {
            token = token.replaceAll("[\n\t·,;.]+", "");
            if (token.matches("[\u0380-\u03FF\u1F00-\u1FFF’]+")) {
                if (hyphenatedFirstPart != null) {
                    token = hyphenatedFirstPart + token;
                    hyphenatedFirstPart = null;
                }
                Element el=new Element("w");
                el.setAttribute("id",""+id++);
                el.setAttribute("text",token);
                el.setAttribute("uc",upperTrans.parse(token));
                rootOut.addContent(el);
            } else if (token.endsWith("-")) {
                hyphenatedFirstPart = token.substring(0, token.length() - 1);
            }
        }
    }
    
    private void injectOcc(Element root){
        makeOccHm(root);
        List<Element> words=root.getChildren();
        for(Element word:words){
            String upWord=word.getAttributeValue("uc");
            String occ=occHm.get(upWord).toString();
            word.setAttribute("occ", occ);
        }
    }
    
    private void makeOccHm(Element root){
        List<Element> words=root.getChildren();
        for(Element word:words){
            String upWord=word.getAttributeValue("uc");
            Integer counter;
            counter=((counter=occHm.get(upWord))==null?new Integer(1):new Integer(counter.intValue()+1));
            occHm.put(upWord,counter);                
        }
    }

    public Element getRoot(String inFileName) throws Exception{
        builder = new SAXBuilder();
        docIn = builder.build(inFileName);
        return docIn.getRootElement();
    }
    
    public static void align(String inFileName, String outFileName) throws Exception{
        AlignedQuotationParser aqp = new AlignedQuotationParser();
        Element rootOut=aqp.parse(inFileName); //e.g. xxx002_001_ft-xi_frag.xml
        XMLOutputter xop=new XMLOutputter(Format.getPrettyFormat().setEncoding("UTF-8").setLineSeparator("\n").setIndent("   "));
        String output=xop.outputString(rootOut);
        System.out.println(output);        
    }
    
    public static void main(String[] args) throws Exception {
        align(args[0], args[1]);
    }
}
