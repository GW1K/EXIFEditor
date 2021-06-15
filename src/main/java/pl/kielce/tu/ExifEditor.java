package pl.kielce.tu;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffDirectory;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.util.Vector;

/**
 * Klasa implementujaca operacje na metadanych plikow graficznych.
 * */
public class ExifEditor {

    /**
     * Publiczna i statyczna metoda sluzaca do odczytu metadanych z podanego
     * w parametrze pliku docelowego. Umieszcza odczytane nazwy katalogow,
     * znacznikow i ich wartosci w wektorze przechowujacym wiersze dla
     * tabeli interfejsu graficznego. W momencie wystapienia bledow
     * zwraca odpowiednie wyjatki.
     *
     * @param src Docelowy plik z metadanymi do odczytu.
     * @return Zwraca wektor w postaci dwu-wymiarowej tablicy jesli
     * operacja odczytu sie powiodla lub null w przeciwnym przypadku.
     * @throws IOException W przypadku wystapienia bledu z odczytem pliku.
     * @throws ImageReadException Jesli odczyt metadanych z pliku
     * zrodlowego sie nie powiodl.
     * */
    public static Vector<Vector<String>> readEXIFDataFromFile(File src) throws IOException, ImageReadException {
        ImageMetadata imageMetadata = Imaging.getMetadata(src);
        JpegImageMetadata jpegMetadata = null;
        Vector<Vector<String>> metadata = null;
        if (imageMetadata == null || imageMetadata instanceof GenericImageMetadata) {
            throw new ImageReadException("No metadata found.");
        }
        if (imageMetadata instanceof JpegImageMetadata) {
            jpegMetadata = (JpegImageMetadata) imageMetadata;
        }
        if (jpegMetadata != null) {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif == null) {
                throw new ImageReadException("No exif data found.");
            }
            metadata = new Vector<>();
            Vector<String> row;
            for(TiffDirectory dir : exif.contents.directories) {
                for(TiffField field : dir.entries) {
                    row = new Vector<>();
                    row.add(dir.description());
                    row.add(field.getTagName());
                    if (field.getFieldType().equals(FieldType.ASCII)) {
                        row.add(String.valueOf(field.getValue()));
                    } else {
                        row.add(field.getValueDescription());
                    }
                    metadata.add(row);
                }
            }
        }
        return metadata;
    }

    /**
     * Publiczna i statyczna metoda pozwalajaca na utworzenie pliku docelowego ze
     * zmodyfikowanymi metadanymi. Metoda pozwala na zapis tylko niekotrych
     * wartosci znacznikow bedacych w formacie ASCII. Reszta wartosci jest pobierana
     * z pliku zrodlowego i sie nie zmienia. W momencie wystapienia problemow
     * z odczytem pliku zrodlowego lub zapisem do pliku docelowego zwracane sa
     * odpowiednie wyjatki.
     *
     * @param src Plik zrodlowy zawierajacy oryginalne metadane.
     * @param dest Plik docelowy w ktorym maja zostac zapisane zmodyfikowane metadane.
     * @param newTagValues Wektor z ciagami znakow zawierajacych nowe wartosci
     *                    dla znacznikow exif.
     * @throws IOException W przypadku wystapienia bledu z odczytem pliku.
     * @throws ImageReadException Jesli odczyt metadanych z pliku
     * zrodlowego sie nie powiodl.
     * @throws ImageWriteException Jesli zapis metadanych do pliku
     * docelowego sie nie powiedzie.
     * */
    public static void writeEXIFDataToFile(File src, File dest, Vector<String> newTagValues) throws IOException, ImageReadException, ImageWriteException {
        TiffOutputSet outputSet = null;
        ImageMetadata metadata = Imaging.getMetadata(src);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (jpegMetadata != null) {
            TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
                if (exif.getAllFields().size() != newTagValues.size()) {
                    throw new IllegalArgumentException("Too few tag values given.");
                }
            }
        }
        if (outputSet == null) {
            outputSet = new TiffOutputSet();
        }
        int i = 0;
        for (TiffOutputDirectory dir : outputSet.getDirectories()) {
            for (TiffOutputField field : dir.getFields()) {
                dir.removeField(field.tag);
                if (field.fieldType.equals(FieldType.ASCII)) {
                    String value = String.valueOf(newTagValues.get(i));
                    TiffOutputField newField = new TiffOutputField(field.tag, field.tagInfo,
                            field.fieldType, field.count, value.getBytes());
                    dir.add(newField);
                } else {
                    dir.add(field);
                }
                i++;
            }
        }
        FileOutputStream fos = new FileOutputStream(dest);
        OutputStream os = new BufferedOutputStream(fos);
        new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
    }

    /**
     * Publiczna i statyczna metoda ktora pozwala na utworzenie pliku wynikowego
     * z usunietymi metadanymi exif zawartymi w pliku zrodlowym. Jesli podczas
     * tej operacji wystapia problemy to zostana zwrocone odpowiednie wyjatki.
     *
     * @param src Plik zrodlowy zawierajacy metadane exif.
     * @param dest Plik wynikowy bez metadanych exif.
     * @throws IOException W przypadku wystapienia bledu z odczytem pliku.
     * @throws ImageReadException Jesli odczyt metadanych z pliku
     * zrodlowego sie nie powiodl.
     * @throws ImageWriteException Jesli zapis metadanych do pliku
     * docelowego sie nie powiedzie.
     * */
    public static void removeEXIFDataFromFile(File src, File dest) throws IOException, ImageWriteException, ImageReadException {
        FileOutputStream fos = new FileOutputStream(dest);
        OutputStream os = new BufferedOutputStream(fos);
        new ExifRewriter().removeExifMetadata(src, os);
    }
}
