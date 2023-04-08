/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openquartz.easyevent.transfer.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.openquartz.easyevent.transfer.disruptor.event.DisruptorTriggerEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * DisruptorTriggerEventHandler.
 *
 * @author svnee
 */
@Slf4j
public class DisruptorTriggerEventHandler implements WorkHandler<DisruptorTriggerEvent>, EventTrigger {

    private final ExecutorService executorService;
    private final Consumer<EventMessage> eventHandler;
    private final LockSupport lockSupport;

    public DisruptorTriggerEventHandler(final Consumer<EventMessage> eventHandler,
        final ExecutorService executorService,
        final LockSupport lockSupport) {
        this.eventHandler = eventHandler;
        this.executorService = executorService;
        this.lockSupport = lockSupport;
    }

    @Override
    public void onEvent(final DisruptorTriggerEvent event) {
        Future<?> submit = executorService.submit(() -> {
            // lock key
            Pair<String, LockBizType> lockKey = Pair
                .of(String.valueOf(event.getEventId().getId()), LockBizType.EVENT_HANDLE);
            // consume if lock
            lockSupport.consumeIfLock(lockKey, () -> eventHandler.accept(event));
        });
        try {
            submit.get();
        } catch (InterruptedException ex) {
            log.error("[DisruptorTriggerEventHandler#onEvent] interrupt-handle-error!event:{}", event, ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[DisruptorTriggerEventHandler#onEvent] handle-error!event:{}", event, ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
