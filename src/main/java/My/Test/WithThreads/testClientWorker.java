package My.Test.WithThreads;


import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class testClientWorker implements Runnable
{
    static String host;
    static int port;
    static int loop;
    static String keyspath;
    static String password;
    static String context;
    static String payload = "{'hello':'hello'}";
    static String accessToken;
    static int length;

    public testClientWorker() {
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("start with threads");
        Scanner scanner = new Scanner(System.in);

        //String path = args[0];
        String path = "/Users/selakapiumal/git/HTTP_client/My_test_client/config_files/config.properties";

        Properties prop = new Properties();
        File initialFile = new File(path);
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(initialFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        if (inputStream != null) {
            try
            {
                prop.load(inputStream);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        } else {
            System.out.println("config.property file is not found");
        }


        host = prop.getProperty("host");
        port = Integer.parseInt(prop.getProperty("port"));
        context = prop.getProperty("context");

        keyspath = prop.getProperty("keyspath");
        password = prop.getProperty("password");
        //accessToken = prop.getProperty("accessToken");
        loop = Integer.parseInt(prop.getProperty("loop"));
        length = payload.length();

        //Create the thread pool
        //ExecutorService executor = Executors.newFixedThreadPool(loop);
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 1; j++) {
                Runnable worker = new testClientWorker();
                executor.execute(worker);//calling execute method of ExecutorService
            }
            Thread.sleep(10000);
        }
        executor.shutdown();


    }

    public void run()
    {
        System.out.println(Thread.currentThread().getName()+" (Start) run()");
        SSLContext sslContext = createSSLContext();
        try
        {
            //create the ssl socket
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket(host, port);
            System.out.println("SSL client started");
            //handover the sslSocket to the thread
            ClientThread(sslSocket);
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+" (End)");
    }

    private SSLContext createSSLContext()
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keyspath), password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, null);
            return sslContext;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    static void ClientThread(SSLSocket sslSocket)
    {
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        try
        {
            sslSocket.startHandshake();
            SSLSession sslSession = sslSocket.getSession();
            //System.out.println("SSLSession :");
            //System.out.println("\tProtocol : " + sslSession.getProtocol());
            //System.out.println("\tCipher suite : " + sslSession.getCipherSuite());
            InputStream inputStream = sslSocket.getInputStream();
            OutputStream outputStream = sslSocket.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
            //headers
            printWriter.print("POST " + context + " HTTP/1.1\r\n");
            printWriter.print("Accept-Encoding: gzip,deflate\r\n");
            printWriter.print("Content-Type: application/json\r\n");
            printWriter.print("Content-Length: " + length + "\r\n");
            printWriter.print("Connection: Keep-Alive\r\n");
            printWriter.print("Host: " + host + "\r\n");
            printWriter.print("Authorization: Bearer "+accessToken+"\r\n");
            printWriter.print("\r\n");
            //payload
            printWriter.print(payload + "\r\n");
            printWriter.print("\r\n");
            printWriter.flush();
            char[] buf = new char[100];
            StringBuilder outt = new StringBuilder();
            try
            {
                for (;;)
                {
                    int read = bufferedReader.read(buf);
                    outt.append(buf, 0, read);
                    if (read < 100) {
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println(outt);
            sslSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
