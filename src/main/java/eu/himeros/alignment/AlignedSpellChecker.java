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

//TODO : ADJUST THIS CLASS!!!

package eu.himeros.alignment;

//import jaspellbridge.Jaspeller;
import eu.himeros.spellchecker.LuceneSpellChecker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accept the Spell-checker suggestion if it is supported by the aligned strings.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class AlignedSpellChecker {
    StringBuffer sb=new StringBuffer();
    char gapChar='\u00B0'; //default gap character
    String gap=""+gapChar;
    String charFilter; //default character filter (for Ancient Greek).
    Pattern p=Pattern.compile(charFilter+"+"); //default pattern
    Matcher m=null;
    String lang="grc"; //defualt language
    String filter="";
    char corruptedChar='^'; //default metacharacter to indicate that a character is an OCR corruption
    String corrupted=""+corruptedChar;

    /**
     * Default Constructor
     */
    public AlignedSpellChecker(){}

    /**
     * Set the gap character (providing single character string).
     * @param gap the gap single character string.
     */
    public void setGap(String gap){
        this.gap=gap;
        gapChar=gap.charAt(0);
    }

    /**
     * Set the gap character.
     * @param gapChar the gap character.
     */
    public void setGap(char gapChar){
        this.gapChar=gapChar;
        gap=""+gapChar;
    }

    /**
     * Set the metacharacter that indicates OCR corruption
     * (providing a single character string).
     *
     * @param corrupted the OCR corruption indicator, a single character string.
     */
    public void setCorrupted(String corrupted){
        this.corrupted=corrupted;
        corruptedChar=corrupted.charAt(0);
    }

    /**
     * Set the metacharacter that indicates OCR corruption.
     *
     * @param corruptedChar the OCR corruption indicator.
     */
    public void setCorruptedChar(char corruptedChar){
        this.corruptedChar=corruptedChar;
        corrupted=""+corruptedChar;
    }


    /**
     * Set language for the Spell-checker.
     * Language code must be Aspell compliant.
     *
     * @param lang the language (e.g. <code>grc</code> for Ancient Greek).
     */
    public void setLang(String lang){
        this.lang=lang;
    }


    /**
     * Set the regex expression to filter characters.
     * Characters that do not match are ignored.
     *
     * @param charFilter the regex to filter characters
     */
    public void setCharFilter(String charFilter){
        this.charFilter=charFilter.substring(0,charFilter.length()-1)+gapChar+corrupted+"]";
        p=Pattern.compile(charFilter+"+");
    }

    /**
     * Get the regex used to filter characters.
     * @return the regex to filter characters.
     */
    public String getFilter(){
        return filter;
    }

    /**
     * The code method: correct the string <code>str</code>, if the correction
     * is supported by the strings contained in the vector.<br/>
     * String is processed word by word. If the word is misspelled, each character
     * of each suggestion is compared with the character in the same position of
     * the strings contained in the string vector.
     * Only the first suggestion whose all characters match at least with one
     * of the correspondent characters in the strings contained in the string vector
     * is accepted.
     *
     * @param algV the string vector containing the aligned strings
     * (usually, the output of different OCR engines).
     * @param str the string to be corrected.
     * @return the corrected string.
     */
    public String correct(ArrayList<String> algV, String str){
        String word;
        m=p.matcher(str);
        sb=new StringBuffer();
        while(m.find()){
            word=str.substring(m.start(),m.end());
            filter=makeFilter(algV, m.start(),m.end());
            m.appendReplacement(sb, makeCorrection(filter, word));
        }
        m.appendTail(sb);
        return sb.toString().replaceAll(" [0-9\\.,;]("+charFilter+")"," $1"); //Adjust that!!!
    }

    /**
     * Helper method for <code>correct</code> method.
     * @param filter the regex to filter characters.
     * @param str the word to be corrected.
     * @return the corrected word.
     */
    private String makeCorrection(String filter, String word){
        word=word.replaceAll(gap,"");
        word=word.replaceAll(corrupted,"");
        if(word.length()>2&&LuceneSpellChecker.exist(word.replaceAll("·",""), lang)){
            return word;
        }else if(word.length()==2&&LuceneSpellChecker.exist(word.replaceAll("·","")+"_", lang)){
            return word;
        }else if(word.length()==1&&LuceneSpellChecker.exist(word.replaceAll("·","")+"__", lang)){
            return word;
        }
        //!!!!!!!!!!!!!!!!!!!
        //String suggestionStr=Jaspeller.aspellQuery(word,lang);
        String[] items;
        //StringBuilder itemSeqSb=new StringBuilder(1000);
        if(word.length()!=0){
            items=LuceneSpellChecker.spellcheck(word,lang,10);

            //String[] items=null;
            //!!!!!!!!!!!!!!!!!!!
            boolean first=true;
            if(items!=null){
                for(String item:items){
                    //if(first) itemSeqSb.append(item);
                    //System.out.println(item);
                    if(first){
                        first=false;
                        continue;
                    }
                    // Adjust that!!!
                    //item=item.replaceAll("\u03AC","\u1F71");
                    //item=item.replaceAll("\u03AD","\u1F73");
                    //item=item.replaceAll("\u03AE","\u1F75");
                    //item=item.replaceAll("\u03AF","\u1F77");
                    //item=item.replaceAll("\u03CC","\u1F79");
                    //item=item.replaceAll("\u03CD","\u1F7B");
                    //item=item.replaceAll("\u03CE","\u1F7D");
                    //item=item.replaceAll("\u03CD","\u1F7B");
                    //if(item.matches(filter)&&!item.equals(word)) System.out.println(word+" --> "+item);
                    if(item.matches(filter)) return item;
                }
            }
        }
        //if(word.length()>2&&!word.contains("-")){
            //return word+"["+itemSeqSb.toString()+"]";
        //}else{
            return word;
        //}
    }

    /**
     * Make the filter (a mask regex) that must match Spell-checker suggestion.
     * It is the combination of the correspondent characters in the strings
     * contained in the string vector.
     *
     * @param algV the string vector.
     * @param beg the beginning position of the word.
     * @param end the end position of the word.
     * @return the mask.
     */
    private String makeFilter(ArrayList<String> algV, int beg, int end){
        StringBuffer regexSb=new StringBuffer("");
        HashSet<String> hs=new HashSet<String>();
        String currChar="";
        boolean gapP=false;
        for(int i=beg;i<end;i++){
            for(int j=0;j<algV.size();j++){
                currChar=""+algV.get(j).charAt(i);
                if(currChar.equals(gap)){
                   gapP=true;
                }else if(currChar.equals(corrupted)){
                    hs.add(".");
                }else if(currChar.matches(charFilter)){
                   hs.add(currChar);
                }
            }
            if(hs.size()>0){
                if(hs.size()>1) regexSb.append("[");
                for(String s:hs){
                    regexSb.append(s);
                }
                if(hs.size()>1) regexSb.append("]");
                if(gapP) regexSb.append("?");
            }
            gapP=false;
            hs.clear();
        }
        return regexSb.toString();
    }

}
