package com.karottc.evernote.note;

import java.util.Map;

/**
 * @author karottc@gmail.com
 * @date 2023-03-24 20:26
 */
public class MdNote {

    public static class Note {
        public String content;
        public Map<String, Resource> media;
        public long ctime;
        public long mtime;
    }

    public static class Resource {
        public String name;
        public String type;
        public byte[] content;
        public String id;
    }
}
