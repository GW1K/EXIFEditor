package pl.kielce.tu;

/**
 * Glowna klasa aplikacji, ktora zawiera
 * punkt wejsciowy programu.
 * */
public class EXIFEditorApp {

    /**
     * Konstruktor klasy tworzacej aplikacje.
     * Odpowiada za utworzenie obiektu klasy
     * implementujacej interfejs graficzny.
     * */
    public EXIFEditorApp() {
        new EXIFEditorGUI();
    }

    /**
     * Metoda stanowiaca glowny punkt wejsciowy programu.
     * Odpowiada za wywolanie konstruktora aplikacji.
     *
     * @param args Dodatkowe parametry. (Nie sa wykorzystywane)
     * */
    public static void main(String[] args) {
        new EXIFEditorApp();
    }
}
