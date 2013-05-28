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

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class SuggestionInContextFilter {
    protected String prevWord;
    protected String currWord;
    protected String nextWord;

    public String getCurrWord() {
        return currWord;
    }

    public void setCurrWord(String currWord) {
        this.currWord = currWord;
    }

    public String getNextWord() {
        return nextWord;
    }

    public void setNextWord(String nextWord) {
        this.nextWord = nextWord;
    }

    public String getPrevWord() {
        return prevWord;
    }

    public void setPrevWord(String prevWord) {
        this.prevWord = prevWord;
    }
    
    
}
