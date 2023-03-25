package com.karottc.evernote.handle;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.hash.Hashing;
import com.karottc.evernote.note.EnexExport;
import com.karottc.evernote.note.MdNote;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private static FlexmarkHtmlConverter HTML_TO_MD_CONVERT;

    private static final Map<String, String> EN_MEDIA_MAP = new HashMap<>(){{
        put("image", "<img src=\"zimgs/%s\" alt=\"%s\" />");
        put("file", "<a href=\"./zimgs/%s\">%s</a>");
    }};

    public Convert() {
        MutableDataSet options = new MutableDataSet();
        // 不使用 <br /> 来换行
        options.set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false);
        // 标题使用 # 符号，不使用 --- 来做标题
        options.set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false);

        HTML_TO_MD_CONVERT = FlexmarkHtmlConverter.builder(options).build();
    }

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
        ret.fileName = enexNote.title;
        ret.media = mapResources(enexNote);
        normalizeHTML(enexNote, ret);
        toMarkdown(enexNote, ret);

        return ret;
    }

    private Map<String, MdNote.Resource> mapResources(EnexExport.Note enexNote) {
        Map<String, MdNote.Resource> media = new HashMap<>();

        for (EnexExport.Resource resource : enexNote.resources) {
            MdNote.Resource r = new MdNote.Resource();
            r.content = decoder(resource.data);

            MimeBo bo = parseMime(resource.mime);
            r.name = resource.filename + bo.extension;
            r.type = bo.type;

            EnexExport.Recognition recognition = parseRecognition(resource.recognition);
            r.id = recognition.objID;

            media.put(r.id, r);
        }

        return media;
    }

    private void normalizeHTML(EnexExport.Note enexNote, MdNote.Note mdNote) {
        // 使用 Jsoup 解析 HTML
        Document doc = Jsoup.parse(enexNote.content);
        handleEnMedia(doc, mdNote);

        enexNote.content = doc.html();
    }

    private void handleEnMedia(Document doc, MdNote.Note mdNode) {
        Elements medias = doc.select("en-media");
        for (Element node : medias) {
            if (!node.hasAttr("hash")) {
                continue;
            }
            String[] types = node.attr("type").split("/");
            String type = types[0];
            String hash = node.attr("hash");

            String fileName = mdNode.media.get(hash).name;
            String e = EN_MEDIA_MAP.get(type).formatted(fileName, fileName);
            node.after(e);
        }
        doc.select("en-media").remove();
    }


    private void toMarkdown(EnexExport.Note enexNote, MdNote.Note mdNote) {
        mdNote.content = HTML_TO_MD_CONVERT.convert(enexNote.content);
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

    private EnexExport.Recognition parseRecognition(String xml) {
        try {
            return xmlMapper.readValue(xml, EnexExport.Recognition.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static class MimeBo {
        public String extension = "";
        public String type = "";
    }
}
