package com.example.wallpaper;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by ske on 2016/11/15.
 */

public class DownloadUtil {
    public static void main(String[] args) {
        download("http://lovepicture.nosdn.127.net/7032654270976152362?imageView&thumbnail=2000y2000&quality=100", "test.jpg");
    }

    public static void download(String downUrl, String file) {
        System.out.println("Downloading image at:\n\t" + downUrl);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(downUrl);
//            设置本地代理
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1080);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
            conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setConnectTimeout(5 * 1000); // 设置连接超时
            conn.setRequestMethod("GET"); // 设置请求方法，这里是“GET”
            conn.connect();

            System.out.println("code:" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                int contentLength = conn.getContentLength();
                // 得到输入流
                InputStream inStream = conn.getInputStream();
                byte[] buffer = new byte[8 * 1024];
                int offset = 0;
                OutputStream out = new FileOutputStream(file);

                int totalLen = 0;

                while ((offset = inStream.read(buffer)) != -1) {
                    System.out.println("download：" + totalLen + "/" + contentLength);
                    out.write(buffer, 0, offset);
                    totalLen += offset;
                }
                out.close();
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("下载出错：" + e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
