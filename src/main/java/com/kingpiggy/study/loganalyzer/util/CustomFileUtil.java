package com.kingpiggy.study.loganalyzer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class CustomFileUtil {

    private static final String INPUT_LOG_PATH = "classpath:input/input.log";
    private static final String OUTPUT_LOG_PATH = "/output/output.log";

    public File getInputLogFile() throws FileNotFoundException {
        return ResourceUtils.getFile(INPUT_LOG_PATH);
    }

    public boolean writeOutputLogFile(List<String> contents) {
        boolean isOK;
        try {
            URL pathUrl = getClass().getResource(OUTPUT_LOG_PATH);
            File file = new File(pathUrl.getPath());
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            for (String content : contents) {
                bw.write(content);
                bw.newLine();
            }

            bw.flush();
            bw.close();
            fw.close();
            isOK = true;
        } catch (Exception e) {
            e.printStackTrace();
            isOK = false;
        }
        return isOK;
    }

}
