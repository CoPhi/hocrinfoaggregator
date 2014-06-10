/*
 * This file is part of eu.himeros_hocraggregator_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2013 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
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

package eu.himeros.hocr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class FlatXml {
    //static String dirName="/usr/local/hocrinfoaggregator/test/demetrius/Demetrius-De_elocutione.book";
    
    public FlatXml(File inFile, File outFile) throws Exception{
        init(inFile, outFile);
    }
    
    private void init(File inFile, File outFile) throws Exception{
        SAXBuilder builder=new SAXBuilder();
        Document doc=builder.build(inFile);
        Element root=doc.getRootElement();
        Namespace xmlns = root.getNamespace();
        Element head=root.getChild("head",xmlns);
        Element title=new Element("title");
        title.addContent("ocr");
        if(head!=null) head.addContent(title);
        Element body=root.getChild("body",xmlns);
        Element oldPage;
        try{
            oldPage=body.getChild("div",xmlns);
        }catch(Exception ex){
            oldPage=new Element("div",xmlns);
        }
        Element page=new Element("div",xmlns);
        page.setAttribute("class","ocr_page");
        page.setAttribute("id","i"+inFile.getName().substring(1).replace(".html",".png"));
        XPathExpression<Element> xpath = XPathFactory.instance().compile("//ns:div[@class='ocr_carea']", Filters.element(), null, Namespace.getNamespace("ns", "http://www.w3.org/1999/xhtml"));
        List<Element> careaElL = xpath.evaluate(oldPage);
        if(careaElL.isEmpty()){
            careaElL=new ArrayList();
            Element pEl=new Element("p");
            for(Element child:oldPage.getChildren()){
                pEl.addContent(child.clone());
            }
            Element careaEl=new Element("div");
            careaEl.setAttribute("class","ocr_carea");
            careaEl.setAttribute("title","bbox 0 0 0 0");
            careaEl.addContent(pEl);
            careaElL.add(careaEl);
            oldPage.addContent(careaEl);
        }
        for (Element careaEl : careaElL) {
            page.addContent(new Comment("<div class=\""+careaEl.getAttributeValue("class")+"\" title=\""+careaEl.getAttributeValue("title")+"\">"));
            for(Element pEl:careaEl.getChildren()){
                page.addContent(new Comment("<p>"));
                for(Element lineEl:pEl.getChildren()){
                    lineEl.removeAttribute("id");
                    for(Element child:lineEl.getChildren()){
                        child.removeAttribute("id");
                        child.removeAttribute("lang");
                        child.removeAttribute("lang",xmlns);
                    }
                    page.addContent(lineEl.clone());
                }
                page.addContent(new Comment("</p>"));    
            }
            page.addContent(new Comment("</div>"));
        }
        oldPage.detach();
        if(body!=null) body.addContent(page);
        XMLOutputter xmlOutputter=new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(doc,new BufferedWriter(new FileWriter(outFile)));
    }
    
    public static void main(String[] args) throws Exception{
        File dir=new File(args[0]);
        for(File file:dir.listFiles()){
            try{
                if(!file.getName().endsWith(".html")) continue;
                //FlatXml fx=new FlatXml(file, new File(file.getAbsolutePath().replace(".html",".1.html")));
                FlatXml fx=new FlatXml(file,file);
                //FlatXml fx=new FlatXml(file, new File(file.getAbsolutePath()+"1.html"));
            }catch(Exception ex){
                ex.printStackTrace(System.err);
                System.err.println(file.getName());
                continue;
            }
        }
    }
}
