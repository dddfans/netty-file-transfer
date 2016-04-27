package org.lqk.netty.server.forward.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.lqk.netty.forward.NettyCommandInvoker;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.util.SemaphoreReleaseOnlyOnce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class NettyRemotingClientSupport implements NettyCommandInvoker {
	private Bootstrap bootstrap;

	private final ConcurrentHashMap<Integer, NettyCommandFuture> requestTable;

	private Semaphore semaphoreAsync = new Semaphore(2048);

	private Semaphore semaphoreOneway = new Semaphore(2048);

	private ConcurrentHashMap<String, ChannelFuture> channelMap = new ConcurrentHashMap<String, ChannelFuture>();



	private static Logger log = LoggerFactory.getLogger(NettyRemotingClientSupport.class);
	public NettyRemotingClientSupport(Bootstrap bootstrap,ConcurrentHashMap<Integer, NettyCommandFuture> requestTable){
		this.bootstrap = bootstrap;
		this.requestTable = requestTable;
	}

	/**
	 * 建立channel
	 *
	 * @param addr
	 * @return
	 */
	private Channel createChannel(String addr) {
		Channel channel = null;
		ChannelFuture future = channelMap.get(addr);
		if (future != null) {
			channel = future.channel();
			return channel;
		}

		String[] split = addr.split(":");
		InetSocketAddress address = new InetSocketAddress(split[0], Integer.valueOf(split[1]));
		ChannelFuture sync = null;
		try {
			sync = this.bootstrap.connect(address).sync();
			channelMap.put(addr, sync);
			channel = sync.channel();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return channel;
	}

	/**
	 * 关闭channel
	 *
	 * @param addr
	 * @param channel
	 */
	private void closeChannel(String addr, Channel channel) {

	}

	public NettyCommand invokeSync(String addr, NettyCommand command, long timeoutMills) {
		try {
			Channel channel = createChannel(addr);
			if (channel != null && channel.isActive()) {
				NettyCommand repsonse = invokeSync(channel, command, timeoutMills);
				return repsonse;
			} else {
				this.closeChannel(addr, channel);
				throw new Exception(addr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void invokeAsync(String addr, NettyCommand command, long timeoutMills, NettyCommandCallBack callBack) {
		try {
			Channel channel = createChannel(addr);
			if (channel != null && channel.isActive()) {
				invokeAsync(channel, command, timeoutMills, callBack);
			} else {
				this.closeChannel(addr, channel);
				throw new Exception(addr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void invokeOneway(String addr, NettyCommand command, long timeoutMills) {
		try {
			Channel channel = createChannel(addr);
			if (channel != null && channel.isActive()) {
				invokeOneway(channel, command, timeoutMills);
			} else {
				this.closeChannel(addr, channel);
				throw new Exception(addr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 异步消息超时重发
	 */
	public void scanForReSend() {
		Collection<NettyCommandFuture> values = requestTable.values();

		for (Iterator<NettyCommandFuture> iter = values.iterator(); iter.hasNext(); ) {
			NettyCommandFuture nettyCommandFuture = iter.next();
			if (nettyCommandFuture.isTimeout()) {

				NettyCommand requestCommand = nettyCommandFuture.getRequestCommand();
				iter.remove();
				if (nettyCommandFuture.isReSendRequest()) {
					try {
						invokeAsync(nettyCommandFuture.getChannel(), requestCommand, nettyCommandFuture.getTimeoutMills(),
								nettyCommandFuture.getCallBack());
					} catch (InterruptedException e) {
						log.error(e.getMessage(),e);
					}
				}

			}
		}
	}


	/**
	 * 异步递送消息，并保证得到应答
	 *
	 */
	private void invokeAsync(final Channel channel, final NettyCommand request, long timeoutMills,
							NettyCommandCallBack callBack) throws InterruptedException {
		boolean acquire = semaphoreAsync.tryAcquire(timeoutMills, TimeUnit.MILLISECONDS);
		if (acquire) {
			SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);
			final NettyCommandFuture nettyCommandFuture = NettyCommandFuture.createAsynFuture(request.getOpaque(), timeoutMills, true,
					once, channel, callBack);
			try {
				requestTable.put(request.getOpaque(), nettyCommandFuture);
				channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						if (channelFuture.isSuccess()) {
							nettyCommandFuture.putRequest(request);
							return;
						} else {
							nettyCommandFuture.release();
							requestTable.remove(request.getOpaque());
						}
					}
				});
			} catch (Exception e) {
				nettyCommandFuture.release();
				requestTable.remove(request.getOpaque());
				log.error(e.getMessage(),e);
			}
		} else {
			// TODO 超时处理
		}
	}

	/**
	 * 同步递送消息
	 *
	 */
	private NettyCommand invokeSync(final Channel channel, final NettyCommand request, long timeoutMills)
			throws Exception {
		final NettyCommandFuture nettyCommandFuture = NettyCommandFuture.createSyncFuture(request.getOpaque(), timeoutMills);
		try {

			channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (channelFuture.isSuccess()) {
						requestTable.put(request.getOpaque(), nettyCommandFuture);
						nettyCommandFuture.putRequest(request);
						nettyCommandFuture.setSendRequestOK(true);
						return;
					} else {
						nettyCommandFuture.setSendRequestOK(false);
						return;
					}
				}
			});

			NettyCommand response = null;
			if (nettyCommandFuture.isSendRequestOK()) {
				try {
					response = nettyCommandFuture.waitResponse(10 * 1000);
				} catch (Exception e) {
					log.error(e.getMessage(),e);
				}
			} else {
                // 请求发送失败，自然就没有响应
			}

			return response;
		} finally {

		}
	}

	/**
	 * 单向递送消息
	 *
	 */
	private void invokeOneway(final Channel channel, NettyCommand request, long timeoutMills)
			throws InterruptedException {
		boolean acquire = semaphoreOneway.tryAcquire(timeoutMills, TimeUnit.MILLISECONDS);
		if (acquire) {
			try {
				final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
				NettyCommandBody body = request.getBody();
				if (body instanceof FileSegmentRequest) {
					FileSegmentRequest f = (FileSegmentRequest) body;
					log.debug("position {},blocksize {},filename {},crc32 {}", f.getPosition(), f.getBlockSize(),
							f.getFileName(), f.getCrc32());

				}
				channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						once.release();
					}
				});
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
	}
}
