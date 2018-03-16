package leo.lionengine.assetdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AssetDB {

	public static class AssetEntry {
		private String name;
		private Integer chunkOffset;
		private Integer sizeCompressed;
		private Integer sizeUncompressed;

		public AssetEntry(ByteBuffer buf) {
			name = getString(buf, 0);
			System.out.println(name + ")");
			buf.position(32);
			chunkOffset = buf.getInt();
			sizeCompressed = buf.getInt();
			sizeUncompressed = buf.getInt();
		}

		public String getName() {
			return name;
		}

		public Integer getChunkOffset() {
			return chunkOffset;
		}

		public Integer getSizeCompressed() {
			return sizeCompressed;
		}

		public Integer getSizeUncompressed() {
			return sizeUncompressed;
		}
	}

	private static Map<String, AssetEntry[]> assetGroups;

	public static void load(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		FileChannel fc = fis.getChannel();
		ByteBuffer bufNum = ByteBuffer.allocate(4);
		ByteBuffer bufString = ByteBuffer.allocate(32);
		ByteBuffer bufAsset = ByteBuffer.allocate(44);
		fc.read(bufNum); // read magic string
		bufNum.flip();
		if (bufNum.get() != 'A' || bufNum.get() != 'S' || bufNum.get() != 'S' || bufNum.get() != 'T') {
			fis.close();
			throw new IOException("Invalid database file: Magic string is wrong!");
		}
		bufNum.clear();
		fc.read(bufNum); // read number of groups
		bufNum.flip();
		// Java 8 can handle unsigned integers if the Integer class is used instead of
		// the int primitive
		Integer groupNum = bufNum.getInt(0);
		if (groupNum == 0) {
			fis.close();
			throw new IOException("Invalid database file: 0 asset groups!");
		}
		bufNum.clear();
		System.out.println("LionEngine AssetDB file has " + groupNum + " asset groups");
		// now let's get moving
		assetGroups = new HashMap<>();
		// start reading
		for (Integer groupId = 0; groupId < groupNum; groupId++) {
			fc.read(bufString); // read group name
			bufString.flip();
			String groupName = getString(bufString, 0);
			bufString.clear();
			fc.read(bufNum); // read group length
			bufNum.flip();
			Integer groupLen = bufNum.getInt(0);
			if (groupLen == 0)
				continue; // if group has 0 entries for some reason, don't bother
			System.out.println(
					"Reading group #" + groupId + " (" + groupName + ") which is " + groupLen + " entries long");
			AssetEntry[] entries = new AssetEntry[groupLen];
			for (Integer assetId = 0; assetId < groupLen; assetId++) {
				System.out.print("Reading asset #" + assetId + " (");
				fc.read(bufAsset);
				bufAsset.flip();
				entries[assetId] = new AssetEntry(bufAsset);
				bufAsset.clear();
			}
			assetGroups.put(groupName, entries);
		}
		fis.close();
	}

	private static String getString(ByteBuffer buf2, int off) {
		byte[] buf = buf2.array();
		int l = 0;
		while (l < buf.length && buf[l] > 0)
			l++;
		byte[] cbuf = Arrays.copyOf(buf, l);
		try {
			return new String(cbuf, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
