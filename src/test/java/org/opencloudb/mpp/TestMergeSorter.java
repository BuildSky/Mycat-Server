package org.opencloudb.mpp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.opencloudb.mpp.tmp.FastRowDataPacketSorter;
import org.opencloudb.mysql.BufferUtil;
import org.opencloudb.net.mysql.RowDataPacket;

public class TestMergeSorter {

	@Test
	public void testOldSorter() {
		ColMeta colMeta = new ColMeta(0, ColMeta.COL_TYPE_INT);
		OrderCol col = new OrderCol(colMeta, OrderCol.COL_ORDER_TYPE_DESC);
		OrderCol[] orderCols = { col };
		RowDataPacketSorter sorter = new FastRowDataPacketSorter(orderCols);
		byte idLen = 4;
		byte packId = 0;
		int maxCount = 10000000;
		int bound = maxCount * 2;
		Random rd = new Random();
		Set<Integer> set = new HashSet<Integer>();
		while (set.size() < maxCount) {
			set.add(rd.nextInt(bound));
		}
		for (Integer integer : set) {
			String name = "name".concat(String.valueOf(integer));
			int length = name.length();
			RowDataPacket row = new RowDataPacket(2);
			ByteBuffer buffer = ByteBuffer.allocate(3 + 1 + 1 + 4 + 1 + length);
			BufferUtil.writeUB3(buffer, buffer.capacity());// PACKLEN
			buffer.put(packId++);// packID
			buffer.put(idLen);// LEN
			BufferUtil.writeInt(buffer, integer);
			buffer.put((byte) length);
			buffer.put(name.getBytes());
			row.read(buffer.array());
			sorter.addRow(row);
		}
		set.clear();
		System.gc();
		System.out.println("add finished");
		for (int i = 0; i < 100000; i++) {
			long st = System.currentTimeMillis();
			Collection<RowDataPacket> res = sorter.getSortedResult();
			long end = System.currentTimeMillis();// 37.246//15.196
			System.out.println((end - st) / 1000.0);
		}
		// for (RowDataPacket row : res) {
		// byte[] x = row.fieldValues.get(0);
		// byte[] name = row.fieldValues.get(1);
		// ByteBuffer wrap = ByteBuffer.wrap(x);
		// wrap.order(ByteOrder.LITTLE_ENDIAN);
		// System.out.println(wrap.getInt()+","+new String(name));
		//
		// }
	}
}