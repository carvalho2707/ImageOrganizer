package pt.tiago.imageorganizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
		} else if (originalName.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}.[0-9]{2}.[0-9]{2}")) {
			// 2013-12-13 19.38.40
			String tmpName = originalName;
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
		} else if (originalName.matches("[aA-zZ]*_[0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}")) {
			// Screenshot_2014-11-09-13-32-08
			int pos = originalName.indexOf("_");
			String tmpName = originalName.substring(pos + 1);
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
		} else if (originalName.matches("[0-9]{8}_[0-9]{6}") || originalName.matches("_[0-9]{8}_[0-9]{6}")) {
			// 20141012_000928
			// _20141012_000928
			String tmpName = originalName;
			if (originalName.indexOf("_") == 0) {
				tmpName = originalName.substring(1);
			}
			String yyyy = tmpName.substring(0, 4);
			String mm = tmpName.substring(4, 6);
			String dd = tmpName.substring(6, 8);
			String hh = tmpName.substring(9, 11);
			String min = tmpName.substring(11, 13);
			String ss = tmpName.substring(13, 15);
			if (!Utils.validateGeneratedDate(yyyy, mm, dd, hh, min, ss, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + yyyy + mm + dd + "_" + hh + min + ss);
			return "IMG_" + yyyy + mm + dd + "_" + hh + min + ss;
		} else if (originalName.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}.*")) {
			// 2014-07-23_10-44-37.539_1095827725
			String tmpName = originalName;
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
		} else if (originalName.matches("^[0-9]{1,13}$") || originalName.matches("FxCam_[0-9]{1,13}$") || originalName.matches("received_[0-9]*")) {
			// 1415539314493
			// FxCam_1362085869892
			String tmpName = originalName;
			if (originalName.contains("received")) {
				int pos = originalName.indexOf("_");
				tmpName = originalName.substring(pos + 1);
				long milis = TimeUnit.MILLISECONDS.convert(Long.valueOf(tmpName), TimeUnit.MICROSECONDS);
				tmpName = String.valueOf(milis);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.valueOf(tmpName));
			calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
			String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
			int month = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);
			int hh = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			int ss = calendar.get(Calendar.SECOND);
			String monthVal = (month < 10) ? (0 + "" + month) : ("" + month);
			String dayVal = (dd < 10) ? (0 + "" + dd) : ("" + dd);
			String hourVal = (hh < 10) ? (0 + "" + hh) : ("" + hh);
			String minVal = (min < 10) ? (0 + "" + min) : ("" + min);
			String secVal = (ss < 10) ? (0 + "" + ss) : ("" + ss);
			if (!Utils.validateGeneratedDate(yyyy, monthVal, dayVal, hourVal, minVal, secVal, null)) {
				return null;
			}
			okLog.info("New Name    :" + " IMG_" + yyyy + monthVal + dayVal + "_" + hourVal + minVal + secVal);
			return "IMG_" + yyyy + monthVal + dayVal + "_" + hourVal + minVal + secVal;
		} else if (originalName.startsWith("DSC") || (originalName.startsWith("IMG_") && originalName.length() < 15)) {
			// DSC_0136
			// DSC00085
			// IMG_001
			String name = "IMG_U" + new Random().nextInt(1000000);
			okLog.info("New Name    : " + name);
			return null;
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

}
