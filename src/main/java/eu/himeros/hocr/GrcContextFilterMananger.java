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

import java.util.LinkedList;
import org.jdom2.Element;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class GrcContextFilterMananger implements ContextFilterManager {

    private LinkedList<Element> queue;
    private GrcSuggestionInContextFilter gsicf;

    public GrcContextFilterMananger() {
        super();
        queue = new LinkedList<>();
        queue.add(new Element("void"));
        queue.add(new Element("void"));
        gsicf = new GrcSuggestionInContextFilter();
    }

    @Override
    public void addSuitableElement(Element el) {
        queue.add(el);
    }

    @Override
    public void adjustPreviousSuitableElement() {
        Element prevEl = queue.poll();
        Element currEl = queue.peek();
        Element nextEl = queue.get(1);
        try {
            Element prevInfo = prevEl.getChild("span", prevEl.getNamespace());
            Element currInfo = currEl.getChild("span", currEl.getNamespace());
            Element nextInfo = nextEl.getChild("span", nextEl.getNamespace());
            if (currInfo != null && "UCWORD".equals(currInfo.getAttributeValue("class"))) {
                String suggestions="";
                try{
                    suggestions = filterSuggestions(currInfo.getText(), prevInfo.getText(), nextInfo.getText(), currInfo.getAttributeValue("title"));
                }catch(NullPointerException npex){
                    //
                }
                if (suggestions.trim().contains(" ")) {
                    currInfo.setAttribute("title", suggestions);
                } else if (suggestions.length() > 0) {
                    currInfo.setAttribute("class", "CORRWORD");
                    currInfo.setAttribute("title", currInfo.getText());
                    currInfo.setText(suggestions);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private String filterSuggestions(String currWord, String prevWord, String nextWord, String suggestionStr) {
        return gsicf.filterSuggestions(currWord, prevWord, nextWord, suggestionStr);
    }
}
