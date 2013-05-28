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

package eu.himeros.text;

import com.ibm.icu.text.Normalizer2;
import java.io.InputStream;

/**
 * Normalize Greek strings according to the icu directions but uses polytonic
 * acute accents
 * 
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class GrcNormalizer {
    Normalizer2 normalizer2;
    InputStream is;
    
    /**
     * Constructor
     */
    public GrcNormalizer(){
        is=ClassLoader.getSystemResourceAsStream("eu/himeros/resources/nrm/grc2nfcbe.nrm");
        normalizer2=Normalizer2.getInstance(is,"nfc", Normalizer2.Mode.COMPOSE);
    }
    
    /**
     * Normalize the source string
     * @param src source string
     * @return 
     */
    public String normalize(String src){
        return normalizer2.normalize(src);
    }
}
