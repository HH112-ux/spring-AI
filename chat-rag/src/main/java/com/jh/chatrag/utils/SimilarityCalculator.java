package com.jh.chatrag.utils;

/**
 * 基于 Embedding 向量的文本相似度计算工具类
 * 整合多种核心相似度算法
 */public class SimilarityCalculator {

    // ====================== 1. 余弦相似度（默认推荐，RAG 最常用） ======================
    /**
     * 计算两个向量的余弦相似度
     * 取值范围：[-1,1]，值越接近1表示越相似
     * @param a 向量A（Embedding 向量）
     * @param b 向量B（Embedding 向量）
     * @return 余弦相似度值
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        // 第一步：校验两个向量是否为空、长度是否一致
        checkVectorLength(a, b);

        // 定义三个变量，分别存储：点积、向量A的模长平方、向量B的模长平方
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        // 遍历向量所有维度，累加计算点积与模长平方
        for (int i = 0; i < a.length; i++) {
            // 点积 = 对应维度相乘后累加
            dotProduct += a[i] * b[i];
            // 向量A的模长平方 = 每个维度平方后累加
            normA += Math.pow(a[i], 2);
            // 向量B的模长平方
            normB += Math.pow(b[i], 2);
        }

        // 防止模长为0，避免除以0异常
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // 余弦相似度公式：点积 / (A的模长 × B的模长)
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ====================== 2. 欧氏距离相似度（归一化后） ======================
    /**
     * 计算欧氏距离，并将距离转为相似度（值越大越相似）
     * @param a 向量A
     * @param b 向量B
     * @return 归一化后的欧氏相似度
     */
    public static double euclideanSimilarity(float[] a, float[] b) {
        // 校验向量长度
        checkVectorLength(a, b);

        // 存储平方差之和
        double sum = 0.0;

        // 欧氏距离核心：每个维度差值的平方累加
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }

        // 开平方得到真实欧氏距离
        double distance = Math.sqrt(sum);

        // 归一化公式：1/(1+距离) → 把距离转为 0~1 之间的相似度
        return 1.0 / (1.0 + distance);
    }

    // ====================== 3. 曼哈顿距离相似度（归一化后） ======================
    /**
     * 计算曼哈顿距离，并转为相似度
     * @param a 向量A
     * @param b 向量B
     * @return 归一化后的曼哈顿相似度
     */
    public static double manhattanSimilarity(float[] a, float[] b) {
        // 校验向量长度
        checkVectorLength(a, b);

        // 存储绝对值差之和
        double sum = 0.0;

        // 曼哈顿距离：每个维度差值的绝对值累加
        for (int i = 0; i < a.length; i++) {
            sum += Math.abs(a[i] - b[i]);
        }

        // 同样归一化为 0~1 相似度
        return 1.0 / (1.0 + sum);
    }

    // ====================== 通用辅助方法 ======================

    /**
     * 校验两个向量：不能为空，且长度必须一致
     * @param a 向量A
     * @param b 向量B
     */
    private static void checkVectorLength(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("向量长度不一致或为空，无法计算相似度");
        }
    }
}