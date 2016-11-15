package com.example.wallpaper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.in;
import static jdk.nashorn.internal.codegen.OptimisticTypesPersistence.load;

/**
 * Created by ske on 2016/11/15.
 */

public class UnsplashWallpaper {

    //    "urls": {
//        "raw": "https://images.unsplash.com/photo-1479156661942-92cd989cdb56",
//                "full": "https://images.unsplash.com/photo-1479156661942-92cd989cdb56?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&s=6d1aea8fb35ad6f473dabc901a25b98a",
//                "regular": "https://images.unsplash.com/photo-1479156661942-92cd989cdb56?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=1080&fit=max&s=f1a58148892407996ab955ecb5a040f0",
//                "small": "https://images.unsplash.com/photo-1479156661942-92cd989cdb56?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&s=62f78350c85cd71355c78ed4ba3b8405",
//                "thumb": "https://images.unsplash.com/photo-1479156661942-92cd989cdb56?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=200&fit=max&s=10a4a944db40eead42b5edec6721606b"
//    },
//            "categories": [],
//            "links": {
//        "self": "https://api.unsplash.com/photos/g3QBQto9Jt0",
//                "html": "http://unsplash.com/photos/g3QBQto9Jt0",
//                "download": "http://unsplash.com/photos/g3QBQto9Jt0/download",
//                "download_location": "https://api.unsplash.com/photos/g3QBQto9Jt0/download"
//    }
    public static void main(String args[]) {
        String clientId = null;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("key.properties"));
            clientId = properties.getProperty("client_id");
        } catch (IOException e) {
            e.printStackTrace();
        }
        double screenWidth, screenHeight;  // 屏幕长宽
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = dim.getWidth();
        screenHeight = dim.getHeight();

        System.out.println("screen size:w=" + screenWidth + ",h=" + screenHeight);

        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.unsplash.com/photos?client_id=" + clientId + "&per_page=5");
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1080);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                InputStream in = connection.getInputStream();
                Gson gson = new Gson();

                List<PaperImage> images = gson.fromJson(new InputStreamReader(in), new TypeToken<List<PaperImage>>() {
                }.getType());

                System.out.println("get images from unsplash:" + images.size());
                for (PaperImage paperImage : images) {
                    if (!new File(".\\wallpaper\\wallpaper_" + paperImage.getId() + ".jpg").exists()) {
                        String imageUrl = paperImage.getUrls().getFull();
                        DownloadUtil.download(imageUrl, ".\\wallpaper\\wallpaper_" + paperImage.getId() + ".jpg");
//                    BufferedImage image = ImageUtil.getURLImage(imageUrl);
                        BufferedImage image = ImageIO.read(new File(".\\wallpaper\\wallpaper_" + paperImage.getId() + ".jpg"));
                        image = ImageUtil.scaleImage(image, screenWidth, screenHeight);
                        image = ImageUtil.cropImage(image, screenWidth, screenHeight);
                        if (saveImage(image, ".\\wallpaper\\wallpaper_" + paperImage.getId() + "_t.jpg")) {
                            installWallpaper("wallpaper_" + paperImage.getId() + "_t.jpg");
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("出错：" + e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }

//        try {
//            BufferedImage image = ImageIO.read(new File(".\\wallpaper\\wallpaper.jpg"));
//            image = ImageUtil.scaleImage(image, screenWidth, screenHeight);
//            image = ImageUtil.cropImage(image, screenWidth, screenHeight);
//            if (saveImage(image, ".\\wallpaper\\wallpaper1.jpg")) {
//                installWallpaper("wallpaper1.jpg");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static boolean saveImage(BufferedImage image, String fname) {
        try {
            File f = new File(fname);
            System.out.println(fname + " " + f.getCanonicalPath());
            return ImageIO.write(image, "jpg", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Wallpaper installation requires three changes to thw Win32
     * registry, and a desktop refresh. The basic idea (using Visual C# and VB)
     * is explained in "Setting Wallpaper" by Sean Campbell:
     * http://blogs.msdn.com/coding4fun/archive/2006/10/31/912569.aspx
     */
    private static void installWallpaper(String fnm) {
        try {
            String fullFnm = new File(".").getCanonicalPath() + File.separator + "wallpaper" + File.separator + fnm;
            System.out.println("fullFnm:" + fullFnm);

            /**
             * 3 registry key changes to HKEY_CURRENT_USER\Control Panel\Desktop
             * These three keys (and many others) are explained at
             * http://www.virtualplastic.net/html/desk_reg.html
             *
             * List of registry functions at MSDN:
             * http://msdn.microsoft.com/en-us/library/ms724875(v=VS.85).aspx
             */
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER,
                    "Control Panel\\Desktop", "Wallpaper", fullFnm);
            Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER,
                    "Control Panel\\Desktop", "WallpaperStyle", 10);  // no stretching
            Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER,
                    "Control Panel\\Desktop", "TileWallpaper", 0);   // no tiling

            // refresh the desktop using User32.SystemParametersInfo(), so avoiding an OS reboot
            int SPI_SETDESKWALLPAPER = 0x14;
            int SPIF_UPDATEINIFILE = 0x01;
            int SPIF_SENDWININICHANGE = 0x02;

            boolean result = UnsplashWallpaper.WinUser32.INSTANCE
                    .SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, fullFnm, SPIF_UPDATEINIFILE | SPIF_SENDWININICHANGE);
            System.out.println("Refresh desktop result: " + result);
        } catch (IOException e) {
            System.out.println("Could not find directory path");
        }
    }  // end of installWallpaper()


    // ---------------------------------------------


    /**
     * JNA Win32 extensions includes a User32 class, but it doesn't contain
     * SystemParametersInfo(), so it must be defined here.
     * <p>
     * MSDN libary docs on SystemParametersInfo() are at:
     * http://msdn.microsoft.com/en-us/library/ms724947(VS.85).aspx
     * <p>
     * BOOL WINAPI SystemParametersInfo(
     * __in     UINT uiAction,
     * __in     UINT uiParam,
     * __inout  PVOID pvParam,
     * __in     UINT fWinIni );
     * <p>
     * When uiAction == SPI_SETDESKWALLPAPER, SystemParametersInfo() sets the desktop wallpaper.
     * The value of the pvParam parameter determines the new wallpaper.
     */
    private interface WinUser32 extends StdCallLibrary {
        UnsplashWallpaper.WinUser32 INSTANCE = (WinUser32) Native.loadLibrary("user32", WinUser32.class);

        // SystemParametersInfoA() is the ANSI name used in User32.dll
        boolean SystemParametersInfoA(int uiAction, int uiParam, String fnm, int fWinIni);
    }
}
