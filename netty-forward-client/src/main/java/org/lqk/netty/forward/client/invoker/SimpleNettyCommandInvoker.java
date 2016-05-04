package org.lqk.netty.forward.client.invoker;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.forward.protocol.future.NettyCommandFutureBuilder;
import org.lqk.netty.forward.util.SemaphoreReleaseOnlyOnce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 以最简单方式存取channel，channel保存在map中，通过信号量来控制压力
 */

public class SimpleNettyCommandInvoker extends AbstractNettyCommandInvoker {

	private Semaphore semaphoreAsync = new Semaphore(2048);

	private Semaphore semaphoreOneway = new Semaphore(2048);

	private ConcurrentHashMap<String, ChannelFuture> channelMap = new ConcurrentHashMap<String, ChannelFuture>();

	private static Logger log = LoggerFactory.getLogger(SimpleNettyCommandInvoker.class);
	public SimpleNettyCommandInvoker(Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable){
		super(bootstrap,requestTable);
	}

	/**
	 * 建立channel
	 *
	 * @param addr
	 * @return
	 */
	protected Channel createChannel(String addr) {
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
			log.error(e.getMessage(),e);
		}
		return channel;
	}

	/**
	 * 关闭channel
	 *
	 * @param addr
	 * @param channel
	 */
	protected void closeChannel(String addr, Channel channel) {

	}


	/**
	 * 异步递送消息，并保证得到应答
	 *
	 */
	public void invokeAsync(final Channel channel, final NettyCommand request, long timeoutMills,
							NettyCommandCallBack callBack) throws InterruptedException {
		boolean acquire = semaphoreAsync.tryAcquire(timeoutMills, TimeUnit.MILLISECONDS);
		if (acquire) {
			SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);
			final NettyCommandFuture nettyCommandFuture = NettyCommandFutureBuilder.createAsynFuture(request.getOpaque(), timeoutMills, true,
					once, channel, callBack);
			super.doInvokeAsync(channel,request,nettyCommandFuture);
		} else {
			// TODO 超时处理
		}
	}



	/**
	 * 单向递送消息
	 *
	 */
    public void invokeOneway(final Channel channel, NettyCommand request, long timeoutMills)
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
