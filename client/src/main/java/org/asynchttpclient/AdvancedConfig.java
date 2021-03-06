/*
 * Copyright (c) 2014 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 *     http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.util.Timer;

import java.util.HashMap;
import java.util.Map;

import org.asynchttpclient.channel.pool.ConnectionStrategy;
import org.asynchttpclient.netty.EagerNettyResponseBodyPart;
import org.asynchttpclient.netty.LazyNettyResponseBodyPart;
import org.asynchttpclient.netty.NettyResponseBodyPart;
import org.asynchttpclient.netty.channel.pool.ChannelPool;
import org.asynchttpclient.netty.handler.DefaultConnectionStrategy;
import org.asynchttpclient.netty.ws.NettyWebSocket;

public class AdvancedConfig {

    private final Map<ChannelOption<Object>, Object> channelOptions = new HashMap<>();
    private EventLoopGroup eventLoopGroup;
    private boolean preferNative;
    private AdditionalPipelineInitializer httpAdditionalPipelineInitializer;
    private AdditionalPipelineInitializer wsAdditionalPipelineInitializer;
    private ResponseBodyPartFactory bodyPartFactory = new EagerResponseBodyPartFactory();
    private ChannelPool channelPool;
    private Timer nettyTimer;
    private NettyWebSocketFactory nettyWebSocketFactory = new DefaultNettyWebSocketFactory();
    private ConnectionStrategy connectionStrategy = new DefaultConnectionStrategy();

    /**
     * @param name the name of the ChannelOption
     * @param value the value of the ChannelOption
     * @param <T> the type of value
     * @return this instance of AdvancedConfig
     */
    @SuppressWarnings("unchecked")
    public <T> AdvancedConfig addChannelOption(ChannelOption<T> name, T value) {
        channelOptions.put((ChannelOption<Object>) name, value);
        return this;
    }

    public Map<ChannelOption<Object>, Object> getChannelOptions() {
        return channelOptions;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    public void setPreferNative(boolean preferNative) {
        this.preferNative = preferNative;
    }
    
    public boolean isPreferNative() {
        return preferNative;
    }

    public AdditionalPipelineInitializer getHttpAdditionalPipelineInitializer() {
        return httpAdditionalPipelineInitializer;
    }

    public void setHttpAdditionalPipelineInitializer(AdditionalPipelineInitializer httpAdditionalPipelineInitializer) {
        this.httpAdditionalPipelineInitializer = httpAdditionalPipelineInitializer;
    }

    public AdditionalPipelineInitializer getWsAdditionalPipelineInitializer() {
        return wsAdditionalPipelineInitializer;
    }

    public void setWsAdditionalPipelineInitializer(AdditionalPipelineInitializer wsAdditionalPipelineInitializer) {
        this.wsAdditionalPipelineInitializer = wsAdditionalPipelineInitializer;
    }

    public ResponseBodyPartFactory getBodyPartFactory() {
        return bodyPartFactory;
    }

    public void setBodyPartFactory(ResponseBodyPartFactory bodyPartFactory) {
        this.bodyPartFactory = bodyPartFactory;
    }

    public ChannelPool getChannelPool() {
        return channelPool;
    }

    public void setChannelPool(ChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    public Timer getNettyTimer() {
        return nettyTimer;
    }

    public void setNettyTimer(Timer nettyTimer) {
        this.nettyTimer = nettyTimer;
    }

    public NettyWebSocketFactory getNettyWebSocketFactory() {
        return nettyWebSocketFactory;
    }

    public void setNettyWebSocketFactory(NettyWebSocketFactory nettyWebSocketFactory) {
        this.nettyWebSocketFactory = nettyWebSocketFactory;
    }

    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

    public void setConnectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }
    
    public static interface AdditionalPipelineInitializer {

        void initPipeline(ChannelPipeline pipeline) throws Exception;
    }

    public static interface ResponseBodyPartFactory {

        NettyResponseBodyPart newResponseBodyPart(ByteBuf buf, boolean last);
    }

    public static class EagerResponseBodyPartFactory implements ResponseBodyPartFactory {

        @Override
        public NettyResponseBodyPart newResponseBodyPart(ByteBuf buf, boolean last) {
            return new EagerNettyResponseBodyPart(buf, last);
        }
    }

    public static class LazyResponseBodyPartFactory implements ResponseBodyPartFactory {

        @Override
        public NettyResponseBodyPart newResponseBodyPart(ByteBuf buf, boolean last) {
            return new LazyNettyResponseBodyPart(buf, last);
        }
    }

    public static interface NettyWebSocketFactory {
        NettyWebSocket newNettyWebSocket(Channel channel, AsyncHttpClientConfig config);
    }

    public class DefaultNettyWebSocketFactory implements NettyWebSocketFactory {

        @Override
        public NettyWebSocket newNettyWebSocket(Channel channel, AsyncHttpClientConfig config) {
            return new NettyWebSocket(channel, config);
        }
    }
}
