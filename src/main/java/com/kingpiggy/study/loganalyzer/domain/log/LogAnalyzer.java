package com.kingpiggy.study.loganalyzer.domain.log;

import com.kingpiggy.study.loganalyzer.application.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LogAnalyzer implements CommandLineRunner {

    private final LogAnalysisService logAnalysisService;

    @Override
    public void run(String...args) throws Exception {
        logAnalysisService.logAnalysis();
    }

}
