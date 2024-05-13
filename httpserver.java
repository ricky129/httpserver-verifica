import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class httpserver implements Runnable {

    private Socket clientSocket = null;
    private Connection DBConn = null;

    public httpserver(Socket clientSocket, Connection DBConn) {
        this.clientSocket = clientSocket;
        this.DBConn = DBConn;
    }

    public static void main(String[] args) {
        int port = 80; // Server port

        try {
            // Load MySQL JDBC driver and establish database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection DBConn = DriverManager.getConnection("jdbc:mysql://192.168.1.17/verifica", "root", ";2=BRhi*LoRI");
            System.out.println("Connected to the database.");

            // Start the HTTP server
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            // Continuously accept incoming connections and handle them in separate threads
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a connection from the client
                System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress());
                // Create a thread to handle the client's request
                Thread thread = new Thread(new httpserver(clientSocket, DBConn));
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(this.clientSocket.getOutputStream());

            String requestString = inFromClient.readLine(); // Read the client's request
            System.out.println("Client request: " + requestString);

            Statement stmt = DBConn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tabella");

            StringBuilder htmlPageBuilder = new StringBuilder();
            htmlPageBuilder.append("<!DOCTYPE html>\n")
                    .append("<html lang=\"it\">\n")
                    .append("<style>\n")
                    .append("table, th, td {\n")
                    .append("border:1px solid black;\n")
                    .append("}\n")
                    .append("</style>\n")
                    .append("<head>\n")
                    .append("<meta charset=\"UTF-8\">\n")
                    .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                    .append("<title>httpserver Prova</title>\n")
                    .append("</head>\n")
                    .append("<body>\n")
                    .append("<table>\n")
                    .append("<tr><th>ID</th><th>Descrizione</th></tr>\n");

            while (rs.next())
                htmlPageBuilder.append("<tr><td>").append(rs.getInt("id")).append("</td><td>").append(rs.getString("descrizione")).append("</td></tr>\n");

            rs.close();
            stmt.close();

            htmlPageBuilder.append("</table>\n")
                    .append("</body>\n")
                    .append("</html>\n");

            String htmlPage = htmlPageBuilder.toString();

            // Send the response to the client
            outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
            outToClient.writeBytes("Content-Type: text/html\r\n");
            outToClient.writeBytes("Content-Length: " + htmlPage.length() + "\r\n");
            outToClient.writeBytes("Date: " + new Date() + "\r\n");
            outToClient.writeBytes("\r\n");
            outToClient.writeBytes(htmlPage);
            outToClient.flush();

            // Close the connection
            System.out.println("Connection with the client closed");
            this.clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
