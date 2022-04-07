# Log Analysis Application

## 목차
1. [Description](#description)
2. [Environment](#environment)
3. [How to run](#how-to-run)
4. [File Path](#file-path)
6. [Example](#example)

<br>

### Description
- API Log 로그 분석 프로그램
- File I/O, Parsing, Regex

<br>

### Environment
- Spring Boot 2.5.13-SNAPSHOT
- Java 8
- Project SDK : 1.8

<br>

### How to run
    Run LogAnalyzerApplication

<br>

### File Path
Input File  

    Path : {Project Root}/src/main/resources/input/input.log

Output File  

    Path : {Project Root}/out/production/resources/output/output.log

<br>

### Example
**Input log**

    [200][http://apis.kingpiggy.net/search/news?apikey=23jf&q=kingpiggy][Firefox][2019-06-10 08:00:11]

**Output File**

    최대호출 APIKEY
    e3ea
    
    상위 3개의 API ServiceID와 각각의 요청 수
    knowledge : 809
    news : 803
    blog : 799

    웹브라우저별 사용 비율
    IE : 85
    Firefox : 7
    Opera : 3
    Chrome : 3
    Safari : 2
