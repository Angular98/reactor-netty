/*
 * Copyright (c) 2011-2019 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.resources;

import java.util.concurrent.ThreadFactory;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * @author Stephane Maldini
 */
final class DefaultLoopEpoll implements DefaultLoop {

	static final Logger log = Loggers.getLogger(DefaultLoopEpoll.class);

	private static final boolean epoll;

	static {
		boolean epollCheck = false;
		try{
			Class.forName("io.netty.channel.epoll.Epoll");
			epollCheck = Epoll.isAvailable();
		}
		catch (ClassNotFoundException cnfe){
		}
		epoll = epollCheck;
		if (log.isDebugEnabled()) {
			log.debug("Default Epoll support : " + epoll);
		}
	}

	static boolean hasEpoll() {
		return epoll;
	}

	@Override
	public EventLoopGroup newEventLoopGroup(int threads, ThreadFactory factory) {
		return new EpollEventLoopGroup(threads, factory);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <CHANNEL extends Channel> CHANNEL getChannel(Class<CHANNEL> channelClass) {
		if (channelClass.equals(SocketChannel.class)) {
			return (CHANNEL)new EpollSocketChannel();
		}
		if (channelClass.equals(ServerSocketChannel.class)) {
			return (CHANNEL)new EpollServerSocketChannel();
		}
		if (channelClass.equals(DatagramChannel.class)) {
			return (CHANNEL)new EpollDatagramChannel();
		}
		if (channelClass.equals(DomainSocketChannel.class)) {
			return (CHANNEL)new EpollDomainSocketChannel();
		}
		if (channelClass.equals(ServerDomainSocketChannel.class)) {
			return (CHANNEL)new EpollServerDomainSocketChannel();
		}
		throw new IllegalArgumentException("Unsupported channel type: "+channelClass.getSimpleName());
	}

	@Override
	public String getName() {
		return "epoll";
	}

	@Override
	public boolean supportGroup(EventLoopGroup group) {
		if (group instanceof ColocatedEventLoopGroup) {
			group = ((ColocatedEventLoopGroup) group).get();
		}
		return group instanceof EpollEventLoopGroup;
	}

}
