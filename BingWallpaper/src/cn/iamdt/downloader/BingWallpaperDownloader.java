package cn.iamdt.downloader;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Scanner;
import java.util.regex.*;
import java.util.stream.Collectors;

public class BingWallpaperDownloader {

    // 壁纸保存的根目录
    private static final String ROOT_SAVE_DIR = System.getProperty("user.home") + File.separator + "BingWallpapers";
    // 默认抓取的页面值
    private static final String DEFAULT_PAGE_URL = "https://bing.iamdt.cn/";

    public static void main(String[] args) {

        System.out.print("""
                ==================================================================================================================================================
                 ____  _           __        __    _ _                             ____                      _                 _          \s
                | __ )(_)_ __   __ \\ \\      / /_ _| | |_ __   __ _ _ __   ___ _ __|  _ \\  _____      ___ __ | | ___   __ _  __| | ___ _ __\s
                |  _ \\| | '_ \\ / _` \\ \\ /\\ / / _` | | | '_ \\ / _` | '_ \\ / _ \\ '__| | | |/ _ \\ \\ /\\ / / '_ \\| |/ _ \\ / _` |/ _` |/ _ \\ '__|
                | |_) | | | | | (_| |\\ V  V / (_| | | | |_) | (_| | |_) |  __/ |  | |_| | (_) \\ V  V /| | | | | (_) | (_| | (_| |  __/ |  \s
                |____/|_|_| |_|\\__, | \\_/\\_/ \\__,_|_|_| .__/ \\__,_| .__/ \\___|_|  |____/ \\___/ \\_/\\_/ |_| |_|_|\\___/ \\__,_|\\__,_|\\___|_|  \s
                               |___/                  |_|         |_|                                                                     \s
                
                工具名称：BingWallpaperDownloader
                工具版本：V1.0.1
                工具作者：冬天冬天W
                作者博客：https://www.iamdt.cn/
                使用文档：https://www.iamdt.cn/archives/1711780461050
                ==================================================================================================================================================
                """);

        Scanner scanner = new Scanner(System.in);

        System.out.println("请输入要抓取的页面网址: ");
        System.out.println("（留空使用默认值：https://bing.iamdt.cn/）");
        String userInput = scanner.nextLine();
        String pageUrl = userInput.isEmpty() ? DEFAULT_PAGE_URL : userInput;

        // 验证用户输入的URL格式是否正确
        if (!isValidUrl(pageUrl)) {
            System.out.println("无效的网址。改用默认网址。");
            pageUrl = DEFAULT_PAGE_URL;
        }

        System.out.println("请输入壁纸的保存目录: ");
        System.out.println("（留空使用默认值：C:\\Users\\用户名\\BingWallpapers）");
        String userDirInput = scanner.nextLine();
        String saveDir = userDirInput.isEmpty() ? ROOT_SAVE_DIR : userDirInput;

        // 验证用户输入的目录路径是否有效
        if (!isValidPath(saveDir)) {
            saveDir = ROOT_SAVE_DIR;
            System.out.println("无效的目录路径。将使用默认目录。");
        } else {
            System.out.println("壁纸将保存在: " + saveDir);
        }

        String htmlContent = downloadPageContent(pageUrl);

        // 确保保存目录存在
        createSaveDir(saveDir);

        // 正则表达式用于匹配壁纸图片的URL和日期
        Pattern pattern = Pattern.compile("<img class=\"bigImg\".*?src=\"(.*?)\".*?<p>(.*?) <a href=\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String imageUrl = matcher.group(1).trim();
            String date = matcher.group(2).trim().replace("-", "");
            String year = date.substring(0, 4);
            String month = date.substring(4, 6);
            imageUrl = removeParametersExcept(imageUrl);

            // 创建年份和月份的子文件夹
            String saveSubDir = saveDir + File.separator + year + File.separator + month;
            createSaveDir(saveSubDir);

            String fileName = saveSubDir + File.separator + date + ".jpg";

            // 检查文件是否已存在
            if (!Files.exists(Paths.get(fileName))) {
                boolean isDownloaded = downloadImage(imageUrl, fileName);
                if (isDownloaded) {
                    System.out.println("已下载并保存壁纸至: " + fileName);
                } else {
                    System.out.println("下载壁纸失败: " + imageUrl);
                }
            } else {
                System.out.println("日期为 " + date + " 的壁纸已存在。跳过下载。");
            }
        }

        System.out.println("程序运行结束");
        scanner.close();
    }

    // 验证URL是否符合预期格式
    private static boolean isValidUrl(String url) {
        String regex = "http(s)?://bing\\.iamdt\\.cn/(\\d{4}-\\d{2}\\.html)?";
        return url.matches(regex);
    }

    // 去掉非关键URL访问参数
    public static String removeParametersExcept(String url) {
        int paramStart = url.indexOf("?");
        if (paramStart == -1) {
            return url; // URL中无参数
        }

        String baseUrl = url.substring(0, paramStart);
        String[] params = url.substring(paramStart + 1).split("&");
        StringBuilder newUrl = new StringBuilder(baseUrl);

        for (String param : params) {
            if (param.startsWith("id" + "=")) {
                newUrl.append("?").append(param);
                break;
            }
        }

        return newUrl.toString();
    }

    // 方法用于下载网页内容
    private static String downloadPageContent(String pageUrl) {
        try (InputStream in = new URL(pageUrl).openStream()) {
            return new BufferedReader(new InputStreamReader(in))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // 方法用于下载图片并以日期命名保存到本地
    private static boolean downloadImage(String imageUrl, String fileName) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("下载图片时出错 " + imageUrl + ": " + e.getMessage());
            return false;
        }
    }

    // 创建保存壁纸的目录
    private static void createSaveDir(String saveDir) {
        Path saveDirPath = Paths.get(saveDir);
        if (!Files.exists(saveDirPath)) {
            try {
                Files.createDirectories(saveDirPath);
                System.out.println("已创建保存壁纸的目录: " + saveDir);
            } catch (IOException e) {
                System.out.println("创建目录失败: " + saveDir);
                e.printStackTrace();
            }
        }
    }

    // 验证路径是否有效的方法
    private static boolean isValidPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }
}