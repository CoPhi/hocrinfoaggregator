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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class RunAll {

    HocrInfoAggregator hocrInfoAggregator;

    public RunAll() {
    }

    public void run(File dir) throws Exception {
        if(System.getProperty("grc.lucene.spellchecker")==null)System.setProperty("grc.lucene.spellchecker","/usr/local/hocrinfoaggregator/test/lucene/lucene-grc");
        hocrInfoAggregator = new HocrInfoAggregator();
        List<File> listFiles=Arrays.asList(dir.listFiles());
        Collections.sort(listFiles);
        for (File file : listFiles) {
            try {
                if (file.getName().endsWith(".html") && !file.getName().endsWith(".ngt.html")) {
                    System.out.println(file.getAbsolutePath());
                    hocrInfoAggregator.initFile(file.getAbsolutePath());
                    hocrInfoAggregator.parse();
                    hocrInfoAggregator.alignToGroundTruth();
                    //hocrInfoAggregator.output(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 5) + "-out.html");
                    hocrInfoAggregator.output(file.getAbsolutePath());
                }
            } catch (Exception ex) {
                System.err.println("ERROR: " + file.getName());
                ex.printStackTrace(System.err);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        XmlWordListExtractor.main(new String[]{"/usr/local/hocrinfoaggregator/test/demetrius/demetrius-de_elocutione-u.xml","/usr/local/hocrinfoaggregator/test/demetrius/demetrius-de_elocutione.ngt.csv"});
        FlatXml.main(new String[]{"/usr/local/hocrinfoaggregator/test/demetrius/Demetrius-De_elocutione.book"});
        NgtMaker.main(new String[]{"/usr/local/hocrinfoaggregator/test/demetrius/demetrius-de_elocutione.ngt.csv","/usr/local/hocrinfoaggregator/test/demetrius/Demetrius-De_elocutione.book"});
        RunAll ra = new RunAll();
        ra.run(new File("/usr/local/hocrinfoaggregator/test/demetrius/Demetrius-De_elocutione.book"));
    }
}
