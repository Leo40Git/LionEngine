package leo.lionengine.assets.db;

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

	public static final byte[] ASST_HEAD = new byte[] { 'A', 'S', 'S', 'T' };
	
	public static final int ASST_TYPE_IMAGE = 0;
	public static final int ASST_TYPE_AUDIO = 1;

	public static class AssetEntry {
		private Integer type;
		private String name;
		private Integer chunkOffset;
		private Integer sizeCompressed;
		private Integer sizeUncompressed;

		public AssetEntry(ByteBuffer buf) {
			type = buf.getInt();
			name = getString(buf, 4, 64);
			System.out.println(name + ")");
			buf.position(68);
			chunkOffset = buf.getInt();
			sizeCompressed = buf.getInt();
			sizeUncompressed = buf.getInt();
		}
		
		public Integer getType() {
			return type;
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
		ByteBuffer bufString = ByteBuffer.allocate(64);
		ByteBuffer bufAsset = ByteBuffer.allocate(80);
		fc.read(bufNum); // read magic string
		bufNum.flip();
		byte[] head = bufNum.array();
		if (!Arrays.equals(ASST_HEAD, head)) {
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
			String groupName = getString(bufString, 0, 64);
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

	private static String getString(ByteBuffer buf, int off, int len) {
		if (len > buf.limit())
			throw new IndexOutOfBoundsException(Integer.toUnsignedString(len));
		byte[] cbuf = new byte[len];
		buf.position(off);
		for (int i = 0; i < len; i++)
			cbuf[i] = buf.get();
		String ret = null;
		try {
			ret = new String(cbuf, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ret = ret.trim();
		return ret;
	}

}
