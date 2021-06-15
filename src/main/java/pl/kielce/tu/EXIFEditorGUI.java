package pl.kielce.tu;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Klasa tworzaca glowne okno aplikacji i implementujaca
 * interfejs graficzny dla edytora metadanych EXIF
 * plikow graficznych. Zawiera metody obslugujace
 * wyswietlanie, modyfikowanie i usuwanie metadanych.
 * */
public class EXIFEditorGUI extends JFrame {
    /**
     * Panel glowny zawierajacy wszystkie uzywane komponenty.
     * */
    private JPanel mainPanel;
    /**
     * Panel pozwalajacy na przewijanie zawartosci tabeli metadanych.
     * */
    private JScrollPane tableScrollPanel;
    /**
     * Uchwyt dla przycisku "Open".
     * */
    private JButton buttonOpen;
    /**
     * Uchwyt dla przycisku "Save".
     * */
    private JButton buttonSave;
    /**
     * Uchwyt dla przycisku "Edit".
     * */
    private JButton buttonEdit;
    /**
     * Uchwyt dla przycisku "Remove exif".
     * */
    private JButton buttonRemove;
    /**
     * Pole z tekstem, ktore wyswietla aktualnie modyfikowany plik.
     * */
    private JLabel labelDisplayFileName;
    /**
     * Panel zawierajacy przyciski.
     * */
    private JPanel buttonsPanel;
    /**
     * Panel zawierajacy pole z tekstem modyfikowanego pliku.
     * */
    private JPanel fileNamePanel;
    /**
     * Uchwyt dla tabeli wyswietlajacej i przechowujacej metadane pliku.
     * */
    private JTable table;
    /**
     * Zmienna prywatna przechowujaca aktualnie modyfikowany plik.
     * */
    private File srcFile;
    /**
     * Zmienna prywatna ze stalymi nazwami kolumn tabeli.
     * */
    private static final String[] columnNames = { "Directory", "Tag name", "Extracted value"};
    /**
     * Stala prywatna zawierajaca nazwe programu.
     * */
    private static final String title = "EXIFEditor";

    /**
     * Prywatna metoda inicjujaca elementy glownego okna edytora.
     * Ustala wlasciwosci wyswietlania tabeli metadanych oraz przypisuje
     * metody obslugujace zdarzenia dla przyciskow.
     *  */
    private void initComponents() {
        table.setModel(new DefaultTableModel(columnNames, 0));
        table.setRowHeight(25);
        int columnWidth = table.getParent().getWidth()/3;
        table.getColumnModel().getColumn(2).setPreferredWidth(columnWidth);
        table.getTableHeader().setReorderingAllowed(false);
        table.setEnabled(false);

        buttonOpen.addActionListener(this::buttonOpenActionListener);
        buttonEdit.addActionListener(this::buttonEditActionListener);
        buttonEdit.setEnabled(false);
        buttonSave.addActionListener(this::buttonSaveActionListener);
        buttonSave.setEnabled(false);
        buttonRemove.addActionListener(this::buttonRemoveActionListener);
        buttonRemove.setEnabled(false);
    }

    /**
     * Konstruktor klasy tworzacej interfejs graficzny.
     * */
    public EXIFEditorGUI() {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        ImageIcon icon = new ImageIcon("EXIFIcon.ico");
        this.setIconImage(icon.getImage());
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
        this.initComponents();
    }

    /**
     * Prywatna metoda sluzaca do zaladowania odczytanych metadanych
     * z pliku zrodlowego do tabeli. W momencie wystapienia bledu
     * odczytu wyswietlany jest odpowiedni komunikat o bledzie.
     *
     * @return Zwraca {@code true} jesli operacja odczytu i zapisu
     * metadanych w tabeli sie powiodla lub {@code false}
     * w przeciwnym przypadku.
     * */
    private boolean readAndLoadMetadataToTable() {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(0);
        labelDisplayFileName.setText(null);
        try {
            Vector<Vector<String>> metadata = ExifEditor.readEXIFDataFromFile(srcFile);
            metadata.forEach(tableModel::addRow);
        } catch (IOException | ImageReadException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "File read", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        table.setModel(tableModel);
        labelDisplayFileName.setText("Detailed EXIF metadata for file: " + srcFile.getName());
        return true;
    }

    /**
     * Prywatna metoda obslugujaca zdarzenia klikniecia przycisku
     * "Open". W wyniku jej dzialania tworzone jest okno dialogowe
     * wyboru pliku zrodlowego oraz podejmowana jest proba odczytu
     * i zapisu metadanych do tabeli.
     *
     * @param e Zawiera dane dotyczace wykrytego zdarzenia klikniecia
     *          na przycisk.
     * */
    private void buttonOpenActionListener(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int retVal = fileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            buttonEdit.setEnabled(false);
            buttonRemove.setEnabled(false);
            srcFile = fileChooser.getSelectedFile();
            if (readAndLoadMetadataToTable()) {
                table.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonEdit.setEnabled(true);
                buttonRemove.setEnabled(true);
            }
        }
    }

    /**
     * Prywatna metoda obslugujaca zdarzenia klikniecia przycisku
     * "Edit". W wyniku jej dzialania  odblokowywana jest mozliwosc
     * edycji wartosci w komorkach tabeli.
     *
     * @param e Zawiera dane dotyczace wykrytego zdarzenia klikniecia
     *          na przycisk.
     * */
    private void buttonEditActionListener(ActionEvent e) {
        table.setEnabled(true);
        buttonEdit.setEnabled(false);
        buttonSave.setEnabled(true);
    }

    /**
     * Prywatna i statyczna metoda uzywana do okreslenia
     * rozszerzenia pliku.
     *
     * @param fileName Nazwa pliku z rozszerzeniem.
     * @return Zwraca rozszerzenie pliku.
     * */
    public static String extractFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index);
    }

    /**
     * Prywatna metoda sluzaca do wyboru pliku docelowego
     * w ktorym maja byc zapisane dane wynikowe.
     *
     * @return Zwraca wybrany plik docelowy lub {@code null}
     * w przypadku niepowodzenia.
     * */
    private File chooseDestFile() {
        JFileChooser fileChooser = new JFileChooser();
        int retVal = fileChooser.showSaveDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File dest;
            String srcFileExt = extractFileExtension(srcFile.getName());
            if (!fileChooser.getSelectedFile().getName().endsWith(srcFileExt)) {
                dest = new File(fileChooser.getSelectedFile() + srcFileExt);
            } else {
                dest = fileChooser.getSelectedFile();
            }
            if (dest.equals(srcFile)) {
                JOptionPane.showMessageDialog(this, "Can't overwrite source file",
                        "Source file selected", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            try {
                dest.createNewFile();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                        "Save file", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return dest;
        } else {
            return null;
        }
    }

    /**
     * Prywatna metoda sluzaca do zapisu zmodyfikowanych danych
     * w tabeli do pliku docelowego. W momencie wystapienia problemow
     * wyswietlany jest odpowiedni komunikat o bledzie.
     *
     * @param dest Plik w ktorym zostana zapisane zmodyfikowane metadane.
     * @return Zwraca {@code true} jesli operacja odczytu i zapisu
     * metadanych w tabeli sie powiodla lub {@code false}
     * w przeciwnym przypadku.
     * */
    private boolean writeMetadataFromTableToFile(File dest) {
        Vector<String> newTagValues = new Vector<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            newTagValues.add((String) table.getValueAt(i, 2));
        }
        try {
            ExifEditor.writeEXIFDataToFile(srcFile, dest, newTagValues);
        } catch (ImageReadException | IOException | ImageWriteException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Save file", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Prywatna metoda obslugujaca zdarzenia klikniecia przycisku
     * "Save". W wyniku jej dzialania tworzone jest okno dialogowe
     * wyboru pliku docelowego oraz podejmowana jest proba zapisu
     * zmodyfikowanych metadanych z tabeli do wybranego pliku.
     *
     * @param e Zawiera dane dotyczace wykrytego zdarzenia klikniecia
     *          na przycisk.
     * */
    private void buttonSaveActionListener(ActionEvent e) {
        File dest = chooseDestFile();
        if (dest == null)
            return;
        if (writeMetadataFromTableToFile(dest)) {
            table.setEnabled(false);
            table.clearSelection();
            table.editCellAt(-1,-1);
            buttonSave.setEnabled(false);
            buttonEdit.setEnabled(true);
            srcFile = dest;
            if (readAndLoadMetadataToTable()) {
                table.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonEdit.setEnabled(true);
                buttonRemove.setEnabled(true);
            }
        }
    }

    /**
     * Prywatna metoda ktora obsluguje operacje usuwania metadanych
     * exif z pliku zrodlowego i zapisu pozostalych danych w pliku
     * docelowym. Jesli podczas tej operacji wystapia problemy
     * to zostanie wyswietlone okno z informacja o bledzie.
     *
     * @param dest Plik wynikowy bez metadanych exif.
     * */
    private void removeExifMetadataFromSrcFile(File dest) {
        try {
            ExifEditor.removeEXIFDataFromFile(srcFile, dest);
        } catch (IOException | ImageReadException | ImageWriteException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Exif remove", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prywatna metoda obslugujaca zdarzenia klikniecia przycisku
     * "Remove exif". W wyniku jej dzialania tworzone jest okno dialogowe
     * wyboru pliku docelowego oraz podejmowana jest proba usuniecia
     * metadanych exif z pliku zrodlowego i zapisu danych wynikowych
     * w wybranym pliku docelowym.
     *
     * @param e Zawiera dane dotyczace wykrytego zdarzenia klikniecia
     *          na przycisk.
     * */
    private void buttonRemoveActionListener(ActionEvent e) {
        File dest = chooseDestFile();
        if (dest == null)
            return;
        removeExifMetadataFromSrcFile(dest);
    }
}
