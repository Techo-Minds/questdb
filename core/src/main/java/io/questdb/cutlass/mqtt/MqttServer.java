/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cutlass.mqtt;

import io.questdb.FactoryProvider;
import io.questdb.Metrics;
import io.questdb.cairo.CairoEngine;
import io.questdb.cutlass.auth.SocketAuthenticator;
import io.questdb.cutlass.pgwire.BadProtocolException;
import io.questdb.cutlass.pgwire.CircuitBreakerRegistry;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.mp.Job;
import io.questdb.mp.WorkerPool;
import io.questdb.network.*;
import io.questdb.std.Misc;
import io.questdb.std.ObjList;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;

import static io.questdb.network.IODispatcher.*;

public class MqttServer implements Closeable {
    private static final Log LOG = LogFactory.getLog(MqttServer.class);
    private final MqttConnectionContextFactory contextFactory;
    private final IODispatcher<MqttConnectionContext> dispatcher;
    private final ObjList<MqttProcessor> processors;
    private final int workerCount;
    private final WorkerPool workerPool;
    CircuitBreakerRegistry registry;

    public MqttServer(
            MqttServerConfiguration configuration,
            CairoEngine engine,
            WorkerPool pool,
            Metrics metrics,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.workerCount = pool.getWorkerCount();
        this.processors = new ObjList<>();
        for (int i = 0; i < workerCount; i++) {
            this.processors.set(i, new MqttProcessor());
        }
        this.contextFactory = new MqttConnectionContextFactory(
                engine,
                configuration,
                circuitBreakerRegistry,
                metrics
        );
        this.dispatcher = IODispatchers.create(configuration.getDispatcherConfiguration(), contextFactory);
        this.workerPool = pool;
        this.registry = circuitBreakerRegistry;

        this.workerPool.assign(this.dispatcher);

        for (int i = 0, n = workerPool.getWorkerCount(); i < n; i++) {
            workerPool.assign(i, new Job() {
                private final IORequestProcessor<MqttConnectionContext> processor = (operation, context, dispatcher) -> {
                    try {
                        if (operation == IOOperation.HEARTBEAT) {
                            dispatcher.registerChannel(context, IOOperation.HEARTBEAT);
                            return false;
                        }
                        context.handleClientOperation(operation);
                        dispatcher.registerChannel(context, IOOperation.READ);
                        return true;
                    } catch (PeerIsSlowToWriteException e) {
                        dispatcher.registerChannel(context, IOOperation.READ);
                    } catch (PeerIsSlowToReadException e) {
                        dispatcher.registerChannel(context, IOOperation.WRITE);
                    } catch (QueryPausedException e) {
                        context.setSuspendEvent(e.getEvent());
                        dispatcher.registerChannel(context, IOOperation.WRITE);
                    } catch (PeerDisconnectedException e) {
                        dispatcher.disconnect(
                                context,
                                operation == IOOperation.READ
                                        ? DISCONNECT_REASON_PEER_DISCONNECT_AT_RECV
                                        : DISCONNECT_REASON_PEER_DISCONNECT_AT_SEND
                        );
                    } catch (BadProtocolException e) {
                        dispatcher.disconnect(context, DISCONNECT_REASON_PROTOCOL_VIOLATION);
                    } catch (Throwable e) { // must remain last in catch list!
                        LOG.critical().$("internal error [ex=").$(e).$(']').$();
                        // This is a critical error, so we treat it as an unhandled one.
                        metrics.health().incrementUnhandledErrors();
                        dispatcher.disconnect(context, DISCONNECT_REASON_SERVER_ERROR);
                    }
                    return false;
                };

                @Override
                public boolean run(int workerId, @NotNull RunStatus runStatus) {
                    return dispatcher.processIOQueue(processor);
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        Misc.free(dispatcher);
        Misc.free(contextFactory);
    }

    private static class MqttConnectionContextFactory extends IOContextFactoryImpl<MqttConnectionContext> {

        public MqttConnectionContextFactory(
                CairoEngine engine,
                MqttServerConfiguration configuration,
                CircuitBreakerRegistry registry,
                Metrics metrics
        ) {
            super(
                    () -> {
//
                        MqttConnectionContext mqttConnectionContext = new MqttConnectionContext(
                                engine,
                                configuration
                        );
                        FactoryProvider factoryProvider = configuration.getFactoryProvider();
                        SocketAuthenticator authenticator = factoryProvider.getMqttAuthenticatorFactory().getMqttAuthenticator(configuration);
                        mqttConnectionContext.setAuthenticator(authenticator);
                        return mqttConnectionContext;
                    },
//                configuration.getConnectionPoolInitialCapacity()
                    // todo: add capacity
                    1
            );
        }
    }
}

