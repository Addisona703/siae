#!/bin/bash

# JWT网关优化性能测试脚本

GATEWAY_URL="http://localhost:8080"
SERVICE_URL="http://localhost:8020"
TEST_TOKEN="your_jwt_token_here"
TEST_COUNT=50

echo "=== JWT网关优化性能测试 ==="
echo "测试次数: $TEST_COUNT"
echo "网关URL: $GATEWAY_URL"
echo "服务URL: $SERVICE_URL"
echo ""

# 测试1: 通过网关访问（优化后，无重复JWT解析）
echo "1. 测试网关访问性能（优化后）..."
total_time=0
for i in $(seq 1 $TEST_COUNT); do
    response_time=$(curl -w "%{time_total}" -o /dev/null -s \
        -H "Authorization: Bearer $TEST_TOKEN" \
        "$GATEWAY_URL/api/v1/user/test/auth/performance")
    total_time=$(echo "$total_time + $response_time" | bc)
done
avg_gateway_time=$(echo "scale=4; $total_time / $TEST_COUNT" | bc)
echo "网关访问平均响应时间: ${avg_gateway_time}秒"

# 测试2: 直接服务访问（如果启用，传统JWT校验）
if [ "$ENABLE_DIRECT_ACCESS" = "true" ]; then
    echo ""
    echo "2. 测试直接服务访问性能（传统JWT校验）..."
    total_time=0
    for i in $(seq 1 $TEST_COUNT); do
        response_time=$(curl -w "%{time_total}" -o /dev/null -s \
            -H "Authorization: Bearer $TEST_TOKEN" \
            "$SERVICE_URL/api/v1/user/test/auth/performance")
        total_time=$(echo "$total_time + $response_time" | bc)
    done
    avg_direct_time=$(echo "scale=4; $total_time / $TEST_COUNT" | bc)
    echo "直接访问平均响应时间: ${avg_direct_time}秒"

    # 计算性能提升
    improvement=$(echo "scale=2; ($avg_direct_time - $avg_gateway_time) / $avg_direct_time * 100" | bc)
    echo ""
    echo "性能提升: ${improvement}%"
else
    echo ""
    echo "2. 直接服务访问已禁用（生产环境推荐配置）"
fi

echo ""
echo "测试完成！"