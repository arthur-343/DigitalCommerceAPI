# Usa uma imagem base com a versão 21 do Java
FROM bellsoft/liberica-openjdk-alpine:21

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo .jar da sua aplicação para dentro do container
# Supondo que seu arquivo .jar se chame "digitalcommerce-0.0.1-SNAPSHOT.jar"
COPY target/digitalcommerce-0.0.1-SNAPSHOT.jar digitalcommerce.jar

# Define o comando que será executado quando o container iniciar
ENTRYPOINT ["java", "-jar", "digitalcommerce.jar"]