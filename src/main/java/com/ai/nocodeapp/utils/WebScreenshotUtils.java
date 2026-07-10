package com.ai.nocodeapp.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    private static final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();

    private static WebDriver getDriver() {
        WebDriver driver = webDriverThreadLocal.get();

        if (driver == null) {
            driver = initChromeDriver(1600, 900);
            webDriverThreadLocal.set(driver);
        }

        return driver;
    }

    public static void closeDriver() {
        WebDriver driver = webDriverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            webDriverThreadLocal.remove();
        }
    }

    public static String saveScreenshot(String url) {
        try {
            WebDriver webDriver = getDriver();
            // 保存目录：/tmp/screenshots/{randomUUID.substring(0, 8)}
            String saveDir = System.getProperty("user.dir") + File.separator +
                    "tmp" + File.separator + "screenshots" + File.separator + UUID.randomUUID().toString().substring(0, 8);
            // 保存文件名
            final String suffix = ".png";
            String saveFileName =
                    saveDir + File.separator + RandomUtil.randomString(5) + suffix;
            // 获取网页
            webDriver.get(url);
            waitForPageLoad(webDriver);
            // 保存截图
            byte[] screenshotAsBytes =
                    ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotAsBytes, saveFileName);
            // 压缩图片
            String compressFileName =
                    saveDir + File.separator + RandomUtil.randomString(5) +
                            "_compress" + suffix;
            compressImage(saveFileName, compressFileName);
            // 删除原始保存图片
            FileUtil.del(saveFileName);
            return compressFileName;
        } catch (Exception e) {
            log.error("保存截图失败： {}", e.getMessage());
            return null;
        } finally {
            closeDriver();
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width,
                    height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; " +
                    "Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome " +
                    "浏览器失败");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {

            // 创建等待页面加载对象
            WebDriverWait wait =
                    new WebDriverWait(driver,
                            Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return " +
                                    "document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    private static void saveImage(byte[] imageAsBytes, String path) {
        try {
            FileUtil.writeBytes(imageAsBytes, path);
        } catch (Exception e) {
            log.error("保存图片失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    private static void compressImage(String sourcePath, String targetPath) {
        final float COMPRESS_QUALITY = 0.3F;
        try {
            ImgUtil.compress(FileUtil.file(sourcePath),
                    FileUtil.file(targetPath), COMPRESS_QUALITY);
        } catch (Exception e) {
            log.error("图片压缩失败 {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

}

