package com.letest;

import static leo.lionengine.assets.db.AssetDB.ASST_HEAD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.JOptionPane;

public class DummyAssetDBWriter {

	public static void main(String[] args) {
		try {
			main0();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to write to file test.lad:\n" + e, "Writer failure",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		System.exit(0);
	}

	private static void main0() throws Exception {
		File out = new File("test.lad");
		FileOutputStream fos = null;
		FileChannel ch = null;
		try {
			fos = new FileOutputStream(out);
			ch = fos.getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to open output stream to file test.lad", "Writer failure",
					JOptionPane.ERROR_MESSAGE);
		}
		ByteBuffer strBuf = ByteBuffer.allocate(64);
		ByteBuffer intBuf = ByteBuffer.allocate(Integer.BYTES);
		/*writeHead:*/ {
			// headBuf isn't required outside this
			ByteBuffer headBuf = ByteBuffer.allocate(ASST_HEAD.length);
			headBuf.put(ASST_HEAD);
			headBuf.position(0);
			ch.write(headBuf);
			headBuf = null;
		}
		getIntegerFromUser(intBuf, "amount of groups in database");
		ch.write(intBuf);
		Integer groupNum = intBuf.getInt(0);
		if (groupNum == 0) {
			fos.close();
			return;
		}
		for (int groupID = 0; groupID < groupNum; groupID++) {
			getStringFromUser(strBuf, "name of group #" + groupID, 64);
			ch.write(strBuf);
			getIntegerFromUser(intBuf, "size of group #" + groupID);
			ch.write(intBuf);
			Integer groupSize = intBuf.getInt(0);
			intBuf.clear();
			intBuf.putInt(0xFFFFFFFF);
			intBuf.position(0);
			for (int assetID = 0; assetID < groupSize; assetID++) {
				ch.write(intBuf); // asset type
				intBuf.position(0);
				getStringFromUser(strBuf, "name of asset #" + assetID, 64);
				ch.write(strBuf);
				ch.write(intBuf); // offset in chunk file
				intBuf.position(0);
				ch.write(intBuf); // compressed size
				intBuf.position(0);
				ch.write(intBuf); // uncompressed size
				intBuf.position(0);
			}
		}
		fos.close();
	}

	private static void getIntegerFromUser(ByteBuffer out, String message) {
		out.clear();
		String in = null;
		while (true) {
			in = JOptionPane.showInputDialog(null, String.format("Enter %s:", message), "1");
			if (in == null)
				continue;
			if (in.isEmpty())
				continue;
			break;
		}
		Integer i = Integer.parseUnsignedInt(in);
		out.putInt(i);
		out.position(0);
	}

	private static void getStringFromUser(ByteBuffer out, String message, int length) {
		out.clear();
		String in = null;
		while (true) {
			in = JOptionPane.showInputDialog(null, String.format("Enter %s:", message), "");
			if (in.length() > length)
				continue;
			break;
		}
		byte[] inB = new byte[length];
		byte[] inBTemp = null;
		try {
			inBTemp = in.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.arraycopy(inBTemp, 0, inB, 0, inBTemp.length);
		out.put(inB);
		out.position(0);
	}

}
