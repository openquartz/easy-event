package com.openquartz.easyevent.core.logo;

import java.io.PrintStream;
import org.slf4j.Logger;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.utils.VersionUtils;

/**
 * EasyEvent logo.
 *
 * @author svnee
 */
public class EasyEventBannerPrinter {

    private static final String EASY_EVENT_BANNER = "\n" +
        " ______                ______               _   \n"
        + " |  ____|              |  ____|             | |  \n"
        + " | |__   __ _ ___ _   _| |____   _____ _ __ | |_ \n"
        + " |  __| / _` / __| | | |  __\\ \\ / / _ \\ '_ \\| __|\n"
        + " | |___| (_| \\__ \\ |_| | |___\\ V /  __/ | | | |_ \n"
        + " |______\\__,_|___/\\__, |______\\_/ \\___|_| |_|\\__|\n"
        + "                   __/ |                         \n"
        + "                  |___/                          ";

    /**
     * banner println
     */
    public void printBanner(PrintStream out) {
        String bannerText = buildBannerText();
        out.println(bannerText);
        out.println();
    }

    /**
     * logo println
     */
    public void printBanner(Logger logger) {
        String bannerText = buildBannerText();
        logger.info(bannerText);
    }

    /**
     * print banner
     *
     * @param logger log
     */
    public void print(Logger logger) {
        if (logger.isInfoEnabled()) {
            printBanner(logger);
            return;
        }
        printBanner(System.out);
    }

    private String buildBannerText() {
        return CommonConstants.LINE_SEPARATOR
            + CommonConstants.LINE_SEPARATOR
            + EASY_EVENT_BANNER
            + CommonConstants.LINE_SEPARATOR
            + " :: EasyEvent :: (v" + VersionUtils.getVersion() + ")  @author svnee"
            + CommonConstants.LINE_SEPARATOR;
    }

}
