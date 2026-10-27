package com.jh.toolcalling.tools;

import com.jh.toolcalling.vo.OrderRequest;
import com.jh.toolcalling.vo.OrderResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class OrderTool {

    @Tool(description = "创建订单工具，用户下单时调用")
    public OrderResponse createOrder(@ToolParam(description = "需要提供用户ID、商品列表和数量") OrderRequest request) {
        // 1. 参数校验
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单商品不能为空");
        }

        // 2. 模拟业务逻辑
        String orderNo = "order" + System.currentTimeMillis();

        // 3. 返回结果
        OrderResponse response = new OrderResponse();
        response.setOrderNo(orderNo);
        response.setStatus("success");
        return response;
    }
}
