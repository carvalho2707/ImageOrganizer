package pt.tiago.imagenamenomeclature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

public class ImageUpdater {
	public static final String ERROR_PATH = "error\\";
	public static final String BACKUP_PATH = "backup\\";
	public static final String UPDATED_PATH = "updated\\";
	public static final String UNTOUCHED_PATH = "untouched\\";
	public static final String FINAL_PATH = "final\\";
	public static final Logger okLog = Logger.getLogger("okLogger");
	public static final Logger noktLog = Logger.getLogger("nokLogger");

	public static void main(String[] args) {
		String basePath = "";
		List<String> unknownFormat = new ArrayList<String>();

		File imageFolder = new File(basePath);
		if (!imageFolder.exists()) {
			noktLog.error("Folder Not Found");
			return;
		}
		iterateFolder(imageFolder, unknownFormat);
		for (String format : unknownFormat) {
			okLog.info(format);
		}
	}

	public static void iterateFolder(File folder, List<String> unknownFormat) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				iterateFolder(file, unknownFormat);
			} else {
				handleImage(file, unknownFormat);
			}
		}
		String path = FilenameUtils.getFullPath(folder.getPath()) + folder.getName() + File.separator;
		File finalFolder = new File(path + FINAL_PATH);
		if (!finalFolder.exists()) {
			finalFolder.mkdirs();
		}

		try {
			for (File img : new File(path + UNTOUCHED_PATH).listFiles()) {
				String originalFileName = FilenameUtils.removeExtension(img.getName());
				File backup = new File(path + FINAL_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));
				Utils.copyFileUsingStream(img, backup);
			}

			for (File img : new File(path + UPDATED_PATH).listFiles()) {
				String originalFileName = FilenameUtils.removeExtension(img.getName());
				File backup = new File(path + FINAL_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));
				Utils.copyFileUsingStream(img, backup);
			}
		} catch (IOException e) {
			noktLog.error(e);
		}
	}

	public static void handleImage(File img, List<String> unknownFormat) {
		try {
			boolean allowed = false;
			String name = null;
			String path = FilenameUtils.getFullPath(img.getPath());
			String originalFileName = FilenameUtils.removeExtension(img.getName());

			File backupFolder = new File(path + BACKUP_PATH);
			if (!backupFolder.exists()) {
				backupFolder.mkdirs();
			}

			File errorFolder = new File(path + ERROR_PATH);
			if (!errorFolder.exists()) {
				errorFolder.mkdirs();
			}

			File updatedFolder = new File(path + UPDATED_PATH);
			if (!updatedFolder.exists()) {
				updatedFolder.mkdirs();
			}

			File notTouchedFolder = new File(path + UNTOUCHED_PATH);
			if (!notTouchedFolder.exists()) {
				notTouchedFolder.mkdirs();
			}

			File backup = new File(path + BACKUP_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));

			Utils.copyFileUsingStream(img, backup);

			Metadata metadata = ImageMetadataReader.readMetadata(img);
			int method = 0;
			if (metadata != null) {
				name = Utils.getNameFromExifSubIFDDirectory(img, metadata);
				method = 1;
			}
			if (name == null) {
				name = Utils.getNameFromImageName(img, metadata);
				if (name == null) {
					unknownFormat.add(originalFileName);
				}
				method = 2;
			}
			if (name == null && allowed) {
				name = Utils.getNameFromFileMetadataDirectory(img, metadata);
				method = 3;
			}
			if (name != null && !originalFileName.equals(name)) {
				okLog.info("Name is different! Method : " + method);
				File newImage = new File(path + UPDATED_PATH + name + "." + FilenameUtils.getExtension(img.getName()));
				boolean result = img.renameTo(newImage);
				if (result) {
					okLog.info("DONE");
				} else {
					noktLog.error("ERROR");
				}
			} else if (name != null && originalFileName.equals(name)) {
				System.out.println("Name is Correct! Method : " + method);
				File newImage = new File(path + UNTOUCHED_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));
				boolean moved = img.renameTo(newImage);
				if (moved) {
					okLog.info("Moved: OK");
				} else {
					noktLog.error("Moved:NOK");
				}
			} else if (name == null) {
				okLog.info("Moving unknow files");
				File newImage = new File(path + ERROR_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));
				boolean moved = img.renameTo(newImage);
				if (moved) {
					okLog.info("Moved: OK");
				}
			}
			System.out.println("-----------------------------------------------");
		} catch (ImageProcessingException e) {
			noktLog.error(e);
		} catch (IOException e) {
			noktLog.error(e);
		}
	}
}
