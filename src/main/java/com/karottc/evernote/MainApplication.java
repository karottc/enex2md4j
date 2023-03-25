package com.karottc.evernote;


import com.karottc.evernote.handle.Convert;
import com.karottc.evernote.note.EnexExport;
import com.karottc.evernote.note.MdNote;

/**
 * @author karottc@gmail.com
 * @date 2023-03-24 16:18
 */
public class MainApplication {

    public static void main(String[] args) {
        Convert convert = new Convert();
        String outPath = "/Users/cy/Downloads/ob-out/";

        // 读取 XML 文件并将其转换为 Java 对象
        EnexExport enexExport = convert.parseEnexFile("/Users/cy/Downloads/ob-out/fu.enex");

        MdNote.Note note = convert.convert(enexExport.notes.get(1));

        System.out.println(enexExport);
    }


}
