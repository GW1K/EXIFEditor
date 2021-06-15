import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.kielce.tu.EXIFEditorGUI;

public class EXIFEditorGUITest {

    @Test
    public void extractFileExtension_invalidPath() {
        //given
        String filePath = new String("file/path/invalid.test.png");
        //when
        String result = EXIFEditorGUI.extractFileExtension(filePath);
        //then
        Assertions.assertEquals(".png", result);
    }

    @Test
    public void extractFileExtension_validPath() {
        //given
        String filePath = new String("file/path/test.jpg");
        //when
        String result = EXIFEditorGUI.extractFileExtension(filePath);
        //then
        Assertions.assertEquals(".jpg", result);
    }
}
