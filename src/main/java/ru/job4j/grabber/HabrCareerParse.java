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
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
    private static int pageNumber = 1;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String fullLink) {
        List<Post> result = new ArrayList<>();
        Connection connection = Jsoup.connect(fullLink);
        try {
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> result.add(createPost(row)));
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        while (pageNumber <= 5) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            List<Post> listPost = habrCareerParse.list(fullLink);
            listPost.forEach(System.out::println);
            pageNumber++;
        }
    }
}