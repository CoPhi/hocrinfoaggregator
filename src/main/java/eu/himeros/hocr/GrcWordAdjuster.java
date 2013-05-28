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

import eu.himeros.transcoder.Transcoder;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class GrcWordAdjuster implements WordAdjuster {
    Transcoder monotonic2polytonic=new Transcoder();
    Transcoder ocr2u=new Transcoder();
    
    public GrcWordAdjuster(){
         monotonic2polytonic.setTranscoder(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/transcoders/monotonic2polytonic.txt"));
         ocr2u.setTranscoder(ClassLoader.getSystemResourceAsStream("eu/himeros/resources/transcoders/ocr2u.txt"));
        
    }

    @Override
    public String adjust(String[] features, String word){
        for(String feature:features){
            word=adjust(feature,word);
        }
        return word;
    }
    
    @Override
    public String adjust(String feature, String word) {
        switch (feature) {
            case "monotonic2polytonic":
                return monotonic2polytonic.parse(word);
            case "ocr2u":
                return ocr2u.parse(word);
            default: return null;
        }
        
    }
}
