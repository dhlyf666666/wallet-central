package com.dhlyf.walletaleo.job;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.dhlyf.walletaleo.util.aleo.ProgramArguments;



public class JsonParser {

    public static void main(String[] args) {
        // 原始的非标准 JSON 字符串
        String jsonString = "{ program_id: credits.aleo,  function_name: transfer_public,  arguments: [  aleo189r23h7dng8qf7pw03spjpggpsszc5n5wcvdue09g4suzxv5acpqeyx4tg, aleo14y748xnwz6urrv3zkvydw9q9sat73u282adhalfacf4dj3jxky8sxfaw8r, 9600000u64  ]}";

        // 移除大括号
        String replacedBraces = jsonString.replace("{", "").replace("}", "").trim();

        // 将每个字段用逗号分隔
        String[] fields = replacedBraces.split(",(?=\\s*\\w+\\s*:)");  // 只分割键值对，跳过数组内部的逗号

        // 使用 StringBuilder 构建最终的 JSON 字符串
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");  // 开始大括号

        // 遍历每个字段
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim(); // 移除前后空格

            // 对字段进行键值对分割
            String[] kv = field.split(":");

            if (kv.length == 2) { // 如果是合法的键值对
                String key = kv[0].trim(); // 获取 key
                String value = kv[1].trim(); // 获取 value

                // 检查是否是数组字段
                if (value.startsWith("[")) {
                    jsonBuilder.append("  \"" + key + "\": [\n");

                    // 移除数组的方括号
                    value = value.replace("[", "").replace("]", "").trim();

                    // 解析数组项
                    String[] arrayItems = value.split(",");
                    for (int j = 0; j < arrayItems.length; j++) {
                        jsonBuilder.append("    \"" + arrayItems[j].trim() + "\"");
                        if (j < arrayItems.length - 1) {
                            jsonBuilder.append(",\n");  // 每个数组项之间添加逗号
                        } else {
                            jsonBuilder.append("\n");
                        }
                    }
                    jsonBuilder.append("  ]");
                } else {
                    // 普通键值对，值加上引号
                    jsonBuilder.append("  \"" + key + "\": \"" + value + "\"");
                }

                // 如果不是最后一个字段，添加逗号
                if (i < fields.length - 1) {
                    jsonBuilder.append(",\n");
                } else {
                    jsonBuilder.append("\n");
                }
            }
        }

        jsonBuilder.append("}");  // 结束大括号

        // 打印最终拼接成的 JSON 字符串
        System.out.println(jsonBuilder.toString());
    }

}
