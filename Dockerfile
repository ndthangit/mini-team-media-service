# Sử dụng base image chính thức của OpenJDK với phiên bản Java 24.0.1
FROM eclipse-temurin:25.0.1_8-jdk-ubi10-minimal
# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR của ứng dụng vào container
COPY target/*.jar app.jar

COPY .env .env

# Mở port mà ứng dụng Spring Boot chạy (thay đổi nếu cần)
EXPOSE 9020

# Thiết lập thời gian chạy
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

#docker build -t management-image:latest .
