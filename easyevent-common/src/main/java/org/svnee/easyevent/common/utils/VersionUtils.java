package org.svnee.easyevent.common.utils;

import java.security.CodeSource;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.constant.CommonConstants;

/**
 * VersionUtils.
 *
 * @author svnee
 */
@Slf4j
@SuppressWarnings("all")
public class VersionUtils {

    private static final String VERSION = getVersion(VersionUtils.class, "1.0.0");

    private VersionUtils() {
    }

    /**
     * Gets project version.
     *
     * @return the version
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Gets project version.
     *
     * @param cls the cls
     * @param defaultVersion the default version
     * @return the version
     */
    public static String getVersion(final Class<?> cls, final String defaultVersion) {
        try {
            // find version info from MANIFEST.MF first
            String version = cls.getPackage().getImplementationVersion();
            // if version is not exist, use package specification version!
            if (version == null || version.length() == 0) {
                version = cls.getPackage().getSpecificationVersion();
            }
            if (version == null || version.length() == 0) {
                // guess version fro jar file name if nothing's found from MANIFEST.MF
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if (codeSource == null) {
                    log.info(
                        "[VersionUtils#getVersion]No codeSource for class {} when getVersion, use default version {}",
                        cls.getName(), defaultVersion);
                } else {
                    String file = codeSource.getLocation().getFile();
                    if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                        file = file.substring(0, file.length() - 4);
                        int i = file.lastIndexOf(CommonConstants.SLASH_SEPARATOR);
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        i = file.indexOf("-");
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        while (file.length() > 0 && !Character.isDigit(file.charAt(0))) {
                            i = file.indexOf("-");
                            if (i >= 0) {
                                file = file.substring(i + 1);
                            } else {
                                break;
                            }
                        }
                        version = file;
                    }
                }
            }
            // return default version if no version info is found
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) {
            // return default version when any exception is thrown
            log.error("[VersionUtils#getVersion]return default version, ignore exception " + e.getMessage(), e);
            return defaultVersion;
        }
    }

}
