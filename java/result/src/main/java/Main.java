import java.util.List;
public class Main {
    public static void main(String[] args) {
        Scraper scraper = new UjiScraper();
        List<Noticia> noticias = scraper.scrape();
        System.out.println("Noticias encontradas: " + noticias.size());
        for (Noticia n : noticias) {
            System.out.println("- " + n.titulo() + " (" + n.fecha() + ")");
            System.out.println("  Link: " + n.enlace());
        }
    }
}
