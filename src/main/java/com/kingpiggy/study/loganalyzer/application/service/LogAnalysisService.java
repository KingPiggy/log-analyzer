package com.kingpiggy.study.loganalyzer.application.service;

import com.kingpiggy.study.loganalyzer.util.CustomFileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LogAnalysisService {

    private final CustomFileUtil customFileUtil;

    private static final Pattern PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final int IDX_STATUS_CODE = 0;
    private static final int IDX_URL = 1;
    private static final int IDX_WEB_BROWSER = 2;
    private static final int IDX_CALLED_AT = 3;
    private static final String STATUS_CODE_OK = "200";

    private static final Map<String, Integer> apiKeyCallCountMap = new HashMap<>();
    private static final Map<String, Integer> apiServiceIdCallCountMap = new HashMap<>();
    private static final Map<String, Integer> webBrowserCountMap = new HashMap<>();

    public void logAnalysis() throws Exception {
        System.out.println("[log-analysis-service] log analysis start ####################");

        // Get input log file
        File inputLogFile = customFileUtil.getInputLogFile();

        // Read input log file
        readFile(inputLogFile);

        // Write output log file
        makeOutputFile();

        System.out.println("[log-analysis-service] log analysis end ####################");
    }

    public void readFile(File inputLogFile) throws Exception {
        if (inputLogFile.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(inputLogFile));
            String line = null;

            while ((line = br.readLine()) != null) {
                analysis(line);
            }
        }
    }

    public void analysis(String line) throws Exception {
        Matcher m = PATTERN.matcher(line);
        int fieldIdx = 0;
        boolean isStatusOk = false;
        while (m.find()) {
            String field = m.group(1);
            switch (fieldIdx) {
                case IDX_STATUS_CODE:
                    if (field.equals(STATUS_CODE_OK)) {
                        isStatusOk = true;
                        break;
                    }
                    isStatusOk = false;
                    break;
                case IDX_URL:
                    if (!isStatusOk) {
                        break;
                    }

                    // 1. Make URL
                    URL url = new URL(field);

                    // 2. Count called API Service ID
                    String apiServiceID = getLastSegmentOfUrl(url);
                    Integer apiServiceIDCount =
                        apiServiceIdCallCountMap.getOrDefault(apiServiceID, 0) + 1;
                    apiServiceIdCallCountMap.put(apiServiceID, apiServiceIDCount);

                    // 3. Count called API Key which status is ok
                    Map<String, String> map = getQueryMap(url.getQuery());
                    String apiKey = map.get("apikey");
                    if (isStatusOk && (apiKey != null)) {
                        Integer apiKeyCount = apiKeyCallCountMap.getOrDefault(apiKey, 0) + 1;
                        apiKeyCallCountMap.put(apiKey, apiKeyCount);
                    }
                    break;
                case IDX_WEB_BROWSER:
                    if (!isStatusOk) {
                        break;
                    }

                    // Count web browser usage rate
                    Integer webBrowserCount = webBrowserCountMap.getOrDefault(field, 0) + 1;
                    webBrowserCountMap.put(field, webBrowserCount);
                    break;
                case IDX_CALLED_AT:
                default:
                    break;
            }
            fieldIdx++;
        }
    }

    public String getLastSegmentOfUrl(URL url) {
        String path = url.getPath();
        String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        return lastSegment;
    }

    public Map<String, String> getQueryMap(String query) {
        if (query == null) {
            return null;
        }

        int pos1 = query.indexOf("?");
        if (pos1 >= 0) {
            query = query.substring(pos1 + 1);
        }

        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void makeOutputFile() throws IOException {
        // Make content data
        String mostCalledApiKey = apiKeyCallCountMap.entrySet()
            .stream().max(Map.Entry.comparingByValue())
            .get().getKey();

        Map<String, Integer> max3Map = apiServiceIdCallCountMap.entrySet()
            .stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(3)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        int sum = webBrowserCountMap.values().stream().reduce(0, Integer::sum);
        Map<String, Integer> reverseSortedWebBrowserCountMap = webBrowserCountMap.entrySet()
            .stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Write file
        List<String> contents = new ArrayList<>();

        contents.add("최대호출 APIKEY");
        contents.add(mostCalledApiKey);
        contents.add("");

        contents.add("상위 3개의 API ServiceID와 각각의 요청 수");
        for (Map.Entry<String, Integer> entry : max3Map.entrySet()) {
            contents.add(entry.getKey() + " : " + entry.getValue());
        }
        contents.add("");

        contents.add("웹브라우저별 사용 비율");
        for (Map.Entry<String, Integer> entry : reverseSortedWebBrowserCountMap.entrySet()) {
            double value = entry.getValue();
            String rate = String.format("%.0f", value / sum * 100);
            contents.add(entry.getKey() + " : " + rate);
        }

        customFileUtil.writeOutputLogFile(contents);
    }

}
