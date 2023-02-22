package org.svnee.easyevent.starter.transaction;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.svnee.easyevent.common.transaction.AfterTransactionCallback;
import org.svnee.easyevent.common.transaction.InTransactionCallback;
import org.svnee.easyevent.common.transaction.TransactionSupport;

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
