package com.karottc.evernote.note;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author karottc@gmail.com
 * @date 2023-03-24 16:21
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnexExport {
    @JacksonXmlProperty(localName = "export-date")
    public String date;

    @JacksonXmlElementWrapper(localName = "note", useWrapping = false)
    @JacksonXmlProperty(localName = "note")
    public List<Note> notes = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Note {
        public String title;
        public String content;
        public String updated;
        public String created;

        @JacksonXmlElementWrapper(localName = "tag", useWrapping = false)
        @JacksonXmlProperty(localName = "tag")
        public List<String> tags;

        @JacksonXmlElementWrapper(localName = "resource", useWrapping = false)
        @JacksonXmlProperty(localName = "resource")
        public List<Resource> resources = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resource {
        public Data data;
        public String mime; // e.g: image/jpeg
        public int width;
        public int height;

        // 非xml字段
        public String filename;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JacksonXmlProperty(localName = "encoding", isAttribute = true)
        public String encoding;

        @JacksonXmlText
        public String value;
    }
}
