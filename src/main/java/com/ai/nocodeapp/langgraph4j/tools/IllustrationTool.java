package com.ai.nocodeapp.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class IllustrationTool {

    private static final String API_URL = "https://undraw.co/_next/data/nS41BRGVYK4TTVjGNap_q/search/%s.json?term=%s";

    @Tool("搜集插画图片，网站美化和装饰")
    public List<String> searchIllustrations(@P("搜索关键词，必须是单个英文单词") String query) {
        String apiUrl = String.format(API_URL, query, query);
        List<String> imageList = new ArrayList<>();
        int maxCount = 12;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 2. 构建一个 HttpRequest 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // 3. 发送请求并获取响应
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 4. 处理结果
            if (response.statusCode() == 200) {
                JSONObject jsonObject = JSONUtil.parseObj(response.body());
                JSONObject pageProps = jsonObject.getJSONObject("pageProps");
                JSONArray result = pageProps.getJSONArray("initialResults");
                if (result.isEmpty()) {
                    return imageList;
                }
                int cnt = Math.min(maxCount, result.size());
                for (int i = 0; i < cnt; i++) {
                    JSONObject imgObj = result.getJSONObject(i);
                    String media = imgObj.getStr("media");
                    if (!StrUtil.isEmpty(media) && !StrUtil.isBlank(media)) {
                        imageList.add(media);
                    }
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}
