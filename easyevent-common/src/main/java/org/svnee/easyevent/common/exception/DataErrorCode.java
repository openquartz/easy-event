package org.svnee.easyevent.common.exception;

/**
 * 数据异常码
 *
 * @author svnee
 **/
public enum DataErrorCode implements EasyEventErrorCode {

    INSERT_ERROR("01", "保存失败,应影响数据条数:{0},实际:{1}", true),
    UPDATE_ERROR("02", "更新失败,应影响数据条数:{0},实际:{1}", true),
    DELETE_ERROR("03", "删除失败,应影响数据条数:{0},实际:{1}", true),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "DataError-";

    DataErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    DataErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }
}
