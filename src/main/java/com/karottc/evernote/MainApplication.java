package com.karottc.evernote;


import com.karottc.evernote.handle.Convert;
import com.karottc.evernote.note.EnexExport;
import com.karottc.evernote.note.MdNote;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author karottc@gmail.com
 * @date 2023-03-24 16:18
 */
public class MainApplication {

    public static void main(String[] args) throws IOException {
        Convert convert = new Convert();
        String enexFile = "/Users/xxx/Downloads/ob-out/fu.enex";
        String outPath = "/Users/xxx/Downloads/ob-out/notes22/";

        // 读取 XML 文件并将其转换为 Java 对象
        EnexExport enexExport = convert.parseEnexFile(enexFile);

        List<MdNote.Note> mdNotes = enexExport.notes.stream().map(convert::convert).toList();

        Files.createDirectories(Path.of(outPath));

        for (MdNote.Note note : mdNotes) {
            for (String id : note.media.keySet()) {
                saveMedia(outPath, note.media.get(id));
            }
            saveMd(outPath, note);
        }
    }

    private static void saveMedia(String outPath, MdNote.Resource resource) throws IOException {
        String mediaPath = outPath + "zimgs/";
        Files.createDirectories(Path.of(mediaPath));
        Files.write(Path.of(mediaPath + resource.name), resource.content);
    }

    private static void saveMd(String outPath, MdNote.Note note) throws IOException {
        Files.writeString(Path.of(outPath + note.fileName + ".md"), note.content, StandardCharsets.UTF_8);
    }
}
