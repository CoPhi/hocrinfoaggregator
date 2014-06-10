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

package eu.himeros.ocr.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class WordListFromXmlExtractor {
    public WordListFromXmlExtractor(String inFileName, String outFileName) throws Exception{
        init(inFileName,outFileName);
    }
    
    private void init(String inFileName, String outFileName) throws Exception{
        SAXBuilder builder=new SAXBuilder();
        Document doc=builder.build(inFileName);
        BufferedWriter bw=new BufferedWriter(new FileWriter(outFileName));
        Element el=doc.getRootElement();
        String s=el.getValue();
        s=s.replaceAll("\n"," ");
        s=s.replaceAll(" +"," ");
        s=s.replace("- ","");
        s=s.replace(" ","\n");
        String[] ss=s.split("\n");
        for(String item:ss){
            item=item.replaceAll("[^’\u0370-\u03FF\u1F00-\u1FFF]|[·;]","");
            if(item.length()<1) continue;
            bw.write(item);
            bw.newLine();
        }
        bw.close();
    }
    
    public static void main(String[] args) throws Exception{
        WordListFromXmlExtractor xwle=new WordListFromXmlExtractor(args[0],args[1]);
    }
}
