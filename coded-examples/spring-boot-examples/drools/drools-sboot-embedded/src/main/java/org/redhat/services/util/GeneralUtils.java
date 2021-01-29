package org.redhat.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
public class GeneralUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralUtils.class);

    @Autowired
    private ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> Stream<T> collectionToStream(Collection<T> collection) {
        return Optional.ofNullable(collection).map(Collection::stream).orElseGet(Stream::empty);
    }

    public Date futureDate(Integer days) {
        Calendar cal = Calendar.getInstance();
        Date to = new Date();
        cal.setTime(to);
        cal.add(Calendar.DATE, days);
        to = cal.getTime();
        return to;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void printMap(Map s) {

        s.forEach((key, value) -> {
            LOGGER.debug("key {} \n value {}", key, toJson(value));
        });

    }

    public <T> void printList(List<T> s) {

        s.forEach(i -> {
            LOGGER.debug(toJson(i));
        });

    }

    public String dateToString(Date date) {
        String pattern = "dd.MM.yyyy HH:mm"; // need to confirm
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public String formatDoubleToString(Double number) {

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }

    public <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
