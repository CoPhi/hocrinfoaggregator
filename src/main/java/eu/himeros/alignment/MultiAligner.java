/*
 * Copyright © 2009 Perseus Project - Tufts University <http://www.perseus.tufts.edu>
 *
 * This file is part of UniCollatorPerseus.
 *
 * AlignmentPerseus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * AlignmentPerseus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with AlignmentPerseus.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.himeros.alignment;

//import eu.himeros.ocr.AdjustOcr;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Perform multiple alignment.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class MultiAligner {
    private ArrayList<String> errorPatternFileNames=new ArrayList<String>();
    private StringBuffer[] sbs=null;
    private BufferedWriter bw=null;
    private String gapSymbols="\u00B0#@";
    private String corruptedSymbol="^";
    private String rcSymbol="\u00B6";
    private String charFilter;
    /**
     * Default Constructor.
     */
    public MultiAligner(){}

    /**
     *
     * @param fileName
     */
    public void addErrorPatternFile(String fileName){
        errorPatternFileNames.add(fileName);
    }

    /**
     * Execute the multiple alignment.
     *
     * @param al the vector with text to be aligned.
     * @param multiAlignedFileName the alignment report file name.
     * @param mergedFileName the aligned text output file name.
     * @param upperTransFileName the file to transcode lowercase in uppercase characters.
     */
    //TODO create methods to set gap characters and filters.

    public void exec(ArrayList<String>[] al, String mergedFileName){
        exec(al,null,mergedFileName);
    }

    public void exec(ArrayList<String>[] al, String multiAlignedFileName, String mergedFileName){
        int n=3;
        if(multiAlignedFileName!=null){
            try {
                bw=new BufferedWriter(new FileWriter(multiAlignedFileName));
            } catch (IOException ex) {
                Logger.getLogger(MultiAligner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        BufferedWriter bwText=null; 
        try {
            bwText = new BufferedWriter(new FileWriter(mergedFileName)); // aligned text
        } catch (IOException ex) {
            Logger.getLogger(MultiAligner.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringAligner alg=new StringAligner(new UpperCaseSimEvaluator());
        MutualCorrector mcorr=new MutualCorrector(errorPatternFileNames);
        AlignedSpellChecker asc=new AlignedSpellChecker();
        asc.setCharFilter(charFilter);
        sbs=new StringBuffer[n+1];
        alg.setGapPenalty(-1);
        String[][] algs=new String[n][];
        int len=al[0].size();
        for(int i=0;i<len;i++){
            for(int j=0;j<n+1;j++){
                sbs[j]=new StringBuffer(5000);
            }
            alg.setGapChar(gapSymbols.charAt(1));
            algs[0]=alg.align(al[0].get(i),al[1].get(i));
            alg.setGapChar(gapSymbols.charAt(0));
            algs[1]=alg.align(al[2].get(i), al[0].get(i)); // al[3] instead of al[0]
            alg.setGapChar(gapSymbols.charAt(2));
            algs[2]=alg.align(algs[0][1],algs[1][0]);
            algs[0][0]=alg.adjustGap(algs[0][0],algs[2][0]);
            algs[1][1]=alg.adjustGap(algs[1][1],algs[2][1]);
            algs[0][0]=algs[0][0].replaceAll("["+gapSymbols.substring(1,3)+"]",gapSymbols.substring(0,1));
            algs[2][0]=algs[2][0].replaceAll("["+gapSymbols.substring(1,3)+"]",gapSymbols.substring(0,1));
            algs[2][1]=algs[2][1].replaceAll("["+gapSymbols.substring(1,3)+"]",gapSymbols.substring(0,1));
            ArrayList<String> strV=new ArrayList<String>();
            strV.add(algs[0][0]);
            strV.add(algs[2][0]);
            strV.add(algs[2][1]);
            String mergedStr=mcorr.merge(strV);
            //AdjustClassicalOcr adjocr = new AdjustClassicalOcr(System.getProperty("eu.himeros.ocr2grk"), System.getProperty("eu.himeros.ocr2lat"));
            //mergedStr=adjocr.adjust(mergedStr);
            String correctedStr=asc.correct(strV, mergedStr);
            correctedStr=correctedStr.replaceAll(corruptedSymbol,"");
            try{
                sbs[0].append(algs[0][0]);
                sbs[1].append(algs[2][0]);
                sbs[2].append(algs[2][1]);
                sbs[3].append(mergedStr);
                correctedStr=correctedStr.replaceAll("["+gapSymbols.substring(0,1)+corruptedSymbol+"]", "").trim();
                correctedStr=correctedStr.replaceAll(" +"," ")+" ";
                correctedStr=correctedStr.replaceAll(rcSymbol,"\n");
                //!!!???correctedStr=correctedStr.replaceAll("([\u0370-\u03FF\u1F00-\u1FFF])([^\u0370-\u03FF\u1F00-\u1FFF \n])([\u0370-\u03FF\u1F00-\u1FFF])","$1$3");
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //AdjustOcr adjocr=AdjustOcr.newInstance("eu.himeros.ocr.AdjustClassicalOcr");
                //adjocr.makeAdjusters(new String[]{System.getProperty("eu.himeros.ocr2grk"),System.getProperty("eu.himeros.ocr2lat")});
                //correctedStr=adjocr.adjust(correctedStr);
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                bwText.write(correctedStr);
            }catch(Exception ex){ex.printStackTrace(System.err);}
        }
        try{
            if(multiAlignedFileName!=null){
                writeReport();
                bw.close();
            }
            bwText.close();
        }catch(Exception ex){ex.printStackTrace(System.err);}
    }
    
    private void writeReport(){
        int mod=80;
        int len=sbs[0].length();
        int offset=0;
        int beg;
        int end;
        try{
            while(offset<len){
                beg=offset;
                end=beg+mod;
                if(end>len) end=len;
                bw.write(sbs[0].substring(beg,end));bw.newLine();
                bw.write(sbs[1].substring(beg,end));bw.newLine();
                bw.write(sbs[2].substring(beg,end));bw.newLine();
                //bw.write(sbs[3].substring(beg,end));bw.newLine();
                bw.newLine();
                offset+=mod;
            }
        }catch(Exception ex){ex.printStackTrace(System.err);}
    }

    public String getCorruptedSymbol() {
        return corruptedSymbol;
    }

    public void setCorruptedSymbol(String corruptedSymbol) {
        this.corruptedSymbol = corruptedSymbol;
    }

    public String getGapSymbols() {
        return gapSymbols;
    }

    public void setGapSymbols(String gapSymbols) {
        this.gapSymbols = gapSymbols;
    }

    public String getRcSymbol() {
        return rcSymbol;
    }

    public void setRcSymbol(String rcSymbol) {
        this.rcSymbol = rcSymbol;
    }

    public String getCharFilter() {
        return charFilter;
    }

    public void setCharFilter(String charFilter) {
        this.charFilter = charFilter;
    }
    
    
    
}