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

package eu.himeros.ocr.util;

import eu.himeros.transcoder.Transcoder;
import java.io.*;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Makes an upper case dictionary from a lower case dictionary
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class UpperCaseDictMaker {
    BufferedReader br;
    BufferedWriter bw;
    Transcoder trans;
    HashMap<String, StringBuilder> upperGrcHm;
    String serFileName;

    /**
     * Constructor
     * @param inFileName input file name
     * @param outFileName output file name
     * @param transFileName transcoding file name, containing the rules for transcoding lower case to upper case
     * @param serFileName serialized file containing the dictionary
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public UpperCaseDictMaker(String inFileName, String outFileName, String transFileName, String serFileName) throws FileNotFoundException, IOException{
        this.serFileName=serFileName;
        br=new BufferedReader(new FileReader(inFileName));
        bw=new BufferedWriter(new FileWriter(outFileName));
        upperGrcHm=new HashMap<>(1500000);
        trans=new Transcoder(transFileName);
    }

    /**
     * Makes the uppercase dictionary
     * @throws IOException 
     */
    public void makeUpperCaseDict() throws IOException{
       String word;
       String upperWord;
       StringBuilder lowerLine;
        while((word=br.readLine())!=null){
            upperWord=trans.parse(word);
            lowerLine=((lowerLine=upperGrcHm.get(upperWord))==null?new StringBuilder(""):lowerLine);
            lowerLine=((lowerLine.toString().equals(""))?lowerLine.append(word):lowerLine.append(" ").append(word));
            upperGrcHm.put(upperWord,lowerLine);
            
        }
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(serFileName));
        out.writeObject(upperGrcHm);
        out.close();
        SortedSet<String> keys = new TreeSet<>(upperGrcHm.keySet());
        for(String key:keys){
            bw.write(key+"\t"+upperGrcHm.get(key).toString());
            bw.newLine();
        }
        bw.close();
    }
    
    /**
     * Main method
     * @param args inFileName, outFileName, transFileName, serFileName
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception{
        String inFileName=args[0]; //e.g. greek.dic
        String outFileName=args[1]; //e.g. up2low-greek.dic
        String transFileName=args[2]; //e.g. low2up.txt
        String serFileName=args[3]; //e.g. up2low-greek.ser
        UpperCaseDictMaker ucdm=new UpperCaseDictMaker(inFileName,outFileName,transFileName,serFileName);
        ucdm.makeUpperCaseDict();
    }
}
