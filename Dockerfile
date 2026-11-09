# 使用官方OpenJDK运行时作为基础镜像
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 复制打包好的jar文件到容器中
# 假设你的jar文件名为 uyi-0.0.1-SNAPSHOT.jar
COPY uYi-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8082

# 运行应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]