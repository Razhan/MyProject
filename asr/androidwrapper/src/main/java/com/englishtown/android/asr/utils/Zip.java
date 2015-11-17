package com.englishtown.android.asr.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {
	
	private static byte[] buffer = new byte[1024];

	public static synchronized void unpack(String path, String zipname) throws IOException {
		InputStream is;
		ZipInputStream zis;

		String filename;
		is = new FileInputStream(path + "/" + zipname);
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;
		
		int count = 0;

		while ((ze = zis.getNextEntry()) != null) {

			filename = ze.getName();
			File outputFile = new File(path + "/" + filename);
			if(ze.isDirectory()) {
				outputFile.mkdirs();
			} else {
				outputFile.getParentFile().mkdirs();
				FileOutputStream fout = new FileOutputStream(outputFile);
				

				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}


				fout.close();
				
			}
			
			zis.closeEntry();
		}

		zis.close();


		return;
	}
	
	public static synchronized boolean unpackToDir(InputStream is, String unpackDirPath) throws IOException {

		ZipInputStream zis;

		String filename;
		zis = new ZipInputStream(new BufferedInputStream(is));
		ZipEntry ze;
		byte[] buffer = null;
		int count = 0;

		while ((ze = zis.getNextEntry()) != null) {

			filename = ze.getName();
			File outputFile = new File(unpackDirPath + "/" + filename);
			if(ze.isDirectory()) {
				outputFile.mkdirs();
			} else {
				outputFile.getParentFile().mkdirs();
				FileOutputStream fout = new FileOutputStream(outputFile);
				
				if(buffer == null)
					buffer = new byte[1024];

				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}


				fout.close();
				
			}
			
			zis.closeEntry();
		}

		zis.close();


		return true;
	}

}
