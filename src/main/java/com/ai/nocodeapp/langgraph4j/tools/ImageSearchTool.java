package com.ai.nocodeapp.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ImageSearchTool {

    @Value("${pexels.api-key}")
    private String api_key;

    private static final String IMAGE_SEARCH_API = "https://api.pexels.com/v1/search";

    @Tool("搜集内容相关的图片，用于网站展示")
    public List<String> searchImages(@P("搜索关键词，必须是单个英文单词") String query) {
        List<String> imageList = new ArrayList<>();
        int maxCount = 12;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 2. 构建一个 HttpRequest 请求
            URIBuilder uriBuilder = new URIBuilder(IMAGE_SEARCH_API)
                    .addParameter("query", query)
                    .addParameter("per_page", "12")
                    .addParameter("page", "1");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriBuilder.build())
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("Authorization", api_key)
                    .GET()
                    .build();

            // 3. 发送请求并获取响应
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // 4. 处理结果
            if (response.statusCode() == 200) {
                JSONObject jsonObject = JSONUtil.parseObj(response.body());
                JSONArray result = jsonObject.getJSONArray("photos");
                if (result.isEmpty()) {
                    return imageList;
                }
                int cnt = Math.min(maxCount, result.size());
                for (int i = 0; i < cnt; i++) {
                    JSONObject imgObj = result.getJSONObject(i);
                    JSONObject src = imgObj.getJSONObject("src");
                    String medium = src.getStr("medium");
                    if (!StrUtil.isEmpty(medium) && !StrUtil.isBlank(medium)) {
                        imageList.add(medium);
                    }
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }
}
