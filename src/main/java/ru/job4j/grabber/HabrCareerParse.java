package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String fullLink) {
        List<Post> result = new ArrayList<>();
        int numberPage = 5;
        while (numberPage > 0) {
            Connection connection = Jsoup.connect(fullLink + numberPage);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> result.add(createPost(row)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            numberPage--;
        }
        return result;
    }

    private Post createPost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element date = row.select(".vacancy-card__date").first().child(0);
        String dateName = date.attr("datetime");
        String title = titleElement.text();
        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description;
        try {
            description = habrCareerParse.retrieveDescription(link);
        } catch (IOException e) {
            e.printStackTrace();
            description = "There is no description";
        }
        LocalDateTime created = habrCareerParse.dateTimeParser.parse(dateName);
        return new Post(title, link, description, created);
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element vacancyDescription = document.select(".vacancy-description__text").first();
        return vacancyDescription.text();
    }
}