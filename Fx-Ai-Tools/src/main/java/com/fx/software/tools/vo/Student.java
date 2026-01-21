package com.fx.software.tools.vo;

import lombok.Data;

/**
 * @FileName Student
 * @Description
 * @Author fx
 * @date 2026-01-20
 */
@Data
public class Student {

    private String studentId;      // 学号
    private String studentName;    // 姓名
    private String className;      // 班级
    private Integer age;           // 年龄
    private String gender;         // 性别

    // 各科成绩
    private Double chineseScore;   // 语文
    private Double mathScore;      // 数学
    private Double englishScore;   // 英语
    private Double physicsScore;   // 物理
    private Double chemistryScore; // 化学
    private Double biologyScore;   // 生物
    private Double historyScore;   // 历史
    private Double geographyScore; // 地理
    private Double politicsScore;  // 政治

    // 构造器、getter、setter
    public Student() {}

    public Student(String studentId, String studentName, String className,
                   Double chineseScore, Double mathScore, Double englishScore) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.chineseScore = chineseScore;
        this.mathScore = mathScore;
        this.englishScore = englishScore;
    }

    // 获取指定科目的成绩
    public Double getScoreBySubject(String subject) {
        switch(subject) {
            case "语文": return chineseScore;
            case "数学": return mathScore;
            case "英语": return englishScore;
            case "物理": return physicsScore;
            case "化学": return chemistryScore;
            case "生物": return biologyScore;
            case "历史": return historyScore;
            case "地理": return geographyScore;
            case "政治": return politicsScore;
            default: return null;
        }
    }

    // 获取总成绩
    public Double getTotalScore() {
        double total = 0;
        if (chineseScore != null) total += chineseScore;
        if (mathScore != null) total += mathScore;
        if (englishScore != null) total += englishScore;
        if (physicsScore != null) total += physicsScore;
        if (chemistryScore != null) total += chemistryScore;
        if (biologyScore != null) total += biologyScore;
        if (historyScore != null) total += historyScore;
        if (geographyScore != null) total += geographyScore;
        if (politicsScore != null) {
            total += politicsScore;
        }
        return total;
    }

    // 获取平均分
    public Double getAverageScore() {
        int count = 0;
        double total = 0;
        Double[] scores = {chineseScore, mathScore, englishScore, physicsScore,
                chemistryScore, biologyScore, historyScore, geographyScore, politicsScore};
        for (Double score : scores) {
            if (score != null) {
                total += score;
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }



}
