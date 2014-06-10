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

import com.ibm.icu.text.Normalizer2;
import eu.himeros.transcoder.Transcoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class GrcSuggestionInContextFilter extends SuggestionInContextFilter {
    private boolean capitalPredictable;
    private String accentedCharRegex=".*?[άάἌἄἍἅᾴᾌᾄᾍᾅέέἜἔἝἕήήἬἤἭἥῄᾜᾔᾝᾕΐίἴἵόόὌὄὍὅύὔὕΰώώὬὤὭὥῴᾬᾤᾭᾥὰἊἂἋἃᾲᾊᾂᾋᾃὲἚἒἛἓὴἪἢἫἣῂᾚᾒᾛᾓῒὶἲἳὸὊὂὋὃὺὒὓῢὼὪὢὫὣῲᾪᾢᾫᾣᾶἎἆἏἇᾷᾎᾆᾏᾇῆἮἦἯἧῇᾞᾖᾟᾗῗῧῶὮὦὯὧῷᾮᾦᾯᾧῖἶἷῦὖὗ].*?";
    private String graveCharRegex=".*?[ὰἊἂἋἃᾲᾊᾂᾋᾃὲἚἒἛἓὴἪἢἫἣῂᾚᾒᾛᾓῒὶἲἳὸὊὂὋὃὺὒὓῢὼὪὢὫὣῲᾪᾢᾫᾣ].*?";
    private String enclitAccentRegex=".*?[άάἌἄἍἅᾴᾌᾄᾍᾅέέἜἔἝἕήήἬἤἭἥῄᾜᾔᾝᾕΐίἴἵόόὌὄὍὅύὔὕΰώώὬὤὭὥῴᾬᾤᾭᾥᾶἎἆἏἇᾷᾎᾆᾏᾇῆἮἦἯἧῇᾞᾖᾟᾗῗῧῶὮὦὯὧῷᾮᾦᾯᾧῖἶἷῦὖὗ].*?[άέήίόύώ].*?";
    private String uppercaseRegex= ".*?[ΑἈἉᾼἌἍἊἋἎἏᾈᾉᾌᾍᾊᾋᾎᾏΒΓΔΕἘἙἜἝἚἛΖΗἨἩῌἬἭἪἫἮἯᾙᾙᾜᾝᾚᾛᾞᾟΘΙἸἹἼἽἺἻἾἿΚΛΜΝΞΟὈὉὌὍὊὋΠΡΣΤΥὙὝὛὟΦΧΨΩὨὩῼὬὭὪὫὮὯᾨᾩᾬᾭᾪᾫᾮᾯ].*?";
    private String iotaSubCharRegex=".*?[ῃῇῳῷᾳᾷ].?.?$";
    private ArrayList<String> suggestions;
    private Transcoder trans;
    private Normalizer2 normalizer2 = Normalizer2.getNFCInstance();
    private Pattern p=Pattern.compile("([^\u0300-\u03FF\u1F00-\u1FFF]*)([\u0300-\u03FF\u1F00-\u1FFF]+)([^\u0300-\u03FF\u1F00-\u1FFF]*.?.?)");
    
    public GrcSuggestionInContextFilter(){
        trans=new Transcoder();
        trans.setTranscoder(this.getClass().getResourceAsStream("/eu/himeros/resources/transcoders/lowercase2uppercase.txt"));
        trans.reverse();
    }
    
    public boolean isCapitalRequired() {
        return currWord.matches("^.?.?[ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ].*?")||(isCapitalPredictable()&&isFirstWordInSentence());
    }

    private static String adjustAccents(String str){
        str=str.replaceAll("\u03AC","\u1F71");
        str=str.replaceAll("\u03AD","\u1F73");
        str=str.replaceAll("\u03AE","\u1F75");
        str=str.replaceAll("\u03AF","\u1F77");
        str=str.replaceAll("\u03CC","\u1F79");
        str=str.replaceAll("\u03CD","\u1F7B");
        str=str.replaceAll("\u03CE","\u1F7D");
        str=str.replaceAll("\u03CD","\u1F7B");
        return str;
    }
    
    public boolean isEncliticAccentRequired() {
        String normNextWord=adjustAccents(normalizer2.normalize(nextWord));
        return !normNextWord.matches(accentedCharRegex);
    }
    

    public boolean isFirstWordInSentence() {
        return isWordFollowedByPunctMark(prevWord);
    }

    public boolean isGraveRequired() {
        return !isWordFollowedByPunctMark(currWord);
    }

    public boolean isIotaSubRequired() {
        return currWord.matches(iotaSubCharRegex);
    }
    
    public boolean isWordFollowedByPunctMark(String word) {
        return word.matches(".*?[.,;\u38B7].*?");
    }

    public boolean isCapitalPredictable() {
        return capitalPredictable;
    }

    public void setCapitalPredictable(boolean capitalPredictable) {
        this.capitalPredictable = capitalPredictable;
    }

    public TreeSet<String> filterSuggestions(String currWord, String prevWord, String nextWord, ArrayList<String> suggestionAl){
        Matcher m=p.matcher(currWord);
        String prefix="";
        String suffix="";
        if(m.matches()){
            prefix=m.group(1);
            suffix=m.group(3);
        }
        TreeSet<String> res=new TreeSet<>();
        String prevAddedSuggestion="";
        boolean graveSuggestion;
        boolean encliticAccentSuggestion;
        boolean iotaSubSuggestion;
        if(suggestionAl.isEmpty()){
             System.out.println("empty");
            res.add("");
            return res;
        }
        for(String suggestion:suggestionAl){
            if(currWord.matches(uppercaseRegex)){
                StringBuilder suggestionSb=new StringBuilder();
                if(suggestion!=null&&suggestion.length()>0){
                    if(trans.decode(suggestion.charAt(0))!=null){
                        suggestionSb.append(trans.decode(suggestion.charAt(0)));
                        if(suggestion.length()>1) suggestionSb.append(suggestion.substring(1,suggestion.length()));
                        suggestion=suggestionSb.toString();
                    }
                }
            }else{
                    if(suggestion!=null&&suggestion.matches(uppercaseRegex)) continue;
            }
            if(suggestionAl.size()==1){
                res.add(prefix+suggestion+suffix);
            }else{
                graveSuggestion=suggestion.matches(graveCharRegex);
                encliticAccentSuggestion=suggestion.matches(enclitAccentRegex);
                iotaSubSuggestion=suggestion.matches(iotaSubCharRegex);
                //fix iotasub use!!!
                if((isGraveRequired()&&graveSuggestion)||
                   (isEncliticAccentRequired()&&encliticAccentSuggestion)){
                    res.remove(prefix+prevAddedSuggestion+suffix);
                    res.add(prefix+suggestion+suffix);
                    prevAddedSuggestion=suggestion;
                }else if(!graveSuggestion&&!encliticAccentSuggestion){
                    res.add(prefix+suggestion+suffix);
                    prevAddedSuggestion=suggestion;
                }
            }
        }
        return res;
    }
    
    public String filterSuggestions(String currWord, String prevWord, String nextWord, String suggestionStr){
        this.currWord=currWord;
        this.prevWord=prevWord;
        this.nextWord=nextWord;
        String[] suggestionArray=suggestionStr.split(" ");
        ArrayList<String> suggestionAl=new ArrayList<>();
        StringBuilder resSb=new StringBuilder();
        suggestionAl.addAll(Arrays.asList(suggestionArray));
        TreeSet<String> resTs=filterSuggestions(currWord,prevWord,nextWord,suggestionAl);
        for(String suggestion:resTs){
            resSb.append(suggestion).append(" ");
        }
        try{
            return resSb.toString().substring(0,resSb.length()-1);
        }catch(Exception ex){
            return currWord;
        }
    }
}
