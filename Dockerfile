FROM centos:7
EXPOSE 4000/tcp

# Update the package repository and install Java
RUN yum -y update && \
    yum -y install java-1.8.0-openjdk-devel

# Copy the Java source file
COPY httpserver.java /httpserver/

# Copy the MySQL JDBC driver JAR file to the appropriate location
COPY mysql-connector-j-8.0.32.jar /usr/local/lib/mysql-connector-j-8.0.32.jar

# Set the working directory
WORKDIR /httpserver/

# Compile the Java source file with the MySQL JDBC driver in the classpath
RUN javac -cp /usr/local/lib/mysql-connector-j-8.0.32.jar httpserver.java

# Set the command to run the application
CMD ["java", "-cp", "/usr/local/lib/mysql-connector-j-8.0.32.jar:.", "httpserver", "4000"]
