package pt.tiago.imageorganizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

public class ImageUpdater {
	public static final String ERROR_PATH = "error\\";
	public static final String BACKUP_PATH = "backup\\";
	public static final String UPDATED_PATH = "updated\\";
	public static final Logger okLog = Logger.getLogger("okLogger");

	public static void main(String[] args) {
		okLog.info("Started");
		String basePath = args[0] + "\\";
		okLog.info(basePath);
		List<String> unknownFormat = new ArrayList<String>();

		File imageFolder = new File(basePath);
		okLog.info(imageFolder.getPath());
		if (!imageFolder.exists()) {
			okLog.error("Folder Not Found");
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
				String fileExtenstion = FilenameUtils.getExtension(file.getName());
				if (fileExtenstion.equalsIgnoreCase("jpeg") || fileExtenstion.equalsIgnoreCase("jpg") || fileExtenstion.equalsIgnoreCase("png")
						|| fileExtenstion.equalsIgnoreCase("gif") || fileExtenstion.equalsIgnoreCase("bmp")) {
					handleImage(file, unknownFormat);
				} else {
					okLog.info("Skipped not supported file type: " + fileExtenstion + "  ; Filename: " + file.getName());
					continue;
				}
			}
		}
	}

	public static void handleImage(File img, List<String> unknownFormat) {
		try {
			okLog.info("-----------------------------------------------");
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

			File backup = new File(path + BACKUP_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));

			Utils.copyFileUsingStream(img, backup);

			Metadata metadata = ImageMetadataReader.readMetadata(img);
			int method = 0;
			okLog.info("OriginalName: " + originalFileName);
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

			if (name != null) {
				okLog.info("Name is " + (originalFileName.equals(name) ? "Equal!" : "Different") + " ;Method: " + method);
				File newImage = new File(path + UPDATED_PATH + name + "." + FilenameUtils.getExtension(img.getName()));
				boolean result = false;
				if (newImage.exists()) {
					okLog.error("File already exists");
					String auxName = name.substring(0, name.length() - 1);
					auxName += new Random().nextInt(10);
					okLog.info("Generating new name: " + auxName);
					newImage = new File(path + UPDATED_PATH + auxName + "." + FilenameUtils.getExtension(img.getName()));
					if (newImage.exists()) {
						okLog.error("Second File already exists");
					} else {
						result = img.renameTo(newImage);
					}
				} else {
					result = img.renameTo(newImage);
				}
				if (result) {
					okLog.info("DONE");
				} else {
					okLog.error("FAILED");
				}
			} else {
				okLog.info("Moving unknow files");
				File newImage = new File(path + ERROR_PATH + originalFileName + "." + FilenameUtils.getExtension(img.getName()));
				if (newImage.exists()) {
					okLog.error("File already exists");
				}
				boolean moved = img.renameTo(newImage);
				if (moved) {
					okLog.info("DONE");
				} else {
					okLog.error("FAILED");
				}
			}
			okLog.info("-----------------------------------------------");
		} catch (ImageProcessingException e) {
			okLog.error(e);
		} catch (IOException e) {
			okLog.error(e);
		}
	}
}
