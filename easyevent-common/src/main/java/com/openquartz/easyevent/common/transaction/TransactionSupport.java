package com.openquartz.easyevent.common.transaction;

/**
 * Transaction Support
 *
 * @author svnee
 */
public interface TransactionSupport {

    /**
     * 执行
     *
     * @param callback 回调
     * @param <T> T
     * @return 返回结果
     */
    <T> T execute(InTransactionCallback<T> callback);

    /**
     * 事务提交后回调执行
     *
     * @param callback 回调
     */
    void executeAfterCommit(AfterTransactionCallback callback);

}
