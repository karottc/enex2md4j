package com.karottc.evernote.handle;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.hash.Hashing;
import com.karottc.evernote.note.EnexExport;
import com.karottc.evernote.note.MdNote;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author karottc@gmail.com
 * @date 2023-03-24 20:34
 */
public class Convert {

    private static MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    private static XmlMapper xmlMapper = new XmlMapper();


    public EnexExport parseEnexFile(String fileName) {
        try {
            EnexExport enexExport = xmlMapper.readValue(new File(fileName), EnexExport.class);
            for (EnexExport.Note note : enexExport.notes) {
                note.content = pureContent(note.content);
                for (EnexExport.Resource resource : note.resources) {
                    resource.filename = Hashing.md5()
                            .hashString(resource.data.value, StandardCharsets.UTF_8)
                            .toString().toLowerCase();
                }
            }
            return enexExport;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String pureContent(String str) {
        int first = str.indexOf("<en-note>");
        int last = str.lastIndexOf("</en-note>");
        return str.substring(first + 9, last);
    }

    public MdNote.Note convert(EnexExport.Note enexNote) {
        MdNote.Note ret = new MdNote.Note();
        ret.media = mapResources(enexNote);
//        normalizeHTML()
//        toMarkdown();

        return ret;
    }

    private Map<String, MdNote.Resource> mapResources(EnexExport.Note enexNote) {
        Map<String, MdNote.Resource> media = new HashMap<>();

        int index = 0;
        for (EnexExport.Resource resource : enexNote.resources) {
            MdNote.Resource r = new MdNote.Resource();
            r.content = decoder(resource.data);

            MimeBo bo = parseMime(resource.mime);
            r.name = resource.filename + bo.extension;
            r.type = bo.type;
            media.put(String.valueOf(index++), r);
        }

        return media;
    }

    private void toMarkdown(EnexExport.Note enexNote, MdNote.Note mdNote) {

    }

    private byte[] decoder(EnexExport.Data data) {
        if ("base64".equals(data.encoding)) {
            return Base64.getDecoder().decode(data.value.trim().replaceAll("[\\r\\n]", ""));
        }
        return null;
    }

    private MimeBo parseMime(String mime) {
        MimeBo ret = new MimeBo();
        try {
            MimeType m = MIME_TYPES.forName(mime);
            ret.extension = m.getExtension();
            ret.type = m.getType().getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static class MimeBo {
        public String extension = "";
        public String type = "";
    }
}
