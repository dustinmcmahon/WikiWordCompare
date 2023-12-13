package mcmahon.wikiWordCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParsePage implements java.io.Serializable {
    String url;
    String title;
    ObjFreqHashMap wordMap = new ObjFreqHashMap();

    public ParsePage (String url) {
        this.url = url;
        try{
            Document website = Jsoup.connect(url).get();
            title = website.title();
            parseWebToMap(website);
            wordMap.generateTF();
            
            //wordMap.printAll();
        } catch (IOException e){
            e.printStackTrace();
            url += ": URL BROKEN";
            title = "Broken Page";
        }
    }

    public ParsePage(){
        url = null;
        title = null;
        wordMap = null;
    }

    private void writeObject(ObjectOutputStream s) {
        try {
            s.writeObject(url);
            s.writeObject(title);
            s.writeObject(wordMap);
        } catch(IOException e){
            System.out.println("Could not write ParsePage");
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream s) {
        //s.defaultReadObject();
        try{
            url = (String)s.readObject();
            title = (String)s.readObject();
            wordMap = (ObjFreqHashMap)s.readObject();
        } catch(IOException e){
            System.out.println("Could not read ParsePage");
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            System.out.println("Could not find class");
            e.printStackTrace();
        }
    }

    private void parseWebToMap(Document website){
        Elements paragraphs = getParagraphs(website);
        paragraphs = cleanUnparsable(paragraphs);
        String tempString;
        String[] pWords;
        for (Element e : paragraphs){
            tempString = e.text();
            if(tempString.length() != 0){
                //remove some punctuation
                tempString = removeExtraChars(tempString.trim());

                pWords = tempString.split(" ");
                for (String word : pWords){
                    wordMap.add(word.toLowerCase());
                }
            }
        }
        /*
        wordMap.printAll();
        System.out.println("Unique Word Count: " + wordMap.uniqueWords);
        System.out.println("Total Word Count: " + wordMap.totalWords);
         */
    }

    private Elements getParagraphs(Document website){
        return website.select(".mw-parser-output p");
    }

    /**
     * This function is to remove some of the portions that will not parse well
     * currently actions:
     * * replace '<math*</math>' with 'MathFunction'
     * * removing '<sup*</sup>'
     */
    private Elements cleanUnparsable(Elements paragraphs){
        for(Element e: paragraphs.select("math")){
            e.html(" MathFunction ");
        }
        for(Element e: paragraphs.select("sup")){
            e.remove();
        }
        return paragraphs;
    }

    /**
     * removes punctuation from the target string
     * @param theString
     * @return
     */
    private static String removeExtraChars(String theString){
        String tempString = theString.replaceAll("[^a-zA-Z]", " ");
        return tempString.replaceAll(" +", " ");
    }

    public static String getUrl(File fileLocation, long index){
        try{
            return _getUrl(fileLocation, index);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return "";
    }

    private static String _getUrl(File fileLocation, long index) throws FileNotFoundException,IOException{
        long i = index;
        String result;
        
        // used for decoding the strings
        Charset cs = Charset.forName("UTF-8");
        CharsetDecoder dec = cs.newDecoder();

        FileInputStream inStream = new FileInputStream(fileLocation);
        FileChannel inChannel = inStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        int length;

        // how big is the url
        inChannel.read(bb, i);
        i += Integer.BYTES;
        bb.position(0);
        length = bb.getInt();
        bb = ByteBuffer.allocate(length);
        inChannel.read(bb, i);
        i = i + length;

        // reset buffer then decode it to a string
        bb.position(0);
        result = dec.decode(bb).toString().trim();

        inStream.close();
        return result;
    }

    public static String getTitle(File fileLocation, long index){
        try{
            return _getTitle(fileLocation, index);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return "";
    }

    private static String _getTitle(File fileLocation, long index) throws FileNotFoundException,IOException{
        long i = index;
        String result;
        
        // used for decoding the strings
        Charset cs = Charset.forName("UTF-8");
        CharsetDecoder dec = cs.newDecoder();

        FileInputStream inStream = new FileInputStream(fileLocation);
        FileChannel inChannel = inStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        int length;

        // how big is the url
        inChannel.read(bb, i);
        i += Integer.BYTES;
        bb.position(0);
        length = bb.getInt();
        bb = ByteBuffer.allocate(length);
        inChannel.read(bb, i);
        i = i + length;

        // reset buffer then decode it to a string
        bb.position(0);
        result = dec.decode(bb).toString().trim();

        // how big is the title
        bb = ByteBuffer.allocate(Integer.BYTES);
        inChannel.read(bb, i);
        i = i + Integer.BYTES;
        bb.position(0);
        length = bb.getInt();
        bb = ByteBuffer.allocate(length);
        inChannel.read(bb, i);
        i = i + length;
        // turn byte buffer into a char buffer then to a char array then into a string
        bb.position(0);
        result = dec.decode(bb).toString().trim();

        inStream.close();
        return result;
    }

    public static ParsePage getPage(File fileLocation, long index){
        ParsePage result = null;
        try{
            result = _getPage(fileLocation, index);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Order of Read/Write
     * 1. URL Length
     * 2. URL String
     * 3. Title Size
     * 4. Title String
     * 5. Hash Map
     * @param fileLocation
     * @param index
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static ParsePage _getPage(File fileLocation, long index) throws FileNotFoundException,IOException{
        //used to create the object before passing it as the result
        long i = index;
        ParsePage result = new ParsePage();
        
        // used for decoding the strings
        Charset cs = Charset.forName("UTF-8");
        CharsetDecoder dec = cs.newDecoder();

        FileInputStream inStream = new FileInputStream(fileLocation);
        FileChannel inChannel = inStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        int length;

        // how big is the url
        inChannel.read(bb, i);
        i += Integer.BYTES;
        bb.position(0);
        length = bb.getInt();
        bb = ByteBuffer.allocate(length);
        inChannel.read(bb, i);
        i = i + length;

        // reset buffer then decode it to a string
        bb.position(0);
        String temp = dec.decode(bb).toString().trim();
        //System.out.println(temp);
        result.url = temp;

        // how big is the title
        bb = ByteBuffer.allocate(Integer.BYTES);
        inChannel.read(bb, i);
        i = i + Integer.BYTES;
        bb.position(0);
        length = bb.getInt();
        bb = ByteBuffer.allocate(length);
        inChannel.read(bb, i);
        i = i + length;
        // turn byte buffer into a char buffer then to a char array then into a string
        bb.position(0);
        temp = dec.decode(bb).toString().trim();
        //System.out.println(temp);
        result.title = temp;

        // close the stream before envoking the new get call with the file
        inChannel.close();
        inStream.close();
        result.wordMap = ObjFreqHashMap.getMap(fileLocation, i);

        return result;
    }

    public int insertPage(File fileLocation){
        try{
            return _insertPage(fileLocation, -1);
        } catch (FileNotFoundException e){
            System.out.println("InsertFile Not Found: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("File Stream Issue: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        }
        return 0;
    }

    public int insertPage(File fileLocation, long index){
        try{
            return _insertPage(fileLocation, index);
        } catch(BufferOverflowException e){
            e.printStackTrace();
            System.out.println(this.url);
            System.out.println(this.title);
            System.out.println("Buffer OverFlow!");
        } catch (FileNotFoundException e){
            System.out.println("InsertFile Not Found: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("File Stream Issue: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        }
        return 0;
    }

    private int _insertPage(File fileLocation, long index) throws FileNotFoundException,IOException,BufferOverflowException{
        // used for decoding the strings
        long i = index;
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder enc = cs.newEncoder();

        FileOutputStream outStream = new FileOutputStream(fileLocation, true);
        FileChannel outChannel = outStream.getChannel();

        // https://stackoverflow.com/questions/25941286/what-is-the-length-of-a-string-encoded-in-a-bytebuffer
        // this page talks about the averageBytesPerChar function
        int wordSize = (int)(this.url.length() * 1.3);
        int size = Integer.BYTES + wordSize;

        ByteBuffer bb1 = ByteBuffer.allocate(size);
        bb1.putInt(wordSize);
        bb1.put(enc.encode(CharBuffer.wrap(this.url)));

        wordSize = (int)(this.title.length() * 1.3);
        size += Integer.BYTES + wordSize;

        ByteBuffer bb2 = ByteBuffer.allocate(size);
        bb1.position(0);
        bb2.put(bb1);
        bb2.putInt(wordSize);
        bb2.put(enc.encode(CharBuffer.wrap(this.title)));        
        
        if(i == -1) i = 0;
        
        //outChannel.position(index);
        bb2.position(0);
        outChannel.write(bb2,i);

        outChannel.close();
        outStream.close();

        size += wordMap.insertMap(fileLocation, i+bb2.capacity());
        
        return size;
    }
}
