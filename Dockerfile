# Base image được sử dụng để build image
FROM --platform=amd64 openjdk:17.0.2-oraclelinux8

# Thông tin tác giả
LABEL authors="demo"

# Set working directory trong container
WORKDIR /app
# Copy file JAR được build từ ứng dụng Spring Boot vào working directory trong container
COPY ./target/demo-0.0.1-SNAPSHOT.jar demo.jar

# Chỉ định command để chạy ứng dụng khi container khởi chạy
ENTRYPOINT ["java","-jar","demo.jar"]