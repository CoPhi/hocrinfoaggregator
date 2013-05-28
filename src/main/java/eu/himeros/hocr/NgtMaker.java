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
package eu.himeros.hocr;

import eu.himeros.transcoder.Transcoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class NgtMaker {
    StringBuilder prolog;
    BufferedReader br;
    BufferedWriter bw;
    Document doc;
    Element root;
    Namespace xmlns;
    SAXBuilder builder;
    TreeMap<String, Integer> ngtTm;
    ArrayList<String> ngtAl;
    ArrayList<String> ocrAl;
    Transcoder trans;
    int prevValue=-1;
    int start=-1;
    int end=-1;
    String outFileName;
    XPathExpression<Element> xpath;

    public NgtMaker(File ngtName) throws Exception {
        init(ngtName);
    }

    private void init(File ngtName) throws Exception {
        br = new BufferedReader(new FileReader(ngtName));
        trans = new Transcoder(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/transcoders/low2up.txt"));
        ngtTm = new TreeMap<>();
        ngtAl = new ArrayList<>(300000);
        prolog=new StringBuilder("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        prolog.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        prolog.append("<meta content=\"riguadon 0.3\" name=\"ocr-system\" />\n");
        prolog.append("<meta name=\"ocr-nmber-of-pages\" content=\"???\" />\n");
        prolog.append("<meta name=\"ocr-langs\" content=\"grc lat\" />\n");
        prolog.append("<meta content=\"ocr_line ocr_page\" name=\"ocr-capabilities\" />\n");
        prolog.append("<link href=\"hocraggregate.css\" rel=\"stylesheet\" type=\"text/css\"/>\n");
        String line;
        String it1;
        String it2;
        String it3;
        String key;
        while ((line = br.readLine()) != null) {
            ngtAl.add(line);
        }
        ngtAl.add("###");
        ngtAl.add("###");
        for (int i = 0; i < ngtAl.size() - 2; i++) {
            it1 = trans.parse(ngtAl.get(i));
            it2 = trans.parse(ngtAl.get(i + 1));
            it3 = trans.parse(ngtAl.get(i + 2));
            key = (new StringBuilder(it1)).append(it2).append(it3).toString();
            if (ngtTm.containsKey(key)) {
                ngtTm.remove(key);
            } else {
                ngtTm.put(key, i);
            }
        }
        br.close();
    }

    public void parseAll(File dir) throws Exception {
        for (File file : dir.listFiles()) {
            try{
                if(!file.getName().matches("^.+\\.html$")) continue;
                parseDoc(file);
            }catch(Exception ex){
                System.err.println(file.getName());
                continue;
            }
        }
    }

    public void parseDoc(File file) throws Exception {
        adjustFile(file);
        start=-1;
        end=-1;
        prevValue=-1;
        ocrAl = new ArrayList<>(1000);
        outFileName=file.getAbsolutePath().substring(0,file.getAbsolutePath().length()-4)+"ngt.xml";
        builder = new SAXBuilder();
        doc = builder.build(file);
        root = doc.getRootElement();
        xmlns = root.getNamespace();
        xpath = XPathFactory.instance().compile("//ns:span[@class='ocr_word']", Filters.element(), null, Namespace.getNamespace("ns", "http://www.w3.org/1999/xhtml"));
        List<Element> elements = xpath.evaluate(root);
        for (Element element : elements) {
            parseOcrWord(element);
        }
        
        
        ocrAl.add("%%%");
        ocrAl.add("%%%");
        findAnchors();
        writeFragment(start,end);
    }

    private void parseOcrPage(Element ocrPage) {
        List<Element> ocrLines = ocrPage.getChildren("span", xmlns);
        for (Element ocrLine : ocrLines) {
            parseOcrLine(ocrLine);
        }
    }

    private void parseOcrLine(Element ocrLine) {
        List<Element> ocrWords = ocrLine.getChildren("span", xmlns);
        for (Element ocrWord : ocrWords) {
            parseOcrWord(ocrWord);
        }
    }

    private void parseOcrWord(Element ocrWord) {
        String word=trans.parse(ocrWord.getText()).replaceAll("[^Α-Ω’]*","").trim();
        if(word.length()==0) return;
        ocrAl.add(word);
    }
    
    private void findAnchors(){
        for(int i=0;i<ocrAl.size()-2;i++){
            String key=(new StringBuilder(ocrAl.get(i))).append(ocrAl.get(i+1)).append(ocrAl.get(i+2)).toString();
            Integer value=ngtTm.get(key);
            if(prevValue==-1&&value!=null){
                start=value;
                prevValue=value;
            }
            if(value!=null){
                if((value-prevValue)<100){
                    prevValue=value;
                    end=value;
                }                
            }
        }
    }
    
    private void writeFragment(int fragStart,int fragEnd) throws Exception{
        bw=new BufferedWriter(new FileWriter(outFileName));
        StringBuilder fragTextSb=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        fragTextSb.append("<fragment>\n");        
        try{
            for(int i=fragStart;i<=fragEnd;i++){
            String word=ngtAl.get(i);
            fragTextSb.append(word).append(" ");
        }
        }catch(Exception ex){
            System.out.println(outFileName);
        }
        fragTextSb.append("\n</fragment>\n");
        bw.write(fragTextSb.toString());
        bw.close();
    }
    
    private void adjustFile(File file) throws Exception{
        BufferedReader tmpBr=new BufferedReader(new FileReader(file));
        String line;
        StringBuilder fileTextSb=new StringBuilder(10000);
        fileTextSb.append(prolog);
        int lineCounter=0;
        while((line=tmpBr.readLine())!=null){
            lineCounter++;
            if(lineCounter<8) continue;
            fileTextSb.append(line).append("\n");
        }
        tmpBr.close();
        BufferedWriter tmpBw=new BufferedWriter(new FileWriter(file));
        tmpBw.write(fileTextSb.toString());
        tmpBw.close();
    }

    public static void main(String[] args) throws Exception {
        //String ngtName="/usr/local/hocrinfoaggregator/test/demetrius/demetrius-de_elocutione.ngt.csv";
        //String dirName="/usr/local/hocrinfoaggregator/test/demetrius/Demetrius-De_elocutione.book";
        NgtMaker nm = new NgtMaker(new File(args[0]));
        nm.parseAll(new File(args[1]));
    }
}