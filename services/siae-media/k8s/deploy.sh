#!/bin/bash

# Media Service Kubernetes 部署脚本
# 用法: ./deploy.sh [namespace] [environment]

set -e

# 默认参数
NAMESPACE=${1:-siae}
ENVIRONMENT=${2:-prod}

echo "========================================="
echo "Deploying siae-media to Kubernetes"
echo "Namespace: $NAMESPACE"
echo "Environment: $ENVIRONMENT"
echo "========================================="

# 创建命名空间（如果不存在）
echo "Creating namespace if not exists..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# 应用 RBAC 配置
echo "Applying RBAC configuration..."
kubectl apply -f rbac.yaml -n $NAMESPACE

# 应用 ConfigMap
echo "Applying ConfigMap..."
kubectl apply -f configmap.yaml -n $NAMESPACE

# 应用 Secret（生产环境应该从安全存储中加载）
echo "Applying Secret..."
if [ "$ENVIRONMENT" = "prod" ]; then
    echo "WARNING: Please ensure secrets are properly configured for production!"
    read -p "Continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
kubectl apply -f secret.yaml -n $NAMESPACE

# 应用 Deployment
echo "Applying Deployment..."
kubectl apply -f deployment.yaml -n $NAMESPACE

# 应用 Service
echo "Applying Service..."
kubectl apply -f service.yaml -n $NAMESPACE

# 应用 HPA
echo "Applying HorizontalPodAutoscaler..."
kubectl apply -f hpa.yaml -n $NAMESPACE

# 应用监控配置（如果 Prometheus Operator 已安装）
if kubectl get crd servicemonitors.monitoring.coreos.com &> /dev/null; then
    echo "Applying ServiceMonitor..."
    kubectl apply -f servicemonitor.yaml -n $NAMESPACE
    
    echo "Applying PrometheusRule..."
    kubectl apply -f prometheusrule.yaml -n $NAMESPACE
else
    echo "Prometheus Operator not found, skipping monitoring configuration"
fi

# 等待部署完成
echo "Waiting for deployment to be ready..."
kubectl rollout status deployment/siae-media -n $NAMESPACE --timeout=5m

# 显示部署状态
echo ""
echo "========================================="
echo "Deployment Status:"
echo "========================================="
kubectl get pods -n $NAMESPACE -l app=siae-media
echo ""
kubectl get svc -n $NAMESPACE -l app=siae-media
echo ""
kubectl get hpa -n $NAMESPACE -l app=siae-media

echo ""
echo "========================================="
echo "Deployment completed successfully!"
echo "========================================="
echo ""
echo "Useful commands:"
echo "  View logs: kubectl logs -f -n $NAMESPACE -l app=siae-media"
echo "  View events: kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp'"
echo "  Scale manually: kubectl scale deployment/siae-media --replicas=5 -n $NAMESPACE"
echo "  Port forward: kubectl port-forward -n $NAMESPACE svc/siae-media 8084:8084"
