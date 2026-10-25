package com.campus.team.utils;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 敏感词过滤工具类
 * 使用 DFA（确定有限状态自动机）算法实现高效的敏感词检测
 */
@Component
public class SensitiveWordFilter {
    
    /**
     * 敏感词库的根节点
     */
    private final Map<Character, Object> sensitiveWordMap = new HashMap<>();
    
    /**
     * 是否结束标识
     */
    private static final String IS_END = "isEnd";
    
    /**
     * 初始化敏感词库
     */
    public SensitiveWordFilter() {
        initSensitiveWordMap();
    }
    
    /**
     * 初始化敏感词库
     * 这里使用硬编码方式，实际项目中可以从数据库或配置文件加载
     */
    private void initSensitiveWordMap() {
        Set<String> sensitiveWords = new HashSet<>();
        
        // 政治敏感词
        sensitiveWords.add("法轮功");
        sensitiveWords.add("共产党");
        sensitiveWords.add("习近平");
        sensitiveWords.add("毛泽东");
        sensitiveWords.add("邓小平");
        sensitiveWords.add("江泽民");
        sensitiveWords.add("胡锦涛");
        sensitiveWords.add("温家宝");
        sensitiveWords.add("李克强");
        sensitiveWords.add("台独");
        sensitiveWords.add("藏独");
        sensitiveWords.add("疆独");
        sensitiveWords.add("港独");
        
        // 色情低俗词汇
        sensitiveWords.add("色情");
        sensitiveWords.add("黄色");
        sensitiveWords.add("裸体");
        sensitiveWords.add("性交");
        sensitiveWords.add("做爱");
        sensitiveWords.add("一夜情");
        sensitiveWords.add("约炮");
        
        // 暴力恐怖词汇
        sensitiveWords.add("杀人");
        sensitiveWords.add("爆炸");
        sensitiveWords.add("恐怖");
        sensitiveWords.add("自杀");
        sensitiveWords.add("炸弹");
        sensitiveWords.add("枪支");
        
        // 广告营销词汇
        sensitiveWords.add("代考");
        sensitiveWords.add("代写");
        sensitiveWords.add("刷单");
        sensitiveWords.add("兼职打字");
        sensitiveWords.add("日赚千元");
        sensitiveWords.add("躺赚");
        sensitiveWords.add("微商");
        sensitiveWords.add("加微信");
        sensitiveWords.add("加QQ");
        
        // 其他违规内容
        sensitiveWords.add("赌博");
        sensitiveWords.add("博彩");
        sensitiveWords.add("六合彩");
        sensitiveWords.add("时时彩");
        sensitiveWords.add("贷款");
        sensitiveWords.add("办证");
        sensitiveWords.add("发票");
        
        // 构建敏感词树
        for (String word : sensitiveWords) {
            addSensitiveWord(word);
        }
    }
    
    /**
     * 添加敏感词到词库
     */
    private void addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        Map<Character, Object> currentMap = sensitiveWordMap;
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            
            @SuppressWarnings("unchecked")
            Map<Character, Object> nextMap = (Map<Character, Object>) currentMap.get(c);
            
            if (nextMap == null) {
                nextMap = new HashMap<>();
                currentMap.put(c, nextMap);
            }
            
            currentMap = nextMap;
            
            // 最后一个字符，标记结束
            if (i == word.length() - 1) {
                currentMap.put(IS_END.charAt(0), "1");
            }
        }
    }
    
    /**
     * 检查文本是否包含敏感词
     * 
     * @param text 待检查的文本
     * @return true-包含敏感词，false-不包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查从指定位置开始是否存在敏感词
     * 
     * @param text 文本
     * @param beginIndex 开始位置
     * @return 敏感词长度，0表示不存在
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        boolean flag = false;
        int matchLength = 0;
        
        Map<Character, Object> currentMap = sensitiveWordMap;
        
        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            
            @SuppressWarnings("unchecked")
            Map<Character, Object> nextMap = (Map<Character, Object>) currentMap.get(c);
            
            if (nextMap != null) {
                matchLength++;
                
                // 检查是否到达词的结尾
                if ("1".equals(nextMap.get(IS_END.charAt(0)))) {
                    flag = true;
                }
                
                currentMap = nextMap;
            } else {
                break;
            }
        }
        
        if (!flag) {
            matchLength = 0;
        }
        
        return matchLength;
    }
    
    /**
     * 获取文本中的所有敏感词
     * 
     * @param text 待检查的文本
     * @return 敏感词列表
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> sensitiveWords = new HashSet<>();
        
        if (text == null || text.trim().isEmpty()) {
            return sensitiveWords;
        }
        
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                String word = text.substring(i, i + length);
                sensitiveWords.add(word);
                i += length - 1;
            }
        }
        
        return sensitiveWords;
    }
    
    /**
     * 替换敏感词为 * 号
     * 
     * @param text 待处理的文本
     * @return 替换后的文本
     */
    public String replaceSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text);
        
        for (int i = 0; i < result.length(); i++) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, '*');
                }
                i += length - 1;
            }
        }
        
        return result.toString();
    }
}
