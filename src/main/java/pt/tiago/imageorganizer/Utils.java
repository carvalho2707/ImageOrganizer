package pt.tiago.imageorganizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileMetadataDirectory;

public class Utils {
	private static final String IMG_PREFFIX = "IMG_";
	public static final Logger okLog = Logger.getLogger("okLogger");

	public static String getNameFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringBuilder finalName = new StringBuilder();
		finalName.append(IMG_PREFFIX);
		finalName.append(sdf.format(cal.getTime()));
		return finalName.toString();
	}

	public static String getHHMMSSFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(cal.getTime());
	}

	@SuppressWarnings("deprecation")
	public static String getNameFromExifSubIFDDirectory(File img, Metadata metadata) {
		String name = null;
		String originalName = img.getName();
		originalName = FilenameUtils.removeExtension(originalName);
		ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		if (directory != null && !directory.isEmpty()) {
			Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (date != null) {
				name = getNameFromDate(date);
				okLog.info("Date Mod    : " + date.toGMTString());
				okLog.info("New Name    : " + name);
			}
		}
		return name;
	}

	@SuppressWarnings("deprecation")
	public static String getNameFromFileMetadataDirectory(File img, Metadata metadata) {
		String name = null;
		String originalName = img.getName();
		originalName = FilenameUtils.removeExtension(originalName);
		FileMetadataDirectory fileDirectory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
		Date date = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
		if (date != null) {
			name = getNameFromDate(date);
			okLog.info("Date Mod    : " + date.toGMTString());
			okLog.info("New Name    : " + name);
		}
		return name;
	}

	public static String getHHMMSSFromFileMetadataDirectory(File img, Metadata metadata) {
		String name = null;
		FileMetadataDirectory fileDirectory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
		Date date = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
		if (date != null) {
			name = getHHMMSSFromDate(date);
		}
		return name;
	}

	public static String getNameFromImageName(File img, Metadata metadata) {
		// IMG-YYYYMMDD-WA0005
		// IMG_YYYYMMDD_HHMMSS
		// runtasticYYYY-MM-DD_HH_MM_SS
		// YYYY-MM-DD HH.MM.SS
		// YYYYMMDD_HHMMSS
		// DSC_0136
		// DSC00085
		// Snapchat-20140531114201
		// Snapchat-3700987456750269998
		// IMG_001
		String originalName = img.getName();
		originalName = FilenameUtils.removeExtension(originalName);

		if (originalName.startsWith("IMG_") && !originalName.contains("WA") && originalName.length() == 19) {
			// IMG_YYYYMMDD_HHMMSS
			String tmpName = originalName.replace("IMG_", "");
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(4, 6);
			String dd = tmpName.substring(6, 8);
			String hh = tmpName.substring(9, 11);
			String min = tmpName.substring(11, 13);
			String ss = tmpName.substring(13, 15);

			if (!Utils.validateGeneratedDate(yyyy, mm, dd, hh, min, ss, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + tmpName);
			return "IMG_" + tmpName;
		} else if (originalName.startsWith("IMG-") && originalName.contains("-WA")) {
			// IMG-YYYYMMDD-WA0005
			String tmpName = originalName.replace("IMG-", "");
			int index = tmpName.indexOf("-WA");
			tmpName = tmpName.substring(0, index);// Remove WA if exists
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(4, 6);
			String dd = tmpName.substring(6, 8);

			String mod = Utils.getHHMMSSFromFileMetadataDirectory(img, metadata);
			if (mod == null) {
				Random random = new Random();
				mod = String.valueOf(random.nextInt(10)) + String.valueOf(random.nextInt(10)) + String.valueOf(random.nextInt(10))
						+ String.valueOf(random.nextInt(10)) + String.valueOf(random.nextInt(10)) + String.valueOf(random.nextInt(10));
			}

			if (!Utils.validateGeneratedDate(yyyy, mm, dd, null, null, null, mod)) {
				return null;
			}

			okLog.info("New Name    :" + " IMG_" + tmpName + "_" + mod);
			return "IMG_" + tmpName + "_" + mod;
		} else if (originalName.startsWith("runtastic")) {
			// runtasticYYYY-MM-DD_HH_MM_SS
			String tmpName = originalName.replace("runtastic", "");
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(5, 7);
			String dd = tmpName.substring(8, 10);
			String hh = tmpName.substring(11, 13);
			String min = tmpName.substring(14, 16);
			String ss = tmpName.substring(17, 19);
			if (!Utils.validateGeneratedDate(yyyy, mm, dd, hh, min, ss, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + yyyy + mm + dd + "_" + hh + min + ss);
			return "IMG_" + yyyy + mm + dd + "_" + hh + min + ss;
		} else if (originalName.contains("WhatsApp")) {
			// WhatsApp Image YYYY-MM-DD at HH.MM.SS
			String tmpName = originalName.replace("WhatsApp Image ", "");
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(5, 7);
			String dd = tmpName.substring(8, 10);
			tmpName = tmpName.substring(14);
			String hh = tmpName.substring(0, 2);
			String min = tmpName.substring(3, 5);
			String ss = tmpName.substring(6, 8);
			if (!Utils.validateGeneratedDate(yyyy, mm, dd, hh, min, ss, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + yyyy + mm + dd + "_" + hh + min + ss);
			return "IMG_" + yyyy + mm + dd + "_" + hh + min + ss;
		} else if (originalName.startsWith("Snapchat-") && originalName.length() == 23) {
			// Snapchat-20140531114201
			String tmpName = originalName.replace("Snapchat-", "");
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(4, 6);
			String dd = tmpName.substring(6, 8);
			String hh = tmpName.substring(8, 10);
			String min = tmpName.substring(10, 12);
			String ss = tmpName.substring(12, 14);
			if (!Utils.validateGeneratedDate(yyyy, mm, dd, hh, min, ss, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + yyyy + mm + dd + "_" + hh + min + ss);
			return "IMG_" + yyyy + mm + dd + "_" + hh + min + ss;
		} else if (originalName.startsWith("DSC") || (originalName.startsWith("Snapchat-") && originalName.length() != 23)
				|| (originalName.startsWith("IMG_") && originalName.length() < 15)) {
			// DSC_0136
			// DSC00085
			// Snapchat-3700987456750269998
			// IMG_001
			// String newName = "IMG_" + new Random().nextInt(100000);
			// System.out.println("New Name : " + newName);
			// return newName;
			String name = "IMG_U" + new Random().nextInt(1000000);
			okLog.info("New Name    : " + name);
			return name;
		} else {
			okLog.error("UNKNOWN FORMAT");
		}
		return null;
	}

	public static boolean validateGeneratedDate(String yyyy, String mm, String dd, String hh, String min, String ss, String hhmmss) {
		if (yyyy == null || !yyyy.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		if (mm == null || !mm.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		if (dd == null || !dd.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		if (hhmmss != null) {
			hh = hhmmss.substring(0, 2);
			min = hhmmss.substring(2, 4);
			ss = hhmmss.substring(4, 6);
		}
		if (hh == null || !hh.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		if (min == null || !min.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		if (ss == null || !ss.replaceAll("[0-9]", "").equals("")) {
			return false;
		}
		return true;
	}

	public static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}
