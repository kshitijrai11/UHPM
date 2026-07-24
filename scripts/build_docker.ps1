Write-Host "Building api-gateway..."
docker build -t ultrahpm/api-gateway:latest -f api-gateway/Dockerfile .
Write-Host "Building config-server..."
docker build -t ultrahpm/config-server:latest -f config-server/Dockerfile .
Write-Host "Building eureka-server..."
docker build -t ultrahpm/eureka-server:latest -f eureka-server/Dockerfile .
Write-Host "Building product-service..."
docker build -t ultrahpm/product-service:latest -f product-service/Dockerfile .
Write-Host "Building order-service..."
docker build -t ultrahpm/order-service:latest -f order-service/Dockerfile .
Write-Host "Building user-service..."
docker build -t ultrahpm/user-service:latest -f user-service/Dockerfile .
Write-Host "Building payment-service..."
docker build -t ultrahpm/payment-service:latest -f payment-service/Dockerfile .
Write-Host "Building notification-service..."
docker build -t ultrahpm/notification-service:latest -f notification-service/Dockerfile .
Write-Host "Building recommendation-service..."
docker build -t ultrahpm/recommendation-service:latest -f recommendation-service/Dockerfile .
Write-Host "Done building all images!"
