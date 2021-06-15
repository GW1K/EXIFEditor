import org.apache.commons.imaging.ImageReadException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.kielce.tu.ExifEditor;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class ExifEditorTest {

    @Test
    public void readEXIFDataFromFile_withExif() {
        //given
        File src = new File("src/test/samples/withExif.jpg");
        Vector<Vector<String>> metadata = null;
        //when
        try {
            metadata = ExifEditor.readEXIFDataFromFile(src);
        } catch (IOException | ImageReadException e) {
            e.printStackTrace();
        }
        //then
        Assertions.assertNotNull(metadata);
    }

    @Test
    public void readEXIFDataFromFile_withoutExif() {
        //given
        File src = new File("src/test/samples/withoutExif.jpg");
        //then
        Assertions.assertThrows(ImageReadException.class, () -> ExifEditor.readEXIFDataFromFile(src));
    }

    @Test
    public void readEXIFDataFromFile_notSupportedExt() {
        //given
        File src = new File("src/test/samples/notSupportedExt.txt");
        //then
        Assertions.assertThrows(ImageReadException.class, () -> ExifEditor.readEXIFDataFromFile(src));
    }

    @Test
    public void writeEXIFDataToFile_invalidNumberOfTagValues() {
        //given
        File src = new File("src/test/samples/withExif.jpg");
        Vector<String> tagValues = new Vector<>();
        //then
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExifEditor.writeEXIFDataToFile(src, null, tagValues));
    }
}
