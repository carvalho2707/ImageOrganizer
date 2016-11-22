package pt.tiago.imageorganizer;

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
	public static final Logger okLog = Logger.getLogger("okLogger");

	public static void main(String[] args) {
		okLog.info("Started");
		if (args.length == 0) {
			return;
		}
		String basePath = args[0] + "\\";
//		String basePath = "C:\\Users\\tiago.carvalho\\Documents\\Teste\\";
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
					unknownFormat.add(img.getPath());
				}
				method = 2;
			}
			if (name == null && allowed) {
				name = Utils.getNameFromFileMetadataDirectory(img, metadata);
				method = 3;
			}

			if (name != null) {
				if (originalFileName.equals(name)) {
					okLog.info("Name is Equal!" + " ;Method: " + method);
					return;
				}
				okLog.info("Name is Different ;Method: " + method);
				File newImage = new File(path + name + "." + FilenameUtils.getExtension(img.getName()));
				boolean notUnique = newImage.exists();
				String auxName = name;
				int count = 0;
				while (notUnique) {
					okLog.error("File: " + auxName + " already exists");
					auxName = name.substring(0, name.length() - 1);
					auxName += count++;
					okLog.info("Generating new name: " + auxName);
					newImage = new File(path + auxName + "." + FilenameUtils.getExtension(img.getName()));
					if (!newImage.exists()) {
						notUnique = false;
					} else if (count > 9) {
						break;
					}
				}
				if (notUnique) {
					okLog.error("NO NAME AVAILABLE");
				} else {
					boolean result = img.renameTo(newImage);
					if (result) {
						okLog.info("DONE");
					} else {
						okLog.error("FAILED");
					}
				}
			} else {
				okLog.error("Unkown Date");
			}
			okLog.info("-----------------------------------------------");
		} catch (ImageProcessingException e) {
			okLog.error(e);
		} catch (IOException e) {
			okLog.error(e);
		}
	}
}
