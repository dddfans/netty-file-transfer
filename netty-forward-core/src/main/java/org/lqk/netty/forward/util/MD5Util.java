package org.lqk.netty.forward.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class MD5Util {

	public final static int THRESHOLD_SIZE = 4 * 1024 * 1024;

//	public static String md5(FileChannel fileChannel) throws IOException {
//		long positionMark = fileChannel.position();
//		ByteBuffer buffer = ByteBuffer.allocate(THRESHOLD_SIZE);
//		byte[] buf = new byte[THRESHOLD_SIZE];
//		MessageDigest md5 = DigestUtils.getMd5Digest();
//		long fileLength = fileChannel.size();
//		try {
//			int position = 0;
//			int size = THRESHOLD_SIZE;
//			boolean done = false;
//			while (!done) {
//				fileChannel.position(position);
//				fileChannel.read(buffer);
//				if (position + THRESHOLD_SIZE >= fileLength) {
//					size = (int) (fileLength - position);
//					done = true;
//				}
//				buffer.flip();
//				buffer.get(buf, 0, size);
//				md5.update(buf, 0, size);
//				position += size;
//				buffer.clear();
//			}
//			return Hex.encodeHexString(md5.digest());
//		} finally {
//			fileChannel.position(positionMark);
//			md5.reset();
//		}
//	}

	public static String md5(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[THRESHOLD_SIZE];
		MessageDigest md5 = DigestUtils.getMd5Digest();
		int len;
		while ((len = fis.read(buffer)) != -1) {
			md5.update(buffer, 0, len);
		}
		IOUtils.closeQuietly(fis);
		String re = Hex.encodeHexString(md5.digest());
		md5.reset();
		return re;
	}

	public static void main(String[] args) throws IOException {
//		String path = "/home/bert/abc.mp3";
//		File rawfile = new File(path);
//		System.out.println("file.length " + rawfile.length());
//		System.out.println(md5(rawfile));
//		RandomAccessFile file = new RandomAccessFile(path, "r");
//		FileChannel channel = file.getChannel();
//		System.out.println("channel.length " + channel.size());
//		System.out.println(channel.position());
//		System.out.println(md5(channel));
//		System.out.println(channel.position());


		byte[] b = new byte[1024];
		b[0] = 1;
		System.out.println(b.length);
	}
}
