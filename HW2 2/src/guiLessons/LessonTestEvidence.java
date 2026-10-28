package guiLessons;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/** Writes actual JavaFX node snapshots as PNG evidence without extra GUI dependencies. */
final class LessonTestEvidence {

	/** Prevents instantiation of this utility class. */
	private LessonTestEvidence() { }

	/**
	 * Captures a laid-out node on the JavaFX Application Thread as an opaque RGB PNG.
	 * @param node rendered scene root or dialog pane
	 * @param destination output PNG path
	 * @throws IOException if creating the output directory or writing the image fails
	 */
	static void capture(Node node, Path destination) throws IOException {
		node.applyCss();
		if (node instanceof javafx.scene.Parent parent) parent.layout();
		WritableImage image = node.snapshot(null, null);
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader pixels = image.getPixelReader();
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
			for (int row = 0; row < height; row++) {
				deflater.write(0);
				for (int column = 0; column < width; column++) {
					int color = pixels.getArgb(column, row);
					deflater.write((color >> 16) & 255);
					deflater.write((color >> 8) & 255);
					deflater.write(color & 255);
				}
			}
		}
		Files.createDirectories(destination.toAbsolutePath().getParent());
		try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(destination))) {
			output.writeLong(0x89504e470d0a1a0aL);
			ByteArrayOutputStream header = new ByteArrayOutputStream();
			try (DataOutputStream dimensions = new DataOutputStream(header)) {
				dimensions.writeInt(width);
				dimensions.writeInt(height);
				dimensions.write(new byte[] {8, 2, 0, 0, 0});
			}
			chunk(output, "IHDR", header.toByteArray());
			chunk(output, "IDAT", compressed.toByteArray());
			chunk(output, "IEND", new byte[0]);
		}
	}

	/**
	 * Writes one PNG chunk with its required CRC.
	 * @param output image stream
	 * @param type four-letter PNG chunk type
	 * @param data chunk payload
	 * @throws IOException if writing fails
	 */
	private static void chunk(DataOutputStream output, String type, byte[] data) throws IOException {
		byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		CRC32 checksum = new CRC32();
		checksum.update(typeBytes);
		checksum.update(data);
		output.writeInt(data.length);
		output.write(typeBytes);
		output.write(data);
		output.writeInt((int) checksum.getValue());
	}
}
