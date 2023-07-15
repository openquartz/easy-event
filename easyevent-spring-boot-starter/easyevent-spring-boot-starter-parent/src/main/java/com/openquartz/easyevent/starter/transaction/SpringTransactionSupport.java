package com.openquartz.easyevent.starter.transaction;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.openquartz.easyevent.common.transaction.AfterTransactionCallback;
import com.openquartz.easyevent.common.transaction.InTransactionCallback;
import com.openquartz.easyevent.common.transaction.TransactionSupport;

/**
 * Spring Transaction Support
 *
 * @author svnee
 **/
public class SpringTransactionSupport implements TransactionSupport {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionSupport(TransactionTemplate transactionTemplate) {

        checkNotNull(transactionTemplate);

        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T execute(InTransactionCallback<T> callback) {
        return transactionTemplate.execute(action -> callback.doInTransaction());
    }

    @Override
    public void executeAfterCommit(AfterTransactionCallback callback) {
        // current is active
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    callback.doAfterCommit();
                }
            });
            return;
        }
        // direct-execute
        callback.doAfterCommit();
    }
}
