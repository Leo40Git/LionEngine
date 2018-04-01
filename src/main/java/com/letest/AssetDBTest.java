package com.letest;

import java.io.File;
import java.io.IOException;

import leo.lionengine.assets.db.AssetDB;

public class AssetDBTest {

	public static void main(String[] args) {
		try {
			AssetDB.load(new File("test.lad"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
