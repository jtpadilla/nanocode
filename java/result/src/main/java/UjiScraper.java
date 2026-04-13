import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class UjiScraper implements Scraper {
    @Override
    public List<Noticia> scrape() {
        List<Noticia> noticias = new ArrayList<>();
        String url = "https://www.uji.es/com/sumari/noticies/";
        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("div.itemGrid");
            for (Element item : items) {
                Element tituloElem = item.selectFirst("p.h4-title > a");
                Element fechaElem = item.selectFirst("div.greyAccesible");
                if (tituloElem == null || fechaElem == null) continue;
                String titulo = tituloElem.text();
                String enlace = tituloElem.attr("abs:href");
                String fecha = fechaElem.text().strip();
                noticias.add(new Noticia(titulo, fecha, enlace));
            }
        } catch (IOException e) {
            System.err.println("Error al acceder a la URL: " + e.getMessage());
        }
        return noticias;
    }
}
