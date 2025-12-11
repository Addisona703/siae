package com.hngy.siae.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 天气查询工具
 * <p>
 * 使用 wttr.in 免费天气 API
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTool {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 查询天气信息
     */
    @Tool(description = "查询指定城市的天气信息，包括温度、天气状况、湿度、风速等。支持中文城市名。")
    public WeatherInfo getWeather(
            @ToolParam(description = "城市名称，如：北京、上海、广州、深圳") String city) {
        
        log.info("Tool invoked: getWeather - city: {}", city);
        
        if (city == null || city.trim().isEmpty()) {
            throw ToolExecutionException.of("城市名称不能为空");
        }
        
        try {
            // 使用 wttr.in 免费 API，添加 lang=zh 参数获取中文
            String url = String.format("https://wttr.in/%s?format=j1&lang=zh", city.trim());
            String response = restTemplate.getForObject(url, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            
            // 安全获取 current_condition
            JsonNode currentConditions = root.path("current_condition");
            if (!currentConditions.isArray() || currentConditions.isEmpty()) {
                throw ToolExecutionException.of("无法获取天气数据，请检查城市名称是否正确");
            }
            JsonNode current = currentConditions.get(0);
            
            // 安全获取 nearest_area
            JsonNode nearestAreas = root.path("nearest_area");
            String areaName = city;
            String country = "";
            if (nearestAreas.isArray() && !nearestAreas.isEmpty()) {
                JsonNode area = nearestAreas.get(0);
                JsonNode areaNames = area.path("areaName");
                if (areaNames.isArray() && !areaNames.isEmpty()) {
                    areaName = areaNames.get(0).path("value").asText(city);
                }
                JsonNode countries = area.path("country");
                if (countries.isArray() && !countries.isEmpty()) {
                    country = countries.get(0).path("value").asText("");
                }
            }
            
            // 安全获取天气描述
            String weatherDesc = "";
            JsonNode langZh = current.path("lang_zh");
            if (langZh.isArray() && !langZh.isEmpty()) {
                weatherDesc = langZh.get(0).path("value").asText("");
            }
            if (weatherDesc.isEmpty()) {
                JsonNode weatherDescNode = current.path("weatherDesc");
                if (weatherDescNode.isArray() && !weatherDescNode.isEmpty()) {
                    weatherDesc = weatherDescNode.get(0).path("value").asText("未知");
                }
            }
            
            WeatherInfo weather = WeatherInfo.builder()
                    .city(areaName)
                    .country(country)
                    .temperature(current.path("temp_C").asText("--") + "°C")
                    .feelsLike(current.path("FeelsLikeC").asText("--") + "°C")
                    .weather(weatherDesc)
                    .humidity(current.path("humidity").asText("--") + "%")
                    .windSpeed(current.path("windspeedKmph").asText("--") + " km/h")
                    .windDirection(current.path("winddir16Point").asText("--"))
                    .visibility(current.path("visibility").asText("--") + " km")
                    .uvIndex(current.path("uvIndex").asText("--"))
                    .build();
            
            log.info("getWeather returned: {}", weather);
            return weather;
            
        } catch (Exception e) {
            log.error("Error getting weather for {}: {}", city, e.getMessage(), e);
            throw ToolExecutionException.of("查询天气失败: " + e.getMessage(), e);
        }
    }

    /**
     * 天气信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeatherInfo {
        private String city;
        private String country;
        private String temperature;
        private String feelsLike;
        private String weather;
        private String humidity;
        private String windSpeed;
        private String windDirection;
        private String visibility;
        private String uvIndex;
    }
}
