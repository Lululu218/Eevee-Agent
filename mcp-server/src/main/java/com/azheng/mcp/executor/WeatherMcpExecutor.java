package com.azheng.mcp.executor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 天气服务执行器
 * 职责：接收解析后的天气查询参数，调用百度地图MCP工具获取天气数据，并格式化返回结果
 * 关键词解析逻辑由上层模块（main-application）负责
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherMcpExecutor {

    private static final String TOOL_NAME = "map_weather";
    private static final Set<String> VALID_DATA_TYPES = Set.of("now", "fc", "index", "alert", "fc_hour", "all");

    private final Gson gson = new Gson();
    private final McpClient baiduMcpClient;

    /**
     * 执行天气查询
     *
     * @param paramsJson 解析后的天气查询参数JSON字符串
     *                   必填：district_id
     *                   可选：province, city, district, data_type
     * @return 格式化后的天气结果
     */
    public String execute(String paramsJson) {

        long totalStart = System.currentTimeMillis();
        log.info("开始执行天气查询，参数：{}", paramsJson);

        // 1. 解析传入的JSON参数
        long stepStart = System.currentTimeMillis();
        JsonObject params = parseParams(paramsJson);
        log.info("[性能] 参数解析耗时：{}ms", System.currentTimeMillis() - stepStart);

        if (params == null) {
            log.warn("参数解析失败");
            return "抱歉，天气查询参数格式错误，请检查参数是否正确。";
        }

        // 2. 验证必填参数 district_id
        if (!hasValidValue(params, "district_id")) {
            log.warn("缺少必填参数：district_id");
            return "缺少必填参数：district_id（地区ID）";
        }

        // 3. 执行MCP工具调用
        stepStart = System.currentTimeMillis();
        String toolResult = executeMcpTool(params);
        log.info("[性能] MCP工具调用耗时：{}ms", System.currentTimeMillis() - stepStart);

        if (toolResult == null || toolResult.isEmpty()) {
            log.warn("工具执行返回空结果");
            return "抱歉，暂时无法获取天气信息，请稍后再试。";
        }

        // 4. 格式化结果
        stepStart = System.currentTimeMillis();
        String result = formatWeatherResult(toolResult);
        log.info("[性能] 结果格式化耗时：{}ms", System.currentTimeMillis() - stepStart);

        log.info("[性能] 天气查询总耗时：{}ms", System.currentTimeMillis() - totalStart);
        return result;
    }

    /**
     * 解析传入的JSON参数
     */
    private JsonObject parseParams(String paramsJson) {
        try {
            return gson.fromJson(paramsJson, JsonObject.class);
        } catch (Exception e) {
            log.error("解析JSON参数失败：{}，参数内容：{}", e.getMessage(), paramsJson);
            return null;
        }
    }

    /**
     * 执行MCP工具调用
     */
    private String executeMcpTool(JsonObject params) {
        try {



            String arguments = buildToolArguments(params);
            log.info("百度地图MCPServer-天气服务：构建工具参数：{}", arguments);

            ToolExecutionRequest request = ToolExecutionRequest.builder()
                    .name(TOOL_NAME) //指定工具名称
                    .arguments(arguments)
                    .build();

            ToolExecutionResult result = baiduMcpClient.executeTool(request);
            log.info("工具执行成功，结果：{}", result.resultText());

            return result.resultText();
        } catch (Exception e) {
            log.error("百度地图MCPServer-天气服务：执行MCP工具失败：{}", e.getMessage(), e);
            throw new RuntimeException("百度地图MCPServer-天气服务：天气查询工具执行失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建工具调用参数
     * 只传递非 null 的字段，data_type 无效时默认为 all
     */
    private String buildToolArguments(JsonObject params) {
        JsonObject toolParams = new JsonObject();
        
        // district_id：必填
        toolParams.addProperty("district_id", params.get("district_id").getAsString().trim());
        
        // data_type：无效时默认为 all
        String dataType = getValidDataType(params);
        toolParams.addProperty("data_type", dataType);
        
        // 其他可选字段：有值才传递
        addIfValid(toolParams, params, "province");
        addIfValid(toolParams, params, "city");
        addIfValid(toolParams, params, "district");
        
        return gson.toJson(toolParams);
    }

    /**
     * 获取有效的 data_type，无效或为空时返回 all
     */
    private String getValidDataType(JsonObject params) {
        if (!hasValidValue(params, "data_type")) {
            return "all";
        }
        String dataType = params.get("data_type").getAsString().trim().toLowerCase();
        return VALID_DATA_TYPES.contains(dataType) ? dataType : "all";
    }

    /**
     * 检查字段是否有有效值（非 null 非空）
     */
    private boolean hasValidValue(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return false;
        }
        String value = json.get(key).getAsString();
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 如果字段有有效值，则添加到目标对象
     */
    private void addIfValid(JsonObject target, JsonObject source, String key) {
        if (hasValidValue(source, key)) {
            target.addProperty(key, source.get(key).getAsString().trim());
        }
    }

    /**
     * 格式化天气结果
     */
    private String formatWeatherResult(String toolResult) {
        try {
            log.debug("开始格式化天气结果，原始结果：{}", toolResult);

            JsonObject resultJson = gson.fromJson(toolResult, JsonObject.class);

            if (!resultJson.has("status")) {
                log.warn("天气结果缺少status字段");
                return "抱歉，天气数据解析失败，请稍后再试。";
            }

            int status = resultJson.get("status").getAsInt();
            if (status != 0) {
                String message = resultJson.has("message") ? resultJson.get("message").getAsString() : "未知错误";
                log.warn("天气API返回错误：status={}, message={}", status, message);
                return "抱歉，天气查询失败：" + message;
            }

            if (!resultJson.has("result") || resultJson.get("result").isJsonNull()) {
                log.warn("天气结果缺少result字段");
                return "抱歉，暂未获取到天气数据。";
            }

            JsonObject result = resultJson.getAsJsonObject("result");
            return formatWeatherOutput(result);

        } catch (Exception e) {
            log.error("格式化天气结果失败：{}，原始结果：{}", e.getMessage(), toolResult);
            return "抱歉，天气数据格式化失败，请稍后再试。";
        }
    }

    /**
     * 格式化天气输出
     */
    private String formatWeatherOutput(JsonObject result) {
        StringBuilder sb = new StringBuilder();

        // 地点信息
        if (result.has("location") && !result.get("location").isJsonNull()) {
            JsonObject location = result.getAsJsonObject("location");
            String province = location.has("province") ? location.get("province").getAsString() : "";
            String city = location.has("city") ? location.get("city").getAsString() : "";
            String name = location.has("name") ? location.get("name").getAsString() : "";
            sb.append("📍 ").append(province).append(city).append(name.isEmpty() || name.equals(city) ? "" : name).append("\n\n");
        }

        // 实时天气
        if (result.has("now") && !result.get("now").isJsonNull()) {
            JsonObject now = result.getAsJsonObject("now");
            sb.append("【实时天气】\n");
            sb.append("🌡️ 温度：").append(now.has("temp") ? now.get("temp").getAsString() + "℃" : "未知").append("\n");
            sb.append("🌡️ 体感温度：").append(now.has("feels_like") ? now.get("feels_like").getAsString() + "℃" : "未知").append("\n");
            sb.append("💧 湿度：").append(now.has("rh") ? now.get("rh").getAsString() + "%" : "未知").append("\n");
            sb.append("🌤️ 天气：").append(now.has("text") ? now.get("text").getAsString() : "未知").append("\n");
            sb.append("🌬️ 风力：").append(now.has("wind_class") ? now.get("wind_class").getAsString() : "未知");
            sb.append(now.has("wind_dir") ? " " + now.get("wind_dir").getAsString() : "").append("\n");
            
            // 能见度
            if (now.has("vis") && !now.get("vis").isJsonNull()) {
                int vis = now.get("vis").getAsInt();
                sb.append("👁️ 能见度：").append(vis >= 1000 ? (vis / 1000) + "公里" : vis + "米").append("\n");
            }
            // AQI空气质量
            if (now.has("aqi") && !now.get("aqi").isJsonNull()) {
                sb.append("🌫️ 空气质量指数(AQI)：").append(now.get("aqi").getAsString());
                sb.append(" (").append(getAqiDescription(now.get("aqi").getAsInt())).append(")").append("\n");
            }
        }

        // 生活指数
        if (result.has("indexes") && result.get("indexes").isJsonArray()) {
            var indexes = result.getAsJsonArray("indexes");
            if (!indexes.isEmpty()) {
                sb.append("\n【生活指数】\n");
                for (JsonElement element : indexes) {
                    JsonObject index = element.getAsJsonObject();
                    String name = index.has("name") ? index.get("name").getAsString() : "";
                    String brief = index.has("brief") ? index.get("brief").getAsString() : "";
                    String detail = index.has("detail") ? index.get("detail").getAsString() : "";
                    sb.append("• ").append(name).append("：").append(brief);
                    if (!detail.isEmpty() && !detail.equals(brief)) {
                        sb.append(" - ").append(detail);
                    }
                    sb.append("\n");
                }
            }
        }

        // 天气预报
        if (result.has("forecasts") && result.get("forecasts").isJsonArray()) {
            sb.append("\n【未来天气预报】\n");
            var forecasts = result.getAsJsonArray("forecasts");
            int count = 0;
            for (JsonElement element : forecasts) {
                if (count >= 7) break;
                JsonObject forecast = element.getAsJsonObject();
                String date = forecast.has("date") ? forecast.get("date").getAsString() : "";
                String week = forecast.has("week") ? forecast.get("week").getAsString() : "";
                String textDay = forecast.has("text_day") ? forecast.get("text_day").getAsString() : "";
                String textNight = forecast.has("text_night") ? forecast.get("text_night").getAsString() : "";
                String high = forecast.has("high") ? forecast.get("high").getAsString() : "";
                String low = forecast.has("low") ? forecast.get("low").getAsString() : "";
                String wcDay = forecast.has("wc_day") ? forecast.get("wc_day").getAsString() : "";
                String wdDay = forecast.has("wd_day") ? forecast.get("wd_day").getAsString() : "";

                sb.append(String.format("%s %s\n", date, week));
                sb.append(String.format("  🌤️ 白天：%s %s~%s℃ %s%s\n", textDay, low, high, wcDay, wdDay));
                sb.append(String.format("  🌙 夜间：%s %s℃\n", textNight, low));

                count++;
            }
        }

        // 逐小时预报
        if (result.has("forecast_hours") && result.get("forecast_hours").isJsonArray()) {
            var forecastHours = result.getAsJsonArray("forecast_hours");
            if (!forecastHours.isEmpty()) {
                sb.append("\n【逐小时预报】\n");
                int count = 0;
                for (JsonElement element : forecastHours) {
                    if (count >= 12) break; // 最多显示12小时
                    JsonObject hour = element.getAsJsonObject();
                    String dataTime = hour.has("data_time") ? hour.get("data_time").getAsString() : "";
                    String text = hour.has("text") ? hour.get("text").getAsString() : "";
                    String temp = hour.has("temp_fc") ? hour.get("temp_fc").getAsString() : "";
                    String windClass = hour.has("wind_class") ? hour.get("wind_class").getAsString() : "";
                    String windDir = hour.has("wind_dir") ? hour.get("wind_dir").getAsString() : "";
                    String rh = hour.has("rh") ? hour.get("rh").getAsString() : "";
                    String pop = hour.has("pop") ? hour.get("pop").getAsString() : "";

                    // 提取小时部分，如 "2026-05-22 17:00:00" -> "17:00"
                    String hourTime = dataTime.length() >= 16 ? dataTime.substring(11, 16) : dataTime;
                    
                    sb.append(String.format("%s %s %s℃ %s%s 湿度%s%%", 
                            hourTime, text, temp, windClass, windDir, rh));
                    if (!pop.equals("0")) {
                        sb.append(" 降水概率").append(pop).append("%");
                    }
                    sb.append("\n");
                    count++;
                }
            }
        }

        // 天气预警
        if (result.has("alerts") && result.get("alerts").isJsonArray()) {
            var alerts = result.getAsJsonArray("alerts");
            if (!alerts.isEmpty()) {
                sb.append("\n【天气预警】\n");
                for (JsonElement element : alerts) {
                    JsonObject alert = element.getAsJsonObject();
                    String title = alert.has("title") ? alert.get("title").getAsString() : "";
                    String type = alert.has("type") ? alert.get("type").getAsString() : "";
                    String level = alert.has("level") ? alert.get("level").getAsString() : "";
                    String desc = alert.has("desc") ? alert.get("desc").getAsString() : "";
                    
                    sb.append("⚠️ ").append(title);
                    if (!level.isEmpty()) {
                        sb.append("（").append(level).append("）");
                    }
                    sb.append("\n");
                    if (!desc.isEmpty()) {
                        sb.append("   ").append(desc).append("\n");
                    }
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * 根据AQI值获取空气质量描述
     */
    private String getAqiDescription(int aqi) {
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    /*
    示例工具调用结果
      📍 河南省郑州市

      【实时天气】
      🌡️ 温度：27℃
      🌡️ 体感温度：29℃
      💧 湿度：59%
      🌤️ 天气：多云
      🌬️ 风力：1级 东北风
      👁️ 能见度：23公里
      🌫️ 空气质量指数(AQI)：57 (良)

      【生活指数】
      • 晨练指数：不宜 - 有降水，建议在室内做适当锻炼。
      • 洗车指数：不适宜 - 两天内有雨，雨水和泥水会弄脏爱车。
      • 感冒指数：较易发 - 湿度大，较易感冒
      • 紫外线指数：弱 - 辐射较弱，涂擦SPF12-15、PA+护肤品。
      • 穿衣指数：舒适 - 建议穿长袖衬衫单裤等服装。
      • 运动指数：较不宜 - 有降水，推荐您在室内进行休闲运动。

      【未来天气预报】
      2026-05-22 星期五
        🌤️ 白天：小雨 19~26℃ <3级南风
        🌙 夜间：阴 19℃
      2026-05-23 星期六
        🌤️ 白天：阴 20~29℃ <3级东南风
        🌙 夜间：大雨 20℃
      ...

      【逐小时预报】
      17:00 多云 26℃ <3级西北风 湿度80%
      18:00 多云 25℃ <3级西北风 湿度80%
      19:00 晴 24℃ <3级东风 湿度80%
      ...

      【天气预警】
      ⚠️ 暴雨黄色预警（黄色）
         预计未来6小时内将出现暴雨天气，请注意防范。
      ⚠️ 大风蓝色预警（蓝色）
         预计未来24小时内将出现大风天气。
     */
}