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

import eu.himeros.alignment.HocrSimEvaluator;
import eu.himeros.alignment.ObjectAligner;
import eu.himeros.alignment.StringAligner;
import eu.himeros.alignment.UpperCaseSimEvaluator;
import eu.himeros.digitaledition.AlignedQuotationParser;
import eu.himeros.spellchecker.LuceneSpellChecker;
import eu.himeros.text.GrcNormalizer;
import eu.himeros.transcoder.Transcoder;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.HyphenationTree;
import org.apache.fop.hyphenation.Hyphenator;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author federico_D0T_boschetti_D0T_73_AT_gmail_D0T_com
 */
public class HocrInfoAggregator {

    final char hyphenChar = '\u00ac'; //TODO: move in the property list
    final String l1NonAlphabeticFilter = "[^Α-Ω]"; //TODO: move in the property list
    final String l1PunctMarkFilter = "[.,;\u0387\\(\\)\\[\\]]";
    final String l1LeftPunctMarkFilter = "^([\\(\\[]).*?$";
    final String l1RightPunctMarkFilter = "^.*?([.,;\u0387][\\)\\]’]?)$";
    final String l1NumFilter = "[0-9]+";
    final String l1PunctMarkExtFilter = "[\u0300-\u0379“]";
    final String l1CharSetFilter = "[\\(]?[’\u0370-\u0386\u0388-\u03FF\u1F00-\u1FFF]+[.,;\u0387\\)]?";
    final String l2CharSetFilter = "^[\\(]?[’a-zA-Z\\-]+[.,;:\\?\\)]?[’\\]]?$";
    private Pattern l1LeftPunctMarkPattern = Pattern.compile(l1LeftPunctMarkFilter);
    private Pattern l1RightPunctMarkPattern = Pattern.compile(l1RightPunctMarkFilter);
    private int id = 1;
    private SAXBuilder builder = null;
    private Document doc = null;
    private Element root = null;
    private XPathExpression<Element> xpath = null;
    private XMLOutputter xop = null;
    private Namespace xmlns = null;
    private GrcNormalizer normalizer2 = new GrcNormalizer(); //TODO: generalize
    private WordAdjuster adjuster = new GrcWordAdjuster(); //TODO: generalize
    private HashSet<String> l1Hs = new HashSet<>();
    private HashSet<String> syllHs = new HashSet<>();
    private HashMap<String, StringBuilder> upL1Hm = new HashMap<>();
    private Transcoder low2upL1Trans = new Transcoder();
    private Transcoder up2lowL1Trans = new Transcoder();
    private HyphenationTree l1HyphenTree = null;
    private Element hyphenPart1 = null;
    private ContextFilterManager l1Fm = null; //TODO: generalize
    private HashMap<String, Integer> occHm = new HashMap<>(2048);
    private AlignedQuotationParser aqp = null;
    private Element nearGt = null;
    private HashMap<String, Element> nearGtHm = new HashMap<>(2048);
    private HashMap<Integer, Element> nearGtIdHm = new HashMap<>(2048);
    private StringAligner sa=new StringAligner(new UpperCaseSimEvaluator());
    
    public static void main(String[] args) throws Exception {
        HocrInfoAggregator hocrInfoAggregator = new HocrInfoAggregator(args[0]);
        hocrInfoAggregator.parse();
        hocrInfoAggregator.alignToGroundTruth();
        hocrInfoAggregator.output(args[1]);
    }

    public HocrInfoAggregator() throws Exception {
        init();
    }

    public HocrInfoAggregator(String inFileName) throws Exception {
        try {
            init();
            initFile(inFileName);
        } catch (JDOMException | IOException | ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void init() throws IOException, ClassNotFoundException {
        UpperCaseSimEvaluator.setResourceName("eu/himeros/resources/transcoders/low2up.txt");
        HashMap<String,String> langSpellcheckerMap=new HashMap<>();
        langSpellcheckerMap.put("grc",System.getProperty("grc.lucene.spellchecker"));
        LuceneSpellChecker.init(langSpellcheckerMap);
        low2upL1Trans.setTranscoder(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/transcoders/low2up.txt"));
        up2lowL1Trans.setTranscoder(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/transcoders/low2up.txt"));
        up2lowL1Trans.reverse();
        l1HyphenTree = Hyphenator.getFopHyphenationTree("el_EL");
        ObjectInputStream in = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/sers/grchs.ser"));
        l1Hs = (HashSet) in.readObject();
        in.close();
        in = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/sers/up2low-greek.ser"));
        upL1Hm = (HashMap) in.readObject();
        in.close();
        in = new ObjectInputStream(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/sers/syllhs.ser"));
        syllHs = (HashSet) in.readObject();
        in.close();
    }

    public void initFile(String inFileName) throws Exception {
        builder = new SAXBuilder();
        doc = builder.build(inFileName);
        root = doc.getRootElement();
        xmlns = root.getNamespace();
        l1Fm = new GreekContextFilterMananger(); //TODO: generalize
        aqp = new AlignedQuotationParser();
        nearGt = aqp.parse(inFileName.substring(0, inFileName.length() - 5) + ".ngt.xml"); //TODO : generalize
        makeNearGtHm();
    }

    private void makeNearGtHm() {
        List<Element> words = nearGt.getChildren();
        for (Element word : words) {
            nearGtHm.put(word.getAttributeValue("uc"), word);
            nearGtIdHm.put(Integer.parseInt(word.getAttributeValue("id")), word);
            IteratorIterable<Content> iterator = word.getDescendants();
            while (iterator.hasNext()) {
                Element nestedWord = (Element) iterator.next();
                nearGtIdHm.put(Integer.parseInt(nestedWord.getAttributeValue("id")), nestedWord);
            }
        }
    }

    public void parse() {
        parse(root);
        updateElements();
    }

    private void parse(Element el) {
        for (Element ocrPage : el.getChild("body", xmlns).getChildren("div", xmlns)) {
            for (Element ocrLine : ocrPage.getChildren("span", xmlns)) {
                for (Element ocrWord : ocrLine.getChildren("span", xmlns)) {
                    parseOcrWord(ocrWord);
                }
            }
        }
    }

    private void parseOcrWord(Element ocrWord) {
        String text = ocrWord.getText();
        text = adjuster.adjust(new String[]{"monotonic2polytonic", "ocr2u"}, normalizer2.normalize(text));
        String upText = low2upL1Trans.parse(text);
        if (text.endsWith("-")) {
            ocrWord.setAttribute("idx", "" + id++);
            hyphenPart1 = ocrWord;
            return;
        } else if (hyphenPart1 != null) {
            text = adjuster.adjust(new String[]{"monotonic2polytonic", "ocr2u"}, normalizer2.normalize(parseOcrHyphenatedWord(hyphenPart1, ocrWord)));
            upText = low2upL1Trans.parse(text);
        }
        Element infoSpan = new Element("span", xmlns);
        infoSpan.setText(adjuster.adjust(new String[]{"monotonic2polytonic", "ocr2u"}, normalizer2.normalize(ocrWord.getText())));
        upText = upText.replaceAll(l1NonAlphabeticFilter, "");
        infoSpan.setAttribute("id", "" + id++);
        Integer occ;
        occ = ((occ = occHm.get(upText)) == null ? 1 : ++occ);
        occHm.put(upText, occ);
        infoSpan.setAttribute("uc", upText);
        try {
            ocrWord.getContent(0).detach();
        } catch (Exception ex) {
        }
        Token token = new Token(text);
        token = setClassiFicationAndScore(token);
        infoSpan = setInfoSpanClass(token, infoSpan);
        ocrWord.addContent(infoSpan);
        l1Fm.addSuitableElement(ocrWord);
        l1Fm.adjustPreviousSuitableElement();
        if (hyphenPart1 != null) {
            text = hyphenPart1.getText();
            hyphenPart1.getContent(0).detach();
            Element infoSpan1 = new Element("span", xmlns);
            infoSpan1.setAttribute("class", infoSpan.getAttributeValue("class"));
            infoSpan1.setText(text);
            hyphenPart1.addContent(infoSpan1);
            hyphenPart1 = null; //TODO: ???
        }
    }

    private String parseOcrHyphenatedWord(Element part1, Element part2) {
        String res = "";
        try {
            res = part1.getText().substring(0, part1.getText().length() - 1) + part2.getText();
        } catch (Exception ex) {
        }
        return res;
    }

    private Token setClassiFicationAndScore(Token token) {
        String sampleOrig = ((token.getWholeWord() == null) ? token.getText() : token.getWholeWord());
        String sample = sampleOrig.replaceAll(l1PunctMarkFilter + "[\\)’]?", "");
        sample = sample.replaceAll("[\\(‘]?", "");
        if (l1Hs.contains(sample) || l1Hs.contains(up2lowL1Trans.parse(sample)) || sample.matches(l1NumFilter)) {
            token.setClassification(Token.Classification.WORD);
            token.setScore(token.getLengthAsDouble());
        } else if (upL1Hm.containsKey(low2upL1Trans.parse(sample.replaceAll(l1PunctMarkExtFilter, "")))) {
            token.setClassification(Token.Classification.UCWORD);
            token.setScore(token.getLengthAsDouble() - token.getLengthAsDouble() / 5);
        } else if (testSyllSeq(sample)) {
            token.setClassification(Token.Classification.SYLLABICSEQ);
            token.setScore(token.getLengthAsDouble() - token.getLengthAsDouble() / 3);
        } else if (testCharSeq(sampleOrig) && sampleOrig.length() > 1) {
            token.setClassification(Token.Classification.CHARSEQ);
            token.setScore(token.getLengthAsDouble() - token.getLengthAsDouble() / 2);
        } else if (testL2CharSeq(sampleOrig)) {
            token.setClassification(Token.Classification.L2WORD);
            token.setScore(token.getLengthAsDouble());
        } else {
            if (sampleOrig.length() > 1) {
                token.setClassification(Token.Classification.BADMANY);
                token.setScore(0);
            } else if (!"\n".equals(sampleOrig)) {
                token.setClassification(Token.Classification.BADONE);
                token.setScore(0);
            }
        }
        return token;
    }

    private boolean testSyllSeq(String str) {
        boolean res = false;
        Hyphenation hp = l1HyphenTree.hyphenate(str, 0, 0);
        int beg = 0;
        int end = str.length();
        int[] poss;
        if (hp == null) {
            poss = new int[1];
            poss[0] = end;
        } else {
            int[] ips = hp.getHyphenationPoints();
            poss = new int[ips.length + 1];
            System.arraycopy(ips, 0, poss, 0, ips.length);
            poss[poss.length - 1] = end;
        }
        int pos;
        String syll;
        for (int idx = 0; idx < poss.length; idx++) {
            if (idx < poss.length - 1) {
                pos = poss[idx] + 1;
                if (idx == 0) {
                    syll = "^";
                } else {
                    syll = "";
                }
                syll += str.substring(beg, pos);
            } else {
                pos = poss[idx];
                syll = "" + str.substring(beg, pos) + "#";
            }
            beg = poss[idx];
            if (syllHs.contains(syll)) {
                res = true;
            } else {
                return false;
            }
        }
        return res;
    }

    private boolean testCharSeq(String str) {
        return str.matches(l1CharSetFilter);
    }

    private boolean testL2CharSeq(String str) {
        //if(str.matches(l2CharSetFilter)) System.out.println(str);
        return str.matches(l2CharSetFilter);
    }

    private Element setInfoSpanClass(Token token, Element infoSpan) {
        switch (token.getClassification()) {
            case WORD:
                infoSpan.setAttribute("class", "WORD");
                break;
            case UCWORD:
                infoSpan.setAttribute("class", "UCWORD");
                infoSpan.setAttribute("title", makeSuggestions(token));
                break;
            case SYLLABICSEQ:
                infoSpan.setAttribute("class", "SYLLABICSEQ");
                infoSpan.setAttribute("title", makeSuggestions(token));
                break;
            case CHARSEQ:
                infoSpan.setAttribute("class", "CHARSEQ");
                infoSpan.setAttribute("title", makeSuggestions(token));
                break;
            case BADONE:
                infoSpan.setAttribute("class", "BADONE");
                infoSpan.setAttribute("title", makeSuggestions(token));
                break;
            case BADMANY:
                infoSpan.setAttribute("class", "BADMANY");
                infoSpan.setAttribute("title", makeSuggestions(token));
                break;
            case L2WORD:
                infoSpan.setAttribute("class", "L2WORD");
                makeSuggestions(token);
                infoSpan.setAttribute("title", token.getText());
                break;
        }
        return infoSpan;
    }

    private String makeSuggestions(Token token) {
        String word;
        StringBuilder sb = new StringBuilder(1000);
        if (token.getPart() != token.getTot() || token.getLength() < 3) {
            return "";
        }
        word = ((token.getWholeWord() == null) ? token.getText() : token.getWholeWord());
        if (token.getClassification() == Token.Classification.UCWORD) {
            String tokenText = token.getText();
            tokenText = tokenText.replaceAll(l1PunctMarkFilter, "");
            tokenText = tokenText.replaceAll(l1PunctMarkExtFilter, "");
            String suggestion = "";
            try {
                suggestion = upL1Hm.get(low2upL1Trans.parse(tokenText)).toString();
            } catch (Exception ex) {
            }
            sb.append(suggestion);
        } else {
            String[] suggestions = LuceneSpellChecker.spellcheck(word, "grc", 3);
            if (suggestions != null && suggestions.length > 0) {
                for (String suggestion : suggestions) {
                    sb.append(suggestion).append(" ");
                }
                if (sb.charAt(sb.length() - 1) == ' ') {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
        }
        return sb.toString();
    }

    private void updateElements() {
        xpath = XPathFactory.instance().compile("//ns:span[@uc!='']", Filters.element(), null, Namespace.getNamespace("ns", "http://www.w3.org/1999/xhtml"));
        List<Element> elements = xpath.evaluate(root);
        for (Element element : elements) {
            String uc = element.getAttributeValue("uc");
            element.setAttribute("occ", "" + occHm.get(uc));
            try {
                if (occHm.get(uc) == 1) {
                    element.setAttribute("anchor", nearGtHm.get(uc).getAttributeValue("uc"));
                    element.setAttribute("anchor-id", nearGtHm.get(uc).getAttributeValue("id"));
                    if ("CORRWORD".equals(element.getAttributeValue("class"))
                            | "UCWORD".equals(element.getAttributeValue("class"))) {
                        String title = element.getAttributeValue("title");
                        title = nearGtHm.get(uc).getAttributeValue("text") + "\u261a " + title;
                        element.setAttribute("title", title);
                    }
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }

    public void alignToGroundTruth() {
        ArrayList<Element> ocrAl = new ArrayList<>();
        ArrayList<Element> nearGtAl;
        int start = 1;
        int end;
        xpath = XPathFactory.instance().compile("//ns:span[@id]", Filters.element(), null, Namespace.getNamespace("ns", "http://www.w3.org/1999/xhtml"));
        List<Element> elements = xpath.evaluate(root);
        for (Element element : elements) {
            if (element.getAttributeValue("anchor-id") == null) {
                if ("".equals(element.getAttributeValue("uc"))) {
                    continue;
                }
                ocrAl.add(element);
            } else {
                end = ((end = Integer.parseInt(element.getAttributeValue("anchor-id")) - 1) < 1 ? 1 : end);
                nearGtAl = makeNearGtAl(start, end);
                makeAlignment(ocrAl, nearGtAl);
                ocrAl = new ArrayList<>();
                start = end + 2;
            }
        }
    }

    private ArrayList<Element> makeNearGtAl(int start, int end) {
        ArrayList<Element> nearGtAl = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            nearGtAl.add(nearGtIdHm.get(i));
        }
        return nearGtAl;
    }

    private void makeAlignment(ArrayList<Element> ocrAl, ArrayList<Element> nearGtAl) {
        ObjectAligner<Element> oala = new ObjectAligner<>();
        oala.setSimEvaluator(new HocrSimEvaluator());
        List<List<Element>> elRes = oala.align(ocrAl, nearGtAl);
        for (int i = 0; i < elRes.get(0).size(); i++) {
            if (elRes.get(0).get(i) != null && elRes.get(1).get(i) != null) {
                String title = elRes.get(0).get(i).getAttributeValue("title");
                if (title == null) {
                    title = "";
                }
                String uc1 = elRes.get(0).get(i).getAttributeValue("uc");
                String uc2 = elRes.get(1).get(i).getAttributeValue("uc");
                if (uc1 == null) {
                    uc1 = "";
                }
                if (uc2 == null) {
                    uc2 = "";
                }
                if (!uc1.equals(uc2)) {
                    title = elRes.get(1).get(i).getAttributeValue("text") + "\u261a " + title;
                    elRes.get(0).get(i).setAttribute("title", title);
                }
            }
        }
    }

    public void output(String outFileName) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8"))) {
            xop = new XMLOutputter(Format.getPrettyFormat().setLineSeparator("\n"));
            makeCompliantHocr();
            xop.output(doc, bw);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void makeCompliantHocr() {
        xpath = XPathFactory.instance().compile("//ns:span[@id|@idx]", Filters.element(), null, Namespace.getNamespace("ns", "http://www.w3.org/1999/xhtml"));
        List<Element> elements = xpath.evaluate(root);
        int spanId = 0;
        for (Element span : elements) {
            if (span.getAttribute("idx") != null) {
                try {
                    span = span.getChildren().get(0);
                } catch (Exception ex) {
                    //
                }
            }
            LinkedList<Attribute> attributeLl = new LinkedList(span.getParentElement().getAttributes());
            attributeLl.addFirst(new Attribute("id", "w_" + spanId++));
            span.getParentElement().setAttributes(attributeLl);
            String[] suggestions = null;
            String title = span.getAttributeValue("title");
            if (title != null) {
                suggestions = title.split(" ");
            }
            if (suggestions == null) {
                suggestions = new String[]{""};
            }
            Element ins = new Element("ins", xmlns);
            ins.setAttribute("class", "alt");
            ins.setAttribute("title", makeNlp(span.getAttributeValue("class")));
            ins.setText(span.getText());
            span.removeContent();
            span.addContent(ins);
            span.setAttribute("class", "alternatives");
            span.removeAttribute("uc");
            span.removeAttribute("occ");
            span.removeAttribute("title");
            span.removeAttribute("anchor");
            span.removeAttribute("anchor-id");
            span.removeAttribute("id");
            span.getParentElement().removeAttribute("idx");
            span.removeAttribute("whole");
            span.getParentElement().removeAttribute("whole");
            if (title == null || "".equals(title)) {
                continue;
            }
            double score = 0.90;
            for (String suggestion : suggestions) {
                if (suggestion == null || "".equals(suggestion)) {
                    continue;
                }
                Element del = new Element("del", xmlns);
                del.setAttribute("title", "nlp " + String.format("%.2f", score));
                score = score - 0.01;
                suggestion = suggestion.replaceAll(l1PunctMarkFilter, "");
                Matcher leftMatcher = l1LeftPunctMarkPattern.matcher(ins.getText());
                if (leftMatcher.matches()) {
                    suggestion = leftMatcher.group(1) + suggestion;
                }
                Matcher rightMatcher = l1RightPunctMarkPattern.matcher(ins.getText());
                if (rightMatcher.matches()) {
                    String ngtSymbol = "";
                    if (suggestion.endsWith("\u261a")) {
                        ngtSymbol = "\u261a";
                        suggestion = suggestion.substring(0, suggestion.length() - 1);
                    }
                    suggestion = suggestion + rightMatcher.group(1) + ngtSymbol;
                }
                ///!!!!
                if (suggestion.endsWith("\u261a") && ins.getParentElement().getParentElement().getAttributeValue("lang", Namespace.XML_NAMESPACE) != null) {
                    String buff = suggestion.substring(0,suggestion.length()-1);
                    sa.align(buff,ins.getText());
                    double sim=1-sa.getEditDistance()/Math.max((double)buff.length(),(double)ins.getText().length());
                    if(sim>0.6){
                        
                        suggestion = ins.getText() + "\u261b";
                        ins.setText(buff);
                        ins.setAttribute("title", "nlp 0.70");
                    }
                }
                del.addContent(suggestion);
                span.addContent(del);
            }
        }
    }

    private String makeNlp(String clazz) {
        switch (clazz) {
            case "WORD":
                return "nlp 1.00";
            case "CORRWORD":
                return "nlp 0.99";
            case "UCWORD":
                return "nlp 0.98";
            case "SYLLABICSEQ":
                return "nlp 0.97";
            case "CHARSEQ":
                return "nlp 0.96";
            case "BADONE":
                return "nlp 0.95";
            case "BADMANY":
                return "nlp 0.94";
            case "L2WORD":
                return "nlp 0.10";
            default:
                return "nlp 0.93";
        }
    }
}
